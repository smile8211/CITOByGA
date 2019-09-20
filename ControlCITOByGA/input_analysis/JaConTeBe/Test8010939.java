/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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
import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import edu.illinois.jacontebe.Helpers;
import edu.illinois.jacontebe.OptionHelper;
import edu.illinois.jacontebe.framework.Reporter;

/**
 * Bug URL:https://bugs.openjdk.java.net/browse/JDK-8010939 
 * This is a deadlock.
 * Reproduce environment: JDK 1.7.0_17
 * This bug affects since JDK 1.7.0_13 and is fixed since JDK 1.7.0_25.
 * 
 * @author jim.gish@oracle.com
 * @collector Ziyi Lin
 */
public class Test8010939 {
    private LogManager mgr = LogManager.getLogManager();
    private final static int MAX_ITERATIONS = 5;
    private int loops;

    public static void main(String... args) throws IOException, Exception {
        Reporter.reportStart("jdk8010939", 0, "deadlock");
        Reporter.printWarning("1.7.0_13", "1.7.0_25", null);
        if (!OptionHelper.optionParse(args)) {
            return;
        }
        Helpers.startDeadlockMonitor();
        new Test8010939().testForDeadlock();
        Reporter.reportEnd(false);
    }

    public static void randomDelay() {
        int runs = (int) Math.random() * 1000000;
        int c = 0;
        for (int i = 0; i < runs; ++i) {
            c = c + i;
        }
    }

    public void testForDeadlock() throws IOException, Exception {
        loops=OptionHelper.getLoopsValue(MAX_ITERATIONS);
        Thread setup = new Thread(new SetupLogger(), "SetupLogger");
        Thread readConfig = new Thread(new ReadConfig(), "ReadConfig");

        // make the threads daemon threads so they will go away when the
        // test exits
        setup.setDaemon(true);
        readConfig.setDaemon(true);

        setup.start();
        readConfig.start();

        try {
            readConfig.join();
            setup.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    class SetupLogger implements Runnable {
        Logger logger = null;

        @Override
        public void run() {
            for (int i = 0; i < loops; i++) {
                logger = Logger.getLogger("DrainFindDeadlockTest" + i);
                Test8010939.randomDelay();
            }
        }
    }

    class ReadConfig implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < loops; i++) {
                try {
                    mgr.readConfiguration();
                } catch (IOException ex) {
                    throw new RuntimeException("FAILED: test setup problem", ex);
                } catch (SecurityException ex) {
                    throw new RuntimeException("FAILED: test setup problem", ex);
                }
                Test8010939.randomDelay();
            }
        }
    }
}
