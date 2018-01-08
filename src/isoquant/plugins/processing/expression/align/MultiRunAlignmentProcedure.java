/** ISOQuant, isoquant.plugins.processing.expression.align, 11.05.2011*/
package isoquant.plugins.processing.expression.align;

import java.util.List;
import java.util.concurrent.Semaphore;

import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.utilities.Bencher;
import isoquant.app.Defaults;
import isoquant.plugins.processing.expression.align.context.*;
import isoquant.plugins.processing.expression.align.io.MassSpectrumPeakStorage;
import isoquant.plugins.processing.expression.align.io.RTWToMedianAdjuster;
import isoquant.plugins.processing.expression.align.methods.SingleRunAlignment;

/**
 * <h3>{@link MultiRunAlignmentProcedure}</h3>
 * @author Joerg Kuharev
 * @version 11.05.2011 16:42:24
 */
public class MultiRunAlignmentProcedure extends AlignmentContext implements Runnable, AlignmentProcessListener
{	
	private Semaphore sem = null;
	private int maxProcesses = 1;
	private boolean normalizeRefRT = false;
	private AlignmentMode mode = AlignmentMode.IN_PROJECT;
	private ReferenceRunSelectionMethod refRunSelectionMethod = ReferenceRunSelectionMethod.AUTO;
	private Integer preferredReferenceRunIndex = 0;
	
	/**
	 * create an environment for aligning retention time of all runs from a project to a reference run.
	 * the reference run is selected automatically by counting number of contained peaks
	 * and selecting the run with the highest numer of peaks.<br>
	 * The alignment will be done using parallelized Hirschberg's method by default  
	 * @param db the database
	 */
	public MultiRunAlignmentProcedure(MySQL db)
	{
		this.storage = new MassSpectrumPeakStorage( db );
		this.setAlignmentListener( this );
	}
	
	/**
	 * create an environment for aligning retention time of all runs from a project to a reference run.
	 * the reference run is selected automatically by counting number of contained peaks
	 * and selecting the run with the highest numer of peaks.
	 * @param db the database
	 * @param alignmentMethod the alignment method to use
	 */
	public MultiRunAlignmentProcedure( MySQL db, AlignmentMethod alignmentMethod )
	{
		this(db);
		setAlignmentMethod( alignmentMethod );
	}
	
	public MultiRunAlignmentProcedure setReferenceRunSelectionMethod(ReferenceRunSelectionMethod method){ refRunSelectionMethod = method; return this; }
	public MultiRunAlignmentProcedure setPreferredReferenceRunIndex(Integer refRunIdx){ preferredReferenceRunIndex = refRunIdx;return this; }

	/** set alignment mode */
	public MultiRunAlignmentProcedure setAlignmentMode(AlignmentMode mode){this.mode = mode;return this;}
	public AlignmentMode getAlignmentMode(){return mode;}
	
	/** if the resulting alignment functions should be normalized (centered to median shifts) */
	public MultiRunAlignmentProcedure setNormalizeRefRT(boolean shouldNormalizeRefRT)
	{
		this.normalizeRefRT = shouldNormalizeRefRT;
		return this;
	}

	/**
	 * get a reference run index out of a list of given run indices 
	 * taking into account the user defined reference run index and the selection method 
	 * @param listOfRunIndices
	 * @return
	 */
	private int findRefRunIndex(List<Integer> listOfRunIndices)
	{
		// in case of manual reference run selection
		// the user defined run index must be one of the available runs
		// otherwise use auto selection
		if (refRunSelectionMethod.equals( ReferenceRunSelectionMethod.MANUAL ) && listOfRunIndices.contains( preferredReferenceRunIndex ))
			return preferredReferenceRunIndex;
		else
			return listOfRunIndices.get( 0 );
	}
	
	@Override public void run()
	{
		if( mode==AlignmentMode.IN_SAMPLE )
			alignInSample();
		else
			alignInProject();
	}
	
	public void alignInProject()
	{
		// start timer
		Bencher timer = new Bencher().start();
		// (re)create RTW table
		storage.initRTWTable();
		// get ordered runIndeces
		List<Integer> allRunIndices = storage.getRunIndexes();
		// synchronize threading by semaphore with limited resources and fifo fairness 
		sem = new Semaphore( maxProcesses, true );
		// select reference run according to the
		// selection method and the user defined run index if possible
		// otherwise select first run as reference!
		alignRuns( allRunIndices );
		// proceed if all alignment threads have finished execution and released resources
		sem.acquireUninterruptibly(maxProcesses);
		System.out.println("\n\t----------------------------------------");
		// adjust rtw.ref_rt to medians
		if(normalizeRefRT)
		{
			new RTWToMedianAdjuster( storage.getDB() ).run();
			System.out.println("\n\t----------------------------------------");
		}
		
		// commit results by injecting them into clustered_emrt table
		storage.commitRTWResults();

		System.out.println( "alignment duration: " + timer.stop().getSecString() );
	}
	
