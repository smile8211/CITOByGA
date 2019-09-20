/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.logging.Logger;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.ResourceAdapter ResourceAdapter}.
 *
 * @author  <a href="mailto:mclaugs@comcast.net">Scott McLaughlin</a>.
 * @version $Revision: 1.3.2.2 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20020301 Scott McLaughlin:</b>
 * <ul>
 * <li> Creation
 * </ul>
 *
 * @jmx:mbean extends="org.jboss.management.j2ee.J2EEManagedObjectMBean"
 **/
public class ResourceAdapter
   extends J2EEManagedObject
   implements ResourceAdapterMBean
{
   // Constants -----------------------------------------------------
   
   public static final String J2EE_TYPE = "ResourceAdapter";
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   public static ObjectName create( MBeanServer pServer, String p, String pResourceAdapterName ) {
      Logger lLog = Logger.getLogger( ResourceAdapter.class );
      if( !J2EEServer.sIsActive ) {
         return null;
      }
      try {
        
         // Now create the ResourceAdapter
         return pServer.createMBean(
            "org.jboss.management.j2ee.ResourceAdapter",
            null,
            new Object[] {
               pResourceAdapterName,
               new ObjectName( p )
            },
            new String[] {
               String.class.getName(),
               ObjectName.class.getName()
            }
         ).getObjectName();
      }
      catch( Exception e ) {
//AS         lLog.error( "Could not create JSR-77 ResourceAdapter: " + pResourceAdapterName, e );
         return null;
      }
   }
   
   public static void destroy( MBeanServer pServer, String pResourceAdapterName ) {
      Logger lLog = Logger.getLogger( ResourceAdapter.class );
      if( !J2EEServer.sIsActive ) {
         return;
      }
      try {
         J2EEManagedObject.removeObject(
            pServer,
            J2EEManagedObject.getDomainName() + ":" +
               J2EEManagedObject.TYPE + "=" + ResourceAdapter.J2EE_TYPE + "," +
               "name=" + pResourceAdapterName + "," +
               "*"
         );
      }
      catch( Exception e ) {
//AS         lLog.error( "Could not destory JSR-77 ResourceAdapter: " + pResourceAdapterName, e );
      }
   }
   
   // Constructors --------------------------------------------------
   
   /**
    * @param pName Name of the ResourceAdapter
    *
    * @throws InvalidParameterException
    **/
   public ResourceAdapter(String pName, ObjectName pResourceAdapterModule )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( J2EE_TYPE, pName, pResourceAdapterModule );
   }

   // java.lang.Object overrides --------------------------------------

   public String toString() {
      return "ResourceAdapter { " + super.toString() + " } []";
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   /**
    * @return A hashtable with the Resource-Adapter-Module, J2EE-Application and J2EE-Server as parent
    **/
   protected Hashtable getParentKeys( ObjectName pParent ) {
      Hashtable lReturn = new Hashtable();
      Hashtable lProperties = pParent.getKeyPropertyList();
      lReturn.put( ResourceAdapterModule.J2EE_TYPE, lProperties.get( "name" ) );
      // J2EE-Application and J2EE-Server is already parent of J2EE-Application therefore lookup
      // the name by the J2EE-Server type
      lReturn.put( J2EEApplication.J2EE_TYPE, lProperties.get( J2EEApplication.J2EE_TYPE ) );
      lReturn.put( J2EEServer.J2EE_TYPE, lProperties.get( J2EEServer.J2EE_TYPE ) );
      
      return lReturn;
   }
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
}
