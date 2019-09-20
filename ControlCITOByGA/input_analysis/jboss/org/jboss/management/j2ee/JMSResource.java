/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import javax.management.AttributeChangeNotification;
import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBean;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.JMS JMS}.  *
 * @author  <a href="mailto:mclaugs@comcast.net">Scott McLaughlin</a>.
 * @version $Revision: 1.3.2.2 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>20020301 Scott McLaughlin</b>
 * <ul>
 * <li> Creation 
 * </ul>
 *
 * @jmx:mbean extends="org.jboss.management.j2ee.StateManageable,org.jboss.management.j2ee.J2EEResourceMBean"
 **/
public class JMSResource
   extends J2EEResource
   implements JMSResourceMBean
{
   
   // Constants -----------------------------------------------------
   
   public static final String J2EE_TYPE = "JMSResource";
   
   // Attributes ----------------------------------------------------
   
   private StateManagement mState;
   private ObjectName mService;
   
   // Static --------------------------------------------------------

   private static final String[] sTypes = new String[] {
                                             "j2ee.object.created",
                                             "j2ee.object.deleted",
                                             "state.stopped",
                                             "state.stopping",
                                             "state.starting",
                                             "state.running",
                                             "state.failed"
                                          };

   public static ObjectName create( MBeanServer pServer, String pName, ObjectName pService ) {
      Logger lLog = Logger.getLogger( JMSResource.class );
      if( !J2EEServer.sIsActive ) {
         return null;
      }
      ObjectName lServer = null;
      try {
         lServer = (ObjectName) pServer.queryNames(
            new ObjectName(
               J2EEManagedObject.getDomainName() + ":" +
               J2EEManagedObject.TYPE + "=" + J2EEServer.J2EE_TYPE + "," +
               "*"
            ),
            null
         ).iterator().next();
      }
      catch( Exception e ) {
//AS         lLog.error( "Could not create JSR-77 Server", e );
         return null;
      }
      try {
         // Now create the JMS Representant
         return pServer.createMBean(
            "org.jboss.management.j2ee.JMSResource",
            null,
            new Object[] {
               pName,
               lServer,
               pService
            },
            new String[] {
               String.class.getName(),
               ObjectName.class.getName(),
               ObjectName.class.getName()
            }
         ).getObjectName();
      }
      catch( Exception e ) {
//AS         lLog.error( "Could not create JSR-77 JMS Resouce", e );
         return null;
      }
   }
   
   public static void destroy( MBeanServer pServer, String pName ) {
      Logger lLog = Logger.getLogger( JMSResource.class );
      if( !J2EEServer.sIsActive ) {
         return;
      }
      try {
         J2EEManagedObject.removeObject(
            pServer,
            J2EEManagedObject.getDomainName() + ":" +
               J2EEManagedObject.TYPE + "=" + JMSResource.J2EE_TYPE + "," +
               "name=" + pName + "," +
               "*"
         );
      }
      catch( Exception e ) {
//AS       lLog.error( "Could not destroy JSR-77 JMSResource Resource", e );
      }
   }
   
   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * @param pName Name of the JMSResource
    *
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public JMSResource( String pName, ObjectName pServer, ObjectName pService )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( J2EE_TYPE, pName, pServer );
      mService = pService;
      mState = new StateManagement( this );
   }

   // org.jboss.ServiceMBean overrides ------------------------------------

   public void postCreation() {
      // If set then register for its events
      try {
         getServer().addNotificationListener( mService, mState, null, null );
      }
      catch( JMException jme ) {
         // Ignore It
      }
      sendNotification(
         new Notification(
            StateManagement.sTypes[ 0 ],
            getName(),
            1,
            System.currentTimeMillis(),
            "JMSResource Resource created"
         )
      );
   }
   
   public void preDestruction() {
      sendNotification(
         new Notification(
            StateManagement.sTypes[ 1 ],
            getName(),
            1,
            System.currentTimeMillis(),
            "JMSResource Resource deleted"
         )
      );
      // If set then register for its events
      try {
         getServer().removeNotificationListener( mService, mState );
      }
      catch( JMException jme ) {
         // When the service is not available anymore then just ignore the exception
      }
   }
   
   // javax.managment.j2ee.EventProvider implementation -------------
   
   public String[] getEventTypes() {
      return StateManagement.sTypes;
   }
   
   public String getEventType( int pIndex ) {
      if( pIndex >= 0 && pIndex < StateManagement.sTypes.length ) {
         return StateManagement.sTypes[ pIndex ];
      } else {
         return null;
      }
   }
   
   // javax.management.j2ee.StateManageable implementation ----------
   
   public long getStartTime() {
      return mState.getStartTime();
   }
   
   public int getState() {
      return mState.getState();
   }
   
   public void mejbStart()
   {
      try {
         getServer().invoke(
            mService,
            "start",
            new Object[] {},
            new String[] {}
         );
      }
      catch( Exception e ) {
         getLog().error( "Start of JMS Resource failed", e );
      }
   }
   
   public void mejbStartRecursive() {
      mejbStart();
   }
   
   public void mejbStop() {
      try {
         getServer().invoke(
            mService,
            "stop",
            new Object[] {},
            new String[] {}
         );
      }
      catch( Exception e ) {
         getLog().error( "Stop of JMS Resource failed", e );
      }
   }
   
   // java.lang.Object overrides ------------------------------------
   
   public String toString() {
      return "JMSResource { " + super.toString() + " } [ " +
         " ]";
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
