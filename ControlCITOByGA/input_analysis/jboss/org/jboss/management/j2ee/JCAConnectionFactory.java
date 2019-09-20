/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import java.util.Hashtable;
import java.util.Set;

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
 * {@link javax.management.j2ee.JCAConnectionFactory JCAConnectionFactory}.
 *
 * @author  <a href="mailto:mclaugs@comcast.net">Scott McLaughlin</a>.
 * @version $Revision: 1.4.2.3 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20020303 Scott McLaughlin:</b>
 * <ul>
 * <li> Finishing first real implementation
 * </ul>
 *
 * @jmx:mbean extends="org.jboss.management.j2ee.StateManageable,org.jboss.management.j2ee.J2EEManagedObjectMBean"
 **/
public class JCAConnectionFactory
   extends J2EEManagedObject
   implements JCAConnectionFactoryMBean
{
   // Constants -----------------------------------------------------
   
   public static final String J2EE_TYPE = "JCAConnectionFactory";
   
   // Attributes ----------------------------------------------------
   
   private StateManagement mState;
   private ObjectName mService;
   private ObjectName mManagedConnectionFactory;
   
   // Static --------------------------------------------------------
   
   public static ObjectName create( MBeanServer pServer, String pName, ObjectName pService ) {
      Logger lLog = Logger.getLogger( JCAConnectionFactory.class );
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
//AS         lLog.error( "Could not locate JSR-77 Server: " + pName, e );
         return null;
      }
      // First create its parent, the JCA resource
      ObjectName lJCAResource = null;
      try {
         // Check if the JCA Resource exists and if not create one
         Set lNames = pServer.queryNames(
            new ObjectName(
               J2EEManagedObject.getDomainName() + ":" +
               J2EEManagedObject.TYPE + "=" + JCAResource.J2EE_TYPE + "," +
               "*"
            ),
            null
         );
         if( lNames.isEmpty() ) {
            // Now create the JCA resource
            lJCAResource = JCAResource.create( pServer, "JCA" );
         } else {
            lJCAResource = (ObjectName) lNames.iterator().next();
         }
      }
      catch( Exception e ) {
//AS         lLog.error( "Could not create JSR-77 JCA resource", e );
         return null;
      }
      
      try {
         return pServer.createMBean(
            "org.jboss.management.j2ee.JCAConnectionFactory",
            null,
            new Object[] {
               pName,
               lJCAResource,
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
//AS         lLog.error( "Could not create JSR-77 JCAConnectionFactory: " + pName, e );
         return null;
      }
   }
   
   public static void destroy( MBeanServer pServer, String pName ) {
      Logger lLog = Logger.getLogger( JCAConnectionFactory.class );
      if( !J2EEServer.sIsActive ) {
         return;
      }
      try {
         J2EEManagedObject.removeObject(
            pServer,
            J2EEManagedObject.getDomainName() + ":" +
               J2EEManagedObject.TYPE + "=" + JCAConnectionFactory.J2EE_TYPE + "," +
               "name=" + pName + "," +
               "*"
         );
         // Now let us try to destroy the JDBC Manager
         JCAResource.destroy( pServer, "JCA" );
      }
      catch( javax.management.InstanceNotFoundException infe ) {}
      catch( Exception e ) {
//AS         lLog.error( "Could not destroy JSR-77 JCAConnectionFactory: " + pName, e );
      }
   }
   
   // Constructors --------------------------------------------------
   
   /**
   * @param pName Name of the JCAConnectionFactory 
   *
   * @throws InvalidParameterException If list of nodes or ports was null or empty
   **/
   public JCAConnectionFactory(String pName, ObjectName pServer, ObjectName pService) throws MalformedObjectNameException, InvalidParentException
   {
      super( J2EE_TYPE, pName, pServer );
      mService = pService;
      mState = new StateManagement( this );
   }
   
   // Public --------------------------------------------------------
   
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
         getLog().error( "Start of JCA Connection Factory failed", e );
      }
   }
   
   public void mejbStartRecursive() {
      // No recursive start here
      try {
         mejbStart();
      }
      catch( Exception e ) {
         getLog().error( "Recursive Start of JCA Connection Factory failed", e );
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
         getLog().error( "Stop of JCA Connection Factory failed", e );
      }
   }
   
   // javax.management.j2ee.JCAConnectionFactory implementation -----------------
   
   /**
    * @jmx:managed-attribute
    **/
   public ObjectName getManagedConnectionFactory()
   {
      return mManagedConnectionFactory;
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
            "JCA Connection Factory created"
         )
      );
   }
   
   public void preDestruction() {
      Logger lLog = getLog();
      if( lLog.isInfoEnabled() ) {
         lLog.info( "JCAConnectionFactory.preDeregister(): " + getName() );
      }
      sendNotification(
         new Notification(
            StateManagement.sTypes[ 1 ],
            getName(),
            1,
            System.currentTimeMillis(),
            "JCA Connection Factory deleted"
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
      return "JCAConnectionFactory { " + super.toString() + " } [ " +
         " ]";
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   /**
    * @return A hashtable with the JCA-Resource and J2EE-Server as parent
    **/
   protected Hashtable getParentKeys( ObjectName pParent ) {
      Hashtable lReturn = new Hashtable();
      Hashtable lProperties = pParent.getKeyPropertyList();
      lReturn.put( JCAResource.J2EE_TYPE, lProperties.get( "name" ) );
      // J2EE-Server is already parent of J2EE-Application therefore lookup
      // the name by the J2EE-Server type
      lReturn.put( J2EEServer.J2EE_TYPE, lProperties.get( J2EEServer.J2EE_TYPE ) );
      
      return lReturn;
   }
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
