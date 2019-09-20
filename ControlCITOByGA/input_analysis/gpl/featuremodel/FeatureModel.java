package featuremodel;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.InstanceReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import runspl.JavaUtility;

import featuremodel.guidsl.CNFModel;
import featuremodel.guidsl.DParseException;
import featuremodel.guidsl.Tool;
import featuremodel.guidsl.Variable;

/**
 * Enumerates relevant configurations based on feature model and relevant features 
 * using a SAT solver, rather than JPF.
 * 
 * @author Peter Kim
 *
 */
public class FeatureModel {
	public static final int BASEFEATURE_INDEX = 0;
	
	private List<String> features;
	private String fmCNF;
	private Set<Configuration> solutions;
	
	public FeatureModel(List<String> f, String s)
	{
		this.features = new Vector<String>(f);
		fmCNF = getCNF(s);
	//	computeUnboundFeatures();		
	}
	
	public String getCNF()
	{
		return fmCNF;
	}
	
	/**
	 * Returns the solution to this feature model with the additional clauses (feature values).
	 * Does not modify the feature model
	 * 
	 * @param additionalClauses
	 * @return
	 */
	public Set<Configuration> getSolutions(Set<Integer> clauses)
	{
		//<Alternative implementation that doesn't solve the entire feature model
		//	String newCNF = getCNFWithAddedClauses(fmCNF, clauses);
		//	return getRelevantConfigurations(features, newCNF);
		//>
		
		Set<Configuration> solns = new HashSet<Configuration>();
		for(Configuration c: getSolutions())
		{
			boolean match = true;
			for(Integer featureVal: clauses)
			{
				String feature = Variable.findVar(Math.abs(featureVal));
				FeatureValue featureValue = featureVal >= 0? FeatureValue.TRUE : FeatureValue.FALSE;
				if(c.getFeatureValue(feature) != featureValue)
				{
					match = false;
					break;
				}
			}				
			if(match)
				solns.add(c);			
		}
		return solns;
	}
	
	public List<String> getFeatures()
	{
		return features;
	}
	
	/*
	private void computeUnboundFeatures()
	{
		for(String f: features)
		{
			FeatureValue featureValue = getFeatureValue(fmCNF, f);
			//System.out.println(f + ": " + featureValue);
			if(featureValue.equals(FeatureValue.UNBOUND))
				unboundFeatures.add(f);
		}				
	} */

	/**
	 * Currently, if this method can only be called once in a program.  If called more, 
	 * result will be inaccurate because Tool and CNFModel classes relies on static fields that
	 * are not reset after each invocation 
	 * 
	 * @param featureModelStrForm
	 * @return
	 */
	private static String getCNF(String featureModelStrForm)
	{
		Tool tool = new Tool(new ByteArrayInputStream(featureModelStrForm.getBytes()));
		CNFModel cnfModel = tool.getCNFModel();
		String cnfStr = cnfModel.getModel();
		return cnfStr;
	}

	private static String getCNFWithAddedClauses(String cnf, List<Integer> clauses)
	{
		String newCNF = "";

		String[] lines = cnf.split("\n");

		// Parse CNF to increment the number of clauses by one.  
		// Ideally, we should be using the API, e.g. ISolver.addClause, to modify
		// the CNF, but there is a problem with removing the clause after adding it (the
		// constraint returned by ISolver.addClause is null, which makes it impossible to call
		// ISolver.removeConstr(.)
		String[] header = lines[0].split("\\s+");		
		for(int i = 0; i < header.length-1; i++)
		{
			newCNF += header[i] + " ";
		}
		newCNF += String.valueOf(Integer.parseInt(header[header.length-1])+1) + "\n";

		// Add 2nd line to the last line
		for(int i = 1; i < lines.length; i++)
		{
			newCNF += lines[i] + "\n";
		}

		// Add the constraint (i.e. var=true or var=false)
		for(Integer clause: clauses)
			newCNF += clause + " 0\n";

		return newCNF;
	}

	public static ISolver getSATSolver(String cnf)
	{
		ISolver satSolver = null;

		try
		{
			satSolver = SolverFactory.newDefault();		
			satSolver.newVar(100000);
			satSolver.setExpectedNumberOfClauses(500000);			
			satSolver.setTimeout(3600); // 1 hour timeout

			Reader reader = new DimacsReader(satSolver);						
			reader.parseInstance(new ByteArrayInputStream(cnf.getBytes()));
		}
		catch (ContradictionException e)
		{			
			//e.printStackTrace();
			satSolver = null;
		} catch (ParseFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			satSolver = null;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			satSolver = null;
		} 
		return satSolver;
	}

