package tests;
import runspl.RunSPL;
import smashed.Main;
public class GPLTEST {
	public static GPLTEST SINGLETON = new GPLTEST();
	public  boolean BASE___;
	public  boolean DIRECTED___;
	public  boolean UNDIRECTED___;
	public  boolean WEIGHTED___;
	public  boolean SEARCH___;
	public  boolean BFS___;
	public  boolean DFS___;
	public  boolean NUMBER___;
	public  boolean CONNECTED___;
	public  boolean STRONGLYCONNECTED___;
//	public  boolean TRANSPOSE___;
	public  boolean CYCLE___;
	public  boolean MSTPRIM___;
	public  boolean MSTKRUSKAL___;
	public  boolean SHORTEST___;

	public boolean get_BASE___() {
		return BASE___;
	}

	public boolean get_DIRECTED___() {
		return DIRECTED___;
	}

	public boolean get_UNDIRECTED___() {
		return UNDIRECTED___;
	}

	public boolean get_WEIGHTED___() {
		return WEIGHTED___;
	}

	public boolean get_SEARCH___() {
		return SEARCH___;
	}

	public boolean get_BFS___() {
		return BFS___;
	}

	public boolean get_DFS___() {
		return DFS___;
	}

	public boolean get_NUMBER___() {
		return NUMBER___;
	}

	public boolean get_CONNECTED___() {
		return CONNECTED___;
	}

	public boolean get_STRONGLYCONNECTED___() {
		return STRONGLYCONNECTED___;
	}

	/*
	public boolean get_TRANSPOSE___() {
		return TRANSPOSE___;
	} */

	public boolean get_CYCLE___() {
		return CYCLE___;
	}

	public boolean get_MSTPRIM___() {
		return MSTPRIM___;
	}

	public boolean get_MSTKRUSKAL___() {
		return MSTKRUSKAL___;
	}

	public boolean get_SHORTEST___() {
		return SHORTEST___;
	}

	public static void main(String args[])
	{
		RunSPL.loadOneConfiguration(GPLTEST.class);
		int testCase = Integer.parseInt(args[args.length-2]);
		
		Main.splStart___();
				
		if(testCase == 0)
			Main.cycleTest(args);
		else if(testCase == 1)
		{
			Main.kruskalTest(args);
		}			
		else
			Main.mainBody___(args);
		
		Main.splEnd___();		
	}
}
