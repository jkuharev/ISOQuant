/** ISOQuant, isoquant.plugins.processing.expression.align, 11.05.2011 */
package isoquant.plugins.processing.expression.align.methods;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.mz.jk.jsix.libs.XCSV;
import de.mz.jk.jsix.libs.XFiles;
import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.math.interpolation.Interpolator;
import de.mz.jk.jsix.plot.pt.XYPlotter;
import de.mz.jk.jsix.plot.pt.XYPlotter.PointStyle;
import de.mz.jk.jsix.utilities.Bencher;
import isoquant.app.Defaults;
import isoquant.plugins.processing.expression.align.context.AlignmentContext;

/**
 * <h3>{@link SingleRunAlignment}</h3>
 * @author Joerg Kuharev
 * @version 11.05.2011 15:31:17
 */
public abstract class SingleRunAlignment extends Thread
{
	protected int theRun = 0;
	protected int refRun = 0;
	protected AlignmentContext alignmentContext = null;
	private Bencher timer = new Bencher();

	/**
	 * @param storage
	 * @param runIndex
	 * @param sem
	 */
	public SingleRunAlignment(AlignmentContext alnContext)
	{
		this.alignmentContext = alnContext;
		this.theRun = alnContext.getAlignedRunIndex();
		this.refRun = alnContext.getReferenceRunIndex();
	}

	public void start()
	{
		alignmentContext.getAlignmentListener().beforeAlignment(alignmentContext);
		super.start();
	}

	public void run()
	{
		timer.start();
		
		List<Interpolator> resultFunctions = align(); 
		
		if( alignmentContext.getDebugLogEnabled() )
		{
			for(int i=0; i<resultFunctions.size(); i++)
			{
				logAlnFunc( resultFunctions.get( i ), theRun + "_f" + i );
			}
		}
		
		// align and store results inside of context
		alignmentContext.setResultFunctions( resultFunctions );

		// stop timer and store measured time
		alignmentContext.setProcessingTime( timer.stop().getSec() );
		
		if (Defaults.DEBUG)
		{
			System.out.println( "\t" + theRun + "\talignment approach resulted in [" +
					XJava.joinList( alignmentContext.getMatchCounts(), ", " ) + "] matches." );
			
			System.out.println("\t" + theRun + "\tstoring alignment results ... ");
		}

		alignmentContext.getStorage().storeRTW( alignmentContext );
		
		if (Defaults.DEBUG) System.out.println("\t" + theRun + "\toverall duration is " + timer.stop().getSecString() + ".");

		// notify listener about the 'end of alignment'
		alignmentContext.getAlignmentListener().afterAlignment(alignmentContext);
	}

	/**
	 * alignment of peaks by a single column 
	 * @param rtCol
	 * @return
	 */
	abstract protected List<Interpolator> align();

	public int getAlignedRunIndex()
	{
		return theRun;
	}

	public int getReferenceRunIndex()
	{
		return refRun;
	}

	public AlignmentContext getContext()
	{
		return alignmentContext;
	}

	protected void logAlnFunc(Interpolator func, String fileName)
	{
		List<List<Double>> xy = new ArrayList<List<Double>>();
		xy.add( func.getOriginalX() );
		xy.add( func.getOriginalY() );
		double[][] m = XJava.getDoubleArrayMatrix( xy );
		File logFileFolder = alignmentContext.getDebugLogDir();
		if (!logFileFolder.exists()) XFiles.mkDir( logFileFolder );
		File outFile = new File( logFileFolder, fileName + ".csv" );
		System.out.println( "writing file " + outFile.getAbsolutePath() + " ..." );
		try
		{
			XCSV.writeCSV( outFile, m, ",", ".", "\"", "src,ref".split( "," ), null );
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	private XYPlotter xyp = null;

	protected void plotAlnFunc(Interpolator func, String label, boolean line)
	{
		if (xyp == null)
		{
			xyp = new XYPlotter( 1200, 800 );
			xyp.setPlotTitle( "run " + theRun + " aligned to ref run " + refRun );
			xyp.setXAxisLabel( "source time" );
			xyp.setYAxisLabel( "reference time" );
			xyp.setPointStyle( PointStyle.points );
		}
		xyp.plotXY( func.getOriginalX(), func.getOriginalY(), label, line );
	}

}
