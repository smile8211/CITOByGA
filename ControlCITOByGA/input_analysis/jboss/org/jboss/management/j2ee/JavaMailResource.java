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
 * {@link javax.management.j2ee.JavaMailResource JavaMailResource}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.3.2.3 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011126 Andreas Schaefer:</b>
 * <ul>
 * <li> Adjustments to the JBoss Guidelines
 * </ul>
 *
 * @jmx:mbean extends="org.jboss.management.j2ee.StateManageable,org.jboss.management.j2ee.J2EEResourceMBean"
 **/
public class JavaMailResource
   extends J2EEResource
   implements JavaMailResourceMBean
{
   // Constants -----------------------------------------------------
   
   public static final String J2EE_TYPE = "JavaMailResource";
   
   // Attributes ----------------------------------------------------
   
   private StateManagement mState;
   private ObjectName mService;
   
   // Static --------------------------------------------------------
   
   public static ObjectName create( MBeanServer pServer, String pName, ObjectName pService ) {
      Logger lLog = Logger.getLogger( JavaMailResource.class );
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
         // Now create the JNDI Representant
         return pServer.createMBean(
            "org.jboss.management.j2ee.JavaMailResource",
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
//AS         lLog.error( "Could not create JSR-77 JavaMail Resouce", e );
         return null;
      }
   }
   
   public static void destroy( MBeanServer pServer, String pName ) {
      Logger lLog = Logger.getLogger( JavaMailResource.class );
      if( !J2EEServer.sIsActive ) {
         return;
      }
      try {
         // Find the Object to be destroyed
         pServer.unregisterMBean( new ObjectName( pName ) );
      }
      catch( javax.management.InstanceNotFoundException infe ) {}
      catch( Exception e ) {
//AS         lLog.error( "Could not destroy JSR-77 JavaMail Resource", e );
      }
   }
   
   // Constructors --------------------------------------------------
   
   /**
    * @param pName Name of the JavaMailResource
    *
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public JavaMailResource( String pName, ObjectName pServer, ObjectName pService )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( J2EE_TYPE, pName, pServer );
      mService = pService;
      mState = new StateManagement( this );
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
         getLog().error( "start failed", e );
      }
   }
   
   public void mejbStartRecursive() {
      // No recursive start here
      try {
         mejbStart();
      }
      catch( Exception e ) {
         getLog().error( "start failed", e );
      }
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
         getLog().error( "Stop of JavaMailResource failed", e );
      }
   }
   
   public void postCreation() {
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
            "Java Mail Resource created"
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
            "Java Mail Resource deleted"
         )
      );
      // Remove the listener of the target MBean
      try {
         getServer().removeNotificationListener( mService, mState );
      }
      catch( JMException jme ) {
         // When the service is not available anymore then just ignore the exception
      }
   }
   
   // java.lang.Object overrides ------------------------------------
   
   public String toString() {
      return "JavaMailResource { " + super.toString() + " } [ " +
         " ]";
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
