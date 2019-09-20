package featuremodel;

import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import featuremodel.guidsl.Variable;



/**
 * Represents a configuration, i.e. a combination of feature values.
 * 
 * @author chpkim
 *
 */
public class Configuration {
	private Map<String, FeatureValue> idToValue = new Hashtable<String, FeatureValue>();
	private boolean[] bitVector = null;
	private int bitVectorIndex = -1;

	public List<String> features; 
	
	public Configuration(List<String> features, String bitArray)
	{
		this.features = features;
		for(int i = 0; i < bitArray.length(); i++)
		{			
			setFeatureValue(features.get(features.size()-1-i), bitArray.charAt(i) == '1'? FeatureValue.TRUE : FeatureValue.FALSE);
		}
	}

	public Configuration(List<String> features, int[] model)
	{
		this.features = features;
		for(int featureVal: model)
		{
			FeatureValue boolFeatureValue = featureVal >= 0? FeatureValue.TRUE : FeatureValue.FALSE;				
			setFeatureValue(Variable.findVar(Math.abs(featureVal)), boolFeatureValue);
		}
	}

	public boolean[] getBitVector()
	{
		if(bitVector == null)
		{
			bitVector = new boolean[features.size()];

			for(int i = 0; i < features.size(); i++)
				bitVector[i] = idToValue.get(features.get(i)).booleanValue();
		}
		return bitVector;
	}

	public int getBitVectorIndex()
	{
		if(bitVectorIndex == -1)
		{
			bitVectorIndex = 0;
			for(int i = 0; i < getBitVector().length; i++)
				bitVectorIndex += getBitVector()[i]? (1 << i): 0;			
		}
		return bitVectorIndex;
	}

	public void setFeatureValue(String featureId, FeatureValue featureValue)
	{
		idToValue.put(featureId, featureValue);
	}

	//	/**
	//	 * Note: only returns features that satisfy FeatureID's criteria of a feature
	//	 * @return
	//	 
	//	public Set<String> getFeatures()
	//	{
	//		Set<String> features = new HashSet<String>();
	//		for(String f: idToValue.keySet())
	//		{
	//			if(FeatureID.isFeatureID(f))
	//				features.add(f);
	//		}
	//		return features;
	//	}

	public FeatureValue getFeatureValue(String featureId)
	{
		FeatureValue featureValue = idToValue.get(featureId);
		assert featureValue != null: "Could not find value for featureId='" + featureId + "' in Configuration.getFeatureValue(featureId)";

		return featureValue;
	}


	public String toString()
	{
		StringBuffer s = new StringBuffer();
		for(int i = 0; i < getBitVector().length; i++)
			s.insert(0, getBitVector()[i]? 1:0);				
		return s.toString();
	}

	public static void sortByBitVector(List<Configuration> configurations)
	{
		Collections.sort(configurations, new Comparator<Configuration>(){
			@Override
			public int compare(Configuration c1, Configuration c2) {
				return c1.getBitVectorIndex() - c2.getBitVectorIndex();
			}
		});
	}

	@Override
	public boolean equals(Object o)
	{
		Configuration c = (Configuration)o;
		return c.getBitVector().equals(this.getBitVector());
	}

	@Override
	public int hashCode()
	{
		int h = 0;
		for(boolean b: this.getBitVector())
			h += b? 1: 0; 
		return h;
	}
}
