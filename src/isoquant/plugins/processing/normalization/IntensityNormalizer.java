package isoquant.plugins.processing.normalization;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.math.Lowess;
import de.mz.jk.jsix.math.interpolation.Interpolator;
import de.mz.jk.jsix.math.interpolation.LinearInterpolator;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.mysql.StdOutSQLBatchExecutionAdapter;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.jsix.utilities.Settings;
import de.mz.jk.plgs.data.ClusteredEMRT;
import isoquant.app.Defaults;
import isoquant.interfaces.iMainApp;
import isoquant.interfaces.log.iLogManager;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.log.LogEntry;
import isoquant.kernel.plugin.SingleActionPlugin4DB;
import isoquant.plugins.processing.normalization.xy.XYPointList;

/**
 * <h3>{@link IntensityNormalizer}</h3>
 * 
 * Normalization of EMRT intensities based on locally weighted polynomial regression 
 * using LOESS (or LOWESS locally weighted scatter plot smoothing)
 * <hr> 
 * normalization is performed to correct bias depending on intensity, time, mass levels 
 * <hr>
 * Normalization standards are calculated by using emrt cluster data over all runs within a project 
 * 
 * @author Joerg Kuharev
 * @version 18.01.2011 15:28:58
 *
 */
public class IntensityNormalizer extends SingleActionPlugin4DB
{
	private MySQL db = null;

	/**
	 * <h3>NormalizationMode</h3>
	 * mode of normalization approach
	 * @author Joerg Kuharev
	 * @version 19.01.2011 15:56:16
	 */
	public enum NormalizationMode
	{
		IN_PROJECT_MEAN,
		IN_GROUP_MEAN,
		IN_SAMPLE_MEAN,
		RUN_VALUE;
	}

	/**
	 * <h3>NormalizationDimension</h3>
	 * dimension of intensity bias 
	 * @author Joerg Kuharev
	 * @version 19.01.2011 15:57:20
	 */
	public enum NormalizationDimension
	{
		rt
		{
			public String toString()
			{
				return "ref_rt";
			}
		},
		inten
		{
			public String toString()
			{
				return "LOG2(cor_inten)";
			}
		},
		mass
		{
			public String toString()
			{
				return "mass";
			}
		};
	}
	private NormalizationMode norMode = NormalizationMode.IN_PROJECT_MEAN;
	private double OPTION_LOWESS_BANDWIDTH = 0.3;
	private String OPTION_ORDER_SEQUENCE = "XPIR";
	private boolean OPTION_LEAVE_TEMPORARY_TABLES = false;
	private int OPTION_MIN_INTENSITY_CUTOFF = 3000;
	/** 
		constant value of ln(2) calculated by Math.log(2) <br>
		usage: log2(x)=Math.log(x)/ln2
	 */
	public final double ln2 = Math.log(2);
	private String[] tables = new String[] {
			"clustered_emrt", "peptide", "protein",
			"query_mass", "low_energy", "cluster_average",
			"workflow", "sample", "group", "expression_analysis", "project"
	};
	private List<Integer> workflowIndices = null;

	public IntensityNormalizer(iMainApp app)
	{
		super(app);
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		db = p.mysql;
		progressListener.setStatus( "normalizing '" + p.data.title + "'" );
		logParameters(p.log);
		Bencher t = new Bencher().start();
		normalize();
		System.out.println("whole normalization duration: " + t.stop().getSec() + "s");
		p.log.add(LogEntry.newEvent("emrt intensities normalized", "normalization duration=" + t.getSecString()));
		progressListener.setStatus("normalization done");
	}

	@Override public String getMenuItemIconName()
	{
		return "normalize";
	}

	@Override public String getMenuItemText()
	{
		return "normalize emrt intensity";
	}

	@Override public String getPluginName()
	{
		return "Multidimensional EMRT Intensity Normalizer";
	}

	// ----------------------------------------------------------------------------
	private void optimizeTables()
	{
		System.out.print("optimizing tables ...");
		Bencher t = new Bencher().start();
		for (String table : tables)
			db.optimizeTable(table);
		System.out.println("\t" + t.stop().getTime(Bencher.SECONDS) + "s");
	}

