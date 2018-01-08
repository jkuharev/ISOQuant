/** ISOQuant, isoquant.plugins.processing.expression.align.methods, 30.06.2014*/
package isoquant.plugins.processing.expression.align.methods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.math.Lowess;
import de.mz.jk.jsix.math.interpolation.Interpolator;
import de.mz.jk.jsix.math.interpolation.LinearInterpolator;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.ms.align.com.DTWPathDescription;
import de.mz.jk.ms.align.com.IMSPeak;
import de.mz.jk.ms.align.com.IMSPeakComparator;
import de.mz.jk.ms.align.com.abstraction.PeakListAbstractor;
import isoquant.app.Defaults;
import isoquant.plugins.processing.expression.align.context.AlignmentContext;

/**
 * <h3>{@link SingleRunIterativeMassSplitAlignment}</h3>
 * @author kuharev
 * @version 30.06.2014 13:25:18
 */
public abstract class SingleRunIterativeMassSplitAlignment extends SingleRunAlignment
{
	private static int numberOfMassWindows = 10;

	public SingleRunIterativeMassSplitAlignment(AlignmentContext alnContext)
	{
		super( alnContext );
	}

	@Override protected List<Interpolator> align()
	{
		List<Interpolator> res = new ArrayList<Interpolator>();

		// find N mass windows for the reference run
		// which will split all peaks into N equally sized parts
		double[][] massWindows = findMassWindows();
		
		List<Double> srcTimes = new ArrayList<>();
		List<Double> refTimes = new ArrayList<>();
				
		// align every mass window separately
		for ( int i = 0; i < numberOfMassWindows; i++ )
		{
			double[] massWindow = massWindows[i];
			List<Interpolator> thisRes = align( massWindow[0], massWindow[1] );
			
			// collect all matches
			for ( int j = 0; j < thisRes.size(); j++ )
			{
				Interpolator f = thisRes.get( j );
				srcTimes.addAll( f.getOriginalX() );
				refTimes.addAll( f.getOriginalY() );
				
				if (alignmentContext.getDebugPlotEnabled())
					plotAlnFunc( f, (int)massWindow[0] + " - " + (int)massWindow[1] + " Da => " + f.getOriginalSize(), false );

				if (alignmentContext.getDebugLogEnabled())
					logAlnFunc( f, theRun + "_" + i + "_" + j );
			}
			
			// store result functions
			res.addAll( thisRes );
		}

		List<Integer> sortedIndices = XJava.getSortOrder( srcTimes, new XJava.AscendingDoubleComparator() );
		double[] srcTimeArray = XJava.getDoubleArray( XJava.subListByIndex( srcTimes, sortedIndices ) );
		double[] refTimeArray = XJava.getDoubleArray( XJava.subListByIndex( refTimes, sortedIndices ) );
		double[] smoothedRefTimeArray = Lowess.lowess( srcTimeArray, refTimeArray, 0.01, 2 );
		int nMatches = srcTimeArray.length;
		srcTimes = XJava.getDoubleList( srcTimeArray );
		refTimes = XJava.getDoubleList( smoothedRefTimeArray );
		double lastRT = Math.max( srcTimeArray[nMatches - 1], refTimeArray[nMatches - 1] );
		// set interpolator's upper boundary to the next order of magnitude
		double bigNumber = Math.pow( 10, (int)Math.log10( lastRT ) + 2 );
		Interpolator smoothedRes = new LinearInterpolator( srcTimes, refTimes, 0, 0, bigNumber, bigNumber );

		if (alignmentContext.getDebugPlotEnabled())
			plotAlnFunc( smoothedRes, "smoothed => nMatches", true );
		
		res.clear();
		res.add( smoothedRes );

		return res;
	}

	private double[][] findMassWindows()
	{
		List<IMSPeak> allRefPeaks = getContext().getStorage().getAllPeaks( refRun );
		Collections.sort( allRefPeaks, IMSPeakComparator.MASS_ASC );
		// count number of peaks in ref run
		int nPeaks = allRefPeaks.size();
		// find breaks for the target number of windows
		int[] breaks = XJava.getBreaks( 0, nPeaks - 1, numberOfMassWindows );
		// store mass windows boundaries
		double[][] massWindows = new double[numberOfMassWindows][];
		for ( int i = 0; i < numberOfMassWindows; i++ )
		{
			float fromMass = allRefPeaks.get( breaks[i] ).mass;
			float toMass = allRefPeaks.get( breaks[i + 1] ).mass;
			massWindows[i] = new double[] { fromMass, toMass };
		}

		// forget peaks & clean memory (just a help for the garbage collector)
		allRefPeaks.clear();

		return massWindows;
	}

