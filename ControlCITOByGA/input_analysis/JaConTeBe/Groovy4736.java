import edu.illinois.jacontebe.Helpers;
import edu.illinois.jacontebe.OptionHelper;
import edu.illinois.jacontebe.framework.Reporter;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyResourceLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Bug URL:http://jira.codehaus.org/browse/GROOVY-4736
 * This is a deadlock
 * Reproduce environment: groovy 1.7.9, JDK 1.6.0_33
 * 
 * --threadnum, -tn thread number, default value is 3. 
 * --monitoroff, -mo : Turn deadlock monitor off. 
 * When monitor is turned on, it reports the deadlock
 * message and stops the program.
 * 
 * @author Szymon Kuklewicz
 * @collector Ziyi Lin
 */
public class Groovy4736 {

    private static final int DEFAULT_THREAD_NUM = 3;
    private final File root;
    private final GroovyClassLoader classLoader;
    private final static int LOOPS = 50;

    public Groovy4736() {
        root = new File(System.getProperty("user.dir"));
        classLoader = new GroovyClassLoader();
        classLoader.setResourceLoader(new GroovyResourceLoader() {
            public URL loadGroovySource(String name)
                    throws MalformedURLException {
                File file = getFile(name);
                return file.exists() ? file.toURL() : null;
            }
        });
    }

    private File getFile(String name) {
        if (name.endsWith(".groovy")) {
            name = name.substring(0, name.length() - 7);
        }
        String path = name.replace('.', '/') + ".groovy";
        return new File(root, path);
    }

    private synchronized void writeFile(String name, String body)
            throws IOException {
        File file = getFile(name);
        if (file.exists())
            file.delete();
        file.getParentFile().mkdirs();
        FileOutputStream output = new FileOutputStream(file);
        try {
            OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
            try {
                writer.write(body);
                writer.flush();
            } finally {
                writer.close();
            }
            output.flush();
        } finally {
            output.close();
        }
    }

    private static String getName(int x, int y) {
        return String.valueOf((char) ('A' - 1 + x)) + y;
    }

    private static String getRandomName(int x, int y) {
        assert x != 5 || y != 5;
        while (true) {
            int randomX = x + (int) ((5 - x + 1) * Math.random());
            int randomY = y + (int) ((5 - y + 1) * Math.random());
            if (randomX == x && randomY == y)
                continue;
            return getName(randomX, randomY);
        }
    }

    private void writeFile(int x, int y) throws IOException {
        String name = getName(x, y);
        StringWriter output = new StringWriter(512);
        PrintWriter print = new PrintWriter(output);
        print.println("package test;");
        print.println("class " + name + " {");
        print.println(" private final long createdAt = System.currentTimeMillis();");
        if (x != 5 || y != 5) {
            for (int i = 1; i <= 5; i++) {
                String randomName = getRandomName(x, y);
                print.println(" private final " + randomName + ' '
                        + randomName.toLowerCase() + '_' + i + " = new "
                        + randomName + "();");
            }
        }
        print.println("}");
        writeFile("test." + name, output.toString());
    }

    private void setup() throws IOException {
        for (int x = 1; x <= 5; x++)
            for (int y = 1; y <= 5; y++)
                writeFile(x, y);
    }

    public static void main(String[] args) throws IOException {

        Reporter.reportStart("groovy4736", 10, "deadlock");
        if (!OptionHelper.optionParse(args)) {
            return;
        }
        Helpers.startDeadlockMonitor();
        int threadNumber = OptionHelper.getThreadNumValue(DEFAULT_THREAD_NUM);

        final Groovy4736 test = new Groovy4736();
        test.setup();
        for (int i = 1; i <= threadNumber; i++) {
            new Thread("Saving and clearing cache " + i) {
                @Override
                public void run() {
                    int i = 0;
                    while (i < LOOPS * 2) {
                        i++;
                        // saving file
                        try {
                            test.writeFile(1 + (int) (5 * Math.random()),
                                    1 + (int) (5 * Math.random()));
                        } catch (IOException e) {
                            e.printStackTrace();
                            continue;
                        }
                        // clearing cache
                        test.classLoader.clearCache();

                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        }
        for (int i = 1; i <= threadNumber; i++) {
            new Thread("Compiling and instantiation " + i) {
                @Override
                public void run() {
                    int i = 0;
                    while (i < LOOPS) {
                        i++;
                        // compiling
                        Class groovyClass;
                        try {
                            groovyClass = test.classLoader.loadClass("test."
                                    + test.getName(
                                            1 + (int) (5 * Math.random()),
                                            1 + (int) (5 * Math.random())));
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                            continue;
                        }
                        // instantiation
                        try {
                            groovyClass.getConstructor().newInstance();
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                        // sleeping
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        }
    }

}