	private boolean featureCanBe(String cnf, String feature, boolean truthValue)
	{
		boolean canTakeTheTruthValue = false;

		try {
			// Get the CNF clause for the feature value 
			List<Integer> clauses = new Vector<Integer>();
			clauses.add(getFeatureValueClause(feature, truthValue));

			// Get the CNF with the added clause
			String newCNF = getCNFWithAddedClauses(cnf, clauses);

			// Check if the feature model is satisfiable, in which case
			// the variable can take the truth value
			ISolver satSolver = getSATSolver(newCNF);
			
			canTakeTheTruthValue = (satSolver != null && satSolver.isSatisfiable());			
		}
		catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return canTakeTheTruthValue;
	}

	public int getFeatureValueClause(String feature, boolean value)
	{
		int featureValue = getFeatureAsInteger(feature);
		if(!value)
			featureValue = -featureValue;		
		return featureValue;
	}
	
	public int getFeatureAsInteger(String feature)
	{
		int featureAsInt = -1;
		try
		{
			// Get the CNF variable identifier for this feature
			featureAsInt = Variable.findNumber(feature);
		}
		catch(DParseException e)
		{
			e.printStackTrace();
		}
		return featureAsInt;
	}

	private FeatureValue getFeatureValue(String cnf, String feature)
	{
		FeatureValue featureValue = null;		

		if(!featureCanBe(cnf, feature, true))
		{
			// If a feature's value cannot be true, it must be false.
			featureValue = FeatureValue.FALSE;
		}
		else
		{
			// If a feature's value can be true, it may be able to be false too (i.e. making the
			// feature undecided)
			boolean canBeFalseAlso = featureCanBe(cnf, feature, false);
			if(canBeFalseAlso) // meaning feature is undecided
				featureValue = FeatureValue.UNBOUND;
			else
				featureValue = FeatureValue.TRUE;
		}

		return featureValue;
	}

	/*
	public Set<String> getUnboundFeatures()
	{
		return unboundFeatures;
	} */
	
	/**
	 * Determines the solutions to this feature model
	 * @return
	 */
	public Set<Configuration> getSolutions()
	{
	//	long startTime = System.nanoTime();
		if(solutions == null)
		{	
			solutions = getRelevantConfigurations(features, features, fmCNF);		
		}
	//	RunSPL.fmSolvingTimes.add(ExecutionOutput.getSeconds(System.nanoTime() - startTime));
		return solutions;
	}
	
	public Set<Configuration> getSolutions(List<String> allFeatures, List<String> relevantFeatures)
	{
		return getRelevantConfigurations(allFeatures, relevantFeatures, fmCNF);
	}

	public long countSolutions()
	{
		return getSolutions().size();
	}
	
	/*
	public long countSolutionsBAD()
	{
		long solutionCount = -1;		

		try {
			ISolver satSolver = getSATSolver(fmCNF);

			if (satSolver != null && satSolver.isSatisfiable()) {
				SolutionCounter sc = new SolutionCounter(satSolver);
				//System.out.println("Satisfiable !");
				//System.out.println(reader.decode(problem.model()));
				IVecInt backbone = RemiUtils.backbone(satSolver);
				//System.out.println("BackBone:" + backbone);
				//System.out.println("Counting solutions...");
				//System.out.println("Number of solutions : "
				//	+ sc.countSolutions());
				solutionCount = sc.countSolutions();
			} else {
				//System.out.println("Unsatisfiable !");
				solutionCount = 0;
			}
		}
		catch (TimeoutException e) {
			System.out.println("Timeout, sorry!");
		}

		return solutionCount;
	} */

