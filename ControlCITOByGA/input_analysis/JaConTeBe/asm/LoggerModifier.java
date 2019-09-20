package asm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.illinois.jacontebe.OptionHelper;
import edu.illinois.jacontebe.asm.Constants;
import edu.illinois.jacontebe.asm.ModifyDriver;

/**
 * This is a driver class to instrument codes in byte codes for Test4779253.
 * void log(LogRecord record) method in java.util.logging.Logger class is
 * instrumented with a sleep to extend the race buggy window.
 * 
 * 
 * @author Ziyi Lin
 * 
 */
public class LoggerModifier {

    public static void main(String[] args) throws IOException {
        if (!OptionHelper.optionParse(args)) {
            System.out
                    .println("Fail to instrument code. Test may not reproduce bug.");
            return;
        }
        long sleepTime = OptionHelper.getSleepTimeValue(1000L);
        Map<String, Object> properties = new HashMap<String, Object>();

        String desc = "(Ljava/util/logging/LogRecord;)V";
        String projectLoc = System.getProperty("user.dir");
        String outputDirectory = projectLoc + "/classes/java/util/logging/";
        properties.put(Constants.METHOD_DESC, desc);
        properties.put(Constants.QUALIFIED_CLASS_NAME,
                "java.util.logging.Logger");
        properties.put(Constants.METHOD_NAME, "log");
        properties.put(Constants.OUTPUT_DIRECTORY, outputDirectory);
        properties.put(Constants.OUTPUT_FILENAME, "Logger.class");

        ModifyDriver.modify2File(properties, new SleepMVFactory(sleepTime));
    }
}
