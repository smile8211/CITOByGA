package featuremodel;


public class FeatureID {
	public static final String FEATURE_VARIABLE_POSTFIX = "___";
	
	public static final String FEATURE_PACKAGE_NAME = "features" + FEATURE_VARIABLE_POSTFIX;
	public static final String FEATURE_CLASS_NAME = "Features";
	
	public static final String BASE = "BASE" + FEATURE_VARIABLE_POSTFIX;
	
	public static boolean isFeatureID(String id)
	{
		return id.endsWith(FEATURE_VARIABLE_POSTFIX); // && Character.isLetter(id.charAt(0));
	}

//	/*
//	public static boolean isFeatureRead(Instruction insn)
//	{
//		return insn instanceof GETSTATIC && isFeatureID(((GETSTATIC)insn).getFieldInfo().getName());
//	}
//
//	public static boolean isFeatureWrite(Instruction insn)
//	{
//		return insn instanceof PUTSTATIC && isFeatureID(((PUTSTATIC)insn).getFieldInfo().getName());
//	}
//	
//	public static boolean isFeatureRead(byte storageType, int parentIndex, int index)
//	{
//		boolean featureRead = false;
//		
//		if(storageType == VariableLocation.STATIC_FIELD)
//		{
//			ElementInfo ei = JVM.getVM().getKernelState().getStaticArea().get(parentIndex);
//			FieldInfo fieldInfo = ei.getFieldInfo(index);
//			featureRead = isFeatureID(fieldInfo.getName());
//		}
//		
//		return featureRead;
//	}
//	
//	/*
//	public static boolean isFeatureVariable(VariableLocation v)
//	{
//		boolean featureRead = false;
//		
//		if(v.getStorageType() == VariableLocation.STATIC_FIELD && v.getParentIndex() instanceof Integer)
//		{
//			ElementInfo elementInfo = JVM.getVM().getSystemState().getKernelState().getStaticArea().get((Integer)v.getParentIndex());
//			FieldInfo fieldInfo = elementInfo.getFieldInfo(v.getIndex());
//			featureRead = isFeatureID(fieldInfo.getName());			
//		}
//		return featureRead;
//	} */
//	
//	/**
//	 * Returns the fi
//	 * @param fieldInfo
//	 * @return
//	 */
//	public static String getFeature(AnnotationInfo annotationInfo)
//	{
//		String feature = null;
//		if(annotationInfo != null)
//			feature = annotationInfo.valueAsString();
//		else
//			feature = BASE;
//		return feature;
//	}
//
//	/**
//	 * Returns the feature variable with the specified feature name.
//	 * 
//	 * @param featureName
//	 * @return
//	 */
//	public static final VariableLocation getFeatureVar(String featureName)
//	{	
//		ElementInfo elementInfo = JVMUtility.getMainMethodsElementInfo();
//		FieldInfo fieldInfo = elementInfo.getClassInfo().getStaticField(featureName);
//	
//		// Due to a limitation in VariableRecorder, boolean types are returned as byte types.
//		// So we must do the same here for feature variables to be detected.
//		//String fullFeatureName = vm.getMainClassName() + "." + featureName; 
//		VariableLocation featureVar = new VariableLocation
//			(VariableLocation.STATIC_FIELD, elementInfo.getIndex(), fieldInfo.getFieldIndex(), new FeaturePredicate());
//		return featureVar;
//	} */
}
