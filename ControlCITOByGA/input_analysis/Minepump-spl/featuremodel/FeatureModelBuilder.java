package featuremodel;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import runspl.JavaUtility;




public class FeatureModelBuilder {
	public static final String FEATURES_FILE_NAME = "features.txt";

	public static String getFeatureModelString(String inputDir) // String classWithMainMethod)
	{
		String featureModelStr = null;
		featureModelStr = JavaUtility.INSTANCE.getFileContents(inputDir);

		return featureModelStr;
	}

	//	public static List<String> getFeatures(JVM vm)
	//	{
	//		List<String> features = new Vector<String>();
	//		ClassInfo mainClassInfo = vm.getMainClassInfo();
	//		for(int i = 0; i < mainClassInfo.getNumberOfStaticFields(); i++)
	//		{
	//			FieldInfo staticFieldInfo = mainClassInfo.getStaticField(i);
	//			if(FeatureID.isFeatureID(staticFieldInfo.getName()))
	//			{
	//				features.add(staticFieldInfo.getName());
	//			}
	//		}
	//
	//		return features;		
	//	}

	public static List<String> getFeatures(Class mainClass)
	{
		List<String> features = new Vector<String>();
		try
		{
			for(Field f: mainClass.getDeclaredFields())
			{
				if(FeatureID.isFeatureID(f.getName()))
				{
					features.add(f.getName());
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return features;
	}

	public static Set<String> getFeatures(String inputPath)
	{
		Set<String> features = new LinkedHashSet<String>();

		// Parse .equation file into features 
		String str = JavaUtility.INSTANCE.getFileContents(inputPath + System.getProperty("file.separator") + FEATURES_FILE_NAME);
		String[] tokens = str.split("\\s+");

		for(int i = 0; i < tokens.length; i++)
		{
			String token = tokens[i];

			if(!token.trim().isEmpty())
				features.add(token);
		}	

		return features;
	}

}
