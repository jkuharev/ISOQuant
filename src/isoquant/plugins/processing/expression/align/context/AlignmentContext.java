/** ISOQuant, isoquant.plugins.processing.expression.align, 12.05.2011*/
package isoquant.plugins.processing.expression.align.context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.mz.jk.jsix.math.interpolation.Interpolator;
import de.mz.jk.ms.align.com.abstraction.AbstractionMethod;
import isoquant.app.Defaults;
import isoquant.plugins.processing.expression.align.io.MassSpectrumPeakStorage;
import isoquant.plugins.processing.expression.align.methods.DynamicProgrammingPathMode;

/**
 * <h3>{@link AlignmentContext}</h3>
 * @author Joerg Kuharev
 * @version 12.05.2011 10:56:16
 */
public class AlignmentContext
{
	protected File debugLogDir = new File( "rtw_log" );
	protected boolean debugLogEnabled = false;
	protected boolean debugPlotEnabled = false;
	
	protected AlignmentProcessListener alignmentListener = null;
	protected MassSpectrumPeakStorage storage = null;
	
	protected double maxDeltaMassPPM = 10.0;
	protected double maxDeltaDrift = 2.0;
	protected double rtStepSize = 0.01;
	
	protected double minMass = 500;
	protected int minInten = 1000;
	
	protected int refRunIndex;
	protected int alignedRunIndex;
	
	protected boolean smoothRefRT = false;
	protected int maxProcessForkingDepth = Math.max( Defaults.availableProcessors / 2, 2 );
	protected int minPeaksForPrealignment = 5000;
	protected AbstractionMethod peakAbstractionMethod = AbstractionMethod.MASS_IN_RT_WINDOWS;
	protected double prealignmentPathRefinementRadius = 500;
	protected float prealignmentGrowthFactor = 2.0f;
	protected float prealignmentGrowthFactorLimit = 2.5f;
	protected DynamicProgrammingPathMode alignmentPathMode = DynamicProgrammingPathMode.BOTH;
	
	protected List<Interpolator> resultFunctions = null;
	protected double processingTime = 0.0;

	protected AlignmentMethod method = AlignmentMethod.PARALLEL_FAST_RECURSIVE;

	public AlignmentContext()
	{}
	
	public AlignmentContext forkForPairwiseAlignment(int refRun, int alignedRun)
	{
		AlignmentContext fork = new AlignmentContext();
		fork.alignmentListener = alignmentListener;
		fork.storage = storage;
		fork.maxDeltaMassPPM = maxDeltaMassPPM;
		fork.maxDeltaDrift = maxDeltaDrift;
		fork.rtStepSize = rtStepSize;
		fork.minMass = minMass;
		fork.minInten = minInten;
		fork.smoothRefRT = smoothRefRT;
		fork.maxProcessForkingDepth = maxProcessForkingDepth;
		fork.minPeaksForPrealignment = minPeaksForPrealignment;
		fork.peakAbstractionMethod = peakAbstractionMethod;
		fork.prealignmentPathRefinementRadius = prealignmentPathRefinementRadius;
		fork.alignmentPathMode = alignmentPathMode;
		fork.refRunIndex = refRun;
		fork.alignedRunIndex = alignedRun;
		fork.debugLogDir = debugLogDir;
		fork.debugLogEnabled = debugLogEnabled;
		fork.debugPlotEnabled = debugPlotEnabled;
		return fork;
	}
	
	/**
	 * @return array of matches from resulting functions
	 */
	public List<Integer> getMatchCounts()
	{
		List<Interpolator> fs = getResultFunctions();
		List<Integer> counts = new ArrayList<Integer>(fs.size());
		for ( int i = 0; i < fs.size(); i++ )
			counts.add( fs.get( i ).getOriginalSize() );
		return counts;
	}
	
	public AlignmentContext setAlignmentListener(AlignmentProcessListener listener){alignmentListener=listener; return this;}
	public AlignmentProcessListener getAlignmentListener(){return alignmentListener;}	
	
	/** @param minMass the minMass to set */
	public AlignmentContext setMinMass(double minMass){this.minMass = minMass; return this;}
	/** @return the minMass */
	public double getMinMass(){return minMass;}
	
	/** @param minInten the minInten to set */
	public AlignmentContext setMinIntensity(int minInten){this.minInten = minInten; return this;}
	/** @return the minInten */
	public int getMinIntensity(){return minInten;}
	
	/** @param resultFunctions the resultFunctions to set */
	public AlignmentContext setResultFunctions(List<Interpolator> resultFunctions){this.resultFunctions = resultFunctions; return this;}
	public List<Interpolator> getResultFunctions(){return resultFunctions;}
	
