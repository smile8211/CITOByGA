package tests;

import java.io.File;

import runspl.RunSPL;


public class EmailRUN {
	public static void main(String args[])
	{
		RunSPL.run("tests.EmailTEST",
				new String[]{
					//<Application args
					"2",
					//>
					"1", // Test case
					System.getProperty("user.dir")+File.separator+"Email.m"}, // Feature model file, 
				""); // System.getProperty("user.dir") + File.separator + "v70"); // File listing configurations to run. Must be a subset of feature model solutions. If blank, runs all configurations
	}
}
