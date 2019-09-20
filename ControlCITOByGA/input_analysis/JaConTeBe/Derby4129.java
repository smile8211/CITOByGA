import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

import edu.illinois.jacontebe.Helpers;
import edu.illinois.jacontebe.OptionHelper;
import edu.illinois.jacontebe.framework.Reporter;

/**
 * Bug URL:https://issues.apache.org/jira/browse/DERBY-4129
 * This is a deadlock
 * Reproduce environment: 10.5.1.1, JDK 1.6.0_33
 * 
 * Options: --monitoroff, -mo : Turn deadlock monitor off. When monitor is
 * turned on, it reports the deadlock message and stop the program.
 * 
 * @collector Ziyi Lin
 * 
 */

public class Derby4129 {
    private static final String URL = "jdbc:derby:DB";

    public static void main(String[] args) throws Exception {
        Reporter.reportStart("derby4129", 30, "deadlock");
        if (!OptionHelper.optionParse(args)) {
            return;
        }
        Helpers.startDeadlockMonitor();

        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        final Connection c = DriverManager.getConnection(URL + ";create=true");
        Statement s = c.createStatement();
        try {
            s.execute("drop table t");
        } catch (SQLException sqle) {
            // ignore, table doesn't exist
        }
        s.execute("create table t (b blob)");
        s.close();

        PreparedStatement ps = c.prepareStatement("insert into t values ?");
        ps.setBytes(1, new byte[50000]);
        ps.execute();
        ps.close();

        final AtomicInteger opCounter = new AtomicInteger();
        final AtomicInteger errCounter = new AtomicInteger();

        Runnable r = new Runnable() {
            public void run() {
                try {
                    PreparedStatement ps = c
                            .prepareStatement("select b from t");
                    while (true) {
                        ResultSet rs = ps.executeQuery();
                        rs.next();
                        try {
                            rs.getBytes(1);
                        } catch (SQLException sqle) {
                            // Expect errors here now and then since the
                            // other thread may commit and close the container
                            errCounter.incrementAndGet();
                        }
                        rs.close();
                        opCounter.incrementAndGet();
                    }
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
            }
        };

        new Thread(r).start();
        new Thread(r).start();

        while (true) {
            Thread.sleep(5000);
            int ops = opCounter.getAndSet(0);
            int errors = errCounter.getAndSet(0);
            System.out.println("Operations last 5 seconds: " + ops);
            if (errors > 0) {
                System.out.println("  Errors: " + errors);
            }
        }
    }
}
