import edu.illinois.jacontebe.OptionHelper;
import edu.illinois.jacontebe.framework.Reporter;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;

/**
 * Bug URL: http://jira.codehaus.org/browse/GROOVY-5198
 * This is a race.
 * <p>
 * Reproduce environment: groovy 1.7.9, JDK 1.6.0_33.
 * This class starts a groovy shell to run a groovy script test.
 * </p>
 * Options:
 * <ul>
 * <li>--threadnum, -tn thread number , default value is 2000</li>
 * <li>--loops, -l loops number, default value is 100</li>
 * </ul>
 * 
 * @author Ziyi Lin
 *
 */
public class Groovy5198 {

    private static final int DEFAULT_THREAD_NUM = 2000;
    private static final int DEFAULT_LOOP_NUM = 100;

    public static void main(String[] args) throws ResourceException,
            ScriptException, IOException {
        Reporter.reportStart("groovy5198", 0, "race");
        if (!OptionHelper.optionParse(args)) {
            return;
        }
        int threadNum = OptionHelper.getThreadNumValue(DEFAULT_THREAD_NUM);
        int loopNum = OptionHelper.getLoopsValue(DEFAULT_LOOP_NUM);

        Binding binding = new Binding();
        binding.setVariable("threadNum", threadNum);
        binding.setVariable("loopNum", loopNum);

        String scriptText=
        "import edu.illinois.jacontebe.framework.Reporter;"+
        "import java.util.concurrent.atomic.AtomicBoolean;"+
        "enum Foo {"+
            "foo,"+
            "bar,"+
            "baz"+
        "}\n"+
        "AtomicBoolean buggy=new AtomicBoolean(false);"+
        "List<Closure> closures = []\n"+
        "threadNum.times { int index ->"+
            "closures << {"+
                "loopNum.times {"+
                    "String key = \"bar\"\n"+
                    "try{"+
                        "Foo f = key as Foo"+
                    "}"+
                    "catch(MissingMethodException e){"+
                        "if(!buggy.get()){"+
                            "e.printStackTrace();"+
                        "}\n"+
                        "buggy.set(true);"+
                    "}}}}\n"+
        "List<Thread> threads = closures.collect { Thread.start(it) }\n"+
        "threads.each { it.join() }\n"+
        "Reporter.reportEnd(buggy.get());";
        GroovyShell shell=new GroovyShell(binding);
        Script script = shell.parse(scriptText);
        script.run();
    }
}
