package com.a;

import org.apache.log4j.Logger;

/**
 * 
 * @author Marcelo S. Miashiro (marc_sm2003@yahoo.com.br)
 */
public class AnObject {
    private static final Logger LOGGER = Logger.getLogger(AnObject.class);
    private final String name;

    public AnObject(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        LOGGER.info("Logging DEBUG in AnObject [" + name + "]");
        return name;
    }
}