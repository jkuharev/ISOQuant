/** ISOQuant, isoquant.plugins.processing.expression.align, 12.05.2011*/
package isoquant.plugins.processing.expression.align.methods;

import isoquant.app.Defaults;
import isoquant.plugins.processing.expression.align.context.AlignmentContext;

import java.util.List;

import de.mz.jk.jsix.math.interpolation.Interpolator;
import de.mz.jk.ms.align.com.DTWPathDescription;
import de.mz.jk.ms.align.com.IMSPeak;
import de.mz.jk.ms.align.method.dtw.linear.FastLinearRTW;
import de.mz.jk.ms.align.method.dtw.linear.ParallelLinearRTW;

/**
 * <h3>{@link SingleRunFastRecursiveRTW}</h3>
 * @author Joerg Kuharev
 * @version 12.05.2011 12:35:03
 */
public class SingleRunFastRecursiveRTW extends SingleRunIterativeAlignment
{
	/**
	 * @param context the alignment context
	 */
	public SingleRunFastRecursiveRTW(AlignmentContext context) //, int maxPeaksForPrealignment)
	{
		super( context );
	}

	protected Interpolator alignFull(List<IMSPeak> runPeaks, List<IMSPeak> refPeaks, DTWPathDescription path) throws Exception
	{
		ParallelLinearRTW rtw = new ParallelLinearRTW(runPeaks, refPeaks);
		rtw.setMaxDepth( Defaults.availableProcessors - 1 );
		rtw.setMaxDeltaMassPPM( getContext().getMaxDeltaMassPPM() );
		rtw.setMaxDeltaDriftTime( getContext().getMaxDeltaDrift() );
		rtw.setPathMode( path );
		rtw.run();
		return rtw.getInterpolator(true);
	}
	
	protected Interpolator alignFast(List<IMSPeak> runPeaks, List<IMSPeak> refPeaks, List<Interpolator> pre, DTWPathDescription path, int radius) throws Exception
	{
		FastLinearRTW rtw = new FastLinearRTW(runPeaks, refPeaks);
		rtw.setMaxDeltaMassPPM( getContext().getMaxDeltaMassPPM() );
		rtw.setMaxDeltaDriftTime( getContext().getMaxDeltaDrift() );
		rtw.setPathMode( path );
		rtw.setRadius(radius);
		for(Interpolator f : pre) rtw.addCorridorFunction(f);
		rtw.run();
		return rtw.getInterpolator(true);
	}
}
