package org.apache.log4j;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import edu.illinois.jacontebe.Helpers;
import edu.illinois.jacontebe.OptionHelper;
import edu.illinois.jacontebe.framework.Reporter;

/**
 * bug URL: https://issues.apache.org/bugzilla/show_bug.cgi?id=50463 
 * This is a wait-notify deadlock.
 * Reproduce environment: log4j 1.2.14, JDK 1.6.0_33
 * 
 * @author Louis Jacomet (axjuz)
 * @collector Ziyi Lin
 */
public class Test50463 {
    private static int BUFFER_SIZE = 12;
    private Logger logger;
    private static final int THREAD_NUM = 10;
    private CyclicBarrier barrier;

    public Test50463() {
        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setBufferSize(BUFFER_SIZE);
        asyncAppender.addAppender(new ConsoleAppender(new SimpleLayout()));
        LogManager.getRootLogger().addAppender(asyncAppender);
        logger = Logger.getLogger(Test50463.class);
        barrier = new CyclicBarrier(THREAD_NUM + 1);
    }

    public static void main(String[] args) throws InterruptedException {
        int timeout = 30;
        if (!OptionHelper.optionParse(args)) {
            return;
        }
        Helpers.startWaitingMonitor(timeout);
        Reporter.reportStart("log4j50463", timeout, "deadlock");

        Test50463 test = new Test50463();
        test.logToFillBuffer();
        Reporter.reportEnd(false);
    }

    private void logToFillBuffer() {
        // start several threads to fill up the buffer.
        Thread[] fills = new Thread[THREAD_NUM];
        for (int i = 0; i < THREAD_NUM; i++) {
            fills[i] = new FillBufferThread();
            fills[i].start();
        }
        logToKillDispatcher();
        for (int i = 0; i < THREAD_NUM; i++) {
            try {
                fills[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class FillBufferThread extends Thread {
        public void run() {
            try {
                barrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < BUFFER_SIZE * 10; i++) {
                logger.error("Locking me up");
            }
        }
    }

    private void logToKillDispatcher() {
        try {
            barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
        logger.error(new Object() {
            public String toString() {
                throw new RuntimeException();
            }
        });
    }
}