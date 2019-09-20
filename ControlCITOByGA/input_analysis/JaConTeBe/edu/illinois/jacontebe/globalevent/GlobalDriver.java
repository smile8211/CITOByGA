package edu.illinois.jacontebe.globalevent;

import java.util.Hashtable;
import java.util.Map;

/**
 * This class enforces a multi-threaded program to execute with a global
 * sequence.
 * 
 * @author Ziyi Lin
 * 
 */
public class GlobalDriver {

    private static final GlobalDriver INSTANCE = new GlobalDriver();
    private volatile Map<Integer, Object> sequenceObjects;
    private static final int TIMEOUT = 60000;

    public static GlobalDriver getInstance() {
        return INSTANCE;
    }

    private GlobalDriver() {
        sequenceObjects = new Hashtable<Integer, Object>();

    }

    // Do nothing, this is a symbol of the end of the sequence.
    public void endOfSequence() {
        // System.out.println("End of Sequence");
    }

    public void endStep(int i) {
        int j = 0;
        while (sequenceObjects.get(i) == null && j < 3000) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            j++;
        }
        if (sequenceObjects.get(i) == null) {
            return;
        }
        synchronized (sequenceObjects.get(i)) {
            // System.out.println("Notify others that step "+ i
            // +" is finished.(end of "+i+")");
            sequenceObjects.get(i).notify();
        }
    }

    public void startStep(int i) {
        if (i > 0) {
            Object ob = new Object();
            synchronized (ob) {
                sequenceObjects.put(i - 1, ob);
                try {
                    sequenceObjects.get(i - 1).wait(TIMEOUT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else if (i == 0) {
            // System.out.println("Start sequence.");
        }
    }
}
