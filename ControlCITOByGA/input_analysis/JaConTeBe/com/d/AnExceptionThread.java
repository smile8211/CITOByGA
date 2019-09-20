package com.d;

import org.apache.log4j.Logger;

import com.b.AnException;

/**
 * 
 * @author Marcelo S. Miashiro (marc_sm2003@yahoo.com.br)
 */
public class AnExceptionThread extends Thread {
    private static final Logger LOGGER = Logger
            .getLogger(AnExceptionThread.class);

    public AnExceptionThread(String threadName) {
        super(threadName);
    }

    @Override
    public void run() {
        LOGGER.info("Just logging INFO in AnExceptionThread", new AnException(
                "test exception", new AnException("cause exception")));
    }
}