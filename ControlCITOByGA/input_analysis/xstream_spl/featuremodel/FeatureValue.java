package featuremodel;


/**
 * A feature value can be true, false or undecided.
 * @author chpkim
 *
 */
public enum FeatureValue {
	FALSE, TRUE, UNBOUND;
	
	/**
	 * Returns the Boolean equivalent value if the value is FALSE or TRUE
	 * @return
	 */
	public boolean booleanValue()
	{
		assert this != UNBOUND : "FeatureValue is UNBOUND in FeatureValue.booleanValue()";
		
		return this == TRUE;
	}
	
	public int intValue()
	{
		assert this != UNBOUND : "FeatureValue is UNBOUND in FeatureValue.booleanValue()";
		
		return this == TRUE? 1: 0;
	}
}