	/**
	 * Enumerates all the configurations of the relevantFeatures.  The rest of the
	 * features are set to dont_care.
	 * 
	 * @return
	 
	public Set<Configuration> enumerateConfigs(List<String> relevantFeatures)
	{
		Set<Configuration> configs = new LinkedHashSet<Configuration>();
		List<String> irrelevantFeatures = new Vector<String>(features);
		irrelevantFeatures.removeAll(relevantFeatures);
		enumerateFeatureValues(configs, new Configuration(), irrelevantFeatures, relevantFeatures, 0);

		return configs;
	}

	private void enumerateFeatureValues(Set<Configuration> configurations,
			Configuration curConfig,
			List<String> irrelevantFeatures,
			List<String> relevantFeatures,
			int relFeatureIndex)
	{
		for(int i = 0; i < 2; i++)
		{
			// FALSE in the first iteration, TRUE in the second.
			FeatureValue featureValue = null;
			if(i == 0)
				featureValue = FeatureValue.FALSE;			
			else
				featureValue = FeatureValue.TRUE;
			curConfig.setFeatureValue(relevantFeatures.get(relFeatureIndex), featureValue);

			// Check which feature we're currently at
			if(relFeatureIndex < relevantFeatures.size()-1)
			{
				// Move on to the next variable
				enumerateFeatureValues(configurations, curConfig, irrelevantFeatures, relevantFeatures, relFeatureIndex+1);
			}
			else
			{			
				// We're at the last variable, which means that all the relevant features   
				// are set.  Now we set the irrelevant features, i.e. the dont-care features
				// by asking the SAT solver.
				// But before we do the asking, get the CNF with constraints corresponding to 
				// the bound features
				Configuration configToAdd = curConfig.copy();				

				String modifiedCNF = originalFmCNF;
				for(String relFeature: relevantFeatures)
				{
					FeatureValue relFeatureVal = configToAdd.getFeatureValue(relFeature);
					assert !relFeatureVal.equals(FeatureValue.UNBOUND);
					String clause = getFeatureValueClause(relFeature, relFeatureVal.equals(FeatureValue.TRUE));

					// Add constraint corresponding to the bound feature to the CNF 
					modifiedCNF = getCNFWithAddedClause(modifiedCNF, clause);					
				}

				try
				{
					// Ask the SAT solver to fill in the dont-cares, if it's possible
					ISolver satSolver = getSATSolver(modifiedCNF);
					if(satSolver != null && satSolver.isSatisfiable())
					{
						// Go through each filled-in dont-care and set it in the configuration
						for(int varValue: satSolver.findModel())
						{
							String id = Variable.findVar(Math.abs(varValue));

							if(irrelevantFeatures.contains(id))
							{						
								FeatureValue dontCareValue = null;
								if(varValue > 0)
									dontCareValue = FeatureValue.TRUE;
								else if(varValue < 0)
									dontCareValue = FeatureValue.FALSE;

								configToAdd.setFeatureValue(id, dontCareValue);
							}
						}
						// Finally, configuration is all filled up, so add it to the configurations!
						configurations.add(configToAdd);
						System.out.println(configToAdd);
					}
					else
						System.out.println("excluding " + count++);
				} catch (TimeoutException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}								
			}	
		}			
	} */

	/**
	 * Returns the relevant configurations, i.e. the configurations on which to run 
	 * a given test, given the relevantFeatures.
	 */
	public static Set<Configuration> getRelevantConfigurations(List<String> allFeatures, List<String> relevantFeatures, String cnf)
	{
		Set<Configuration> configs = new LinkedHashSet<Configuration>();

		try {			
			// Convert set of relevant features (strings) into that of integer IDs
			Set<Integer> relevantVars = new LinkedHashSet<Integer>();
			for(String f: relevantFeatures)
				relevantVars.add(Variable.findNumber(f));
						
			// Create a temporary file containing CNF without the comments
			File tempFile = File.createTempFile("tempcnf", "featuremodel");
			JavaUtility.INSTANCE.writeToFile(tempFile, getCNFWithoutComments(cnf));
			
			// Create a solver that will be used to determine the configurations
			ISolver solver = SolverFactory.newDefault();			
			RelevantModelIterator mi = new RelevantModelIterator(relevantVars, solver);
			solver.setTimeout(3600); // 1 hour timeout
			Reader reader = new InstanceReader(mi);
			IProblem problem = reader.parseInstance(tempFile.getAbsolutePath());

			// Get the configurations
			while (problem.isSatisfiable()) {
				int [] model = problem.model();
				Configuration config = new Configuration(allFeatures, model);
				configs.add(config);				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ParseFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ContradictionException e) {
			System.out.println("Unsatisfiable (trivial)!");
		} catch (TimeoutException e) {
			System.out.println("Timeout, sorry!");
		} /*catch (DParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} */ catch (DParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return configs;
	}
	
	/**
	 * Strips comments (lines starting with "c") from the specified cnf string
	 * and returns the result.  Some operations require a CNF without the comments
	 * @param cnf
	 * @return
	 */
	private static String getCNFWithoutComments(String cnf)
	{
		String[] lines = cnf.split("\n");
		String cnfNoComments = "";
		for(String l: lines)
		{
			if(!l.startsWith("c"))
			{
				cnfNoComments += l + "\n";
			}
		}
		
		return cnfNoComments;
	}
}

