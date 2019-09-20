//
// Copyright (C) 2005 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
// (NASA).  All Rights Reserved.
// 
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3.  The NOSA has been approved by the Open Source
// Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
// 
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//
package DEOS;

/**
 * DOCUMENT ME!
 */
class DEOSKernel {
  //typedef enum
  //{
  public static final int threadSuccess = 0;
  public static final int threadInvalidHandle = 1;
  public static final int threadInvalidInterrupt = 2;
  public static final int threadNotDormant = 3;
  public static final int threadNotSchedulable = 4;
  public static final int threadInsufficientPrivilege = 5;
  public static final int threadNotDynamic = 6;
  public static final int threadNotStatic = 7;
  public static final int threadMaximumThreadsExceeded = 8;
  public static final int threadInsufficientRAMForStack = 9;
  public static final int threadNoSuchThread = 10;
  public static final int threadInvalidTemplate = 11;
  public static final int threadNotActive = 12;
  public static final int threadInScheduleBefore = 13;
  public static final int threadInsufficientBudget = 14;
  public static final int threadDuplicateISR = 15;
  public static final int threadInvalidFromDynamicProcess = 16;
  public static final int threadPrimaryCannotBeISR = 17;

  static void coldStartKernel () {
    //System.out.println("DEOSKernel.coldStartKernel");		
    // Must be done before Scheduler.
    StartOfPeriodEvent.initialize();


    // Must be done after System.
    // Scheduler initialize doesn't return unless we get a shutdown()
    Scheduler.initialize();

    //    System.out.println("DEOSKernel: Finished Initialization");	
  }

  static int createThreadK (String name, int threadTemplateId, int threadBudget, 
                            int periodIndex) {
    //System.out.println("API: createThreadK Period " + periodIndex + 
    //                                     " Budget " + threadBudget );
    int         returnStatus;
    DEOSProcess currentProcess = Scheduler.currentProcess();

    // Allocate a thread, then initialize it
    Thread threadCreated = new Thread(name);

    if (threadCreated == null) {
      System.out.println("Thread could not be created");
      returnStatus = threadMaximumThreadsExceeded;
    } else {
      // Allocate stack and initialize the thread...
      if (!threadCreated.ConceptualObjectConstructor(periodIndex)) {
        threadCreated = null;
        returnStatus = threadInsufficientRAMForStack;
      } else {
        int interruptState = CPU.enterCritical();
        returnStatus = localStartThread(threadCreated, threadBudget, 
                                        periodIndex);
        CPU.exitCritical(interruptState);

        if (threadSuccess == returnStatus) {
        } else {
          threadCreated.ConceptualObjectDestructor();
          threadCreated = null;
        }
      }
    }

    return returnStatus;
  }

  static int deleteThreadK (Thread theThread) {
    //System.out.println(theThread + " Made it into deleteThread ");
    if (theThread != Scheduler.currentThread()) {
      System.out.println("Thread " + theThread + " no longer running delete");

      return 0;
    }

    int result;
    int interruptState = CPU.enterCritical();

    CPU.exitCritical(interruptState);
    theThread.initiateStopAndDelete();
    result = threadSuccess;
    interruptState = CPU.enterCritical();
    CPU.exitCritical(interruptState);

    return result;
  }

  static int localStartThread (Thread theThread, int requestedThreadBudget, 
                               int periodIndex) {
    // changed the following code because can't pass int (budget) by reference.
    //  cpuTimeInMicroseconds budget; 
    int budget; // budget set by following call.

    //System.out.println("DEOSKernel.localStartThread");
    budget = DEOSProcess.allocateCPUBudgetForThread(theThread, 
                                                  requestedThreadBudget, 
                                                  periodIndex);

    if (budget > -1) {
      theThread.startThread(budget);

      return threadSuccess;
    } else {
      return threadNotSchedulable;
    }
  }

  static int waitUntilNextPeriodK (Thread th) {
    //    System.out.println(th + " Made it into WaitUntil..." + Scheduler.currentThread());
    if (th != Scheduler.currentThread()) {
      System.out.println("Thread " + th + " no longer running");

      return 0;
    }


    //DEOS.handler.resetTimerInterrupt();
    Scheduler.currentThread().waitForNextPeriod();

    return 0; // void really
  }

  //} threadStatus;
}