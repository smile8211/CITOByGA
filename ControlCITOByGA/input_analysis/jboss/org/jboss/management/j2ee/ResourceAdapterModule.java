/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import java.net.URL;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.AttributeChangeNotification;
import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import java.security.InvalidParameterException;

import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBean;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.ResourceAdapterModule ResourceAdapterModule}.
 *
 * @author  <a href="mailto:mclaugs@comcast.net">Scott McLaughlin</a>.
 * @version $Revision: 1.5.2.2 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20020301 Scott McLaughlin:</b>
 * <ul>
 * <li>
 *      Creation
 * </ul>
 * <p><b>20020301 Andreas Schaefer:</b>
 * <ul>
 * <li>
 *      Convertion to mejb...() methods and using XDoclet to generate MBean interface
 * </ul>
 *
 * @jmx:mbean extends="org.jboss.management.j2ee.StateManageable,org.jboss.management.j2ee.J2EEModuleMBean"
 **/
public class ResourceAdapterModule
  extends J2EEModule
  implements ResourceAdapterModuleMBean
{

   // Constants -----------------------------------------------------
   
   public static final String J2EE_TYPE = "ResourceAdapterModule";
   
   // Attributes ----------------------------------------------------
   
   private StateManagement mState;
   private List mResourceAdapters = new ArrayList();
   private ObjectName mService;
   //used to see if we should remove our parent when we are destroyed.
   private static final Map mCreatedParents = new HashMap();
   
   // Static --------------------------------------------------------
   
   /**
    * @todo Now support for JMVs right now !
    **/
   public static ObjectName create( MBeanServer pServer, String pApplicationName, String pName, URL pURL, ObjectName pService) {
      Logger lLog = Logger.getLogger( ResourceAdapterModule.class );
      if( !J2EEServer.sIsActive ) {
         return null;
      }
      String lDD = null;
      ObjectName lApplication = null;
      ObjectName lCreated = null;
      try {
         ObjectName lServer = (ObjectName) pServer.queryNames(
             new ObjectName(
               J2EEManagedObject.getDomainName() + ":" +
               J2EEManagedObject.TYPE  + "=" + J2EEServer.J2EE_TYPE + "," +
               "*"
             ),
             null
         ).iterator().next();
         String lServerName = lServer.getKeyPropertyList().get( J2EEManagedObject.TYPE ) + "=" +
                              lServer.getKeyPropertyList().get( "name" );
         lLog.debug( "ResourceAdapterModule.create(), server name: " + lServerName );

         // if pName is equal to pApplicationName then we have 
         // a stand alone Module so do not create a J2EEApplication
         if( pName.compareTo(pApplicationName) != 0 )
         {
             ObjectName parentAppQuery =  new ObjectName(
                J2EEManagedObject.getDomainName() + ":" +
                J2EEManagedObject.TYPE + "=" + J2EEApplication.J2EE_TYPE + "," +
                "name=" + pApplicationName + "," +
                lServerName + "," +
                "*"
             );
             Set parentApps =  pServer.queryNames(parentAppQuery, null);

             if (parentApps.size() == 0)
             {
                lCreated = J2EEApplication.create(
                   pServer,
                   pApplicationName,
                   null
                );
                lApplication = lCreated;

             } // end of if ()
             else if (parentApps.size() == 1)
             {
                lApplication = (ObjectName)parentApps.iterator().next();
             } // end of if ()
             else
             {
//AS                lLog.error("more than one parent app for this ResourceAdapterModule: " + parentApps.size());
                return null;
             } // end of else
          }
          else
          {
             Hashtable lProperties = new Hashtable();
             lProperties.put( J2EEServer.J2EE_TYPE, lServer.getKeyPropertyList().get( "name" ));
             lProperties.put( "name" , " " );
             lApplication = new ObjectName( getDomainName(), lProperties );
          }
          // First get the deployement descriptor
          lDD = J2EEDeployedObject.getDeploymentDescriptor( pURL, J2EEDeployedObject.RAR );

      }
      catch( Exception e ) {
//AS         lLog.error( "Could not create JSR-77 ResourceAdapterModule: " + pApplicationName, e );
         return null;
      }
      try {
         // Now create the ResourceAdapterModule
         lLog.debug(
            "Create ResourceAdapterModule, name: " + pName +
            ", application: " + lApplication +
            ", dd: " + lDD
         );
         ObjectName lModule = pServer.createMBean(
            "org.jboss.management.j2ee.ResourceAdapterModule",
            null,
            new Object[] {
               pName,
               lApplication,
               null,
               lDD,
               pService
            },
            new String[] {
               String.class.getName(),
               ObjectName.class.getName(),
               ObjectName[].class.getName(),
               String.class.getName(),
               ObjectName.class.getName()
            }
         ).getObjectName();
         if( lCreated != null ) {
            mCreatedParents.put( lModule, lCreated );
         }
         return lModule;
      }
      catch( Exception e ) {
//AS         lLog.error( "Could not create JSR-77 ResourceAdapterModule: " + pApplicationName, e );
         return null;
      }
   }
   
   public static void destroy( MBeanServer pServer, String pModuleName ) {
      Logger lLog = Logger.getLogger( ResourceAdapterModule.class );
      if( !J2EEServer.sIsActive ) {
         return;
      }
      try {
         // Find the Object to be destroyed
         ObjectName lSearch = new ObjectName(
            J2EEManagedObject.getDomainName() + ":" +
            J2EEManagedObject.TYPE + "=" + ResourceAdapterModule.J2EE_TYPE + "," +
            "name=" + pModuleName + "," +
            "*"
         );
         Set lNames = pServer.queryNames(
            lSearch,
            null
         );
         if( !lNames.isEmpty() ) {
            ObjectName lResourceAdapterModule = (ObjectName) lNames.iterator().next();
            // Now remove the ResourceAdapterModule
            pServer.unregisterMBean( lResourceAdapterModule );
            ObjectName lApplication = (ObjectName) mCreatedParents.get( lResourceAdapterModule );
            if( lApplication != null ) 
            {
               lLog.info( "Remove fake JSR-77 parent Application: " + lApplication.toString() );
               J2EEApplication.destroy( pServer, lApplication.toString() );
               
            } // end of if ()
         }
      }
      catch( Exception e ) {
//AS         lLog.error( "Could not destory JSR-77 ResourceAdapterModule: " + pModuleName, e );
      }
   }
   
   // Constructors --------------------------------------------------
   
   /**
   * Constructor taking the Name of this Object
   *
   * @param pName Name to be set which must not be null
   * @param pDeploymentDescriptor
   *
   * @throws InvalidParameterException If the given Name is null
   **/
   public ResourceAdapterModule( String pName, ObjectName pApplication, ObjectName[] pJVMs, String pDeploymentDescriptor, ObjectName pService )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( J2EE_TYPE, pName, pApplication, pJVMs, pDeploymentDescriptor );
      mService = pService;
      mState = new StateManagement( this );
   }

   // Public --------------------------------------------------------
   
   // ResourceAdapterodule implementation --------------------------------------
   
   /**
    * @jmx:managed-attribute
    **/
   public ObjectName[] getResourceAdapters() {
      return (ObjectName[]) mResourceAdapters.toArray( new ObjectName[ 0 ] );
   }
   
   /**
    * @jmx:managed-operation
    **/
   public ObjectName getResourceAdapter( int pIndex ) {
      if( pIndex >= 0 && pIndex < mResourceAdapters.size() )
      {
         return (ObjectName) mResourceAdapters.get( pIndex );
      }
      else
      {
         return null;
      }
   }
   
   // J2EEManagedObjectMBean implementation -------------------------
   
   public void addChild( ObjectName pChild ) {
      String lType = J2EEManagedObject.getType( pChild );
      if( ResourceAdapter.J2EE_TYPE.equals( lType ))
      {
         mResourceAdapters.add( pChild );
      }
   }
   
   public void removeChild( ObjectName pChild ) {
      String lType = J2EEManagedObject.getType( pChild );
      if( ResourceAdapter.J2EE_TYPE.equals( lType )) 
      {
         mResourceAdapters.remove( pChild );
      }
   }

   // org.jboss.ServiceMBean overrides ------------------------------------

   public void postCreation() {
      sendNotification(
         new Notification(
            StateManagement.sTypes[ 0 ],
            getName(),
            1,
            System.currentTimeMillis(),
            "Resource Adapter Module created"
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
            "Resource Adapter Module deleted"
         )
      );
/* AS Currently the org.jboss.resource.RARDeployer does not implement
      the NotificationBroadcaster therefore we cannot unregister a listener here
      // Remove the listener of the target MBean
      try {
         getServer().removeNotificationListener( mService, mState );
      }
      catch( JMException jme ) {
         // When the service is not available anymore then just ignore the exception
      }
*/
   }
   
   // org.jboss.managment.j2ee.EventProvider implementation -------------
   
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
   
   public void mejbStart() {
      mState.setState( ServiceMBean.STARTING + 2 );
      try {
         getServer().invoke(
            mService,
            "start",
            new Object[] {},
            new String[] {}
         );
      }
      catch( Exception e ) {
         getLog().error( "Start of Resource Adapter Module failed", e );
      }
      mState.setState( ServiceMBean.STARTED + 2 );
   }

   public void mejbStartRecursive() {
      // No recursive start here
      mejbStart();
   }

   public void mejbStop() {
      mState.setState( ServiceMBean.STOPPING + 2 );
      try {
         getServer().invoke(
            mService,
            "stop",
            new Object[] {},
            new String[] {}
         );
      }
      catch( Exception e ) {
         getLog().error( "Stop of Resource Adapter Module failed", e );
      }
      mState.setState( ServiceMBean.STOPPED + 2 );
   }
   
   // Object overrides ---------------------------------------------------
   
   public String toString() {
      return "ResourceAdapterModule[ " + super.toString() +
         "ResourceAdapters: " + mResourceAdapters +
         " ]";
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   /**
    * @return A hashtable with the J2EE-Application and J2EE-Server as parent
    **/
   protected Hashtable getParentKeys( ObjectName pParent ) {
      Hashtable lReturn = new Hashtable();
      Hashtable lProperties = pParent.getKeyPropertyList();
      lReturn.put( J2EEApplication.J2EE_TYPE, lProperties.get( "name" ) );
      // J2EE-Server is already parent of J2EE-Application therefore lookup
      // the name by the J2EE-Server type
      lReturn.put( J2EEServer.J2EE_TYPE, lProperties.get( J2EEServer.J2EE_TYPE ) );
      
      return lReturn;
   }
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
