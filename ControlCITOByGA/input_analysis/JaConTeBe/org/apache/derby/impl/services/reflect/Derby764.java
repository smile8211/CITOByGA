package org.apache.derby.impl.services.reflect;

import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.apache.derby.iapi.error.StandardException;
import org.apache.derby.iapi.services.locks.Lockable;
import org.apache.derby.iapi.services.locks.ShExQual;
import org.apache.derby.iapi.services.monitor.Monitor;
import org.apache.derby.iapi.util.IdUtil;
import org.apache.derby.impl.services.locks.LockOperator;
import org.apache.derby.impl.services.locks.SinglePool;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import edu.illinois.jacontebe.framework.Reporter;
import edu.illinois.jacontebe.monitors.DeadlockMonitor;

/**
 * Bug URL: https://issues.apache.org/jira/browse/DERBY-764 
 * This is a deadlock bug.
 * Reproduce environment: junit 4, derby 10.5.1.1, JDK 1.6.0_33
 * 
 * @author Ziyi Lin
 * 
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Monitor.class, IdUtil.class })
public class Derby764 {

    private class Thread1 extends Thread {
        public void run() {
            try {
                updateLoader.modifyJar(false);
            } catch (StandardException e) {
                e.printStackTrace();
            }
        }
    }

    private class Thread2 extends Thread {
        public void run() {

            try {
                operator.unlock();
            } catch (StandardException e) {
                e.printStackTrace();
            }
        }
    }

    private UpdateLoader updateLoader;
    private SinglePool factory;
    private LockOperator operator;

    @Test
    public void run() throws StandardException, InterruptedException {
        Reporter.reportStart("derby764", 0, "deadlock");
        DeadlockMonitor monitor = new DeadlockMonitor();
        monitor.start();

        Thread th1 = new Thread1();
        Thread th2 = new Thread2();

        th1.start();
        th2.start();
        th1.join();
        th2.join();
        // If test comes to this line, it means no deadlock happens.So we need
        // to report the failure of bug reproduction.
        Reporter.reportEnd(false);
    }

    @Before
    public void setUp() throws StandardException {
        factory = new SinglePool();

        // Prepare mock methods and instances to make sure
        // the program goes along the expected path.
        mockStatic(Monitor.class);
        Mockito.when(Monitor.getServiceModule(Mockito.any(ReflectClassesJava2.class),Mockito.anyString()))
                .thenReturn(factory);
        String classpath = "org/class";

        mockStatic(IdUtil.class);
        Mockito.when(IdUtil.parseDbClassPath(classpath)).thenReturn(
                new String[1][1]);
        DatabaseClasses parent = new ReflectClassesJava2();
        updateLoader = new UpdateLoader(classpath, parent, false, false);
        Object qualifier = ShExQual.EX;
        Lockable classloaderLock = new ClassLoaderLock(updateLoader);
        operator = new LockOperator(factory, classloaderLock, qualifier);
        operator.lock();
    }
}
