import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import edu.illinois.jacontebe.Helpers;
import edu.illinois.jacontebe.OptionHelper;
import edu.illinois.jacontebe.framework.Reporter;

/**
 * Bug URL:https://issues.apache.org/jira/browse/POOL-149
 * This is a wait-notify deadlock. 
 * Reproduce environment: commons-pool-1.5, JDK 1.6.0_33.
 * 
 * Options: --loops,-l:Number of test iterations, default number is 10. 
 * --monitoroff, -mo: Turn monitor off. When monitor is turned on, 
 *                    it reports the forever waiting message and
 *                    stop the program.
 * 
 * @collector Ziyi Lin
 */
public class Test149 {

    public static int MAX_ACTIVE = 1;
    public static int TEST_TIMES = 10;

    private static int loops;

    public void run() throws Exception {
        loops = OptionHelper.getLoopsValue(TEST_TIMES);

        ObjectPool testPool = new GenericObjectPool(
                new DummyPoolableObjectFactory(), MAX_ACTIVE);
        for (int i = 0; i < loops; i++) {
            runOnce(testPool);
        }
    }

    private void runOnce(ObjectPool testPool) throws Exception {
        Thread t1 = new Thread(new TestThread1(testPool));
        Thread t2 = new Thread(new TestThread2(testPool));

        t1.start();
        t2.start();

        t1.join();
        t2.join();
    }

    public static void main(String[] args) throws Exception {
        int timeOut = 30;
        Reporter.reportStart("pool149", timeOut, "wait-notify deadlock");
        if (!OptionHelper.optionParse(args)) {
            return;
        }
        Helpers.startWaitingMonitor(timeOut);
        Test149 test = new Test149();
        test.run();
        Reporter.reportEnd(false);
    }

    /**
     * A dummy PoolableObjectFactory, creates Strings with unique names
     */
    private class DummyPoolableObjectFactory implements PoolableObjectFactory {

        public Object makeObject() throws Exception {
            return "Object number-" + counter.getAndIncrement();
        }

        public void destroyObject(Object obj) throws Exception {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean validateObject(Object obj) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void activateObject(Object obj) throws Exception {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void passivateObject(Object obj) throws Exception {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        private final AtomicInteger counter = new AtomicInteger();
    }

    class TestThread1 implements Runnable {

        private final ObjectPool testPool;

        public TestThread1(ObjectPool testPool) {
            this.testPool = testPool;
        }

        public void run() {
            Object obj = null;
            try {
                obj = testPool.borrowObject();
            } catch (Exception ex) {
            } finally {
                try {
                    testPool.invalidateObject(obj);
                } catch (Exception ex) {
                }
            }
        }
    }

    class TestThread2 implements Runnable {

        private final ObjectPool testPool;

        public TestThread2(ObjectPool testPool) {
            this.testPool = testPool;
        }

        public void run() {
            try {
                testPool.borrowObject();
            } catch (Exception ex) {
            }
        }
    }
}
