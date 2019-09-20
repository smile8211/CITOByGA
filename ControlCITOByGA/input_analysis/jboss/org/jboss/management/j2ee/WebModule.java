/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import java.security.InvalidParameterException;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.WebModule WebModule}.
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
 * @jmx:mbean extends="org.jboss.management.j2ee.J2EEModuleMBean"
 **/
public class WebModule
  extends J2EEModule
  implements WebModuleMBean
{
   
   // Constants -----------------------------------------------------
   
   public static final String J2EE_TYPE = "WebModule";
   
   // Attributes ----------------------------------------------------
   
   private List mServlets = new ArrayList();
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   /**
   * Constructor taking the Name of this Object
   *
   * @param pName Name to be set which must not be null
   * @param pDeploymentDescriptor
   *
   * @throws InvalidParameterException If the given Name is null
   **/
   public WebModule( String pName, ObjectName pApplication, ObjectName[] pJVMs, String pDeploymentDescriptor, ObjectName[] pServlets )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "WebModule", pName, pApplication, pJVMs, pDeploymentDescriptor );
      if( pServlets == null || pServlets.length == 0 ) {
         throw new InvalidParameterException( "Servlet list may not be null or empty" );
      }
      mServlets = new ArrayList( Arrays.asList( pServlets ) );
   }

   // Public --------------------------------------------------------
   
   /**
    * @jmx:managed-attribute
    **/
   public ObjectName[] getServlets() {
      return (ObjectName[]) mServlets.toArray( new Servlet[ 0 ] );
   }
   
   /**
    * @jmx:managed-operation
    **/
   public ObjectName getServlet( int pIndex ) {
      if( pIndex >= 0 && pIndex < mServlets.size() )
      {
         return (ObjectName) mServlets.get( pIndex );
      }
      else
      {
         return null;
      }
   }
   
   // Object overrides ---------------------------------------------------
   
   public String toString() {
      return "WebModule[ " + super.toString() +
         ", Servlets: " + mServlets +
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
