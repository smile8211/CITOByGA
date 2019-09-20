import edu.illinois.jacontebe.framework.Reporter;

/**
 * Bug URL:https://bugs.openjdk.java.net/browse/JDK-4813150. 
 * This is race.
 * Reproduce environment: JDK 1.6.0
 * 
 * @collector Xinxi Chen
 **/

public class Test4813150 {
    private volatile boolean buggy = false;

    public class BreakStringBuffer extends Thread {

        StringBuffer sb = new StringBuffer("abc");

        public void run() {
            while (!buggy) {
                sb.delete(0, 3);
                try {
                    sb.append("abc");
                } catch (StringIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    buggy = true;
                    break;
                }
            }
        }
    }

    public void run() throws Exception {
        BreakStringBuffer bsb = new BreakStringBuffer();
        bsb.start();

        while (!buggy) {
            try {
                StringBuffer s = new StringBuffer();
                s.append(bsb.sb);
            } catch (StringIndexOutOfBoundsException e) {
                e.printStackTrace();
                buggy = true;
                break;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Reporter.reportStart("jdk4813150", 0, "race");
        Test4813150 test = new Test4813150();
        test.run();
        Reporter.reportEnd(test.buggy);
    }
}
