package com.main;

import org.apache.log4j.Logger;

/**
 * 
 * @author Marcelo S. Miashiro (marc_sm2003@yahoo.com.br)
 */
public class RootLoggerThread extends Thread {
    private static final Logger LOGGER = Logger
            .getLogger(RootLoggerThread.class);

    public RootLoggerThread(String threadName) {
        super(threadName);
    }

    @Override
    public void run() {
        LOGGER.info("Just logging INFO in RootLoggerThread");
    }
}