	List<Interpolator> align(double minMass, double maxMass)
	{
		List<Interpolator> res = null;

		// download peaks from database
		List<IMSPeak> allRefPeaks = getContext().getStorage().getPeaksInMassWindow( refRun, minMass, maxMass );
		List<IMSPeak> allRunPeaks = getContext().getStorage().getPeaksInMassWindow( theRun, minMass, maxMass );

		// create abstractors
		PeakListAbstractor refPeakAbstractor = getContext().getPrealignmentAbstractionMethod().toPeaksAbstractor( allRefPeaks );
		PeakListAbstractor runPeakAbstractor = getContext().getPrealignmentAbstractionMethod().toPeaksAbstractor( allRunPeaks );

		// count peaks
		int nRefPeaks = allRefPeaks.size();
		int nRunPeaks = allRunPeaks.size();
		
		if(Defaults.DEBUG) System.out.println( "\t" + theRun + ":\taligning " + nRunPeaks + " peaks against " + nRefPeaks + " peaks of the reference run " + refRun + " ... " );
		
		// upper bound for the number of peaks to align
		int nPeaksMax = Math.max( 1, Math.max( nRefPeaks, nRunPeaks ) );
		// lower bound for the number of peaks to align
		int nPeaksMin = Math.min( getContext().getPrealignmentMinPeaks(), nPeaksMax );

		float scale = getContext().getPrealignmentGrowthFactor();
		float maxScale = getContext().getPrealignmentGrowthFactorLimit();

		// absolute maximum threshold for the number of peaks to select
		int nLim = (int)( nPeaksMax * scale );
		// if number of peaks is over this value then select all peaks
		int nUpscale = (int)( nPeaksMax / maxScale );

		for ( int n = nPeaksMin; n < nLim; n = (int)( n * scale ) )
		{
			List<IMSPeak> someRefPeaks, someRunPeaks;
			// avoid next loop if the number of peaks to select is close to
			// the the number of peaks available
			if (n >= nUpscale) n = nPeaksMax;
			if (n < nPeaksMax)
			{
				// refine RTW path by alignment of abstracted peaks lists
				someRefPeaks = refPeakAbstractor.getSortedSubPeaks( n, IMSPeakComparator.RT_ASC );
				someRunPeaks = runPeakAbstractor.getSortedSubPeaks( n, IMSPeakComparator.RT_ASC );
				res = align( someRunPeaks, someRefPeaks, res );
			}
			else
			{
				// finally align all peaks
				res = align( allRunPeaks, allRefPeaks, res );
			}
		}

		return res;
	}

	protected List<Interpolator> align(List<IMSPeak> runPeaks, List<IMSPeak> refPeaks, List<Interpolator> preAlignments)
	{
		List<Interpolator> res = new ArrayList<Interpolator>();
		int refSize = refPeaks.size();
		int runSize = runPeaks.size();
		int maxSize = Math.max( refSize, runSize );

		DTWPathDescription[] pathModes = getContext().getAlignmentPathMode().toDTWPathDescription();
		
		double userRadius = getContext().getPrealignmentPathRefinementRadius();
		int radius = (int)( userRadius * ( userRadius > 1 ? 1.0 : maxSize ) );

		for ( DTWPathDescription pathMode : pathModes )
		{
			try
			{
				Interpolator alnFunc = null;
				String status = "\t" + theRun + ":\taligning " + runSize + " x " + refSize + " peaks, path mode: " + pathMode + ", method: ";
				Bencher t = new Bencher( true );
				if (preAlignments != null && preAlignments.size() > 0)
				{
					status += "FAST, radius: " + radius;
					if (Defaults.DEBUG) System.out.println( status + " ... " );
					alnFunc = alignFast( runPeaks, refPeaks, preAlignments, pathMode, radius );
				}
				else
				{
					status += "FULL";
					if (Defaults.DEBUG) System.out.println( status + " ... " );
					alnFunc = alignFull( runPeaks, refPeaks, pathMode );
				}
				res.add( alnFunc );
				int nMatches = alnFunc.getOriginalSize();
				status += " ==> duration: " + t.stop().getSecString() + ", result: " + nMatches + " matches!";
				if (Defaults.DEBUG) System.out.println( status + " ... " );
			}
			catch (Exception e)
			{
				System.err.println( e.getMessage() );
			}
		}

		return res;
	}

	/**
	 * @param runPeaks
	 * @param refPeaks
	 * @param left
	 * @return
	 * @throws Exception 
	 */
	protected abstract Interpolator alignFull(List<IMSPeak> runPeaks, List<IMSPeak> refPeaks, DTWPathDescription pathMode) throws Exception;

	/**
	 * @param runPeaks
	 * @param refPeaks
	 * @param preAlignments
	 * @param left
	 * @param radius
	 * @return
	 * @throws Exception 
	 */
	protected abstract Interpolator alignFast(List<IMSPeak> runPeaks, List<IMSPeak> refPeaks, List<Interpolator> preAlignments, DTWPathDescription pathMode, int radius) throws Exception;
}
