/** ISOQuant_DDAreader, isoquant.plugins.processing.expression.align.methods, May 18, 2016*/
package isoquant.plugins.processing.expression.align.methods;

import java.util.Collections;
import java.util.List;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.math.Lowess;
import de.mz.jk.jsix.math.interpolation.Interpolator;
import de.mz.jk.jsix.math.interpolation.LinearInterpolator;
import isoquant.plugins.processing.expression.align.context.AlignmentContext;
import isoquant.plugins.processing.normalization.xy.XYPointList;

/**
 * <h3>{@link SingleRunIdentificationAlignment}</h3>
 * @author jkuharev
 * @version May 18, 2016 4:00:37 PM
 */
public class SingleRunIdentificationAlignment extends SingleRunAlignment
{
	/**
	 * @param alnContext
	 */
	public SingleRunIdentificationAlignment(AlignmentContext alnContext)
	{
		super( alnContext );
	}

	@Override protected List<Interpolator> align()
	{
		// get retention times of commonly identified peptides
		XYPointList points = getContext().getStorage().getPairwiseIdentifiedPeptideRTs( theRun, refRun );
		System.out.println( "	" + theRun + ":		" + points.getPoints().size() + " matches to the reference run " + refRun );

		double[] runRTs = points.getXArray();
		double[] refRTs = points.getYArray();
		// smooth along matches
		double[] smoothedRefRT = Lowess.lowess( runRTs, refRTs, 0.3, 2 );
		// generate linear interpolator by retention times of matching sequences
		// and set interpolator's upper boundary to the next order of magnitude
		double maxRT = getContext().getStorage().getMaxRT();
		double bigNumber = Math.pow( 10, (int)Math.log10( maxRT ) + 2 );
		Interpolator interpolator = new LinearInterpolator( XJava.getDoubleList( runRTs ), XJava.getDoubleList( smoothedRefRT ), 0, 0, bigNumber, bigNumber );

		return Collections.singletonList( interpolator );
	}
}
