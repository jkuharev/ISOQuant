/** ISOQuant, isoquant.plugins.processing.expression.align, 12.05.2011*/
package isoquant.plugins.processing.expression.align.methods;

import java.util.List;

import de.mz.jk.jsix.math.interpolation.Interpolator;
import de.mz.jk.ms.align.com.DTWPathDescription;
import de.mz.jk.ms.align.com.IMSPeak;
import de.mz.jk.ms.align.method.dtw.FullRTW;
import de.mz.jk.ms.align.method.dtw.QuickRTW;
import de.mz.jk.ms.align.method.dtw.RTW;
import isoquant.plugins.processing.expression.align.context.AlignmentContext;

/**
 * <h3>{@link SingleRunIterativeRTW}</h3>
 * @author Joerg Kuharev
 * @version 12.05.2011 12:35:03
 */
public class SingleRunIterativeRTW extends SingleRunIterativeAlignment
{
	/**
	 * @param context the alignment context
	 */
	public SingleRunIterativeRTW(AlignmentContext context) //, int maxPeaksForPrealignment)
	{
		super( context );
	}

	private RTW lastRTW = null;
	private int lastRTWPeakSize = 0;

	@Override protected Interpolator alignFull(List<IMSPeak> runPeaks, List<IMSPeak> refPeaks, DTWPathDescription pathMode) throws Exception
	{
		int rtwPeakSize = runPeaks.size() + refPeaks.size();
		FullRTW rtw = null;
		if (lastRTW instanceof FullRTW && lastRTW != null && lastRTWPeakSize == rtwPeakSize)
		{
			rtw = (FullRTW)lastRTW;
		}
		else
		{
			rtw = new FullRTW( runPeaks, refPeaks );
			rtw.setMaxDeltaMassPPM( getContext().getMaxDeltaMassPPM() );
			rtw.setMaxDeltaDriftTime( getContext().getMaxDeltaDrift() );
			rtw.run();
			lastRTW = rtw;
		}
		rtw.setPathMode( pathMode );
		return rtw.getInterpolator( true );
	}

	@Override protected Interpolator alignFast(List<IMSPeak> runPeaks, List<IMSPeak> refPeaks, List<Interpolator> preAlignments, DTWPathDescription pathMode, int radius) throws Exception
	{
		int rtwPeakSize = runPeaks.size() + refPeaks.size();
		QuickRTW rtw = null;
		if (lastRTW instanceof QuickRTW && lastRTW != null && lastRTWPeakSize == rtwPeakSize)
		{
			rtw = (QuickRTW)lastRTW;
		}
		else
		{
			rtw = new QuickRTW( runPeaks, refPeaks );
			rtw.setMaxDeltaMassPPM( getContext().getMaxDeltaMassPPM() );
			rtw.addCorridorFunctions( preAlignments );
			rtw.run();
			lastRTW = rtw;
		}
		rtw.setPathMode( pathMode );
		return rtw.getInterpolator( true );
	}
}
