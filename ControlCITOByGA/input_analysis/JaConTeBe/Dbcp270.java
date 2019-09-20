import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.PoolableConnection;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import edu.illinois.jacontebe.Helpers;
import edu.illinois.jacontebe.OptionHelper;
import edu.illinois.jacontebe.framework.Reporter;

/**
 * <p>
 * Bug URL: https://issues.apache.org/jira/browse/DBCP-270 <br>
 * Implemented Version: dbcp 1.2, JDK 1.6.0_33
 * </p>
 * <p>
 * This is a deadlock. <br>
 * Refer to 270.html in description directory for more information.
 * </p>
 * 
 * @author Ziyi Lin
 */
public class Dbcp270 {

    private GenericObjectPool pool;
    private PoolableConnection poolableConnection;

    private class Thread1 extends Thread {

        public Thread1() {
            super("Thread1");
        }

        public void run() {
            try {
                poolableConnection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class Thread2 extends Thread {

        public Thread2() {
            super("Thread2");
        }

        public void run() {
            try {
                pool.evict();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Dbcp270() throws Exception {
        // Prepare mock methods and instances to make sure
        // the program goes along the expected path leading to deadlock.
        Connection conn = mock(Connection.class);
        when(conn.isClosed()).thenReturn(false);
        when(conn.isReadOnly()).thenReturn(false);
        when(conn.getAutoCommit()).thenReturn(false);

        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        when(connectionFactory.createConnection()).thenReturn(conn);
        KeyedObjectPoolFactory stmtPoolFactory = mock(KeyedObjectPoolFactory.class);
        KeyedObjectPool kp = mock(KeyedObjectPool.class);
        when(stmtPoolFactory.createPool()).thenReturn(kp);
        pool = new GenericObjectPool();
        pool.setTestWhileIdle(true);
        PoolableConnectionFactory factory = new PoolableConnectionFactory(
                connectionFactory, pool, stmtPoolFactory, "", false, false);

        pool.addObject();
        poolableConnection = (PoolableConnection) pool.borrowObject();
        pool.returnObject(poolableConnection);
        factory.activateObject(poolableConnection);
    }

    public void run() throws Exception {
        new Thread1().start();
        new Thread2().start();
    }

    public static void main(String[] args) throws Exception {
        Reporter.reportStart("dbcp270", 0, "deadlock");
        if (!OptionHelper.optionParse(args)) {
            return;
        }
        Helpers.startDeadlockMonitor();
        Dbcp270 test = new Dbcp270();
        test.run();
    }

}