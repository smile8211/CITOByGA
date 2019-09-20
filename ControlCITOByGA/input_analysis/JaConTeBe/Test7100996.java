import edu.illinois.jacontebe.framework.Reporter;

/**
 * Bug URL:https://bugs.openjdk.java.net/browse/JDK-7100996 
 * This is a race
 * Reproduce environment: JDK 1.6.0, JDK 1.7.0_40. 
 * This bug affects since 1.6.0(included) and is fixed in JDK 1.8.0.
 * 
 * Expected exception is:
 * java.lang.IndexOutOfBoundsException: start 0, end 3, s.length() 2
 * 
 * @collector Ziyi Lin
 *
 */
public class Test7100996 {

    private volatile boolean buggy = false;

    public static void main(String[] args) throws Exception {
        Reporter.reportStart("jdk7100996", 0, "race");
        Reporter.printWarning("1.6.0", "1.8.0", null);
        Test7100996 test = new Test7100996();
        test.run();
        Reporter.reportEnd(test.buggy);

    }

    private void run() throws Exception {
        for (int i = 0; i < 100000 && !buggy; i++) {
            final StringBuffer sb = new StringBuffer("abc");

            Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        sb.insert(1, sb);
                    } catch (IndexOutOfBoundsException e) {
                        buggy = true;
                        e.printStackTrace();
                    }
                }
            });
            Thread t2 = new Thread(new Runnable() {
                public void run() {
                    sb.deleteCharAt(0);
                }
            });
            t1.start();
            t2.start();
            try {
                t1.join();
                t2.join();
            } catch (InterruptedException ie) {
            }
        }
    }
}