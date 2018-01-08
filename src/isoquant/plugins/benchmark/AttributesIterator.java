package isoquant.plugins.benchmark;

import java.util.*;

/**
 * permutate given value sets for given attributes.
 * 
 * <h3>{@link AttributesIterator}</h3>
 * @author kuharev
 * @version 23.07.2012 16:54:18
 */
public class AttributesIterator implements Runnable
{

	/**
	 * listener to be notified if a new attributes permutation is generated
	 * <h3>{@link PermutationListener}</h3>
	 * @author kuharev
	 * @version 07.08.2012 10:01:40
	 */
	public static interface PermutationListener
	{
		public void attributesChanged( String[] varNames, Map<String, String> att2val );
	}
	
	private String[] varNames = {};
	private String[][] varValues = {};
	private int nVars = 0;
	private Stack<Integer> stack = null;
	private Set<Map<String, String>> states = null;
	private PermutationListener permutationListener = null; 

	public AttributesIterator(String[] varNames, String[][] varValues, PermutationListener permutationListener)
	{
		this.varNames = varNames;
		this.varValues = varValues;
		this.nVars  = varNames.length;
		this.permutationListener = permutationListener;
	}
	
	@Override public void run()
	{
		// clear states
		states = new HashSet<Map<String, String>>();
		
		stack = new Stack<Integer>();
		while(stack.size()<nVars) stack.push( 0 );
		
		while( !stack.isEmpty() )
		{
			process();
			
			int count = stack.pop();
			int level = stack.size();
			
			count++;
			
			if( count < varValues[ level ].length )
			{
				stack.push(count);
				while(stack.size()<nVars) 
				{ 
					stack.push(0); 
				}
			}
		}			
	}

	/**
	 * @param level
	 * @param count
	 */
	private void process()
	{
		Map<String, String> varValueMap = new HashMap<String, String>();
		for(int i=0; i<nVars; i++)
		{
			String var = varNames[i];
			String val = varValues[i][ ( i<stack.size() ) ? stack.get(i) : 0 ];
			varValueMap.put( var, val );
		}
		
		if( !states.contains( varValueMap) )
		{
			states.add(varValueMap);
			
			if(permutationListener==null)
			{
				changeAttributes( varValueMap );
			}
			else
			{
				permutationListener.attributesChanged( varNames, varValueMap );
			}
		}
	}

	int loopCount = 0;
	private void changeAttributes(Map<String, String> varMap)
	{
		loopCount++;
		Set<String> vars = varMap.keySet();
		for(String var : vars)
		{
			System.out.print(varMap.get(var));
		}
		System.out.println();
	}
	
	
	
	
	
	
	
	
	
	
	/**
	 * test scenario: generate all permutations of 123 vs abc vs XYZ
	 * @param args
	 */	
	public static void main(String[] args)
	{
		String[] varNames = {"A", "B", "C"};
		String[][] varValues = {
				{"1", "2", "3"},
				{"a", "b", "c"},
				{"X", "Y", "Z"}
		};

		PermutationListener l = new PermutationListener()
		{
			int loopCount = 0;
			@Override public void attributesChanged(String[] varNames, Map<String, String> varVals)
			{
				loopCount++;
				System.out.print(loopCount + ":\t");				
				for(String var : varNames)
				{
					System.out.print(varVals.get(var));
				}
				System.out.println();
			}
		};
		
		AttributesIterator it = new AttributesIterator(varNames, varValues, l);
		it.run();		
	}
}