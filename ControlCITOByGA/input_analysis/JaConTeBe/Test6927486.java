import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;

import edu.illinois.jacontebe.Helpers;
import edu.illinois.jacontebe.OptionHelper;
import edu.illinois.jacontebe.framework.Reporter;

/**
 * Bug URL:https://bugs.openjdk.java.net/browse/JDK-6927486 
 * This is a deadlock.
 * Reproduce environment: JDK 1.6.0, fixed in JDK 1.7.0
 *
 * Options:
 * --monitoroff, -mo : Turn deadlock monitor off. When
 *            monitor is turned on, it reports the deadlock message and stop the
 *            program.
 *            
 * @collector Ziyi Lin
 * 
 */
public class Test6927486 {
    public static void main(String args[]) {
        Reporter.reportStart("jdk6927486", 0, "deadlock");
        Reporter.printWarning("1.6.0", "1.7.0", null);
        if (!OptionHelper.optionParse(args)) {
            return;
        }
        Helpers.startDeadlockMonitor();
        MyObject mo1 = new MyObject();
        MyObject mo2 = new MyObject();
        Hashtable ht1 = new Hashtable();
        Hashtable ht2 = new Hashtable();
        mo1.ht = ht2;
        mo1.sleepTime = 100;
        mo2.ht = ht1;
        mo2.sleepTime = 100;
        ht1.put("key", mo1);
        ht2.put("key", mo2);
        MyThread t1 = new MyThread(ht1, "file1");
        MyThread t2 = new MyThread(ht2, "file2");
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {

        }
        Reporter.reportEnd(false);
    }
}

class MyObject implements Serializable {
    public transient Hashtable ht;
    public transient long sleepTime;

    private void writeObject(java.io.ObjectOutputStream s) throws IOException {
        try {
            Thread.sleep(sleepTime);
        } catch (Exception e) {
        }
        s.writeObject(ht);
    }

    private void readObject(java.io.ObjectInputStream s) throws IOException,
            ClassNotFoundException {
        ht = (Hashtable) s.readObject();
    }
}

class MyThread extends Thread {
    Hashtable ht;
    String file;

    public MyThread(Hashtable ht, String file) {
        this.ht = ht;
        this.file = file;
    }

    public void run() {
        try {
            ObjectOutput oo = new ObjectOutputStream(new FileOutputStream(file));
            oo.writeObject(ht);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}