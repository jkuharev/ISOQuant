/** ISOQuant, isoquant.plugins.processing.expression.align.io, 15.11.2011 */
package isoquant.plugins.processing.expression.align.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.mz.jk.jsix.math.SimpleMovingAverage;
import de.mz.jk.jsix.math.interpolation.Interpolator;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.ms.align.com.IMSPeak;
import isoquant.plugins.processing.expression.align.context.AlignmentContext;

/**
 * <h3>{@link PeakStorage}</h3>
 * @author kuharev
 * @version 15.11.2011 16:50:53
 */
public abstract class PeakStorage
{
	public static boolean DEBUG = false;
	protected MySQL db = null;
	protected double minRT = 0;
	protected double maxRT = 130;
	protected List<Integer> rtTypes = Arrays.asList(new Integer[] { 0 });

	public PeakStorage(MySQL _db)
	{
		this.db = _db.clone();
		this.db.getConnection();
	}

	/**
	 * @return the db
	 */
	public MySQL getDB()
	{
		return db;
	}

	public double getMinRT()
	{
		return minRT;
	}

	public double getMaxRT()
	{
		return maxRT;
	}

	/**
	 * recreate RTW table
	 */
	synchronized public void initRTWTable()
	{
		db.executeSQL("DROP TABLE IF EXISTS rtw");
		db.executeSQL("CREATE TABLE rtw (run INT, time DOUBLE, ref_rt DOUBLE, PRIMARY KEY(run, time) )");
	}

	/**
	 * UPDATE clustered_emrt as ce <br>
	 * LEFT JOIN rtw ON ce.`workflow_index`=rtw.`run` AND ce.rt=rtw.rt <br> 
	 * SET ce.ref_rt=rtw.ref_rt
	 */
	synchronized public void commitRTWResults()
	{
		db.executeSQL(
				"UPDATE clustered_emrt as ce " +
						" LEFT JOIN rtw ON ce.`workflow_index`=rtw.`run` AND ce.rt=rtw.time " +
						" SET ce.ref_rt=rtw.ref_rt"
				);
		db.executeSQL("OPTIMIZE TABLE clustered_emrt");
	}

	/**
	 * store RTW results for given run,
	 * each time -> ref_rt is calculated by median of all given result function
	 * @param runIndex
	 * @param interpolators
	 * @param rtStepSize
	 * @param smooth
	 */
	synchronized public void storeRTW(int runIndex, List<Interpolator> interpolators, double rtStepSize, boolean smooth)
	{
		storeRTW(createRTWPeaks(runIndex, interpolators, rtStepSize, smooth));
	}

	/**
	 * store RTW results by using parameters and results from alignment context
	 * @param context
	 */
	synchronized public void storeRTW(AlignmentContext context)
	{
		storeRTW(
				createRTWPeaks(
					context.getAlignedRunIndex(),
					context.getResultFunctions(),
					context.getRTStepSize(),
					context.enabledSmoothRefRT()
				)
		);
	}

	/**
	 * insert a list of peaks into RTW table
	 * @param peaks the list of peaks
	 */
	synchronized public void storeRTW(List<IMSPeak> peaks)
	{
		String table = "`rtw_" + System.currentTimeMillis() + "`";
		db.executeSQL("DROP TABLE IF EXISTS " + table);
		db.executeSQL("CREATE TABLE " + table + " (run INTEGER, time DOUBLE, ref_rt DOUBLE, PRIMARY KEY(run, time) ) ENGINE=MEMORY");
		int bufCnt = 0;
		int bufSize = 200;
		String sqlBuf = "";
		String sqlPrefix = "INSERT INTO " + table + " (`run`,`time`,`ref_rt`) VALUES ";
		for (IMSPeak p : peaks)
		{
			sqlBuf += ((bufCnt > 0) ? "," : "") +
					"('" + p.peak_list_id + "','" + p.rt + "','" + p.ref_rt + "')";
			if (bufCnt++ > bufSize)
			{
				db.executeSQL(sqlPrefix + sqlBuf);
				bufCnt = 0;
				sqlBuf = "";
			}
		}
		if (bufCnt > 0) db.executeSQL(sqlPrefix + sqlBuf);
		db.executeSQL("REPLACE INTO rtw SELECT `run`,`time`,`ref_rt` FROM " + table);
		db.executeSQL("DROP TABLE IF EXISTS " + table);
	}

	/**
	 * reference run has time = ref_rt<br>
	 * for this run we just store all times between 0 and maxRT into RTW table 
	 * @param refIndex
	 */
	synchronized public void storeRefRTW(int refIndex, double rtStepSize)
	{
		List<IMSPeak> peaks = new ArrayList<IMSPeak>();
		// work around for nice numbers due to double errors while adding 0.01
		double rtStep_100 = rtStepSize * 100;
		double maxRT_100 = maxRT * 100 + rtStep_100;
		for ( double rt100 = minRT; rt100 < maxRT_100; rt100 += rtStep_100 )
		{
			IMSPeak p = new IMSPeak();
			p.peak_list_id = refIndex;
			p.rt = p.ref_rt = (float)( rt100 / 100 );
			peaks.add(p);
		}
		storeRTW(peaks);
	}

	protected List<IMSPeak> createRTWPeaks(int runIndex, List<Interpolator> interpolators, double rtStepSize, boolean smooth)
	{
		if (DEBUG) System.out.println("\t" + runIndex + "\tinterpolating alignment results ...");
		List<Double> x = new ArrayList<Double>();
		List<Double> y = new ArrayList<Double>();
		List<IMSPeak> peaks = new ArrayList<IMSPeak>();
		double rtStep_100 = rtStepSize * 100;
		double maxRT_100 = maxRT * 100 + rtStep_100;
		for (double rt100 = minRT; rt100 < maxRT_100; rt100 += rtStep_100)
		{
			double rt = rt100 / 100;
			double ref_rt = Interpolator.getMedianY(interpolators, rt);
			x.add(rt);
			y.add(ref_rt);
		}
		if (smooth)
		{
			if (DEBUG) System.out.println("\t" + runIndex + "\tsmoothing alignment results ...");
			y = SimpleMovingAverage.smooth(y, 5);
		}
		for (int i = 0; i < y.size(); i++)
		{
			IMSPeak p = new IMSPeak();
			p.peak_list_id = runIndex;
			p.rt = x.get( i ).floatValue();
			p.ref_rt = y.get( i ).floatValue();
			peaks.add(p);
		}
		return (peaks);
	}

	/** get all peaks */
	public abstract List<IMSPeak> getAllPeaks(int runIndex);

	/** count all peaks */
	public abstract int countAllPeaks(int runIndex);
}
