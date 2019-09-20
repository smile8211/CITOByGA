package defpackage;
import java.util.List;  

public  interface  PL_Interface {
	
	public List<String> getExecutedActions();

	
	public void start(Actions a, int specification, int variation) throws Throwable;

	
	public void checkOnlySpecification(int specID);

	
	public boolean isAbortedRun();


}
