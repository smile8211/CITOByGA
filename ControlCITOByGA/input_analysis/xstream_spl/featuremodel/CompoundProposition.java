package featuremodel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;



public class CompoundProposition implements Proposition
{
	public static final String AND = "and";
	public static final String OR = "or";
	
	private Set<Proposition> propositions = new HashSet<Proposition>();
	private String connective;
	
	public CompoundProposition(String connective)
	{
		this.connective = connective;
	}
	
	/*
	public Set<Proposition> getPropositions()
	{
		return propositions;
	} */
	
	public int size()
	{
		return propositions.size();
	}
	
	public void addProposition(Proposition p)
	{
		if(connective.equals(OR))
		{
			// If p is a TRUE clause, remove all the existing propositions
			if(p.toString().equals(UnaryProposition.TRUE))
			{
				propositions.clear();
				propositions.add(p);
			}
			else 
			{
				boolean truePropExists = false;
				
				// if there is a TRUE proposition, do not add this proposition - 
				// the loop is not really necessary because if there is a TRUE proposition,
				// only the TRUE proposition should be in the list
				for(Proposition existingProp: propositions)
				{
					if(existingProp.toString().equals(UnaryProposition.TRUE))
					{
						truePropExists = true;
						break;
					}
				}
				
				if(!truePropExists)
					propositions.add(p);				
			}
		}
		else
		{
			propositions.add(p);
			
			// Go through each propositions and collect TRUE propositions to remove
			Set<Proposition> truePropositions = new LinkedHashSet<Proposition>();
			for(Proposition existingProp: propositions)
			{				
				if(existingProp.toString().equals(UnaryProposition.TRUE))
				{
					truePropositions.add(existingProp);
				}				
			}	
			
			// Remove all the TRUE propositions
			propositions.removeAll(truePropositions);
			
			// If there is no proposition left and there was at least one trueProposition,
			// add one trueProposition back into the list
			if(propositions.size() == 0 && truePropositions.size() >= 1)
			{
				propositions.add(truePropositions.iterator().next());
			}
		}
	}
	
	public String getConnective()
	{
		return connective;
	}
	
	@Override
	public boolean equals(Object o)
	{
		boolean result = false;
		if(o instanceof CompoundProposition)
		{
			CompoundProposition compoundProp = (CompoundProposition)o;
			result = propositions.equals(compoundProp.propositions) && connective.equals(compoundProp.connective); 
		}		
		return result;
	}
	
	@Override
	public int hashCode()
	{
		return propositions.hashCode() + connective.hashCode(); 
	}
	
	/**
	 * This method recursively removes unnecessary TRUE propositions.
	 * Although the addProposition() already performs this task a little bit,
	 * calling this method after the tracematch constraints have been finalized
	 * can significantly reduce the size of the constraints. 
	 */
	public void optimize()
	{
		
	}
	
	public String toString()
	{
		StringBuffer str = new StringBuffer();
		
		/*
		Set<Proposition> propositions = new LinkedHashSet<Proposition>();
		
		//<TracematchConstraint-optimization
		// Only add non-TRUE propositions.  If there is no such propositions, then add one TRUE proposition
		for(Proposition p: propositions)
		{
			if(connective.equals(AND) && p instanceof UnaryProposition && 
					((UnaryProposition)p).getValue().equals(UnaryProposition.TRUE))
				continue;
			else
			{
				propositions.add(p);				
			}
		}		
		if(propositions.size() > 0 && propositions.size() == 0)
		{
			propositions.add(propositions.iterator().next());
		}
		//>			
        */
		
		if(propositions.size() >= 2)
			str.append("(");
		
		int i = 0;
		for(Iterator<Proposition> it = propositions.iterator(); it.hasNext();)
		{
			Proposition p = it.next();
			str.append(p);

			if(i < (propositions.size()-1))
				str.append(" " + connective + " ");
			i++;
		}	
		
		if(propositions.size() >= 2)
			str.append(")");
		
		return str.toString();
	}
	
	
}
