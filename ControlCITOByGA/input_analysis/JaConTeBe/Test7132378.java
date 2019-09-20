/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
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
 */

/*
 * @test
 * @bug 7132378
 * @summary Race in FutureTask if used with explicit set ( not Runnable )
 * @author Chris Hegarty
 */

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import edu.illinois.jacontebe.framework.Reporter;

/**
 * Bug URL:https://bugs.openjdk.java.net/browse/JDK-7132378
 * This is a race. 
 * Reproduce environment: JDK 1.7.0
 * This bug affects JDK 1.7.0 and is fixed since JDK 1.7.0_40.
 * The expected exception is NullpointerException.
 * 
 * There are two stages to reproduce this bug.
 * <p>
 * Stage 1: Modify the byte code of buggy class. The buggy window at original
 * source code is very narrow, so we modify the byte code of buggy class to add
 * a short sleep at where race happens to extend the buggy window to make the
 * bug easier to reproduce. Run asm.FutureTaskModifier class first to generate a
 * modified class file at this projet's "classes" directory.
 * 
 * </p>
 * <p>
 * Stage 2: Run this class with -Xbootclasspath/p:classes option to load
 * modified class to replace the one from jdk.
 * 
 * @collector Ziyi Lin
 *
 */
public class Test7132378 {
    static volatile boolean buggy;
    
    static void realMain(String[] args) throws Throwable {
        buggy =false;
        test();
    }

    static void test() throws Throwable {
        final SettableTask task = new SettableTask();
        
        int numOfThread =2;
       
        // More threads to call SettableTask.get() may increase the
        // probability of race.
        Thread[] ts = new Thread[numOfThread];
        for (int i = 0; i < ts.length; i++) {
            ts[i] = new GetThread(task);
            ts[i].start();
        }
        task.set(Boolean.TRUE);
        for (int i = 0; i < ts.length; i++) {
            ts[i].join(5000);
        }
    }

    // A thread to get the value set by SettableTask.
    // A NullPointerException is thrown out if race happens.
    static class GetThread extends Thread {

        private SettableTask task;

        public GetThread(SettableTask t) {
            task = t;
        }

        public void run() {
            try {
                if (task.get() != true) {
                    buggy=true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                buggy=true;
                throw e;
            }
        }
    }

    static class SettableTask extends FutureTask<Boolean> {
        SettableTask() {
            super(new Callable<Boolean>() {
                public Boolean call() {
                    return null;
                };
            });
        }

        @Override
        public void set(Boolean b) {
            super.set(b);
        }
    }

    public static void main(String[] args) throws Throwable {
        Reporter.reportStart("jdk7132378", 0, "race");
        Reporter.printWarning("1.7.0", "1.7.0_40", null);
        try {
            realMain(args);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        Reporter.reportEnd(buggy);
    }
}