	// ----------------------------------------------------------------------------
	@Override public void loadSettings(Settings cfg)
	{
		OPTION_LOWESS_BANDWIDTH = cfg.getDoubleValue("process.normalization.lowess.bandwidth", OPTION_LOWESS_BANDWIDTH, false);
		OPTION_ORDER_SEQUENCE = cfg.getStringValue("process.normalization.orderSequence", OPTION_ORDER_SEQUENCE, false);
		OPTION_MIN_INTENSITY_CUTOFF = cfg.getIntValue("process.normalization.minIntensity", OPTION_MIN_INTENSITY_CUTOFF, false);
		OPTION_LEAVE_TEMPORARY_TABLES = cfg.getBooleanValue("process.normalization.keepTemporaryTables", OPTION_LEAVE_TEMPORARY_TABLES, !Defaults.DEBUG);
	}

	// ----------------------------------------------------------------------------
	private void logParameters(iLogManager log)
	{
		log.add(LogEntry.newParameter("process.normalization.lowess.bandwidth", OPTION_LOWESS_BANDWIDTH + ""));
		log.add(LogEntry.newParameter("process.normalization.orderSequence", OPTION_ORDER_SEQUENCE + ""));
		log.add(LogEntry.newParameter("process.normalization.minIntensity", OPTION_MIN_INTENSITY_CUTOFF));
	}

	// ----------------------------------------------------------------------------
	/**
	 * normalization batch
	 */
	public void normalize()
	{
		changeMode(NormalizationMode.IN_PROJECT_MEAN);
		try
		{
			char[] order = OPTION_ORDER_SEQUENCE.toLowerCase().toCharArray();
			for (int i = 0; i < order.length; i++)
			{
				String resTab = "";
				switch (order[i])
				{
					case 'w':
						changeMode(NormalizationMode.RUN_VALUE);
						break;
					case 's':
						changeMode(NormalizationMode.IN_SAMPLE_MEAN);
						break;
					case 'p':
						changeMode(NormalizationMode.IN_PROJECT_MEAN);
						break;
					case 'g':
						changeMode(NormalizationMode.IN_GROUP_MEAN);
						break;
					case 'i':
						resTab = correctDimension(NormalizationDimension.inten);
						break;
					case 'r':
						resTab = correctDimension(NormalizationDimension.rt);
						break;
					case 'm':
						resTab = correctDimension(NormalizationDimension.mass);
						break;
					case 'x':
						progressListener.setMessage("resetting intensity values ...");
						resetCorrectedIntensities();
						break;
					case 'e':
						progressListener.setMessage("equalizing intensity values ...");
						db.executeSQLFile( getPackageResource( "sql/equalize_inten.sql" ), null );
						break;
					default:
						continue;
				}
				if (Defaults.DEBUG)
					addTrace(0, OPTION_ORDER_SEQUENCE, order[i], i, resTab);
				else if (!OPTION_LEAVE_TEMPORARY_TABLES && resTab.length() > 0)
					db.dropTable( resTab );
			}
			optimizeTables();
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(
					app.getGUI(),
					"Normalization run caused an error!\n\n" +
							"See command line output for details.",
					"Normalization failed!",
					JOptionPane.ERROR_MESSAGE
					);
			e.printStackTrace();
		}
		db.getConnection();
	}

