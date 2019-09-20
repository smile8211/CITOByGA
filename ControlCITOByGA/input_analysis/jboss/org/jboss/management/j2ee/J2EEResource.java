/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.J2EEResource J2EEResource}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.2.2.1 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011126 Andreas Schaefer:</b>
 * <ul>
 * <li> Adjustments to the JBoss Guidelines
 * </ul>
 *
 * @jmx:mbean extends="org.jboss.management.j2ee.J2EEManagedObjectMBean"
 **/
public abstract class J2EEResource
   extends J2EEManagedObject
   implements J2EEResourceMBean
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructor ---------------------------------------------------
   
   /**
    * @param pName Name of the J2EEResource
    *
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public J2EEResource( String pType, String pName, ObjectName pServer )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( pType, pName, pServer );
   }

   // Z implementation ----------------------------------------------
   
   // ServiceMBeanSupport overrides ---------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   /**
    * @return A hashtable with the J2EE Server as parent
    **/
   protected Hashtable getParentKeys( ObjectName pParent ) {
      Hashtable lReturn = new Hashtable();
      Hashtable lProperties = pParent.getKeyPropertyList();
      lReturn.put( J2EEServer.J2EE_TYPE, lProperties.get( "name" ) );
      
      return lReturn;
   }
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
