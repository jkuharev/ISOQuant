/** ISOQuant, isoquant.plugins.processing.expression.test, 10.11.2011 */
package isoquant.plugins.processing.expression.test;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.math.interpolation.Interpolator;
import de.mz.jk.jsix.math.interpolation.LinearInterpolator;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.plot.pt.SQLPlotter;
import de.mz.jk.jsix.plot.pt.XYPlotter;
import de.mz.jk.ms.align.com.DTWPathDescription;
import de.mz.jk.ms.align.com.IMSPeak;
import de.mz.jk.ms.align.method.dtw.linear.ParallelFastLinearRTW;
import de.mz.jk.ms.align.method.dtw.linear.ParallelLinearRTW;
import isoquant.app.Defaults;
import isoquant.plugins.processing.expression.align.MultiRunAlignmentProcedure;
import isoquant.plugins.processing.expression.align.context.AlignmentMethod;
import isoquant.plugins.processing.expression.align.context.AlignmentMode;

/**
 * <h3>{@link SampleConsensusAligner}</h3>
 * @author kuharev
 * @version 10.11.2011 14:56:49
 */
public class SampleConsensusAligner implements Runnable
{
	public final static String sql_file_sample_consensus_creation =
			XJava.getPackageResource(SampleConsensusAligner.class, "create_sample_consensus_runs.sql");
	private static final boolean DEBUG = true;

	public static void main(String[] args)
	{
		MySQL db = new MySQL("localhost:3307", "Proj__12777144136060_2980310262486249_IQ_1", "root", "", false);
		new SampleConsensusAligner(db).run();
	}
	private MySQL db = null;
	private SampleConsensusPeakStorage storage = null;
	private final double rtStepSize = 0.01;

	public SampleConsensusAligner(MySQL _db)
	{
		storage = new SampleConsensusPeakStorage(_db);
		db = storage.getDB();
	}

	@Override public void run()
	{
		db.getConnection();
// --------------------------------------------------------------------------------
// // capture normal old alignment
// // do IN_PROJECT alignment
// MultiRunAlignmentProcedure a = new MultiRunAlignmentProcedure( db );
// a.setAlignmentMethod(AlignmentMethod.PARALLEL_FAST_RECURSIVE);
// a.setAlignmentMode(AlignmentMode.IN_PROJECT);
// a.setNormalizeRefRT(false);
// a.setRTColsForConsensus(new RTColumn[]{RTColumn.RT});
// a.setPoolConsensusAlignment(false);
// a.run();
//
// // backup rtw -> in_project_rtw
// db.cloneTable("rtw", "in_project_rtw");
// plotRTW("in project");
// --------------------------------------------------------------------------------
		// do IN_SAMPLE alignment
		{
			MultiRunAlignmentProcedure a = new MultiRunAlignmentProcedure(db);
			a.setAlignmentMethod(AlignmentMethod.PARALLEL_FAST_RECURSIVE);
			a.setAlignmentMode(AlignmentMode.IN_SAMPLE);
			a.setNormalizeRefRT(false);
// a.setRTColsForConsensus(new RTColumn[] { RTColumn.RT });
// a.setPoolConsensusAlignment(false);
			a.run();
		}
		// backup rtw -> in_sample_rtw
		db.cloneTable( "rtw", "in_sample_rtw", true );
// // do in-sample clustering
// ClusteringProcedure c = new ClusteringProcedure(db);
// c.setMode(PreClusteringMode.IN_SAMPLE);
// c.setParallelizationDegree(8);
// c.run();
//
		// create sample-consensus-runs -> sample_emrt
		db.executeSQLFile(sql_file_sample_consensus_creation, db.defExecListener);
		List<Integer> samples = db.getIntegerValues(
				"SELECT sample_index, COUNT(`index`) as size FROM sample_emrt " +
						"GROUP BY sample_index ORDER BY size DESC"
				);
		// initRTW
		storage.initRTWTable();
		int ref = samples.get(0);
// storage.storeRefRTW(ref, rtStepSize);
		storeRefSample(ref);
		for (int s : samples)
		{
			if (s != ref)
			{
				try
				{
				List<Interpolator> fs = align(ref, s);
				correctRuns(s, fs);
				}
				catch (Exception e)
				{}
			}
		}
		// backup rtw -> between_sample_rtw
		// merge in-sample and between-sample rtw
		// correct clustered_emrt
		storage.commitRTWResults();
		plotRTW("in-sample + between-sample");
		db.closeConnection();
	}

	/**
	 * 
	 */
	private void plotRTW(String title)
	{
		SQLPlotter plotter = new SQLPlotter(db);
		plotter.setPointStyle(XYPlotter.PointStyle.points);
		plotter.setPlotTitle(title);
		List<Integer> runIndexes = db.getIntegerValues("rtw", "DISTINCT run", null);
		for (int i : runIndexes)
		{
			plotter.plot("SELECT time, (ref_rt-time) as `ref_rt - time for run_" + i + "` FROM rtw WHERE run=" + i + " ORDER BY time ASC", true);
		}
		plotter.getDB().closeConnection();
	}

	/**
	 * 
	 */
	private void alignSampleRuns()
	{
	}

	/**
	 * copy unchanged rtw for runs of reference sample 
	 * @param ref
	 */
	private void storeRefSample(int ref)
	{
		List<Integer> runs = db.getIntegerValues("SELECT `index` FROM workflow WHERE sample_index=" + ref);
		for (int r : runs)
		{
			db.executeSQL("INSERT INTO rtw SELECT * FROM in_sample_rtw WHERE run=" + r);
		}
	}

