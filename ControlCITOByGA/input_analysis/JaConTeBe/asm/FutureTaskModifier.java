package asm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.illinois.jacontebe.OptionHelper;
import edu.illinois.jacontebe.asm.Constants;
import edu.illinois.jacontebe.asm.ModifyDriver;

/**
 * This is a driver class to instrument codes in byte codes for Test7132378
 * 
 * @author Ziyi Lin
 *
 */
public class FutureTaskModifier {

    public static void main(String[] args) throws IOException {
        if (!OptionHelper.optionParse(args)) {
            System.out
                    .println("Fail to instrument code. Test may not reproduce bug.");
            return;
        }
        long sleepTime = OptionHelper.getSleepTimeValue(10L);
        Map<String, Object> properties = new HashMap<String, Object>();

        String desc = "(Ljava/lang/Object;)V";
        String projectLoc = System.getProperty("user.dir");
        String outputDirectory = projectLoc + "/classes/java/util/concurrent/";
        String outputFile = "FutureTask$Sync.class";
        properties.put(Constants.METHOD_DESC, desc);
        properties.put(Constants.QUALIFIED_CLASS_NAME,
                "java.util.concurrent.FutureTask$Sync");
        properties.put(Constants.METHOD_NAME, "innerSet");
        properties.put(Constants.OUTPUT_DIRECTORY, outputDirectory);
        properties.put(Constants.OUTPUT_FILENAME, outputFile);

        ModifyDriver.modify2File(properties, new FutureTaskSleepFactory(
                sleepTime));
    }
}
