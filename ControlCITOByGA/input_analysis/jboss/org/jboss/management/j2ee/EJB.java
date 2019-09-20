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
 * {@link javax.management.j2ee.EJB EJB}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.4.2.3 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011126 Andreas Schaefer:</b>
 * <ul>
 * <li> Creation
 * </ul>
 *
 * @jmx:mbean extends="org.jboss.management.j2ee.J2EEManagedObjectMBean"
 **/
public abstract class EJB
   extends J2EEManagedObject
   implements EJBMBean
{
   // Constants -----------------------------------------------------
   
   public static final int ENTITY_BEAN = 0;
   public static final int STATEFUL_SESSION_BEAN = 1;
   public static final int STATELESS_SESSION_BEAN = 2;
   public static final int MESSAGE_DRIVEN_BEAN = 3;
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   private static final String[] sTypes = new String[] {
                                             "EntityBean",
                                             "StatefulSessionBean",
                                             "StatelessSessionBean",
                                             "MessageDrivenBean"
                                          };
   
   public static ObjectName create( MBeanServer pServer, String pEjbModule, int pType, String lJNDIName ) {
      Logger lLog = Logger.getLogger( EJB.class );
      if( !J2EEServer.sIsActive ) {
         return null;
      }
      try {
         // Now create the J2EEApplication
         return pServer.createMBean(
            "org.jboss.management.j2ee." + sTypes[ pType ],
            null,
            new Object[] {
               lJNDIName,
               new ObjectName( pEjbModule )
            },
            new String[] {
               String.class.getName(),
               ObjectName.class.getName()
            }
         ).getObjectName();
      }
      catch( Exception e ) {
//AS         lLog.error( "Could not create JSR-77 EJB: " + lJNDIName, e );
         return null;
      }
   }
   
   public static void destroy( MBeanServer pServer, String pEJBName ) {
      Logger lLog = Logger.getLogger( EJB.class );
      if( !J2EEServer.sIsActive ) {
         return;
      }
      try {
         // Now remove the EJB
         pServer.unregisterMBean( new ObjectName( pEJBName ) );
      }
      catch( javax.management.InstanceNotFoundException infe ) {}
      catch( Exception e ) {
//AS         lLog.error( "Could not destory JSR-77 EJB: " + pEJBName, e );
      }
   }
   
   // Constructors --------------------------------------------------
   
   /**
    * @param pName Name of the EntityBean
    *
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public EJB( String pType, String pName, ObjectName pEjbModule )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( pType, pName, pEjbModule );
   }

   // java.lang.Object overrides --------------------------------------

   public String toString() {
      return "EJB { " + super.toString() + " } []";
   }
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   /**
    * @return A hashtable with the EJB-Module, J2EE-Application and J2EE-Server as parent
    **/
   protected Hashtable getParentKeys( ObjectName pParent ) {
      Hashtable lReturn = new Hashtable();
      Hashtable lProperties = pParent.getKeyPropertyList();
      lReturn.put( EJBModule.J2EE_TYPE, lProperties.get( "name" ) );
      // J2EE-Application and J2EE-Server is already parent of J2EE-Application therefore lookup
      // the name by the J2EE-Server type
      lReturn.put( J2EEApplication.J2EE_TYPE, lProperties.get( J2EEApplication.J2EE_TYPE ) );
      lReturn.put( J2EEServer.J2EE_TYPE, lProperties.get( J2EEServer.J2EE_TYPE ) );
      return lReturn;
   }
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
}
