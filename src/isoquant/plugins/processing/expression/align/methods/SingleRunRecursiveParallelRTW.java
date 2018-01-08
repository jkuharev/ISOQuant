/** ISOQuant, isoquant.plugins.processing.expression.align, 12.05.2011*/
package isoquant.plugins.processing.expression.align.methods;

import java.util.ArrayList;
import java.util.List;

import de.mz.jk.jsix.math.interpolation.Interpolator;
import de.mz.jk.ms.align.com.DTWPathDescription;
import de.mz.jk.ms.align.com.IMSPeak;
import de.mz.jk.ms.align.method.dtw.linear.ParallelLinearRTW;
import isoquant.app.Defaults;
import isoquant.plugins.processing.expression.align.context.AlignmentContext;

/**
 * <h3>{@link SingleRunRecursiveParallelRTW}</h3>
 * @author Joerg Kuharev
 * @version 12.05.2011 12:35:03
 */
public class SingleRunRecursiveParallelRTW extends SingleRunAlignment
{
	/**
	 * @param context the alignment context
	 */
	public SingleRunRecursiveParallelRTW(AlignmentContext context)
	{
		super( context );
	}

	@Override protected List<Interpolator> align()
	{
		// align all(filtered) peaks of run
		List<IMSPeak> refPeaks = getContext().getStorage().getPeaksByMinMassAndIntensity( refRun, alignmentContext.getMinMass(), alignmentContext.getMinIntensity() );
		List<IMSPeak> runPeaks = getContext().getStorage().getPeaksByMinMassAndIntensity( theRun, alignmentContext.getMinMass(), alignmentContext.getMinIntensity() );

		if(Defaults.DEBUG) System.out.println("\t"+theRun + "\taligning " + runPeaks.size() + " x " + refPeaks.size() + " peaks ...");
		
		List<Interpolator> lips = new ArrayList<Interpolator>();
		
		try
		{
			lips.add( align( runPeaks, refPeaks, DTWPathDescription.LEFT ) );
		}
		catch (Exception e)
		{
			System.err.println( e.getMessage() );
		}
		try
		{
			lips.add( align( runPeaks, refPeaks, DTWPathDescription.RIGHT ) );
		}
		catch (Exception e)
		{
			System.err.println( e.getMessage() );
		}
		
		return lips;
	}
	
	private Interpolator align(List<IMSPeak> runPeaks, List<IMSPeak> refPeaks, DTWPathDescription pathMode) throws Exception
	{
		ParallelLinearRTW rtw = new ParallelLinearRTW(runPeaks, refPeaks);
		rtw.setMaxDeltaMassPPM( getContext().getMaxDeltaMassPPM() );
		rtw.setMaxDeltaDriftTime( getContext().getMaxDeltaDrift() );
		rtw.setPathMode( pathMode );
		rtw.run();
		return rtw.getInterpolator(true);
	}
}
