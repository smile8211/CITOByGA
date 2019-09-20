package org.apache.derby.impl.store.raw.data;

import static org.mockito.Mockito.mock;

import java.util.concurrent.CountDownLatch;

import org.apache.derby.iapi.error.StandardException;
import org.apache.derby.iapi.store.raw.ContainerKey;
import org.apache.derby.iapi.store.raw.LockingPolicy;
import org.apache.derby.iapi.store.raw.PageKey;
import org.apache.derby.iapi.store.raw.xact.RawTransaction;

import edu.illinois.jacontebe.Helpers;
import edu.illinois.jacontebe.OptionHelper;
import edu.illinois.jacontebe.framework.Reporter;

/**
 * Bug URL: https://issues.apache.org/jira/browse/DERBY-5447
 * This is a deadlock.
 * Reproduce environment: derby 10.5.1.1, JDK 1.6.0_33
 * 
 * Options: --monitoroff, -mo : Turn deadlock monitor off. When monitor is
 * turned on, it reports the deadlock message and stop the program.
 * 
 * @author Ziyi Lin
 * 
 */
public class Derby5447 {
    // This thread should be started later than another thread in order to
    // let two threads lock each other.
    private class Thread1 extends Thread {

        public Thread1() {
            setName("Thread-1");
        }

        public void run() {
            try {
                latch.await();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            storedPage.releaseExclusive();
        }
    }

    private class Thread2 extends Thread {

        public Thread2() {
            setName("Thread-2");
        }

        public void run() {
            latch.countDown();
            baseContainerHandle.close();
        }
    }

    public static void main(String[] args) throws StandardException,
            InterruptedException {
        Reporter.reportStart("derby5447", 0, "deadlock");
        if (!OptionHelper.optionParse(args)) {
            return;
        }
        Helpers.startDeadlockMonitor();
        Derby5447 test = new Derby5447();
        test.runThreads();
        Reporter.reportEnd(false);
    }

    private StoredPage storedPage;

    private BaseContainerHandle baseContainerHandle;

    private final CountDownLatch latch;

    public Derby5447() throws StandardException {
        // Prepare mock methods and instances to make sure
        // the program goes along the expected path.
        RawTransaction xact = mock(RawTransaction.class);
        LockingPolicy locking = mock(LockingPolicy.class);
        ContainerKey identity = new ContainerKey(1, 1);
        baseContainerHandle = new BaseContainerHandle(null, xact, identity,
                locking, 1);

        storedPage = new StoredPage();
        storedPage.fillInIdentity(new PageKey(new ContainerKey(1, 2), 2));
        storedPage.setExclusive(baseContainerHandle);

        baseContainerHandle.addObserver(storedPage);
        latch = new CountDownLatch(1);
    }

    public void runThreads() throws InterruptedException {
        Thread1 t1 = new Thread1();
        Thread2 t2 = new Thread2();

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }
}
