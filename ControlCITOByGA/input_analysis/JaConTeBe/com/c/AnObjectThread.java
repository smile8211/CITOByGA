package com.c;

import org.apache.log4j.Logger;

import com.a.AnObject;

/**
 * 
 * @author Marcelo S. Miashiro (marc_sm2003@yahoo.com.br)
 */
public class AnObjectThread extends Thread {
    private static final Logger LOGGER = Logger.getLogger(AnObjectThread.class);

    public AnObjectThread(String threadName) {
        super(threadName);
    }

    @Override
    public void run() {
        AnObject anObject = new AnObject("Object created in AnObjectThread");
        LOGGER.info(anObject);
    }
}