/** ISOQuant, isoquant.plugins.processing.expression.align, 12.05.2011*/
package isoquant.plugins.processing.expression.align.methods;

import java.util.ArrayList;
import java.util.List;

import de.mz.jk.jsix.math.interpolation.Interpolator;
import de.mz.jk.ms.align.com.DTWPathDescription;
import de.mz.jk.ms.align.com.IMSPeak;
import de.mz.jk.ms.align.method.dtw.linear.LinearRTW;
import isoquant.app.Defaults;
import isoquant.plugins.processing.expression.align.context.AlignmentContext;

/**
 * <h3>{@link SingleRunRecursiveRTW}</h3>
 * @author Joerg Kuharev
 * @version 12.05.2011 12:35:03
 */
public class SingleRunRecursiveRTW extends SingleRunAlignment
{
	/**
	 * @param context the alignment context
	 */
	public SingleRunRecursiveRTW(AlignmentContext context)
	{
		super( context );
	}

	@Override protected List<Interpolator> align()
	{
		// align all(filtered) peaks of run using pre-alignment
		List<IMSPeak> refPeaks = getContext().getStorage().getPeaksByMinMassAndIntensity( refRun, alignmentContext.getMinMass(), alignmentContext.getMinIntensity() );
		List<IMSPeak> runPeaks = getContext().getStorage().getPeaksByMinMassAndIntensity( theRun, alignmentContext.getMinMass(), alignmentContext.getMinIntensity() );

		if(Defaults.DEBUG) System.out.println("\t"+theRun + "\taligning " + runPeaks.size() + " x " + refPeaks.size() + " peaks ...");
		
		List<Interpolator> interpolators = new ArrayList<Interpolator>();
		
		LinearRTW rtw = new LinearRTW(runPeaks, refPeaks);
			rtw.setMaxDeltaMassPPM( getContext().getMaxDeltaMassPPM() );
			rtw.setMaxDeltaDriftTime( getContext().getMaxDeltaDrift() );
			rtw.setPathMode( DTWPathDescription.LEFT );
			rtw.run();
			try{interpolators.add( rtw.getInterpolator( true ) );} catch (Exception e){e.printStackTrace();}
		
		rtw = new LinearRTW(runPeaks, refPeaks);
			rtw.setMaxDeltaMassPPM( getContext().getMaxDeltaMassPPM() );
			rtw.setMaxDeltaDriftTime( getContext().getMaxDeltaDrift() );
			rtw.setPathMode( DTWPathDescription.RIGHT );
			rtw.run();
			try{interpolators.add( rtw.getInterpolator( true ) );} catch (Exception e){e.printStackTrace();}
	
		return interpolators;
	}
}
