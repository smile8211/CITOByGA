import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import sun.reflect.annotation.AnnotationType;
import edu.illinois.jacontebe.Helpers;
import edu.illinois.jacontebe.OptionHelper;
import edu.illinois.jacontebe.framework.Reporter;

/**
 * Bug URL:https://bugs.openjdk.java.net/browse/JDK-6588239
 * 
 * This is a deadlock bug.
 * Reproduce environment: JDK 1.6.0, JDK 1.7.0
 * 
 * Options: --monitoroff, -mo : Turn deadlock monitor off. When
 *            monitor is turned on, it reports the deadlock message and stop the
 *            program.
 *  @collector Ziyi Lin
 */

public class Test6588239 {
    public static void runTest(String s) {
        Thread1 t1 = new Thread1(s);
        Thread2 t2 = new Thread2(s);

        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {

        }
    }

    public static void main(String[] args) {
        Reporter.reportStart("jdk6588239", 0, "deadlock");
        if (!OptionHelper.optionParse(args)) {
            return;
        }
        Helpers.startDeadlockMonitor();
        runTest("AnnoTest");
        Reporter.reportEnd(false);
    }

    static class Thread1 extends Thread {
        String className;

        public Thread1(String name) {
            className = name;
        }

        public void run() {
            try {
                Annotation[] annArray = Class.forName(className)
                        .getAnnotations();
                for (Annotation a : annArray)
                    System.out.println(a);
            } catch (Exception e) {
                System.out.printf("Thread1 failes: %s %n", e.getCause());
            }
        }
    }

    static class Thread2 extends Thread {
        String className;

        public Thread2(String name) {
            className = name;
        }

        public void run() {
            try {
                AnnotationType test = AnnotationType
                        .getInstance((Class<? extends Annotation>) Class
                                .forName(className));
                System.out.println(test);
            } catch (Exception e) {
                System.out.printf("Thread2 failes: %s %n", e.getCause());
            }
        }
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface AnnoTest {
}