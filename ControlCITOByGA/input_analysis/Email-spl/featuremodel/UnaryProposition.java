package featuremodel;


public class UnaryProposition implements Proposition {
	public static final String FALSE = "not(" + FeatureID.BASE + ")";
	public static final String TRUE = FeatureID.BASE;
	
	private String value;
	
	public UnaryProposition(String value)
	{
		setValue(value);
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String toString()
	{
		return value;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		
		if(obj instanceof UnaryProposition)
		{
			UnaryProposition u = (UnaryProposition)obj;
			result = value.equals(u.value);
		}
		return result;
	}
	
	@Override
	public int hashCode()
	{
		return value.hashCode();
	}
}
