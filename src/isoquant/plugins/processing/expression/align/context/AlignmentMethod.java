/** ISOQuant, isoquant.plugins.processing.expression.align, 09.05.2011*/
package isoquant.plugins.processing.expression.align.context;

import isoquant.plugins.processing.expression.align.methods.*;

/**
 * <h3>{@link AlignmentMethod}</h3>
 * enumeration of alignment methods
 * @author Joerg Kuharev
 * @version 09.05.2011 10:37:10
 */
public enum AlignmentMethod
{
	/** SingleRunIterativeRTW */
	ITERATIVE,
	/** SingleRunRecursiveRTW */
	RECURSIVE,
	/** SingleRunRecursiveParallelRTW */
	PARALLEL_RECURSIVE,
	/** SingleRunFastRecursiveRTW */
	FAST_RECURSIVE,
	/** SingleRunFastRecursiveParallelRTW */
	PARALLEL_FAST_RECURSIVE,
	/** SingleRunIdentificationAlignment */
	PAIRWISE_IDENTIFIED_PEPTIDES,
	/** faking alignment */
	NONE,
	/** whatever else method is unknown */
	UNKNOWN,
	/** TESTING */
	MASS_SPLIT;
	
	public final static String[] implementedMethodNames = {
			ITERATIVE.name(),
			RECURSIVE.name(),
			PARALLEL_RECURSIVE.name(),
			FAST_RECURSIVE.name(),
			PARALLEL_FAST_RECURSIVE.name(),
			PAIRWISE_IDENTIFIED_PEPTIDES.name(),
			MASS_SPLIT.name(),
			NONE.name()
	};

	@Override public String toString()
	{
		switch( this )
		{
			case ITERATIVE:
				return "FastDRTW";
			case RECURSIVE:
				return "linear DRTW";
			case PARALLEL_RECURSIVE:
				return "linear DRTW with parallel recursion";
			case FAST_RECURSIVE:
				return "linear FastDRTW";
			case PARALLEL_FAST_RECURSIVE:
				return "linear FastDRTW and parallel recursion";
			case PAIRWISE_IDENTIFIED_PEPTIDES:
				return "pairwise identified peptides sequences";
			case MASS_SPLIT:
				return "multiple mass windows by linear FastDRTW with parallel recursion ";
			case NONE:
				return "faked alignment";
			default:
				return "unknown method";
		}
	}
	
	public static AlignmentMethod fromString(String s)
	{
		s = s.trim().toLowerCase();
		if(s.startsWith("iterative"))			return ITERATIVE; 
		if(s.startsWith("recursive")) 		return RECURSIVE;
		if(s.matches(".*parallel.*fast.*")) return PARALLEL_FAST_RECURSIVE;
		if(s.startsWith("parallel")) 			return PARALLEL_RECURSIVE;
		if(s.startsWith("fast")) 				return FAST_RECURSIVE;
		if (s.matches( ".*ident.*" )) 			return PAIRWISE_IDENTIFIED_PEPTIDES;
		if(
			s.startsWith("none") ||
			s.startsWith("fake") ||
			s.startsWith("zero") ||
			s.startsWith("false") ||
			s.startsWith("null"))					return NONE;
		if (s.contains( "mass" )) 				return MASS_SPLIT;
		return UNKNOWN; 
	}

	/**
	 * generate a new instance of this alignment methods implementation
	 * @param scope
	 * @return
	 */
	public SingleRunAlignment toImplementation(AlignmentContext scope)
	{
		SingleRunAlignment a = null;
		switch (this)
		{
			case ITERATIVE:
				a = new SingleRunIterativeRTW( scope );
				break;
			case RECURSIVE:
				a = new SingleRunRecursiveRTW( scope );
				break;
			case PARALLEL_RECURSIVE:
				a = new SingleRunRecursiveParallelRTW( scope );
				break;
			case FAST_RECURSIVE:
				a = new SingleRunFastRecursiveRTW( scope );
				break;
			case PAIRWISE_IDENTIFIED_PEPTIDES:
				a = new SingleRunIdentificationAlignment( scope );
				break;
			case MASS_SPLIT:
				a = new SingleRunFastRecursiveParallelMassSplitRTW( scope );
				break;
			case PARALLEL_FAST_RECURSIVE:
			default:
				a = new SingleRunFastRecursiveParallelRTW( scope );
				break;
		}
		return a;
	}
}
