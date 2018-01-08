/** ISOQuant, isoquant.plugins.processing.expression.align.io, 28.06.2011 */
package isoquant.plugins.processing.expression.align.io;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.mz.jk.jsix.math.interpolation.Interpolator;
import de.mz.jk.jsix.math.interpolation.LinearInterpolator;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.ms.align.com.IMSPeak;

/**
 * <h3>{@link RTWToMedianAdjuster}</h3>
 * @author Joerg Kuharev
 * @version 28.06.2011 13:49:34
 */
public class RTWToMedianAdjuster implements Runnable
{
	public static void main(String[] args)
	{
		MySQL db = new MySQL("localhost:3307", "Proj__12465518426020_3564317579743981_IQ_1", "root", "", true);
		RTWToMedianAdjuster rtwn = new RTWToMedianAdjuster(db);
		rtwn.run();
	}
	private boolean DEBUG = true;
	private MySQL db = null;
	private List<Integer> runIndexes = null;
	private double minRefRT = 0;
	private double maxRefRT = 130;
	private double rtStep = 0.01;

	public RTWToMedianAdjuster(MySQL db)
	{
		this.db = db;
	}

	public void run()
	{
		db.optimizeTable("rtw");
		runIndexes = getRunIndexes();
		minRefRT = Math.min(minRefRT, Double.parseDouble(db.getFirstValue("rtw", "MIN(ref_rt)", null)));
		maxRefRT = Math.max(maxRefRT, Double.parseDouble(db.getFirstValue("rtw", "MAX(ref_rt)", null)));
		Bencher tall = new Bencher().start();
		System.out.println("calculating retention time alignment adjustment for " + runIndexes.size() + " runs ...");
		// make refrtw table
		if (DEBUG) System.out.println("\tpreparing refrtw table ...");
		initRefRTWTable();
		// for each run
		for (int runIndex : runIndexes)
		{
			Bencher t = new Bencher().start();
			System.out.print("\t" + runIndex + "\t...\t");
			// calculate refrtw for this run
			calcRefRTWValues(runIndex);
			System.out.println(t.stop().getSecString());
		}
		// calculate medians and create ref_rt_medians table
		if (DEBUG) System.out.println("\tcalculating medians ...");
		makeRefRTWMedians();
		// correct rtw.ref_rt
		System.out.println("\tadjusting reference retention time values...");
		adjustRTW();
		System.out.println("retention time alignment adjustment duration: " + tall.stop().getSecString());
	}

	/**
	 * 
	 */
	private void adjustRTW()
	{
		// backup rtw table
		if (DEBUG) db.cloneTable( "rtw", "rtw_backup_" + System.currentTimeMillis() + "", true );
		db.executeSQL(
				"UPDATE rtw LEFT JOIN `ref_rt_medians` AS rtm ON ROUND(rtw.ref_rt, 2)=rtm.ref_rt " +
						" SET rtw.ref_rt = rtw.ref_rt - rtm.drt_median "
				);
	}

	/** ref_rt_medians( ref_rt DOUBLE, drt_median DOUBLE ) */
	private void makeRefRTWMedians()
	{
		db.dropTable("ref_rt_medians");
		db.executeSQL(
				"CREATE TABLE ref_rt_medians " +
						"SELECT ref_rt, ROUND((" +
						" substring_index( substring_index( group_concat( drt ORDER BY drt ) , ',', ceiling( COUNT( * ) /2 ) ) , ',' , -1 ) + " +
						" substring_index( substring_index( group_concat( drt ORDER BY drt ) , ',' , - ceiling( COUNT( * ) /2 ) ) , ',', 1 ) " +
						") / 2, 4) AS drt_median " +
						"FROM `refrtw` GROUP BY `ref_rt`"
				);
		db.createIndex("ref_rt_medians", "ref_rt");
	}

	private void calcRefRTWValues(int runIndex)
	{
		Interpolator li = getRef2RTInterpolator(runIndex);
		String table = "rtw_" + runIndex + "_" + System.currentTimeMillis() + "";
		db.dropTable(table);
		db.executeSQL( "CREATE TEMPORARY TABLE `" + table + "` ( ref DOUBLE, ret DOUBLE, PRIMARY KEY(ref) ) ENGINE=MEMORY" );
		int bufCnt = 0;
		int bufSize = 200;
		String sqlBuf = "";
		String sqlPrefix = "INSERT INTO `" + table + "` (`ref`,`ret`) VALUES ";
		double rtStep_100 = rtStep * 100;
		double maxRefRT_100 = maxRefRT * 100 + rtStep_100;
		for (double rt100 = minRefRT; rt100 < maxRefRT_100; rt100 += rtStep_100)
		{
			double ref_rt = rt100 / 100;
			double rt = li.getY(ref_rt);
			sqlBuf += ((bufCnt == 0) ? "" : ",") + "('" + ref_rt + "','" + rt + "')";
			if (bufCnt++ > bufSize)
			{
				db.executeSQL(sqlPrefix + sqlBuf);
				bufCnt = 0;
				sqlBuf = "";
			}
		}
		if (bufCnt > 0) db.executeSQL(sqlPrefix + sqlBuf);
		db.executeSQL("REPLACE INTO refrtw SELECT " + runIndex + " as run, ref as ref_rt, (ref-ret) as drt FROM `" + table + "`");
		db.dropTable(table);
	}

	private LinearInterpolator getRef2RTInterpolator(int runIndex)
	{
		List<Double> ret = new ArrayList<Double>();
		List<Double> ref = new ArrayList<Double>();
		ResultSet rs = db.executeSQL("SELECT time, ref_rt FROM rtw WHERE run='" + runIndex + "' ORDER BY ref_rt ASC");
		try
		{
			while (rs.next())
			{
				ret.add(rs.getDouble("time"));
				ref.add(rs.getDouble("ref_rt"));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		LinearInterpolator res = new LinearInterpolator(ref, ret, 0, 0, 1000, 1000);
		return res;
	}

	/**
	 * recreate RTW table
	 */
	synchronized public void initRefRTWTable()
	{
		db.dropTable("refrtw");
		db.executeSQL("CREATE TABLE refrtw (run INT, ref_rt DOUBLE, drt DOUBLE, PRIMARY KEY(run, ref_rt) )");
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
	 * all run indexes in descending order
	 * @return list of indexes
	 */
	synchronized public List<Integer> getRunIndexes()
	{
		ArrayList<Integer> res = new ArrayList<Integer>();
		ResultSet rs = db.executeSQL("SELECT DISTINCT `run` FROM rtw ORDER BY run ASC");
		try
		{
			while (rs.next())
			{
				res.add(rs.getInt(1));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return res;
	}
}
