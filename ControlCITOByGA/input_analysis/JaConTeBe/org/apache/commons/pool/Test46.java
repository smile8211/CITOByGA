package org.apache.commons.pool;

import java.util.concurrent.CountDownLatch;

import edu.illinois.jacontebe.Helpers;
import edu.illinois.jacontebe.OptionHelper;
import edu.illinois.jacontebe.framework.Reporter;

/**
 * Bug URL:https://issues.apache.org/jira/browse/POOL-46
 * 
 * This is a inconsistent synchronization bug. 
 * Reproduce environment: commons-pool 1.2, JDK 1.6.0_33
 * 
 * Options:
 * --threadnum,-tn: thread number. default is 10.
 * 
 * @author Ziyi Lin
 * 
 */
public class Test46 {

    private TestBaseObjectPool testBasePool;

    // use CountDownLatch to control the threads to make
    // sure reader threads start first and writer thread
    // starts after reader threads.
    private CountDownLatch latch;

    private final static int DEFAULT_READS = 10;

    private volatile static int threads;

    public Test46() {
        testBasePool = new TestBaseObjectPool();
        latch = new CountDownLatch(threads);

    }

    public void runTest(boolean dosync) {
        Thread[] reads = new Thread[threads];
        for (int i = 0; i < reads.length; i++) {
            reads[i] = new Reader(dosync, i);
            reads[i].start();
        }
        Thread writer = new Writer(dosync);
        writer.start();
        for (int i = 0; i < reads.length; i++) {
            try {
                reads[i].join();
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }
        try {
            writer.join();
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        int timeout = 30;
        Reporter.reportStart("pool46", timeout, "inconsistent synchronization");

        if (!OptionHelper.optionParse(args)) {
            return;
        }
        Helpers.startEndlessLoopMonitor(timeout, "Reader");
        threads = OptionHelper.getThreadNumValue(DEFAULT_READS);
        startTest(true); // start the test with proper synchronization
        startTest(false); // start the test without synchronization
        System.exit(0);
    }

    private static void startTest(boolean sync) {
        Test46 test = new Test46();

        String startText = sync ? " proper " : " not ";
        String endText = sync ? "Proper" : "Not";
        System.out
                .println("Now starting" + startText + "synchronized version:");
        if (!sync) {
            System.out.println("This could be an endless loop.");
        }
        long start = System.currentTimeMillis();
        test.runTest(sync);

        System.out.println(endText
                + " synchronized version's execution time is "
                + (System.currentTimeMillis() - start) + " ms");
    }

    /**
     * Reader thread checks the value of shared value.
     * 
     * @author Ziyi Lin
     * 
     */
    private class Reader extends Thread {

        private boolean dosync;

        public Reader(boolean sync, int index) {
            dosync = sync;
            setName("Reader-" + index);
        }

        public void run() {
            latch.countDown();
            // Check if the shared variable's value has been changed
            // by other threads. Exit the loop until the value has been
            // changed.
            if (dosync) {
                while (true) {
                    synchronized (testBasePool) {
                        if (testBasePool.isClosed()) {
                            break;
                        }
                    }
                }
            }
            else {
                while (true) {
                    if (testBasePool.isClosed()) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Writer thread modify shared variable's value.
     * 
     * @author Ziyi Lin
     * 
     */
    private class Writer extends Thread {

        private boolean dosync;

        public Writer(boolean sync) {
            dosync = sync;
            setName("Writer");
        }

        public void run() {
            try {
                latch.await();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            try {
                // call close method to change the value of field "closed"
                if (dosync) {
                    synchronized (testBasePool) {
                        testBasePool.close();
                    }
                } else {
                    testBasePool.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

/**
 * This is a mock class.
 * 
 * @author Ziyi Lin
 * 
 */
class TestBaseObjectPool extends BaseObjectPool {

    private volatile boolean done;

    @Override
    public Object borrowObject() throws Exception {
        return null;
    }

    @Override
    public void returnObject(Object obj) throws Exception {

    }

    @Override
    public void invalidateObject(Object obj) throws Exception {

    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
