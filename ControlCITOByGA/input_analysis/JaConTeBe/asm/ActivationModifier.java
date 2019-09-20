package asm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.jacontebe.OptionHelper;
import edu.illinois.jacontebe.asm.Constants;
import edu.illinois.jacontebe.asm.MethodInfor;
import edu.illinois.jacontebe.asm.ModifyDriver;

/**
 * This is a driver class to instrument codes in byte codes for Test8023541.
 * constructor method of sun.rmi.server.Activation$SystemRegistryImpl class is
 * instrumented with a sleep to extend the race buggy window.
 * 
 * @author Ziyi Lin
 * 
 */
public class ActivationModifier {

    public static void main(String[] args) throws IOException {
        if (!OptionHelper.optionParse(args)) {
            System.out
                    .println("Fail to instrument code. Test may not reproduce bug.");
            return;
        }
        String projectLoc = System.getProperty("user.dir");
        modifyActivationClass(projectLoc);
        // modifyNamingClass(projectLoc);
    }

    private static void modifyNamingClass(String projectLoc) throws IOException {

        List<MethodInfor> mis = new ArrayList<MethodInfor>();

        String outputDirectory = projectLoc + "/classes/java/rmi/";
        String outputFile = "Naming.class";

        Map<String, Object> properties = new HashMap<String, Object>();
        // first method to transform
        String desc = "(Ljava/lang/String;)Ljava/rmi/Remote;";
        mis.add(new MethodInfor("lookup", desc));

        properties.put(Constants.QUALIFIED_CLASS_NAME, "java.rmi.Naming");
        properties.put(Constants.METHOD_INFOR_LIST, mis);
        properties.put(Constants.OUTPUT_DIRECTORY, outputDirectory);
        properties.put(Constants.OUTPUT_FILENAME, outputFile);

        ModifyDriver.modify2File(properties, new NamingMVFactory());
    }

    private static void modifyActivationClass(String projectLoc)
            throws IOException {
        List<MethodInfor> mis = new ArrayList<MethodInfor>();

        String outputDirectory = projectLoc + "/classes/sun/rmi/server/";
        String outputFile = "Activation$SystemRegistryImpl.class";

        Map<String, Object> properties = new HashMap<String, Object>();
        // first method to transform
        String desc = "(ILjava/rmi/server/RMIClientSocketFactory;Ljava/rmi/server/RMIServerSocketFactory;Ljava/rmi/activation/ActivationSystem;)V";
        mis.add(new MethodInfor("<init>", desc));
        // second method to transform
        mis.add(new MethodInfor("lookup",
                "(Ljava/lang/String;)Ljava/rmi/Remote;"));

        properties.put(Constants.QUALIFIED_CLASS_NAME,
                "sun.rmi.server.Activation$SystemRegistryImpl");
        properties.put(Constants.METHOD_INFOR_LIST, mis);
        properties.put(Constants.OUTPUT_DIRECTORY, outputDirectory);
        properties.put(Constants.OUTPUT_FILENAME, outputFile);

        ModifyDriver.modify2File(properties, new ActivationMVFactory());
    }

}
