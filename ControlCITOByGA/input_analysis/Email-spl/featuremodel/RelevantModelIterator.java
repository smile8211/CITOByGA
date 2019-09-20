package featuremodel;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.SolverDecorator;

/**
 * 
 * This class, a variation of ModelIterator, returns models where all the combinations of
 * "relevant" boolean variables are considered with the remaining variables in those 
 * combinations considered to be "dont-cares".
 * The difference between ModelIterator and this class can be found by "diffing".
 * I've also tried as best as I can to mark the differences with "chpkim"
 * 
 * @author of ModelIterator leberre
 * @autnhor of RelevantModelIterator Peter Kim
 */
public class RelevantModelIterator extends SolverDecorator<ISolver> {
	private static final long serialVersionUID = 1L;

	private boolean trivialfalsity = false;
	private final int bound;
	private int nbModelFound = 0;
	
	//<chpkim
	private Set<Integer> relevantVariables;
	//>
	
	/**
	 * @param solver
	 */
	public RelevantModelIterator(Set<Integer> relevantVars, ISolver solver) {
		this(relevantVars, solver, Integer.MAX_VALUE);
	}

	/**
	 * 
	 * @param solver
	 * @param bound
	 * @since 2.1
	 */
	public RelevantModelIterator(Set<Integer> relevantVars, ISolver solver, int bound) {
		super(solver);		
		this.bound = bound;
		
		//<chpkim
		relevantVariables = new LinkedHashSet<Integer>(relevantVars);
		//>
	}

	//<chpkim
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sat4j.ISolver#model()
	 */
	@Override
	public int[] model() {
		int[] last = super.model();
		nbModelFound++;
		IVecInt clause = new VecInt(last.length);
		for (int q : last) {
			// Only add the variable to the blocking clause  
			// if the variable is a relevant variable (i.e. a variable that we
			// want to take both values of, as opposed to an irrelevant or dont-care
			// variable).
			if(relevantVariables.contains(Math.abs(q)))
				clause.push(-q);
		}
		try {
			// System.out.println("adding " + clause);
			addBlockingClause(clause);
		} catch (ContradictionException e) {
			trivialfalsity = true;
		}
		return last;
	}
	//>

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sat4j.ISolver#isSatisfiable()
	 */
	@Override
	public boolean isSatisfiable() throws TimeoutException {
		if (trivialfalsity || nbModelFound >= bound) {
			return false;
		}
		trivialfalsity = false;
		return super.isSatisfiable(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sat4j.ISolver#isSatisfiable(org.sat4j.datatype.VecInt)
	 */
	@Override
	public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
		if (trivialfalsity || nbModelFound >= bound) {
			return false;
		}
		trivialfalsity = false;
		return super.isSatisfiable(assumps, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sat4j.ISolver#reset()
	 */
	@Override
	public void reset() {
		trivialfalsity = false;
		nbModelFound = 0;
		super.reset();
	}
}
