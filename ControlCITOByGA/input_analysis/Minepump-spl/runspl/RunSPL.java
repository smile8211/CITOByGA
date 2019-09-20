package runspl;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import featuremodel.Configuration;
import featuremodel.FeatureModel;
import featuremodel.FeatureModelBuilder;

public class RunSPL {
	public static final String NO_SAVED_FEATUREMODEL = "";
	public static Configuration executingConfig;

	public static int testCase;

	// Feature model is constructed once for different calls to run()
	public static Set<Configuration> featureModelSolutions;
	public static List<String> features;

	public static double staticAnalysisDuration;
	private static String applicationMain;

	public void saveConfigurations(String outputPath, FeatureModel featureModel, List<String> allFeatures)
	{
		StringBuffer configsStr = new StringBuffer();
		Set<Configuration> featureModelSolutions = featureModel.getSolutions();
		for(Configuration c: featureModelSolutions)
			configsStr.append(c + "\n");		
		JavaUtility.INSTANCE.writeToFile(outputPath, configsStr.toString());
	}

	public static Set<Configuration> loadConfigurations(String configsPath, List<String> features)
	{
		Set<Configuration> result = new LinkedHashSet<Configuration>();
		try
		{
			String configsStr = JavaUtility.INSTANCE.getFileContents(configsPath);
			String[] lines = configsStr.split("\\n");

			for(String line: lines)
			{
				if(!line.startsWith("//"))
				{
					Configuration c = new Configuration(features, line);
					result.add(c);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return result;		
	}


	private static Object getSingleton(Class mainClass)
	{
		Object singleton = null;
		try {
			Field f = mainClass.getField("SINGLETON");
			singleton = f.get(null);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return singleton;
	}


	public static void loadOneConfiguration(Class mainClass)
	{
		try
		{
			Object singleton = getSingleton(mainClass);
			for(String feature: executingConfig.features)
			{
				Field f = mainClass.getField(feature);
				f.setBoolean(singleton, executingConfig.getFeatureValue(feature).booleanValue());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void runEachConfiguration(List<String> features, Set<Configuration> solutions, Class applicationMain, String[] applicationArgs)
	{		
		try
		{
			//<original
			List<Configuration> sortedConfigs = new Vector<Configuration>(solutions); 
			Configuration.sortByBitVector(sortedConfigs);

			// Get the main method that will be invoked through reflection on each configuration
			Method mainMethod = applicationMain.getMethod("main", String[].class);			

			for(int i = 0; i < sortedConfigs.size(); i++)
			{
				executingConfig = sortedConfigs.get(i);

				try {
					mainMethod.invoke(null, (Object)applicationArgs);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			System.out.println("List of explored configurations -------------------");
			//int i = 0;
			for(Configuration c: sortedConfigs)
			{
				System.out.println(c);
				//JavaUtility.INSTANCE.writeToFile(System.getProperty("user.dir") + File.separator + "c" + i, c.toString());
			//	i++;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}


	/*
	public static void startConfiguration(Class mainClass, String featureModelPath)
	{
	//	applicationMain = mainClass.getName();
		loadOneConfiguration(mainClass);					
	} */

	
	public static void run(String appClass, String[] applicationArgs, String configsPath)
	{
		try
		{
			Class applicationClass = Class.forName(appClass);
		//	applicationMain = applicationClass.getName();

			String featureModelPath = applicationArgs[applicationArgs.length-1];
			features = FeatureModelBuilder.getFeatures(applicationClass);
			if(configsPath.equals(NO_SAVED_FEATUREMODEL))
			{				
				if(featureModelSolutions == null)
				{								
					String featureModelText = FeatureModelBuilder.getFeatureModelString(featureModelPath);					
					FeatureModel featureModel = new FeatureModel(features, featureModelText);
					featureModelSolutions = featureModel.getSolutions();
				}
			}
			else
			{
				featureModelSolutions = loadConfigurations(configsPath, features);
			}			

			System.out.println("No. of configurations: " + featureModelSolutions.size());

			runEachConfiguration(features, featureModelSolutions, applicationClass, applicationArgs);			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
