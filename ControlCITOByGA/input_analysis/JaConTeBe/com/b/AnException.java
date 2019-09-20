package com.b;

import org.apache.log4j.Logger;

/**
 * 
 * @author Marcelo S. Miashiro (marc_sm2003@yahoo.com.br)
 */
public class AnException extends RuntimeException {
    private static final Logger LOGGER = Logger.getLogger(AnException.class);

    public AnException() {
        super();
    }

    public AnException(String msg) {
        super(msg);
    }

    public AnException(Throwable t) {
        super(t);
    }

    public AnException(String msg, Throwable t) {
        super(msg, t);
    }

    @Override
    public String getMessage() {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        LOGGER.error("Logging ERROR in AnException");
        return super.getMessage();
    }
}