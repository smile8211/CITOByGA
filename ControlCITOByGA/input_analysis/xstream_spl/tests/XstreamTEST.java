package tests;
import runspl.RunSPL;
import chpkim.XstreamExample;

public class XstreamTEST {
	static String SCALE_CONST;
	static String FEATURE_MODEL_PATH;
	static String EXECUTION_MODE;
	private static String TEST_CASE;

	public static XstreamTEST SINGLETON = new XstreamTEST();

	public boolean BASE___;
	public boolean TREE_STRUCTURE___;
	public boolean CLASS_ALIAS___;
	public boolean FIELD_ALIAS___;
	public boolean OMIT_FIELD___;
	public boolean IMPLICIT_ARRAY___;
	public boolean ATTRIBUTES___;	
	public boolean BOOLEAN_CONVERTER___;

	public static int SCALE;
	

	public boolean get_BASE___() {
		return BASE___;
	}

	public boolean get_TREE_STRUCTURE___() {
		return TREE_STRUCTURE___;
	}

	public boolean get_CLASS_ALIAS___() {
		return CLASS_ALIAS___;
	}	

	public boolean get_FIELD_ALIAS___() {
		return FIELD_ALIAS___;
	}	

	public boolean get_OMIT_FIELD___() {
		return OMIT_FIELD___;
	}	

	public boolean get_IMPLICIT_ARRAY___() {
		return IMPLICIT_ARRAY___;
	}	

	public boolean get_ATTRIBUTES___() {
		return ATTRIBUTES___;
	}

	public boolean get_BOOLEAN_CONVERTER___() {
		return BOOLEAN_CONVERTER___;
	}

	public static void main(String args[])
	{
		try
		{
			RunSPL.loadOneConfiguration(XstreamTEST.class);
			int testCase = Integer.parseInt(args[args.length-2]);
			
			SCALE = Integer.parseInt(args[0]);
				
			XstreamExample.COMMON_COUNT = 200 * SCALE;
			XstreamExample.VARIABLE_COUNT = 40 * SCALE;
			XstreamExample.PLACEHOLDER_COUNT = XstreamExample.COMMON_COUNT / 2;
			XstreamExample.splStart___();
			
			XstreamExample.class.getMethod("test" + testCase, new Class[]{}).invoke(null);
			XstreamExample.splEnd___();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
