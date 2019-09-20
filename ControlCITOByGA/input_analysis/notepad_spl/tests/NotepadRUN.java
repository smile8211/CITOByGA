package tests;

import java.io.File;

import runspl.RunSPL;

public class NotepadRUN {
	public static void main(String args[])
	{
		RunSPL.run("tests.NotepadTEST",
				new String[]{
					"1", // Test case
					System.getProperty("user.dir")+File.separator+"notepad.m"}, // Feature model file, 
				""); // System.getProperty("user.dir") + File.separator + "v70"); // File listing configurations to run. Must be a subset of feature model solutions. If blank, runs all configurations
	}
}