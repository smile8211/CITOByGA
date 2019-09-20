package featuremodel;



/**
 * Keep either this class or CompoundProposition!
 * 
 * @author Peter Kim
 *
 */
public class BinaryProposition implements Proposition {
	public static final String IMPLIES = " implies ";
	public static final String EQUALS = " iff ";
	
	private Proposition lhs;
	private Proposition rhs;
	private String operator;

	public BinaryProposition(Proposition lhs, String operator, Proposition rhs)
	{
		setLhs(lhs);
		setOperator(operator);
		setRhs(rhs);
	}
	
	public Proposition getLhs() {
		return lhs;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public void setLhs(Proposition lhs) {
		this.lhs = lhs;
	}

	public Proposition getRhs() {
		return rhs;
	}

	public void setRhs(Proposition rhs) {
		this.rhs = rhs;
	}

	public String toString()
	{
		StringBuffer str = new StringBuffer();
		
		str.append(lhs);
		str.append(operator);
		str.append(rhs);
		
		return str.toString();
	}
	
	@Override
	public boolean equals(Object o)
	{
		boolean result = false;
		if(o instanceof BinaryProposition)
		{
			BinaryProposition binaryProp = (BinaryProposition)o;
			result = lhs.equals(binaryProp.lhs) && 
						operator.equals(binaryProp.operator) &&
						rhs.equals(binaryProp.rhs); 
		}		
		return result;
	}
	
	@Override
	public int hashCode()
	{
		return lhs.hashCode() + operator.hashCode() + rhs.hashCode(); 
	}
}