	public void alignInSample()
	{
		// start timer
		Bencher timer = new Bencher().start();
		// synchronize threading by semaphore with limited resources and fifo fairness 
		sem = new Semaphore( maxProcesses, true );
		// (re)create RTW table
		storage.initRTWTable();
		List<Integer> sis = storage.getParentUnitIndexes();
		for(int s : sis)
		{
			alignSample( s );
			// proceed if all alignment threads have finished execution and released resources
			sem.acquireUninterruptibly(maxProcesses);
			sem.release(maxProcesses);
		}
		// commit results by injecting them into clustered_emrt table
		storage.commitRTWResults();
		System.out.println( "alignment duration: " + timer.stop().getSecString() );
	}
	
	private void alignSample(int sampleIndex)
	{
		// get ordered runIndices
		List<Integer> runIndices = storage.getRunIndexes(sampleIndex);
		alignRuns( runIndices );
	}
	
	/**
	 * @param runIndices
	 */
	private void alignRuns(List<Integer> runIndices)
	{
		// select reference run according to the
		// selection method and the user defined run index if possible
		// otherwise select first run as reference!
		Integer refRunIndex = findRefRunIndex( runIndices );

		// remember number of runs
		int n = runIndices.size();
		
		// end if there is nothing to align
		if(n<2) return;

		System.out.println("aligning " + (n-1) + " runs to the reference run "+refRunIndex+" ...");
		System.out.println( "	parameters:" );
		System.out.println( "		database schema name: '" + storage.getDB().getSchema() + "'" );
		System.out.println( "		alignment method: '" + method + "'" );
		System.out.println( "		path mode: " + alignmentPathMode );
		
		switch(method)
		{
			case ITERATIVE:
			case FAST_RECURSIVE:
			case PARALLEL_FAST_RECURSIVE:
				System.out.println( "		iterative alignment options:" );
				System.out.println( "			peak abstraction method: " + peakAbstractionMethod.name() );
				System.out.println( "			min peaks for pre-alignment: " + minPeaksForPrealignment );
				System.out.println( "			path refinement radius: " + prealignmentPathRefinementRadius );
				System.out.println( "			iterative peak list growth factors: " + prealignmentGrowthFactor + " .. " + prealignmentGrowthFactorLimit );
			default:
		}

		System.out.println( "		parallelization options:" );

		switch(method)
		{
			case PARALLEL_RECURSIVE:
			case PARALLEL_FAST_RECURSIVE:
				System.out.println( "			max process forking depth: " + maxProcessForkingDepth );
		}

		if (method == AlignmentMethod.ITERATIVE) maxProcesses = 1;
		System.out.println( "			max parallel alignment processes: " + maxProcesses );

		System.out.println( "	peak matching options:" );
		System.out.println( "		max delta mass: " + maxDeltaMassPPM + " ppm" );
		System.out.println( "		max delta drift time: " + maxDeltaDrift + " bin" );

		System.out.println( "	peak selection contraints:" );
		System.out.println( "		min feature intensity: " + minInten );
		System.out.println( "		min feature mass: " + minMass );
		System.out.println( "\n	----------------------------------------" );
		
		// align runs to the reference
		for(int runIndex : runIndices)
		{
			alignRunToRef( runIndex, refRunIndex );
		}
	}

	/**
	 * @param refRunIndex
	 * @param runIndex
	 */
	private synchronized void alignRunToRef( int runIndex, int refRunIndex )
	{
		if (method.equals( AlignmentMethod.NONE ) || runIndex == refRunIndex)
		{
			// fake alignment by 1:1 mapping of rt to ref rt
			storage.storeRefRTW( runIndex, rtStepSize );
		}
		else
		{
			// provide parameters for pairwise alignment
			AlignmentContext scope = super.forkForPairwiseAlignment( refRunIndex, runIndex );
			// single run aligning thread
			// decide which algorithm to use
			SingleRunAlignment a = method.toImplementation( scope );
			// wait for free process and start alignment
			a.start();
		}
	}

	/** set maximum number of alignment processes to be simultaneously executed */
	public MultiRunAlignmentProcedure setMaxProcesses(int maxProcesses){ this.maxProcesses = (maxProcesses<1) ? 1 : maxProcesses; return this; }
	
	/** free up resources after alignment calculation */
	@Override public void afterAlignment(AlignmentContext alignmentContext)
	{
		// free used resource after execution  
		sem.release();

		System.out.println( "\t" + alignmentContext.getAlignedRunIndex() + ":\trun alignment duration: " + alignmentContext.getProcessingTime() + "s" );
	}

	/**
	 * wait for free slots and allocate resources before alignment calculation
	 */
	@Override public void beforeAlignment(AlignmentContext alignmentContext)
	{
		if(Defaults.DEBUG) 
			System.out.println( "\t"+alignmentContext.getAlignedRunIndex() + ":\twaiting for free CPU slot ["+sem.availablePermits()+"/"+maxProcesses+"]");
		
		// request resource before execution
		sem.acquireUninterruptibly();
		
		if(Defaults.DEBUG)
			System.out.println( "\t" + alignmentContext.getAlignedRunIndex() + ":\tgot CPU slot, aligning to reference ..." );
	}
}
