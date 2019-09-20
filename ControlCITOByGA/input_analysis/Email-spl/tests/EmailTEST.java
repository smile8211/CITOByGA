package tests;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import runspl.RunSPL;
import EmailSystem.Client;
import classes.gov.nasa.jpf.jvm.Verify;
import defpackage.PL_Interface;
import defpackage.PL_Interface_impl;
import defpackage.Test_Actions;

public class EmailTEST {
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
	
	public static EmailTEST SINGLETON = new EmailTEST();

	public boolean BASE___;
	public boolean KEYS___;
	public boolean ENCRYPT___;
	public boolean AUTORESPONDER___;
	public boolean ADDRESSBOOK___;
	public boolean SIGN___;
	public boolean FORWARD___;
	public boolean VERIFY___;
	public boolean DECRYPT___;

	public static int SCALE; 
	
	/**
	 * Worst case
	 */
	public static void test2()
	{
		testEMAIL(27);
	}


	/**
	 * Average case
	 */
	public static void test1()
	{
		testEMAIL(1);
	}


	/**
	 * Best case
	 */
	public static void test0()
	{
		Client sender = Client.createClient("mysender");
		int recipientsCount = 10*SCALE;
		
		for(int i = 0; i < recipientsCount; i++)
		{
			Client c = Client.createClient("myreceiver"+i);
			c.setPrivateKey(i);
			sender.addKeyringEntry(c, i);
		}
		
		String recipientAddr = "myreceiver"+String.valueOf(recipientsCount-1);		
		Client recipient = Client.getClientByAdress(recipientAddr);
		int pubkey = sender.getKeyringPublicKeyByClient(recipient);
	
		if(pubkey == recipientsCount - 1)
			System.out.println("Encryption successful");
		else
			System.out.println("Encryption failed");
	}


	public static void testEMAIL(int specification)
	{
		PL_Interface interf = new PL_Interface_impl();
		try {
			//int specification = 1;
		//	interf.checkOnlySpecification(1);
				    
			for(int i = 0; i < 10*SCALE; i++)
			{
		        // start program
				interf.start(specification, -1);
				if (interf.isAbortedRun()==true) {
					if (printLog) System.out.println("Aborted");
					Verify.incrementCounter(2);
					System.out.println("ignoredRun#" + Verify.getCounter(2));
					ignoreThisRun = "true";
					return;
				}
				if (printLog)
					System.out.println("Scenario Succeeded");
			}
		} catch (Throwable e) {
			if (printLog) {
				System.out.println("Scenario Failed with error:" + e.getMessage());
				e.printStackTrace();
			}
			error = e.getMessage();
		}
	}


	public static void main(String[] args) {
		RunSPL.loadOneConfiguration(EmailTEST.class);
		int testCase = Integer.parseInt(args[args.length-2]);
		
		Client.clientCounter = 0;
		Test_Actions.executedUnimplementedAction = false;
		
		SCALE = Integer.parseInt(args[0]);
		
		
		try {
			EmailTEST.class.getMethod("test" + testCase, new Class[]{}).invoke(null);
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
	
	public boolean get_BASE___() {
		return BASE___;
	}


	public  boolean get_KEYS___() {
		return KEYS___;
	}
	
	public boolean get_ENCRYPT___() {
		return ENCRYPT___;
	}
	
	public boolean get_AUTORESPONDER___() {
		return AUTORESPONDER___;
	}
	
	public boolean get_ADDRESSBOOK___() {
		return ADDRESSBOOK___;
	}
	
	public boolean get_SIGN___() {
		return SIGN___;
	}
	
	
	public boolean get_FORWARD___() {
		return FORWARD___;
	}
			
	
	public boolean get_VERIFY___() {
		return VERIFY___;
	}
		
	public boolean get_DECRYPT___() {
		return DECRYPT___;
	}
}
