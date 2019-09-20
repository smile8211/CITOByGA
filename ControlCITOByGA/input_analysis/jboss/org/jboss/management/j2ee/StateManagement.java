/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import java.security.InvalidParameterException;
import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.NotificationListener;

import org.jboss.system.ServiceMBean;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.EJBModule EJBModule}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.3.2.1 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011123 Andreas Schaefer:</b>
 * <ul>
 * <li> Adjustments to the JBoss Guidelines and implementing of the
 *      the create() and destroy() helper method
 * </ul>
 **/
public class StateManagement
   implements NotificationListener
{

   // Constants -----------------------------------------------------
   
   public static final String[] sTypes = new String[] {
                                             "j2ee.object.created",
                                             "j2ee.object.deleted",
                                             "state.starting",
                                             "state.running",
                                             "state.stopping",
                                             "state.stopped",
                                             "state.failed"
                                          };
   
   // Attributes ----------------------------------------------------
   
   private long mStartTime = -1;
   private int mState = ServiceMBean.STOPPED;
   private J2EEManagedObject mTarget;
   
   // Static --------------------------------------------------------
   
   /**
    * Converts a start from JBoss Service MBeans to the JSR-77 state
    * and vice versa
    *
    * @param pFromService True if the conversion is from Service to JSR-77
    *                     otherwise false
    * @param pState State to be converted. Must be between 0 and 4.
    *
    * @return Converted state or -1 if unknown.
    **/
   public static int convertState( boolean pFromService, int pState ) {
      if( pFromService ) {
         switch( pState ) {
            case ServiceMBean.STARTING:
               return StateManageable.STARTING;
            case ServiceMBean.STARTED:
               return StateManageable.RUNNING;
            case ServiceMBean.STOPPING:
               return StateManageable.STOPPING;
            case ServiceMBean.STOPPED:
               return StateManageable.STOPPED;
            case ServiceMBean.FAILED:
               return StateManageable.FAILED;
         }
      } else {
         switch( pState ) {
            case StateManageable.STARTING:
               return ServiceMBean.STARTING;
            case StateManageable.RUNNING:
               return ServiceMBean.STARTED;
            case StateManageable.STOPPING:
               return ServiceMBean.STOPPING;
            case StateManageable.STOPPED:
               return ServiceMBean.STOPPED;
            case StateManageable.FAILED:
               return ServiceMBean.FAILED;
         }
      }
      return -1;
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
   public StateManagement( J2EEManagedObject pTarget )
   {
      if( pTarget == null ) {
         throw new InvalidParameterException( "Target for State Management must be defined" );
      }
      mTarget = pTarget;
   }
   
   // Public --------------------------------------------------------
   
   public long getStartTime() {
      return mStartTime;
   }
   
   public void setStartTime( long pTime ) {
      mStartTime = pTime;
   }
   
   public int getState() {
      return mState;
   }
   
   public void setState( int pState ) {
      // Only send a notification if the state really changes
      if( pState != mState ) {
         mState = pState;
         // Now send the event to the JSR-77 listeners
         mTarget.sendNotification(
            new Notification(
               StateManagement.sTypes[ mState + 2 ],
               mTarget.getName(),
               1,
               System.currentTimeMillis(),
               "State changed"
            )
         );
      }
   }
   
   // NotificationListener overrides ---------------------------------
   
   public void handleNotification( Notification pNotification, Object pHandback )
   {
      if( pNotification instanceof AttributeChangeNotification ) {
         AttributeChangeNotification lChange = (AttributeChangeNotification) pNotification;
         if( "State".equals( lChange.getAttributeName() ) )
         {
            int lState = ( (Integer) lChange.getNewValue() ).intValue();
            long lStartTime = -1;
            if( lState == ServiceMBean.STARTED ) {
               lStartTime = lChange.getTimeStamp();
            }
            setStartTime( lStartTime );
            setState( convertState( true, lState ) );
         }
      }
   }
   
   // Object overrides ---------------------------------------------------
   
   public String toString() {
      return "StateManagement [ " +
         "State: " + mState +
         ", Start Time: " + mStartTime +
         " ]";
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
