import java.util.Hashtable;

import edu.illinois.jacontebe.Helpers;
import edu.illinois.jacontebe.OptionHelper;
import edu.illinois.jacontebe.framework.Reporter;

/**
 * Bug URL:https://bugs.openjdk.java.net/browse/JDK-6582568
 * This is a deadlock. 
 * Reproduce environment: JDK 1.6.0, JDK 1.6.0_33, JDK 1.7.0
 * 
 * * Options: 
 * --monitoroff, -mo : Turn deadlock monitor off. When monitor is
 * turned on, it reports the deadlock message and stop the program.
 * 
 * @collector Ziyi Lin
 * 
 */
public class Test6582568 {
    public static void main(String[] args) {
        Reporter.reportStart("jdk6582568", 0, "deadlock");
        if (!OptionHelper.optionParse(args)) {
            return;
        }
        Helpers.startDeadlockMonitor();
        runTest();
        Reporter.reportEnd(false);
    }

    private static void runTest() {
        final Hashtable<String, Object> p1 = new Hashtable<String, Object>();
        p1.put("1", new Object());
        p1.put("2", new Object());
        p1.put("3", new Object());

        final Hashtable<String, Object> p2 = new Hashtable<String, Object>();
        p2.put("1", new Object());
        p2.put("2", new Object());
        p2.put("3", new Object());

        Thread t1 = new Thread() {
            @Override
            public void run() {
                while (true) {
                    p1.equals(p2);
                }
            }
        };
        t1.start();

        Thread t2 = new Thread() {
            @Override
            public void run() {
                while (true) {
                    p2.equals(p1);
                }
            }
        };
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {

        }
    }
}