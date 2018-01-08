/** ISOQuant, isoquant.plugins.processing.expression.align.methods, 30.06.2014*/
package isoquant.plugins.processing.expression.align.methods;

import java.util.ArrayList;
import java.util.List;

import de.mz.jk.jsix.math.interpolation.Interpolator;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.ms.align.com.DTWPathDescription;
import de.mz.jk.ms.align.com.IMSPeak;
import de.mz.jk.ms.align.com.IMSPeakComparator;
import de.mz.jk.ms.align.com.abstraction.PeakListAbstractor;
import isoquant.app.Defaults;
import isoquant.plugins.processing.expression.align.context.AlignmentContext;

/**
 * <h3>{@link SingleRunIterativeAlignment}</h3>
 * @author kuharev
 * @version 30.06.2014 13:25:18
 */
public abstract class SingleRunIterativeAlignment extends SingleRunAlignment
{
	public SingleRunIterativeAlignment(AlignmentContext alnContext)
	{
		super( alnContext );
	}

	@Override protected List<Interpolator> align()
	{
		List<Interpolator> res = null;
		double minMass = alignmentContext.getMinMass();
		int minIntensity = alignmentContext.getMinIntensity();

		// download peaks from database
		List<IMSPeak> allRefPeaks = getContext().getStorage().getPeaksByMinMassAndIntensity( refRun, minMass, minIntensity );
		List<IMSPeak> allRunPeaks = getContext().getStorage().getPeaksByMinMassAndIntensity( theRun, minMass, minIntensity );

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

// String matches = "";
// List<Integer> nMatches = new ArrayList<Integer>( res.size() );
// for ( Interpolator i : res ) if(i!=null) nMatches.add( Math.max(
// i.getOriginalSize(), 0) );
// matches = XJava.joinList( nMatches, "," );
// // if (Defaults.DEBUG) System.out.println( "\t" + theRun + ":\t(" +
// // matches + ") matches." );
// if (Defaults.DEBUG) System.out.println( status + " ==> " + matches + "
// matches" );
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
