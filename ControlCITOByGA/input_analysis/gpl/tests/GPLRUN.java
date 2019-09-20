package tests;

import java.io.File;

import runspl.RunSPL;

public class GPLRUN {
	public static void main(String args[])
	{
		RunSPL.run("tests.GPLTEST",
				new String[]{
					//<Application args
					System.getProperty("user.dir")+File.separator+"gpl-small-graph.txt",
					"v0",
					//>
					"1", // Test case
					System.getProperty("user.dir")+File.separator+"Main.m"}, // Feature model file, 
				""); // System.getProperty("user.dir") + File.separator + "v70"); // File listing configurations to run. Must be a subset of feature model solutions. If blank, runs all configurations
	}
}
