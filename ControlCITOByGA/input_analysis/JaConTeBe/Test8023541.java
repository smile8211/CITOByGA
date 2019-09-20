/*
 * Copyright (c) 2000, 2001, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 * 
 * @author Laird Dornin; code borrowed from Ann Wollrath
 */

import java.net.URL;
import java.rmi.activation.Activatable;
import java.rmi.activation.ActivationDesc;
import java.rmi.activation.ActivationGroup;
import java.rmi.activation.ActivationGroupDesc;
import java.rmi.activation.ActivationGroupID;
import java.rmi.activation.ActivationSystem;

import testUtils.ActivationLibrary;
import testUtils.MyRMI;
import testUtils.RMID;
import testUtils.TestLibrary;
import testUtils.TestParams;
import edu.illinois.jacontebe.framework.Reporter;

/**
 * Bug URL:https://bugs.openjdk.java.net/browse/JDK-8023541<br>
 * This is a race bug.
 * Reproduce environment: JDK 1.7.0<br>
 * This bug affects since JDK 1.7.0 and is fixed since JDK 1.9.0. 
 * The expected exception is NullpointerException.
 * 
 * There are two stages to reproduce this bug.
 * <p>
 * Stage 1: Modify the byte code of buggy class. The buggy window at original
 * source code is very narrow, so we modify the byte code of buggy class to add
 * a short sleep at where race happens to extend the buggy window to make the
 * bug easier to reproduce. Run asm.ActivationModifier class first to generate a
 * modified class file at this projet's "classes" directory.
 * 
 * </p>
 * <p>
 * Stage 2: Run this class with -Xbootclasspath/p:classes option to load
 * modified class to replace the one from JDK, and 
 * -Djava.security.policy=security.policy option to specify the security
 * policy file.
 * 
 * </p>
 * 
 * @collector Ziyi Lin
 */
public class Test8023541 {

    private static MyRMI myRMI = null;
    private static ActivationGroup group = null;

    public static void main(String args[]) {
        Reporter.reportStart("jdk8023541", 60, "race");
        boolean buggy = false;
        /*
         * The following line is required with the JDK 1.2 VM because of gc
         * hocus pocus that may no longer be needed with an exact vm (hotspot).
         */
        String loc = System.getProperty("user.dir");
        RMID rmid = null;
        try {
            URL implcb = TestLibrary.installClassInCodebase("ActivatableImpl",
                    "implcb/testUtils");
            TestLibrary.installClassInCodebase("ActivatableImpl_Stub",
                    "implcb/testUtils");
            TestLibrary
                    .suggestSecurityManager(TestParams.defaultSecurityManager);
            RMID.removeLog();
            rmid = RMID.createRMID();

            rmid.start(loc);

            System.err.println("Create activation group in this VM");
            ActivationGroupDesc groupDesc = new ActivationGroupDesc(null, null);
            ActivationSystem system = ActivationGroup.getSystem();
            ActivationGroupID groupID = system.registerGroup(groupDesc);
            group = ActivationGroup.createGroup(groupID, groupDesc, 0);

            ActivationDesc desc = new ActivationDesc("ActivatableImpl",
                    implcb.toString(), null);
            myRMI = (MyRMI) Activatable.register(desc);
            System.err.println("Checking that impl has correct "
                    + "context class loader");
            if (!myRMI.classLoaderOk()) {
                TestLibrary.bomb("incorrect context class loader for "
                        + "activation constructor");
            }

            System.err.println("Deactivate object via method call");
            myRMI.shutdown();
            System.err.println("\nsuccess: CheckImplClassLoader test passed ");
        } catch (NullPointerException e) {
            buggy = true;
            e.printStackTrace();
        } catch (Exception e) {
            TestLibrary.bomb("\nfailure: unexpected exception ", e);
        } finally {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
            }
            myRMI = null;
            System.err.println("rmid shut down");
            ActivationLibrary.rmidCleanup(rmid);
            TestLibrary.unexport(group);
        }
        Reporter.reportEnd(buggy);
    }
}