	/**
	 * correct rtw for runs of sample
	 * @param s
	 * @param fs
	 */
	private void correctRuns(int s, List<Interpolator> fs)
	{
		// list runs for sample s
		List<Integer> runs = db.getIntegerValues("SELECT `index` FROM workflow WHERE sample_index=" + s);
		for (int r : runs)
		{
			// get RTW for run
			List<IMSPeak> peaks = getRTWPeaks(r);
			for (IMSPeak p : peaks)
			{
				p.ref_rt = (float)LinearInterpolator.getMedianY( fs, p.ref_rt );
			}
			storage.storeRTW(peaks);
		}
	}

	private List<IMSPeak> getRTWPeaks(int runIndex)
	{
		List<IMSPeak> res = new ArrayList<IMSPeak>();
		try
		{
			ResultSet rs = db.executeSQL("SELECT time, ref_rt FROM in_sample_rtw WHERE run=" + runIndex + " ORDER BY time ASC");
			while (rs.next())
			{
				IMSPeak p = new IMSPeak();
				p.rt = rs.getFloat( "time" );
				p.ref_rt = rs.getFloat( "ref_rt" );
				p.peak_list_id = runIndex;
				res.add(p);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}
	private int nPeaksForLevel1 = 1000;
	private int nPeaksForLevel2 = 5000;

	private List<Interpolator> align(int refIndex, int samIndex) throws Exception
	{
		List<Interpolator> res = new ArrayList<Interpolator>();
		List<Interpolator> pre1 = new ArrayList<Interpolator>();
		List<Interpolator> pre2 = new ArrayList<Interpolator>();
		/* pre-align top N peaks of runs */
// prealign with 1000x1000
		List<IMSPeak> refPeaks = getPeaks(refIndex, nPeaksForLevel1);
		List<IMSPeak> samPeaks = getPeaks(samIndex, nPeaksForLevel1);
		if (DEBUG)
			System.out.println("\t" + samIndex + "\tprealigning by " + samPeaks.size() + " x " + refPeaks.size() + " peaks ...");
		pre1.add(alignFull(samPeaks, refPeaks, DTWPathDescription.LEFT));
		pre1.add(alignFull(samPeaks, refPeaks, DTWPathDescription.RIGHT));
// prealign with 5000x5000
		refPeaks = getPeaks(refIndex, nPeaksForLevel2);
		samPeaks = getPeaks(samIndex, nPeaksForLevel2);
		if (DEBUG)
			System.out.println("\t" + samIndex + "\tprealigning by " + samPeaks.size() + " x " + refPeaks.size() + " peaks ...");
		pre2.add(alignFast(samPeaks, refPeaks, pre1, DTWPathDescription.LEFT, 500));
		pre2.add(alignFast(samPeaks, refPeaks, pre1, DTWPathDescription.RIGHT, 500));
// align all peaks
		refPeaks = getPeaks(refIndex, Integer.MAX_VALUE);
		samPeaks = getPeaks(samIndex, Integer.MAX_VALUE);
		if (DEBUG)
			System.out.println("\t" + samIndex + "\taligning " + samPeaks.size() + " x " + refPeaks.size() + " peaks ...");
		res.add(alignFast(samPeaks, refPeaks, pre2, DTWPathDescription.LEFT, 500));
		res.add(alignFast(samPeaks, refPeaks, pre2, DTWPathDescription.RIGHT, 500));
		return res;
	}

	/**
	 * get maximum nPeaks from given sample
	 * @param sampleIndex
	 * @param nPeaks
	 * @return
	 */
	private List<IMSPeak> getPeaks(int sampleIndex, int nPeaks)
	{
		ResultSet rs = db.executeSQL(
				"SELECT * FROM " +
						"(SELECT `index`, `sample_index`, mass, time " +
						" FROM `sample_emrt` WHERE `sample_index`=" + sampleIndex +
						" ORDER BY `inten` DESC LIMIT " + nPeaks + ") as x ORDER BY time ASC"
				);
		List<IMSPeak> res = new ArrayList<IMSPeak>();
		try
		{
			while (rs.next())
			{
				IMSPeak p = new IMSPeak(
						rs.getInt("index"),
						rs.getFloat( "mass" ),
						rs.getFloat( "time" )
						);
				res.add(p);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}

	private Interpolator alignFull(List<IMSPeak> runPeaks, List<IMSPeak> refPeaks, DTWPathDescription path) throws Exception
	{
		ParallelLinearRTW rtw = new ParallelLinearRTW(runPeaks, refPeaks);
		rtw.setMaxDepth(Defaults.availableProcessors - 1);
		rtw.setMaxDeltaMassPPM(10.0);
		rtw.setMaxDeltaDriftTime(2.0);
		rtw.setPathMode(path);
		rtw.run();
		return rtw.getInterpolator(true);
	}

	private Interpolator alignFast(List<IMSPeak> runPeaks, List<IMSPeak> refPeaks, List<Interpolator> pre1, DTWPathDescription path, int radius) throws Exception
	{
		ParallelFastLinearRTW rtw = new ParallelFastLinearRTW(runPeaks, refPeaks);
		rtw.setMaxDepth(Defaults.availableProcessors - 1);
		rtw.setMaxDeltaMassPPM(10.0);
		rtw.setMaxDeltaDriftTime(2.0);
		rtw.setPathMode(path);
		rtw.setRadius(radius);
		for (Interpolator f : pre1)
			rtw.addCorridorFunction(f);
		rtw.run();
		return rtw.getInterpolator(true);
	}
}
