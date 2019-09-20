package com.main;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;

import com.c.AnObjectThread;
import com.d.AnExceptionThread;

import edu.illinois.jacontebe.Helpers;
import edu.illinois.jacontebe.OptionHelper;
import edu.illinois.jacontebe.framework.Reporter;

/**
 * Bug URL:https://issues.apache.org/bugzilla/show_bug.cgi?id=41214
 * This is a deadlock.
 * Reproduce environment: log4j 1.2.13, JDK 1.6.0_33 
 *
 * Options: 
 * --monitoroff, -mo : Turn deadlock monitor off. When monitor is
 * turned on, it reports the deadlock message and stop the program.
 * 
 * @author Marcelo S. Miashiro (marc_sm2003@yahoo.com.br)
 * @collector Ziyi Lin
 */
public class Test41214 {

    public static void main(String[] args) {
        Reporter.reportStart("log4j41214", 20, "deadlock");
        if (!OptionHelper.optionParse(args)) {
            return;
        }
        Helpers.startDeadlockMonitor();
        configLog();
        RootLoggerThread rootLoggerThread = new RootLoggerThread(
                "RootLoggerThread");
        AnObjectThread anObjectThread = new AnObjectThread("AnObjectThread");
        AnExceptionThread anExceptionThread = new AnExceptionThread(
                "AnExceptionThread");
        anExceptionThread.start();
        anObjectThread.start();
        try {
            // To reproduce the bug, com.a.AnObject.toString() and
            // com.b.AnException.getMessage()
            // methods must be called before rootLogger
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        rootLoggerThread.start();
        try {
            rootLoggerThread.join();
        } catch (InterruptedException e) {

        }
        Reporter.reportEnd(false);
    }

    private static void configLog() {
        ConsoleAppender console = new ConsoleAppender(); // create appender
        // configure the appender
        String PATTERN = "[%d{dd-MM-yyyy HH:mm:ss,SSS}][%-5p][%c] %M : %m%n";
        console.setLayout(new PatternLayout(PATTERN));
        console.setThreshold(Level.INFO);
        console.activateOptions();

        LogManager.getRootLogger().addAppender(console);
        LogManager.getLogger("com.c").addAppender(console);
        LogManager.getLogger("com.d").addAppender(console);
    }
}