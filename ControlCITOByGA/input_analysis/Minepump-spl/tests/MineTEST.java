package tests;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import runspl.RunSPL;
import classes.gov.nasa.jpf.jvm.Verify;
import defpackage.Actions;
import defpackage.PL_Interface;
import defpackage.PL_Interface_impl;

public class MineTEST {
	public static String scenarioFinished = "false";
	public static String actions = "";
	public static String selectedFeatures = "";
	public static String ignoreThisRun = "false";
	public static String error = null;
	static boolean printLog = false;

	static String SCALE_CONST;
	static String FEATURE_MODEL_PATH;
	static String EXECUTION_MODE;
	static String TEST_CASE;

	public static MineTEST SINGLETON = new MineTEST();
	public static int SCALE;
	
	/**
	 * Worst case
	 */
	public static void test2()
	{
		Actions a = new Actions();
		//PL_Interface_impl.setActionFlags(true, true, true, true);

		for(int i = 0; i < SCALE; i++)
		{
			a.p.startSystem();
			a.startSystem();
			a.waterRise();
			a.methaneChange();
			a.startSystem();
			a.timeShift();
			a.timeShift();
			a.methaneChange();
			a.timeShift();
			a.waterRise();
			a.timeShift();		
			a.stopSystem();
			a.waterRise();
			a.waterRise();
			a.waterRise();
			a.timeShift();
			a.methaneChange();
		}		
	}

	/**
	 * Average case
	 */
	public static void test1()
	{
		Actions a = new Actions();
		PL_Interface_impl.setActionFlags(true, true, true, true);
		testRandom(a);
	}

	/**
	 * Best case
	 */
	public static void test0()
	{
		Actions a = new Actions();
		PL_Interface_impl.setActionFlags(false, false, false, true);
		testRandom(a);
	}

	public static void testRandom(Actions a)
	{
		PL_Interface interf = new PL_Interface_impl();
		try {
			int specification = -1;
			interf.checkOnlySpecification(specification);
			// start program
			interf.start(a, specification, SCALE);
			if (interf.isAbortedRun()==true) {
				if (printLog) System.out.println("Aborted");
				Verify.incrementCounter(2);
				System.out.println("ignoredRun#" + Verify.getCounter(2));
				ignoreThisRun = "true";
				return;
			}
			if (printLog)
				System.out.println("Scenario Succeeded");
		} catch (Throwable e) {
			if (printLog) {
				System.out.println("Scenario Failed with error:" + e.getMessage());
				e.printStackTrace();
			}
			error = e.getMessage();
		}
	}

	public static void main(String[] args) {
		RunSPL.loadOneConfiguration(MineTEST.class);
		int testCase = Integer.parseInt(args[args.length-2]);
		
		SCALE = Integer.parseInt(args[0]);
		
		try {
			PL_Interface_impl.actionHistory = new Vector<String>();
			MineTEST.class.getMethod("test" + testCase, new Class[]{}).invoke(null);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean BASE___;
	public  boolean get_BASE___() {
		return BASE___;
	}

	public boolean STARTCOMMAND___;
	public  boolean get_STARTCOMMAND___() {
		return STARTCOMMAND___;
	}

	public boolean STOPCOMMAND___;
	public  boolean get_STOPCOMMAND___() {
		return STOPCOMMAND___;
	}

	public boolean METHANEALARM___;
	public  boolean get_METHANEALARM___() {
		return METHANEALARM___;
	}


	public boolean METHANEQUERY___;
	public  boolean get_METHANEQUERY___() {
		return METHANEQUERY___;
	}

	public boolean LOWWATERSENSOR___;
	public  boolean get_LOWWATERSENSOR___() {
		return LOWWATERSENSOR___;
	}


	public boolean HIGHWATERSENSOR___;
	public  boolean get_HIGHWATERSENSOR___() {
		return HIGHWATERSENSOR___;
	}
}