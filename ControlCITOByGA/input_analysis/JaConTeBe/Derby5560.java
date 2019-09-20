import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

import org.apache.derby.client.ClientXAConnection40;
import org.apache.derby.client.ConnectionSetter;
import org.apache.derby.client.am.Agent;
import org.apache.derby.client.am.Connection;
import org.apache.derby.client.am.LogicalConnection;
import org.apache.derby.jdbc.ClientXADataSource;

import edu.illinois.jacontebe.Helpers;
import edu.illinois.jacontebe.OptionHelper;
import edu.illinois.jacontebe.framework.Reporter;

/**
 * Bug URL: https://issues.apache.org/jira/browse/DERBY-5560
 * This is a deadlock bug.
 * Reproduce environment: Derby 10.5.1.1, JDK 1.6.0_33, JDK 1.7.0_25
 * 
 * Options: --monitoroff, -mo : Turn deadlock monitor off. When monitor is
 * turned on, it reports the deadlock message and stop the program.
 * 
 * @author Ziyi Lin
 * 
 */

public class Derby5560 {

    private class Thread1 extends Thread {

        public Thread1() {
            setName("Thread-1");
        }

        public void run() {
            try {
                latch.await();
                Thread.sleep(10);
                logicalConnection.close();
            } catch (SQLException e) {

                e.printStackTrace();
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }
    }

    private class Thread2 extends Thread {

        public Thread2() {
            setName("Thread-2");
        }

        public void run() {
            try {
                // Make sure this thread executes first. Because another thread
                // is
                // very fast, if it executes first, it finishes even before
                // deadlock
                // could happen.
                latch.countDown();
                clientPooledConnection.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Reporter.reportStart("derby5560", 0, "deadlock");
        if (!OptionHelper.optionParse(args)) {
            return;
        }
        Helpers.startDeadlockMonitor();

        Derby5560 test = new Derby5560();
        test.startThreads();
    }

    private final CountDownLatch latch;

    private ClientXAConnection40 clientPooledConnection;
    private LogicalConnection logicalConnection;

    private static final String SERVER = "server";

    public Derby5560() throws Exception {
        latch = new CountDownLatch(1);

        // Prepare mock methods and instances to make sure
        // the program goes along the expected path.
        Connection physicalConnection = mock(Connection.class);
        org.apache.derby.client.am.Configuration.traceSuspended__ = true;

        Agent agent = mock(Agent.class);
        physicalConnection.agent_ = agent;

        when(physicalConnection.isClosed()).thenReturn(false);

        ClientXADataSource ds = mock(ClientXADataSource.class);
        when(ds.maxStatementsToPool()).thenReturn(0);
        when(ds.getServerName()).thenReturn(SERVER);

        clientPooledConnection = mock(ClientXAConnection40.class);
        doCallRealMethod().when(clientPooledConnection).recycleConnection();
        doCallRealMethod().when(clientPooledConnection).close();
        logicalConnection = new LogicalConnection(physicalConnection,
                clientPooledConnection);

        ConnectionSetter.setLogicalConnection(clientPooledConnection,
                logicalConnection);
    }

    public void startThreads() {
        new Thread1().start();
        new Thread2().start();
    }

}
