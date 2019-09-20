import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.dbcp.KeyGenerator;
import org.apache.commons.dbcp.PoolingConnection;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;

import edu.illinois.jacontebe.Helpers;
import edu.illinois.jacontebe.OptionHelper;
import edu.illinois.jacontebe.framework.Reporter;

/**
 * <p>
 * Bug URL: https://issues.apache.org/jira/browse/DBCP-65 <br>
 * Implemented Version: dbcp 1.2, JDK 1.6.0_33
 * </p>
 * <p>
 * This is a deadlock. <br>
 * Refer to 65.html in description directory for more information.
 * </p>
 * 
 * @author Ziyi Lin
 * 
 */
public class Dbcp65 {

    private PoolingConnection poolingConnection;
    private GenericKeyedObjectPool genericObjectPool;

    public Dbcp65() throws Exception {
        // Prepare mock methods and instances to make sure
        // the program goes along the expected path leading to deadlock.
        Connection c = mock(Connection.class);
        PreparedStatement prepareStatement = mock(PreparedStatement.class);
        when(c.prepareStatement(null)).thenReturn(prepareStatement);

        genericObjectPool = new GenericKeyedObjectPool();
        poolingConnection = new PoolingConnection(c, genericObjectPool);
        genericObjectPool.setFactory(poolingConnection);
        genericObjectPool.setTestWhileIdle(true);

        genericObjectPool.addObject(KeyGenerator.generateKey());
    }

    public void run() throws Exception {
        new Thread1().start();
        new Thread2().start();
    }

    private class Thread1 extends Thread {
        public void run() {
            try {
                genericObjectPool.evict();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class Thread2 extends Thread {
        public void run() {
            try {
                poolingConnection.prepareStatement("sql");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Reporter.reportStart("dbcp65", 0, "deadlock");
        if (!OptionHelper.optionParse(args)) {
            return;
        }
        Helpers.startDeadlockMonitor();
        Dbcp65 test = new Dbcp65();
        test.run();
    }

}