	/** @param maxDeltaMassPPM the max mass difference (in ppm) */
	public AlignmentContext setMaxDeltaMassPPM(double maxDeltaMassPPM){ this.maxDeltaMassPPM = maxDeltaMassPPM; return this;}
	public double getMaxDeltaMassPPM(){return maxDeltaMassPPM;}
	
	/** @param maxDeltaDrift the max drift time (ion mobility) difference */
	public AlignmentContext setMaxDeltaDrift(double maxDeltaDrift){this.maxDeltaDrift = maxDeltaDrift; return this;}
	public double getMaxDeltaDrift(){return maxDeltaDrift;}
	
	/** @param rtStepSize the step size for interpolated alignment results */
	public AlignmentContext setRtStepSize(double rtStepSize){ this.rtStepSize = rtStepSize; return this;}
	public double getRTStepSize(){return rtStepSize ;}
	
	/** @param storage the storage to set */
	public AlignmentContext setStorage(MassSpectrumPeakStorage storage){this.storage = storage; return this;}
	public MassSpectrumPeakStorage getStorage(){return storage;}
	
	/** index of run to be aligned to the reference run */
	public AlignmentContext setAlignedRunIndex(int runIndex){alignedRunIndex=runIndex; return this;}
	public int getAlignedRunIndex(){return alignedRunIndex;}
	
	/** index of reference run */
	public AlignmentContext setReferenceRunIndex(int runIndex){refRunIndex=runIndex; return this;}
	public int getReferenceRunIndex(){return refRunIndex;}

	/** set how long the processing took */
	public AlignmentContext setProcessingTime(double processingTime){ this.processingTime = processingTime;  return this;}
	public double getProcessingTime(){ return processingTime; }	
	
	public AlignmentContext enableSmoothRefRT(boolean smoothRefRT){ this.smoothRefRT = smoothRefRT;  return this;}
	public boolean enabledSmoothRefRT(){ return smoothRefRT; }

	/** multi-threading process forking depth */
	public AlignmentContext setMaxProcessForkingDepth(int maxProcessForkingDepth){ this.maxProcessForkingDepth = maxProcessForkingDepth;  return this;}
	public int getMaxProcessForkingDepth(){ return maxProcessForkingDepth; }

	/** number of peaks for first prealignment */
	public AlignmentContext setPrealignmentMinPeaks( int minPeaksForPrealignment ){ this.minPeaksForPrealignment = minPeaksForPrealignment;  return this;}
	public int getPrealignmentMinPeaks(){ return minPeaksForPrealignment; }
	
	/** select peaks for prealignment by mass (instead of intensity) */
	public AlignmentContext setPrealignmentAbstractionMethod(AbstractionMethod abstractionMethod){this.peakAbstractionMethod = abstractionMethod;return this;}
	public AbstractionMethod getPrealignmentAbstractionMethod(){return peakAbstractionMethod;}

	/** path refinement radius for alignment stages */
	public AlignmentContext setPrealignmentPathRefinementRadius(double radius){ this.prealignmentPathRefinementRadius = radius;  return this;}
	public double getPrealignmentPathRefinementRadius(){ return prealignmentPathRefinementRadius; }

	/** factor for scaling peak sequences for the next iteration of path refinement */
	public float getPrealignmentGrowthFactor(){return prealignmentGrowthFactor;}
	public AlignmentContext setPrealignmentGrowthFactor(float multiplier){this.prealignmentGrowthFactor = multiplier; return this;}
	
	/** the maximum scale factor for the last iteration of path refinement */
	public float getPrealignmentGrowthFactorLimit(){return prealignmentGrowthFactorLimit;}
	public AlignmentContext setPrealignmentGrowthFactorLimit(float maxMultiplier){this.prealignmentGrowthFactorLimit = maxMultiplier; return this;}
	
	/** DP-matrix calculation path mode */
	public AlignmentContext setAlignmentPathMode(DynamicProgrammingPathMode alignmentPathMode){this.alignmentPathMode = alignmentPathMode; return this;}
	public DynamicProgrammingPathMode getAlignmentPathMode(){return alignmentPathMode;}

	/** set the alignment method to be used 	 */
	public AlignmentContext setAlignmentMethod(AlignmentMethod method)	{this.method = method;return this;}
	public AlignmentMethod getAlignmentMethod(){return method;}
	
	/** for debug only */
	public AlignmentContext setDebugLogDir(String dir){ this.debugLogDir = new File(dir); return this; }
	public File getDebugLogDir(){ return debugLogDir; }
	
	public AlignmentContext setDebugLogEnabled(boolean enableLog){ this.debugLogEnabled = enableLog; return this; }
	public boolean getDebugLogEnabled(){ return this.debugLogEnabled; }
	
	public AlignmentContext setDebugPlotEnabled(boolean enablePlot){ this.debugPlotEnabled = enablePlot; return this; }
	public boolean getDebugPlotEnabled(){ return this.debugPlotEnabled; }
}
