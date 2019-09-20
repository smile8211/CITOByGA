package tests;

import java.io.File;

import runspl.RunSPL;


public class MineRUN {
	public static void main(String args[])
	{
		RunSPL.run("tests.MineTEST",
				new String[]{
					//<Application args
					"200",
					//>
					"1", // Test case
					System.getProperty("user.dir")+File.separator+"Mine.m"}, // Feature model file, 
				""); // System.getProperty("user.dir") + File.separator + "v70"); // File listing configurations to run. Must be a subset of feature model solutions. If blank, runs all configurations
	}
}
