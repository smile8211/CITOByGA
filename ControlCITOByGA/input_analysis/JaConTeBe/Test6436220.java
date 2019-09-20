import java.io.IOException;
import java.nio.channels.Pipe;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.illinois.jacontebe.framework.Reporter;

/**
 * Bug URL: https://bugs.openjdk.java.net/browse/JDK-6436220
 * This is a race.
 * Reproduce environment: JDK 1.6.0, JDK 1.6.0_33
 * This bug affects from JDK 1.6.0 and has been fixed since JDK 1.7.0.
 * 
 * @author Ziyi Lin
 * 
 */
public class Test6436220 {
    private SelectionKey key;
    private volatile AtomicBoolean buggy;

    public Test6436220() throws IOException {
        // initiate SelectionKey
        Selector selector = Selector.open();
        Pipe pipe = Pipe.open();
        SelectableChannel channel = pipe.sink().configureBlocking(false);
        key = channel.register(selector, 0);
        buggy = new AtomicBoolean();
    }

    public void run() {
        int threadNum = 20;
        int loops = 100;
        TestThread[] ts = new TestThread[threadNum];
        for (int j = 0; j < loops; j++) {
            for (int i = 0; i < threadNum; i++) {
                String s = Integer.toString(i);
                ts[i] = new TestThread(s);
                ts[i].start();
            }
            for (Thread t : ts) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (buggy.get()) {
                break;
            }
        }
    }

    private class TestThread extends Thread {

        String attachment;

        public TestThread(String att) {
            this.attachment = att;
        }

        public void run() {
            key.attach(this.attachment);
            Object ret = key.attachment();
            // ret should be the same as attachment.
            if (!ret.equals(attachment)) {
                buggy.set(true);
                System.out.println("Error happens:Expected value is "
                        + attachment + " but actual is " + ret);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Reporter.reportStart("jdk643620", 0, "race");
        Reporter.printWarning("1.6.0", "1.7.0", null);
        Test6436220 test = new Test6436220();
        test.run();
        Reporter.reportEnd(test.buggy.get());
    }
}
