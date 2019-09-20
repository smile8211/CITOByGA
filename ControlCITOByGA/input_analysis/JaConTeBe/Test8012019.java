import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import edu.illinois.jacontebe.Helpers;
import edu.illinois.jacontebe.OptionHelper;
import edu.illinois.jacontebe.framework.Reporter;

/**
 * Bug URL:https://bugs.openjdk.java.net/browse/JDK-8012019 
 * This is a deadlock.
 * Reproduce environment: JDK 1.7.0_17, Windows system
 * This bug affects JDK 1.7.0_17 and is fixed since JDK 1.7.0_40.
 * 
 * @collector Ziyi Lin
 *
 */
public class Test8012019 {

    public static void main(String[] args) throws Exception {
        Reporter.reportStart("jdk8012019", 30, "deadlock");
        Reporter.printWarning("1.7.0_17", "1.7.0_40", "Windows");
        if(!OptionHelper.optionParse(args)){
            return;
        }
            
        Helpers.startWaitingMonitor(30);
        File tmpFile = File.createTempFile("deadlock", ".txt");

        try {
            writeSomeText(tmpFile);
            for (int i = 1; i <= 100; i++) {
                Test8012019 test = new Test8012019(tmpFile);
                test.runTest();
                test.close();
            }
        } finally {
            tmpFile.delete();
        }
        Reporter.reportEnd(false);
    }

    private static void writeSomeText(File file) throws IOException {
        FileWriter fw = new FileWriter(file);
        try {
            for (int i = 0; i < 1024; i++) {
                fw.write(" I shall never again run into a deadlock!");
            }
        } finally {
            fw.close();
        }
    }

    private final RandomAccessFile raf;

    private final FileChannel fc;

    public Test8012019(File file) throws FileNotFoundException {
        raf = new RandomAccessFile(file, "r");
        fc = raf.getChannel();
    }

    public void runTest() throws InterruptedException {
        Thread t1 = startTestThread(1);
        Thread t2 = startTestThread(2);

        Thread.sleep(100);

        stopThread(t1);
        stopThread(t2);
    }

    public void close() throws IOException {
        raf.close();
    }

    private void stopThread(Thread t) throws InterruptedException {
        t.interrupt();
        t.join(5000);
    }

    private Thread startTestThread(final int id) {
        Thread thread = new Thread(new Runnable() {

            private final ByteBuffer bb = ByteBuffer.allocate(1024);

            @Override
            public void run() {
                try {
                    long pos = 0;
                    while (true) {
                        bb.clear();
                        fc.read(bb, pos);
                        pos += 1024;
                        if (pos > fc.size()) {
                            pos = 0;
                        }
                    }
                } catch (IOException e) {
                    System.out.println("TestThread-" + id + " : "
                            + e.getClass());
                }
            }
        });
        thread.setName("TestThread-" + id);
        thread.start();
        return thread;
    }
}
