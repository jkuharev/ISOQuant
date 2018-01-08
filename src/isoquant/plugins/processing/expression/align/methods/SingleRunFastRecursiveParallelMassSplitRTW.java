/** ISOQuant, isoquant.plugins.processing.expression.align, 12.05.2011 */
package isoquant.plugins.processing.expression.align.methods;

import java.util.List;

import de.mz.jk.jsix.math.interpolation.Interpolator;
import de.mz.jk.ms.align.com.DTWPathDescription;
import de.mz.jk.ms.align.com.IMSPeak;
import de.mz.jk.ms.align.method.dtw.linear.ParallelFastLinearRTW;
import de.mz.jk.ms.align.method.dtw.linear.ParallelLinearRTW;
import isoquant.plugins.processing.expression.align.context.AlignmentContext;

/**
 * <h3>{@link SingleRunFastRecursiveParallelMassSplitRTW}</h3>
 * @author Joerg Kuharev
 * @version 12.05.2011 12:35:03
 */
public class SingleRunFastRecursiveParallelMassSplitRTW extends SingleRunIterativeMassSplitAlignment
{
	/**
	 * @param context the alignment context
	 */
	public SingleRunFastRecursiveParallelMassSplitRTW(AlignmentContext context)
	{
		super(context);
	}

	protected Interpolator alignFull(List<IMSPeak> runPeaks, List<IMSPeak> refPeaks, DTWPathDescription path) throws Exception
	{
		ParallelLinearRTW rtw = new ParallelLinearRTW(runPeaks, refPeaks);
		rtw.setMaxDepth(getContext().getMaxProcessForkingDepth());
		rtw.setMaxDeltaMassPPM(getContext().getMaxDeltaMassPPM());
		rtw.setMaxDeltaDriftTime(getContext().getMaxDeltaDrift());
		rtw.setPathMode(path);
		rtw.run();
		return rtw.getInterpolator(true);
	}

	protected Interpolator alignFast(List<IMSPeak> runPeaks, List<IMSPeak> refPeaks, List<Interpolator> pre, DTWPathDescription path, int radius) throws Exception
	{
		ParallelFastLinearRTW rtw = new ParallelFastLinearRTW(runPeaks, refPeaks);
		rtw.setMaxDepth(getContext().getMaxProcessForkingDepth());
		rtw.setMaxDeltaMassPPM(getContext().getMaxDeltaMassPPM());
		rtw.setMaxDeltaDriftTime(getContext().getMaxDeltaDrift());
		rtw.setPathMode(path);
		rtw.setRadius(radius);

		for ( Interpolator f : pre )
		{
			rtw.addCorridorFunction( f );
		}
		rtw.run();
		return rtw.getInterpolator(true);
	}
}