	// ----------------------------------------------------------------------------
	/**
	 * correcting Xth bias 
	 */
	private String correctDimension(NormalizationDimension dim)
	{
		workflowIndices = getWorkflowIndeces();
		progressListener.setMessage("running " + dim.toString() + " based normalization for " + workflowIndices.size() + " workflows ...");
		optimizeTables();
		Bencher td = new Bencher().start();
		String dimName = dim.toString();
		prepareReference();
		String emrtTableName = prepareTemporaryEMRTTable("normalization_" + dimName);
		progressListener.setProgressMaxValue(workflowIndices.size());
		int progressCounter = workflowIndices.size();
		for (int wi : workflowIndices)
		{
			Bencher t = new Bencher().start();
			XYPointList xy = getLog2Ratios( wi, dimName ); // depending on mode
			xy.autoEnlargeBounds();
			System.out.print("\t" + wi + "\t.");
			List<ClusteredEMRT> emrts = null;
			try
			{
				// calculate regression line
				System.out.print(".");
				double[] x_ = xy.getXArray();
				double[] y_ = xy.getYArray();
				double[] ys = Lowess.lowess(x_, y_, OPTION_LOWESS_BANDWIDTH, 2);
				Interpolator loess = new LinearInterpolator(XJava.getDoubleList(x_), XJava.getDoubleList(ys));

				// correct EMRTs
				emrts = getAllEMRTs( wi ); // depending on mode
				System.out.print(".");
				for (ClusteredEMRT emrt : emrts)
				{
					try
					{
						double x = 0.0;
						if (dim.equals(NormalizationDimension.inten))
						{
							double inten = (emrt.inten < OPTION_MIN_INTENSITY_CUTOFF) ? OPTION_MIN_INTENSITY_CUTOFF : emrt.inten;
							x = Math.log(inten) / ln2;
							emrt.cor_inten_log2ratio = loess.value(x);
						}
						else if (dim.equals(NormalizationDimension.mass))
						{
							x = emrt.mass;
							emrt.cor_inten_log2ratio = loess.value(x);
						}
						else if (dim.equals(NormalizationDimension.rt))
						{
							x = emrt.rt;
							emrt.cor_inten_log2ratio = loess.value(x);
						}
						emrt.cor_inten = emrt.inten / Math.pow(2, emrt.cor_inten_log2ratio);
					}
					catch (Exception e)
					{
						emrt.cor_inten_log2ratio = 0;
						emrt.cor_inten = emrt.inten;
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			// store EMRTs
			try
			{
				System.out.print(".");
				storeCorrectedEMRTs(emrts, emrtTableName);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			System.out.println("\t" + t.stop().getSec() + "s");
			progressListener.setProgressValue(--progressCounter);
		}
		Bencher t = new Bencher().start();
		System.out.print("storing results ...");
		commitTemporaryEMRTTable(emrtTableName);
		System.out.println("\t" + t.stop().getSec() + "s");
		System.out.println(dim.toString() + " based normalization duration:\t" + td.stop().getSec() + "s");
		return emrtTableName;
	}

	// ----------------------------------------------------------------------------
	private List<ClusteredEMRT> getAllEMRTs(int wi)
	{
		switch (norMode)
		{
			case IN_GROUP_MEAN:
				return getAllEMRTsWithGroupReference( wi );
			case IN_SAMPLE_MEAN:
				return getAllEMRTsWithSampleReference( wi );
			default:
				return getAllEMRTsWithProjectReference( wi );
		}
	}

	// ----------------------------------------------------------------------------
	private void prepareReference()
	{
		Bencher t = new Bencher().start();
		// System.out.print("preparing normalization reference by ");
		switch (norMode)
		{
			case IN_SAMPLE_MEAN:
				// System.out.print("in sample cluster mean of intensity ... ");
				prepareReferenceByInSampleClusterAverage();
				break;
			case IN_GROUP_MEAN:
				// System.out.print( "in group cluster mean of intensity ... " );
				prepareReferenceByInGroupClusterAverage();
				break;
			case RUN_VALUE:
				int i = getIndexOfLargestWorkflow();
				// System.out.print("intensity of run " + i + " ... ");
				prepareReferenceByWorkflow(i);
				break;
			case IN_PROJECT_MEAN:
			default:
				// System.out.print("mean intensity of whole clusters ... ");
				prepareReferenceByClusterAverage();
		}
		System.out.println("[" + t.stop().getSecString() + "]");
	}

	// ----------------------------------------------------------------------------
	private XYPointList getLog2Ratios(int wi, String dimName)
	{
		switch (norMode)
		{
			case IN_GROUP_MEAN:
				return getInGroupLog2RatiosByDimension( wi, dimName );
			case IN_SAMPLE_MEAN:
				return getInSampleLog2RatiosByDimension( wi, dimName );
			default:
				return getLog2RatiosByDimension( wi, dimName );
		}
	}

	// ----------------------------------------------------------------------------
	private void changeMode(NormalizationMode mode)
	{
		if (mode != null)
		{
			System.out.println( "setting normalization mode to '" + mode + "' ..." );
			norMode = mode;
		}
	}

	// ----------------------------------------------------------------------------
	private synchronized List<Integer> getWorkflowIndeces()
	{
		String sql = "SELECT DISTINCT `workflow_index` FROM `clustered_emrt` ORDER BY `workflow_index` ASC";
		List<Integer> res = new ArrayList<Integer>();
		try
		{
			ResultSet rs = db.executeSQL(sql);
			while (rs.next())
				res.add(rs.getInt(1));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}

	// ----------------------------------------------------------------------------
	/**
	 * all EMRTs from given workflow will be returned
	 * requested attributes are: index, inten(from column cor_inten), mass, time 
	 * @param wi
	 * @return
	 */
	private synchronized List<ClusteredEMRT> getAllEMRTsWithProjectReference(int wi)
	{
		String sql =
				"SELECT `index`, `cor_inten`, `mass`, ref_rt, `refint`, LOG2(cor_inten/`refint`) as log2ratio \n" +
						"	FROM `clustered_emrt` as ce JOIN normalization_reference as ca ON ce.`cluster_average_index`=ca.`cluster` \n " +
						"	WHERE `workflow_index`='" + wi + "'\n";
		List<ClusteredEMRT> res = new ArrayList<ClusteredEMRT>();
		try
		{
			ResultSet rs = db.executeSQL(sql);
			while (rs.next())
			{
				ClusteredEMRT emrt = new ClusteredEMRT();
				emrt.index = rs.getInt("index");
				emrt.workflow_index = wi;
				emrt.inten = rs.getDouble("cor_inten");
				emrt.mass = rs.getDouble("mass");
				emrt.rt = rs.getDouble("ref_rt");
				emrt.ave_inten = rs.getDouble("refint");
				emrt.inten_log2ratio = rs.getDouble("log2ratio");
				res.add(emrt);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * sets corrected intensities to original intensity values
	 */
	private synchronized void resetCorrectedIntensities()
	{
		db.executeSQL("UPDATE `clustered_emrt` SET cor_inten=inten");
		db.optimizeTable("clustered_emrt");
// db.executeSQLFile( getPackageResource("sql/reset_intensities.sql") );
	}

	/**
	 * make pooled_ce table with columns:
	 * `index`, `workflow_index`, `cluster_average_index`, `cor_inten`
	 */
	private void poolEMRTTable()
	{
		// db.executeSQL("DROP TABLE IF EXISTS pooled_ce");
		// db.executeSQL(
		// "CREATE TABLE pooled_ce " +
		// "	SELECT `index` , `workflow_index` , `cluster_average_index` , sum( `cor_inten` ) AS cor_inten"
		// +
		// "	FROM `clustered_emrt`" +
		// "	GROUP BY `workflow_index` , `cluster_average_index`"
		// );
		// db.executeSQL("ALTER TABLE pooled_ce ADD INDEX(cluster_average_index)");
		// db.executeSQL("ALTER TABLE pooled_ce ADD INDEX(workflow_index)");
		db.executeSQLFile( getPackageResource( "sql/pool_clustered_emrt.sql" ) );
	}

	/**
	 * use AVG(cor_inten) for each cluster as normalization reference 
	 */
	private void prepareReferenceByClusterAverage()
	{
		poolEMRTTable();
		db.executeSQLFile( getPackageResource( "sql/reference_by_cluster_mean.sql" ) );
	}

	/**
	 * use a workflow as normalization reference 
	 */
	private void prepareReferenceByWorkflow(final int workflowIndex)
	{
		poolEMRTTable();
		db.executeSQLFile( getPackageResource( "src/reference_by_run.sql" ),
				new StdOutSQLBatchExecutionAdapter()
				{
					@Override public String processSQLStatementBeforeExecution(String sql)
					{
						return sql.replaceAll( "%RUN_INDEX%", "" + workflowIndex );
					}
				}
				);
	}

	/**
	 * index of workflow having most emrts
	 * @return
	 */
	private int getIndexOfLargestWorkflow()
	{
		ResultSet rs = db.executeSQL(
				"SELECT  `workflow_index` as run, COUNT( * ) as cnt " +
						"	FROM `clustered_emrt` GROUP BY run ORDER BY cnt DESC LIMIT 1"
				);
		try
		{
			if (rs.next()) return rs.getInt("run");
		}
		catch (Exception e)
		{}
		return 1;
	}

	private String prepareTemporaryEMRTTable(String tabNamePrefix)
	{
		String tab = tabNamePrefix + "_" + XJava.timeStamp("yyyyMMdd_HHmmss");
		// make temporary table
		db.executeSQL(
				"CREATE TABLE `" + tab + "` " +
						"	(`index` INT, `int` DOUBLE, `refint` DOUBLE, `cint` DOUBLE, `l2r` DOUBLE, `cl2r` DOUBLE, PRIMARY KEY (`index`)) "
				);
		return tab;
	}

	private void storeCorrectedEMRTs(List<ClusteredEMRT> emrts, String targetTableName)
	{
		String tmpTab = "temp_" + System.currentTimeMillis();
		// make temporary table
		db.dropTable(tmpTab);
		db.executeSQL(
				"CREATE TEMPORARY TABLE `" + tmpTab + "` " +
						"	(`index` INT, `int` DOUBLE, `refint` DOUBLE, `cint` DOUBLE, `l2r` DOUBLE, `cl2r` DOUBLE, PRIMARY KEY (`index`)) " +
						"	ENGINE=MEMORY"
				);
		// insert values into `temp`
		for (ClusteredEMRT emrt : emrts)
		{
			db.executeSQL(
					"INSERT INTO `" + tmpTab + "` SET " +
							"	`index`='" + emrt.index + "', " +
							"	`int`='" + emrt.inten + "', " +
							"	refint='" + emrt.ave_inten + "', " +
							"	`l2r`='" + emrt.inten_log2ratio + "', " +
							"	`cint`='" + emrt.cor_inten + "', " +
							"	`cl2r`='" + emrt.cor_inten_log2ratio + "'"
					);
		}
		// update clustered_emrt
		db.executeSQL("INSERT INTO `" + targetTableName + "` SELECT * FROM `" + tmpTab + "`");
		// drop temp table
		db.dropTable(tmpTab);
	}

	private void commitTemporaryEMRTTable(String tabName)
	{
		db.optimizeTable(tabName);
		db.executeSQL(
				"UPDATE clustered_emrt as c RIGHT JOIN `" + tabName + "` as t ON c.`index`=t.`index` " +
						"	SET c.cor_inten=t.`cint`"
				);
	}

	/**
	 * select a list of XYPoints 
	 * by grouping emrts with equal dimension values.
	 * 
	 * x-attribute contains current value of given dimension
	 * y-attribute contains contains Log2-Ratio
	 * 
	 * @param wi workflow index
	 * @param dimName the dimension name has to be one of following: "time", "mass", "inten" or "LOG2(inten)"
	 * @return List of XYPoint having XYPoint.x = dimension value, XYPoint.y = Log2-Ratio at dimension value's position 
	 */
	private XYPointList getLog2RatiosByDimension(int wi, String dimName)
	{
		ResultSet rs = db.executeSQL(
				"SELECT " + dimName + ", AVG( LOG2(`cor_inten`/`refint`) ) as l2r \n" +
						"	FROM `clustered_emrt` as ce JOIN normalization_reference as ca ON ce.`cluster_average_index`=ca.`cluster` \n " +
						"	WHERE `workflow_index`='" + wi + "' " +
						"	GROUP BY " + dimName + " \n" +
						"	ORDER BY " + dimName + " ASC\n"
				);
		XYPointList res = new XYPointList();
		try
		{
			while (rs.next())
				res.addPoint(rs.getDouble(dimName), rs.getDouble("l2r"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}

// /**
// * Filtered: only best emrts will be used for normalisation
// *
// * @param wi workflow index
// * @param dimName the dimension name has to be one of following: "time",
// "mass", "inten" or "LOG2(inten)"
// * @return List of XYPoint having XYPoint.x = dimension value, XYPoint.y =
// Log2-Ratio at dimension value's position
// */
// private XYPointList getFilteredLog2RatiosByDimension(int wi, String dimName)
// {
// if (!tableExists("best_peptides_for_annotation"))
// {
// System.err.println("Table best_peptides_for_annotation not found! \ngoing to use all EMRTs for Log2Ratio selection ... ");
// return getLog2RatiosByDimension(wi, dimName);
// }
// ResultSet rs = db.executeSQL(
// "SELECT " + dimName + ", AVG( LOG2(`cor_inten`/`refint`) ) as l2r \n" +
// "	FROM `clustered_emrt` as ce JOIN normalization_reference as ca ON ce.`cluster_average_index`=ca.`cluster` \n "
// +
// "	JOIN best_peptides_for_annotation as bp USING(cluster_average_index) " +
// "	WHERE `workflow_index`='" + wi + "' AND `inten`>0 \n" +
// "	GROUP BY " + dimName + " \n" +
// "	HAVING `l2r`!=0 \n" +
// "	ORDER BY " + dimName + " ASC\n"
// );
// XYPointList res = new XYPointList();
// try
// {
// while (rs.next())
// res.addPoint(rs.getDouble(dimName), rs.getDouble("l2r"));
// }
// catch (Exception e)
// {
// e.printStackTrace();
// }
// return res;
// }
	private boolean tableExists(String tableName)
	{
		List<String> tabs = db.listTables();
		for (String tab : tabs)
		{
			if (tab.equalsIgnoreCase(tableName)) return true;
		}
		return false;
	}

	private void addTrace(int ref, String seq, char step, int pos, String tab)
	{
		db.executeSQL("CREATE TABLE IF NOT EXISTS normalization_trace (\n" +
				"	id INT  NOT NULL AUTO_INCREMENT, \n" +
				"	`ref` INT, \n" +
				"	`seq` VARCHAR(255), \n" +
				"	`step` CHAR(1), \n" +
				"	`pos` INT, \n" +
				"	`tab` VARCHAR(255), \n" +
				"	PRIMARY KEY (`id`) \n)"
				);
		db.executeSQL(
				"INSERT INTO normalization_trace SET " +
						"	`ref`='" + ref + "', " +
						"	`seq`='" + seq + "', " +
						"	`step`='" + step + "', " +
						"	`pos`='" + pos + "', " +
						"	`tab`='" + tab + "' "
				);
	}

	// ----------------------------- for IN-SAMPLE NORMALIZATION approach
	/**
	 * use AVG(cor_inten) for each cluster as normalization reference 
	 */
	private void prepareReferenceByInSampleClusterAverage()
	{
		poolEMRTTable();
		db.executeSQLFile( getPackageResource( "sql/reference_by_in_sample_cluster_mean.sql" ) );
	}

	/**
	 * select a list of XYPoints 
	 * by grouping emrts with equal dimension values.
	 * 
	 * x-attribute contains current value of given dimension
	 * y-attribute contains contains Log2-Ratio
	 * 
	 * @param wi workflow index
	 * @param dimName the dimension name has to be one of following: "time", "mass", "inten" or "LOG2(inten)"
	 * @return List of XYPoint having XYPoint.x = dimension value, XYPoint.y = Log2-Ratio at dimension value's position 
	 */
	private XYPointList getInSampleLog2RatiosByDimension(int wi, String dimName)
	{
		ResultSet rs = db.executeSQL(
				"SELECT " + dimName + ", AVG( LOG2(`cor_inten`/`refint`) ) as l2r \n" +
						"FROM `clustered_emrt` as ce \n" +
						" JOIN workflow as w ON ce.workflow_index=w.`index` \n" +
						" JOIN normalization_reference_per_sample_cluster as ca \n" +
						" ON (ce.`cluster_average_index`=ca.`cluster` AND ca.sample=w.sample_index) \n" +
						"WHERE `workflow_index`='" + wi + "' AND `inten`>0 \n" +
						"GROUP BY " + dimName + " \n" +
						"HAVING `l2r`!=0 \n" +
						"ORDER BY " + dimName + " ASC\n"
				);
		XYPointList res = new XYPointList();
		try
		{
			while (rs.next())
				res.addPoint(rs.getDouble(dimName), rs.getDouble("l2r"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * all EMRTs from given workflow will be returned
	 * requested attributes are: index, inten(from column cor_inten), mass, time 
	 * @param wi
	 * @return
	 */
	private synchronized List<ClusteredEMRT> getAllEMRTsWithSampleReference(int wi)
	{
		ResultSet rs = db.executeSQL(
				"SELECT ce.`index`, ce.`cor_inten`, `mass`, `rt`, `refint`, LOG2(cor_inten/`refint`) as log2ratio \n" +
						"FROM `clustered_emrt` as ce \n" +
						" JOIN workflow as w ON ce.workflow_index=w.`index` \n" +
						" JOIN normalization_reference_per_sample_cluster as ca " +
						" ON (ce.`cluster_average_index`=ca.`cluster` AND ca.sample=w.sample_index) \n" +
						"WHERE `workflow_index`='" + wi + "'\n"
				);
		List<ClusteredEMRT> res = new ArrayList<ClusteredEMRT>();
		try
		{
			while (rs.next())
			{
				ClusteredEMRT emrt = new ClusteredEMRT();
				emrt.index = rs.getInt( "index" );
				emrt.workflow_index = wi;
				emrt.inten = rs.getDouble( "cor_inten" );
				emrt.mass = rs.getDouble( "mass" );
				emrt.rt = rs.getDouble( "rt" );
				emrt.ave_inten = rs.getInt( "refint" );
				emrt.inten_log2ratio = rs.getDouble( "log2ratio" );
				res.add( emrt );
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}

	// ----------------------------- for IN-GROUP NORMALIZATION approach
	/**
	 * use AVG(cor_inten) for each cluster as normalization reference 
	 */
	private void prepareReferenceByInGroupClusterAverage()
	{
		poolEMRTTable();
		db.executeSQLFile( getPackageResource( "sql/reference_by_in_group_cluster_mean.sql" ) );
	}

	/**
	 * select a list of XYPoints 
	 * by grouping emrts with equal dimension values.
	 * 
	 * x-attribute contains current value of given dimension
	 * y-attribute contains contains Log2-Ratio
	 * 
	 * @param wi workflow index
	 * @param dimName the dimension name has to be one of following: "time", "mass", "inten" or "LOG2(inten)"
	 * @return List of XYPoint having XYPoint.x = dimension value, XYPoint.y = Log2-Ratio at dimension value's position 
	 */
	private XYPointList getInGroupLog2RatiosByDimension(int wi, String dimName)
	{
		ResultSet rs = db.executeSQL(
				"SELECT " + dimName + ", AVG( LOG2(`cor_inten`/`refint`) ) as l2r \n" +
						"FROM `clustered_emrt` as ce \n" +
						" JOIN workflow as w ON ce.workflow_index=w.`index` \n" +
						" JOIN sample as s ON w.sample_index=s.`index` " +
						" JOIN normalization_reference_per_group_cluster as ca \n" +
						" ON (ce.`cluster_average_index`=ca.`cluster` AND ca.`group`=s.group_index) \n" +
						"WHERE `workflow_index`='" + wi + "' AND `inten`>0 \n" +
						"GROUP BY " + dimName + " \n" +
						"HAVING `l2r`!=0 \n" +
						"ORDER BY " + dimName + " ASC\n"
				);
		XYPointList res = new XYPointList();
		try
		{
			while (rs.next())
				res.addPoint( rs.getDouble( dimName ), rs.getDouble( "l2r" ) );
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * all EMRTs from given workflow will be returned
	 * requested attributes are: index, inten(from column cor_inten), mass, time 
	 * @param wi
	 * @return
	 */
	private synchronized List<ClusteredEMRT> getAllEMRTsWithGroupReference(int wi)
	{
		ResultSet rs = db.executeSQL(
				"SELECT ce.`index`, ce.`cor_inten`, `mass`, `rt`, `refint`, LOG2(cor_inten/`refint`) as log2ratio \n" +
						"FROM `clustered_emrt` as ce \n" +
						" JOIN workflow as w ON ce.workflow_index=w.`index` \n" +
						" JOIN sample as s ON w.sample_index=s.`index` " +
						" JOIN normalization_reference_per_group_cluster as ca \n" +
						" ON (ce.`cluster_average_index`=ca.`cluster` AND ca.`group`=s.group_index) \n" +
						"WHERE `workflow_index`='" + wi + "'\n"
				);
		List<ClusteredEMRT> res = new ArrayList<ClusteredEMRT>();
		try
		{
			while (rs.next())
			{
				ClusteredEMRT emrt = new ClusteredEMRT();
				emrt.index = rs.getInt("index");
				emrt.workflow_index = wi;
				emrt.inten = rs.getDouble("cor_inten");
				emrt.mass = rs.getDouble("mass");
				emrt.rt = rs.getDouble("rt");
				emrt.ave_inten = rs.getInt("refint");
				emrt.inten_log2ratio = rs.getDouble("log2ratio");
				res.add(emrt);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}

	@Override public int getExecutionOrder()
	{
		return 100;
	}
}
