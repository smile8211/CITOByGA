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
 * {@link javax.management.j2ee.JDBCDataSource JDBCDataSource}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.4.2.3 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011126 Andreas Schaefer:</b>
 * <ul>
 * <li> Adjustments to the JBoss Guidelines
 * </ul>
 * <p><b>20011206 Andreas Schaefer:</b>
 * <ul>
 * <li> Finishing first real implementation
 * </ul>
 *
 * @jmx:mbean extends="org.jboss.management.j2ee.StateManageable,org.jboss.management.j2ee.J2EEManagedObjectMBean"
 **/
public class JDBCDataSource
   extends J2EEManagedObject
   implements JDBCDataSourceMBean
{
   // Constants -----------------------------------------------------
   
   public static final String J2EE_TYPE = "JDBCDataSource";
   
   // Attributes ----------------------------------------------------
   
   private StateManagement mState;
   private ObjectName mService;
   private ObjectName mJdbcDriver;
   
   // Static --------------------------------------------------------
   
   public static ObjectName create( MBeanServer pServer, String pName, ObjectName pService ) {
      Logger lLog = Logger.getLogger( JDBCDataSource.class );
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
         // Return because without the JDBC manager go on does not work
         return null;
      }
      // First create its parent the JDBC resource
      ObjectName lJDBC = null;
      try {
         // Check if the JDBC Manager exists and if not create one
         Set lNames = pServer.queryNames(
            new ObjectName(
               J2EEManagedObject.getDomainName() + ":" +
               J2EEManagedObject.TYPE + "=" + JDBCResource.J2EE_TYPE + "," +
               "*"
            ),
            null
         );
         if( lNames.isEmpty() ) {
            // Now create the JDBC Manager
            lJDBC = JDBCResource.create( pServer, "JDBC" );
         } else {
            lJDBC = (ObjectName) lNames.iterator().next();
         }
      }
      catch( Exception e ) {
//AS         lLog.error( "Could not create JSR-77 JDBC Manager", e );
         // Return because without the JDBC manager go on does not work
         return null;
      }
      
      try {
         //AS ToDo: Replace any ':' by '~' do avoid ObjectName conflicts for now
         //AS FixMe: look for a solution
         pName = pName.replace( ':', '~' );
         // Now create the JNDI Representant
         return pServer.createMBean(
            "org.jboss.management.j2ee.JDBCDataSource",
            null,
            new Object[] {
               pName,
               lJDBC,
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
//AS         lLog.error( "Could not create JSR-77 JDBC DataSource: " + pName, e );
         return null;
      }
   }
   
   public static void destroy( MBeanServer pServer, String pName ) {
      Logger lLog = Logger.getLogger( JDBCDataSource.class );
      if( !J2EEServer.sIsActive ) {
         return;
      }
      try {
         J2EEManagedObject.removeObject(
            pServer,
            J2EEManagedObject.getDomainName() + ":" +
               J2EEManagedObject.TYPE + "=" + JDBCDataSource.J2EE_TYPE + "," +
               "name=" + pName + "," +
               "*"
         );
         // Now let us try to destroy the JDBC Manager
         JDBCResource.destroy( pServer, "JDBC" );
      }
      catch( Exception e ) {
//AS         lLog.error( "Could not destroy JSR-77 JDBC DataSource: " + pName, e );
      }
   }
   
   // Constructors --------------------------------------------------
   
   /**
   * @param pName Name of the JDBCDataSource
   *
   * @throws InvalidParameterException If list of nodes or ports was null or empty
   **/
   public JDBCDataSource( String pName, ObjectName pServer, ObjectName pService )
      throws
         MalformedObjectNameException,
         InvalidParentException
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
         getLog().error( "start failed", e );
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
         getLog().error( "Stop of JDBCDataSource failed", e );
      }
   }
   
   public void postCreation() {
      try {
         getServer().addNotificationListener( mService, mState, null, null );
      }
      catch( JMException jme ) {
         //AS ToDo: later on we have to define what happens when service is null or
         //AS ToDo: not found.
         getLog().error( "Could not add listener at target service", jme );
      }
      sendNotification(
         new Notification(
            StateManagement.sTypes[ 0 ],
            getName(),
            1,
            System.currentTimeMillis(),
            "JDBC DataSource Resource created"
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
            "JDBC DataSource Resource deleted"
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
   
   // javax.management.j2ee.JDBCDataSource implementation -----------------
   
   public ObjectName getJdbcDriver()
   {
      return mJdbcDriver;
   }
   
   // java.lang.Object overrides ------------------------------------
   
   public String toString() {
      return "JDBCDatasource { " + super.toString() + " } [ " +
         " ]";
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   /**
    * @return A hashtable with the JDBC-Resource and J2EE-Server as parent
    **/
   protected Hashtable getParentKeys( ObjectName pParent ) {
      Hashtable lReturn = new Hashtable();
      Hashtable lProperties = pParent.getKeyPropertyList();
      lReturn.put( JDBCResource.J2EE_TYPE, lProperties.get( "name" ) );
      // J2EE-Server is already parent of J2EE-Application therefore lookup
      // the name by the J2EE-Server type
      lReturn.put( J2EEServer.J2EE_TYPE, lProperties.get( J2EEServer.J2EE_TYPE ) );
      
      return lReturn;
   }
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
