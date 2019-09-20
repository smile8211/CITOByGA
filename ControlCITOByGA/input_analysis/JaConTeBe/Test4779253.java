import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import edu.illinois.jacontebe.framework.Reporter;

/**
 * Bug URL:https://bugs.openjdk.java.net/browse/JDK-4779253<br>
 * This is a race.
 * Reproduce environment: JDK 1.6.0_33, JDK 1.6.0<br>
 * This bug is fixed since JDK 1.7.0.
 * This bug is very difficult to reproduce, so the byte code of Logger
 * class is instrumented with a short pause at where race happens to extend the
 * buggy window first. And then this test is run based on the instrumented code.
 * 
 * There are two stages to reproduce this bug.
 * <p>
 * Stage 1: Modify the byte code of buggy class. The buggy window at original
 * source code is very narrow, so we modify the byte code of buggy class to add
 * a short sleep at where race happens to extend the buggy window to make the
 * bug easier to reproduce. Run asm.LoggerModifier class first to generate a
 * modified class file at this projet's "classes" directory.
 * 
 * </p>
 * <p>
 * Stage 2: Run this class with -Xbootclasspath/p:classes option to load
 * modified class to replace the one from jdk.
 * </p>
 * Expected exception is a NullPointerException.
 * 
 * @author Ziyi Lin
 *
 */
public class Test4779253 {

    private Logger logger;
    private volatile boolean buggy;
    private CountDownLatch latch;
    // Store exceptions in a queue and print them out
    // at the end of the test.
    private Queue<Exception> exceptionQ;

    public Test4779253() {

        logger = Logger.getLogger("TestLog");
        logger.setLevel(Level.ALL);
        // initiate logger.
        logger.setFilter(new Filter() {

            @Override
            public boolean isLoggable(LogRecord record) {
                return false;
            }

        });
        latch = new CountDownLatch(1);
        buggy = false;
        exceptionQ = new ConcurrentLinkedQueue<Exception>();
    }

    public void run() {
        Thread w1 = new WorkerThread1();
        Thread w2 = new WorkerThread2();
        w1.start();
        w2.start();
        try {
            w1.join();
            w2.join();
        } catch (InterruptedException e) {

        }
    }

    public boolean isBuggy() {
        return buggy;
    }

    public static void main(String[] args) {
        Reporter.reportStart("jdk4779253", 0, "race");
		Reporter.printWarning(null,"1.7.0",null);
        Test4779253 test = new Test4779253();
        test.run();
        if (test.isBuggy()) {
            test.printException();
        }
        Reporter.reportEnd(test.isBuggy());
    }

    private void printException() {
        while (exceptionQ.size() > 0) {
            Exception e = exceptionQ.remove();
            e.printStackTrace();
        }

    }

    private class WorkerThread1 extends Thread {
        public WorkerThread1() {
            super("WorkerThread 1");
        }

        @Override
        public void run() {
            LogRecord record = new LogRecord(Level.ALL, "hello world");
            try {
                latch.countDown();
                // trigger race
                logger.log(record);
            } catch (Exception e) {
                exceptionQ.add(e);
                buggy = true;
            }
        }
    }

    private class WorkerThread2 extends Thread {

        public WorkerThread2() {
            super("WorkerThread 2");
        }

        public void run() {
            try {
                latch.await();
                // trigger race
                logger.setFilter(null);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
