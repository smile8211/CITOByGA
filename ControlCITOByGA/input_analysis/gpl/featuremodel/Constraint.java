package featuremodel;

/**
 * Represents a simple feature constraint in the form LHS => RHS
 * @author Peter Kim
 *
 */
public class Constraint {
	protected String useFeature;
	protected String defFeature;
	
	public Constraint(String useFeature, String defFeature)
	{
		setUseFeature(useFeature);
		setDefFeature(defFeature);
	}

	public String getUseFeature() {
		return useFeature;
	}

	public void setUseFeature(String useFeature) {
		this.useFeature = useFeature;
	}

	public String getDefFeature() {
		return defFeature;
	}

	public void setDefFeature(String defFeature) {
		this.defFeature = defFeature;
	}
	
}
