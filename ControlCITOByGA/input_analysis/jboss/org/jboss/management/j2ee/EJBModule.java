/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
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

import org.jboss.logging.Logger;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.EJBModule EJBModule}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.6.2.4 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011123 Andreas Schaefer:</b>
 * <ul>
 * <li> Adjustments to the JBoss Guidelines and implementing of the
 *      the create() and destroy() helper method
 * </ul>
 *
 * @jmx:mbean extends="org.jboss.management.j2ee.StateManageable,org.jboss.management.j2ee.J2EEModuleMBean"
 **/
public class EJBModule
  extends J2EEModule
  implements EJBModuleMBean
{

   // Constants -----------------------------------------------------

   public static final String J2EE_TYPE = "EJBModule";

   // Attributes ----------------------------------------------------

   private List mEJBs = new ArrayList();
   private StateManagement mState;
   private ObjectName mService;
   //used to see if we should remove our parent when we are destroyed.
   private static final Map mCreatedParents = new HashMap();

   // Static --------------------------------------------------------

   /**
    * @todo AS: Now JVMs managed added now
    **/
   public static ObjectName create( MBeanServer pServer, String pApplicationName, String pName, URL pURL, ObjectName pService ) {
      Logger lLog = Logger.getLogger( EJBModule.class );
      if( !J2EEServer.sIsActive ) {
         return null;
      }
      String lDD = null;
      ObjectName lApplication = null;
      ObjectName lCreated = null;
      try {
         ObjectName serverQuery = new ObjectName(
            J2EEManagedObject.getDomainName() + ":" +
            J2EEManagedObject.TYPE + "=" + J2EEServer.J2EE_TYPE + "," +
            "*"
         );
         Set servers = pServer.queryNames(serverQuery, null);
         if (servers.size() != 1)
         {
//AS            lLog.error("Wrong number of servers found, should be 1: " + servers.size());
            return null; 
         } // end of if ()

         ObjectName lServer = (ObjectName)servers.iterator().next();

         String lServerName = lServer.getKeyPropertyList().get( J2EEManagedObject.TYPE ) + "=" +
                              lServer.getKeyPropertyList().get( "name" );

         lLog.debug( "EJBModule.create(), server name: " + lServerName );

         // if pName is equal to pApplicationName then we have
         // a stand alone Module so do not create a J2EEApplication 

         if(pName.compareTo(pApplicationName) != 0)
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
//AS               lLog.error("more than one parent app for this ejb-module: " + parentApps.size());
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
         lDD = J2EEDeployedObject.getDeploymentDescriptor( pURL, J2EEDeployedObject.EJB );
      }
      catch( Exception e ) {
//AS         lLog.error( "Could not create JSR-77 EJBModule: " + pApplicationName, e );
         return null;
      }
      try {
         // Now create the J2EE EJB module
         lLog.debug(
            "Create EJB-Module, name: " + pName +
            ", application: " + lApplication
         );

         if( lLog.isTraceEnabled() )
         {
            lLog.trace( "Deployment Descriptor:\n" + lDD );
         }

         ObjectName lEJBModule = pServer.createMBean(
            "org.jboss.management.j2ee.EJBModule",
            null,
            new Object[] {
               pName,
               lApplication,
               null, // No JVMs management now
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
         //remember if we created our parent, if we did we have to kill it on destroy.
         if( lCreated != null ) {
            mCreatedParents.put( lEJBModule, lCreated );
         } // end of if ()
         return lEJBModule;

      }
      catch( Exception e ) {
         return null;
      }
   }

   public static void destroy( MBeanServer pServer, String pModuleName ) {
      Logger lLog = Logger.getLogger( EJBModule.class );
      if( !J2EEServer.sIsActive ) {
         return;
      }
      try {
         ObjectName lName = new ObjectName( pModuleName );
         // Now remove the EJBModule
         pServer.unregisterMBean( lName );
         ObjectName lParent = (ObjectName)mCreatedParents.get( lName );
         if( lParent != null ) 
         {
            lLog.info( "Remove fake JSR-77 parent Application: " + lParent.toString() );
            J2EEApplication.destroy( pServer, lParent.toString() );

         } // end of if ()
      }
      catch( javax.management.InstanceNotFoundException infe ) {}
      catch( Exception e ) {
//AS         lLog.error( "Could not destory JSR-77 EJBModule: " + pModuleName, e );
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
   public EJBModule( String pName, ObjectName pApplication, ObjectName[] pJVMs, String pDeploymentDescriptor, ObjectName pService )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( J2EE_TYPE, pName, pApplication, pJVMs, pDeploymentDescriptor );
      mService = pService;
      mState = new StateManagement( this );
   }

   // Public --------------------------------------------------------

   // EJBModule implementation --------------------------------------

   /**
    * @jmx:managed-attribute
    **/
   public ObjectName[] getEJBs() {
      return (ObjectName[]) mEJBs.toArray( new ObjectName[ 0 ] );
   }

   /**
    * @jmx:managed-operation
    **/
   public ObjectName getEJB( int pIndex ) {
      if( pIndex >= 0 && pIndex < mEJBs.size() )
      {
         return (ObjectName) mEJBs.get( pIndex );
      }
      else
      {
         return null;
      }
   }

   // J2EEManagedObjectMBean implementation -------------------------

   public void addChild( ObjectName pChild ) {
      String lType = J2EEManagedObject.getType( pChild );
      if( EntityBean.J2EE_TYPE.equals( lType ) ||
         StatelessSessionBean.J2EE_TYPE.equals( lType ) ||
         StatefulSessionBean.J2EE_TYPE.equals( lType ) ||
         MessageDrivenBean.J2EE_TYPE.equals( lType )
      ) {
         mEJBs.add( pChild );
      }
   }

   public void removeChild( ObjectName pChild ) {
      String lType = J2EEManagedObject.getType( pChild );
      if( EntityBean.J2EE_TYPE.equals( lType ) ||
         StatelessSessionBean.J2EE_TYPE.equals( lType ) ||
         StatefulSessionBean.J2EE_TYPE.equals( lType ) ||
         MessageDrivenBean.J2EE_TYPE.equals( lType )
      ) {
         mEJBs.remove( pChild );
      }
   }

   public void postCreation() {
      try {
         getServer().addNotificationListener( mService, mState, null, null );
      }
      catch( JMException jme ) {
      }
      sendNotification(
         new Notification(
            StateManagement.sTypes[ 0 ],
            getName(),
            1,
            System.currentTimeMillis(),
            "EJB Module created"
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
            "EJB Module deleted"
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
         getLog().error( "Start of EJBModule failed", e );
      }
   }

   public void mejbStartRecursive() {
      // No recursive start here
      try {
         mejbStart();
/*AS EJBs are not state manageable right now
         Iterator i = mEJBs.iterator();
         while( i.hasNext() ) {
            ObjectName lName = (ObjectName) i.next();
            try {
               getServer().invoke(
                  lName,
                  "mejbStart",
                  new Object[] {},
                  new String[] {}
               );
            }
            catch( JMException jme ) {
               getLog().error( "Start of EJBModule failed", jme );
            }
         }
*/
      }
      catch( Exception e ) {
         getLog().error( "Recursive Start of EJBModule failed", e );
      }
   }

   public void mejbStop() {
      try {
/* AS EJBs are not state manageable right now
         Iterator i = mEJBs.iterator();
         while( i.hasNext() ) {
            ObjectName lName = (ObjectName) i.next();
            try {
               getServer().invoke(
                  lName,
                  "mejStop",
                  new Object[] {},
                  new String[] {}
               );
            }
            catch( JMException jme ) {
               getLog().error( "Recursive Stop of EJBModule failed", jme );
            }
         }
*/
         getServer().invoke(
            mService,
            "stop",
            new Object[] {},
            new String[] {}
         );
      }
      catch( Exception e ) {
         getLog().error( "Stop of EJBMOdule failed", e );
      }
   }

   // Object overrides ---------------------------------------------------

   public String toString() {
      return "EJBModule[ " + super.toString() +
         "EJBs: " + mEJBs +
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
/*
vim:ts=3:sw=3:et
*/
