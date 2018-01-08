/*******************************************************************************
 * THIS FILE IS PART OF ISOQUANT SOFTWARE PROJECT WRITTEN BY JOERG KUHAREV
 * 
 * Copyright (c) 2009 - 2013, JOERG KUHAREV and STEFAN TENZER
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 * must display the following acknowledgment:
 * This product includes software developed by JOERG KUHAREV and STEFAN TENZER.
 * 4. Neither the name "ISOQuant" nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY JOERG KUHAREV ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL JOERG KUHAREV BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package isoquant.plugins.report.prot.excel;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.plgs.data.Sample;
import de.mz.jk.plgs.data.Workflow;
import isoquant.kernel.db.DBProject;
import isoquant.plugins.report.prep.ReportPreparator;

public class ExcelReportQueryHandler
{
	// -----------------------------------------------------------------------------
	private MySQL db = null;

	// -----------------------------------------------------------------------------
	public ExcelReportQueryHandler(MySQL db) throws Exception
	{
		this.db = db;
		db.getConnection();
	}

	// -----------------------------------------------------------------------------
	public MySQL getDB()
	{
		return db;
	}

	// -----------------------------------------------------------------------------
	public Map<String, String> getProteinFDR()
	{
		try
		{
			return db.getMap( "SELECT UPPER(entry) as entry, fpr FROM `finalquant_fpr` ORDER BY entry ASC", 1, 2 );
		}
		catch (Exception e)
		{
			return Collections.emptyMap();
		}
	}

	// -----------------------------------------------------------------------------
	public Map<String, String> getProteinCoverage()
	{
		try
		{
			return db.getMap( "SELECT UPPER(entry) as entry, coverage FROM `protein_coverage` ORDER BY entry ASC", 1, 2 );
		}
		catch (Exception e)
		{
			return Collections.emptyMap();
		}
	}

	// -----------------------------------------------------------------------------
	private List<Map<String, String>> quantifiedProteins = null;
	public List<Map<String, String>> getQuantifiedProteins()
	{
		// make sure we don't do it multiple times per session!
		if (quantifiedProteins == null)
		{
			quantifiedProteins = new ArrayList<Map<String, String>>();
			// replaced by faster and more reliable multi-statement version
			// ResultSet rs = db.executeSQL(
			// "SELECT UPPER(entry) as entry, UPPER(accession) as accession,
			// description,
			// MAX(score) as score, mw, pi, rpc " +
			// " FROM protein as p RIGHT JOIN (" +
			// " SELECT entry, COUNT(DISTINCT sequence, modifier) as rpc " +
			// " FROM finalquant JOIN `report_peptide_quantification` as rep
			// USING(entry)
			// GROUP BY `entry`) as q " +
			// " USING(entry) GROUP BY entry ORDER BY score DESC"
			// );
			// count peptides per protein
			db.executeSQL( "DROP TABLE IF EXISTS tmp_rpc" );
			db.executeSQL( "CREATE TEMPORARY TABLE tmp_rpc\n" +
					"		SELECT entry, COUNT(DISTINCT sequence, modifier) as rpc \n" +
					"		FROM `report_peptide_quantification` GROUP BY `entry`" );
			db.executeSQL( "ALTER TABLE tmp_rpc ADD INDEX (entry)" );
			db.executeSQL( "ALTER TABLE tmp_rpc ADD INDEX (rpc)" );
			// collect protein information
			db.executeSQL( "DROP TABLE IF EXISTS tmp_pi" );
			db.executeSQL( "CREATE TEMPORARY TABLE tmp_pi\n" +
					"		SELECT entry, accession, description, MAX(score) as score, mw, pi\n" +
					"		FROM protein GROUP BY entry" );
			db.executeSQL( "ALTER TABLE tmp_pi ADD INDEX (entry)" );
			// retrieve rows
			ResultSet rs = db.executeSQL( "SELECT DISTINCT entry, accession, description, score, mw, pi, rpc\n" +
					"		FROM tmp_rpc JOIN tmp_pi USING(entry) JOIN finalquant USING(entry) ORDER BY score DESC" );
			try
			{
				while (rs.next())
				{
					Map<String, String> prot = new HashMap<String, String>();
					String id = rs.getString( "entry" ).toUpperCase();
					prot.put( "entry", id );
					prot.put( "accession", rs.getString( "accession" ) );
					prot.put( "description", rs.getString( "description" ) );
					prot.put( "score", rs.getString( "score" ) );
					prot.put( "mw", rs.getString( "mw" ) );
					prot.put( "pi", rs.getString( "pi" ) );
					prot.put( "rpc", rs.getString( "rpc" ) );
					quantifiedProteins.add( prot );
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		return quantifiedProteins;
	}


	// -----------------------------------------------------------------------------
	public synchronized List<Sample> getSamples()
	{
		String sql = "SELECT * FROM `sample` ORDER BY `group_index`, `index`";
		List<Sample> res = new ArrayList<Sample>();
		ResultSet rs = db.executeSQL( sql );
		try
		{
			while (rs.next())
			{
				Sample s = new Sample();
				s.index = rs.getInt( "index" );
				s.id = rs.getString( "id" );
				s.group_index = rs.getInt( "group_index" );
				s.name = XJava.decURL( rs.getString( "name" ) );
				res.add( s );
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}

	// -----------------------------------------------------------------------------
	public synchronized List<Workflow> getWorkflows()
	{
		List<Sample> samples = getSamples();
		List<Workflow> res = new ArrayList<Workflow>();
		for ( Sample s : samples )
		{
			String sql = "SELECT * FROM `workflow` WHERE `sample_index`=" + s.index + " ORDER BY `index`";
			ResultSet rs = db.executeSQL( sql );
			try
			{
				while (rs.next())
				{
					Workflow w = new Workflow();
					w.index = rs.getInt( "index" );
					w.sample_index = rs.getInt( "sample_index" );
					w.sample_description = XJava.decURL( rs.getString( "sample_description" ) );
					w.replicate_name = XJava.decURL( rs.getString( "replicate_name" ) );
					w.acquired_name = XJava.decURL( rs.getString( "acquired_name" ) );
					s.workflows.add( w );
					w.sample = s;
					res.add( w );
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return res;
	}

	// -----------------------------------------------------------------------------
	public synchronized List<Integer> getSampleIndexes()
	{
		String sql = "SELECT DISTINCT sample_index FROM `workflow` ORDER BY `sample_index` ASC";
		List<Integer> res = new ArrayList<Integer>();
		ResultSet rs = db.executeSQL(sql);
		try
		{
			while (rs.next())
			{
				res.add(rs.getInt("sample_index"));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}

	// -----------------------------------------------------------------------------
	/**
	 * 
	 * @param runIndex
	 * @return HashMap with entry->intensity pairs
	 */
	public Map<String, Double> getQuantDataForRun(int runIndex)
	{
		Map<String, Double> res = new HashMap<String, Double>();
		String sql = "SELECT UPPER(entry) as entry, top3_avg_inten FROM finalquant WHERE workflow_index=" + runIndex + " ORDER BY entry ";
		ResultSet rs = db.executeSQL(sql);
		try
		{
			while (rs.next())
			{
				res.put(rs.getString("entry"), rs.getDouble("top3_avg_inten"));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}

	// -----------------------------------------------------------------------------
	/**
	 * 
	 * @param runIndex
	 * @param qntCol one of [fmolug, ppm, absqfmol, absqng]
	 * @return
	 */
	public Map<String, Double> getExtraQuantDataForRun(int runIndex, String qntCol)
	{
		Map<String, Double> res = new HashMap<String, Double>();
		String sql =
				"SELECT UPPER(entry) as entry, `" + qntCol + "` FROM finalquant JOIN finalquant_extended USING(entry, workflow_index) WHERE workflow_index="
						+ runIndex
						+ " ORDER BY `entry`";
		ResultSet rs = db.executeSQL(sql);
		try
		{
			while (rs.next())
			{
				res.put(rs.getString("entry"), rs.getDouble(qntCol));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}

	// -----------------------------------------------------------------------------
	/**
	 * assign average of top3-intensities within sample for each protein
	 * @param sampleIndex
	 * @return HashMap with entry->intensity pairs
	 */
	public Map<String, Double> getQuantDataForSample(int sampleIndex)
	{
		Map<String, Double> res = new HashMap<String, Double>();
		String sql =
				"SELECT UPPER(entry) as entry, AVG(top3_avg_inten) as inten" +
						" FROM finalquant WHERE sample_index=" + sampleIndex +
						" GROUP BY `entry` ORDER BY entry";
		ResultSet rs = db.executeSQL(sql);
		try
		{
			while (rs.next())
			{
				res.put(rs.getString("entry"), rs.getDouble("inten"));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * information about emrts used for quantification <br>
	 * and corresponding peptides and proteins 
	 * @return List of data rows<br>
	 * 	first row contains column names,<br>
	 *  all following rows contain data ordered in first row's way
	 */
	public List<List<String>> getQuantifiedEMRTInformation()
	{
		String sql =
				" CREATE TEMPORARY TABLE temp_report " +
						"SELECT " +
						"ce.`cluster_average_index` as cluster, ce.`index` as emrt" +
						",w.`sample_index` as sample, ce.`workflow_index` as workflow" +
						",ce.inten as intensity, ce.corrected_intensity, ce.rt_corrected_intensity" +
						",ce.rt, ce.ref_rt, ce.mass" +
						",pe.`index` as peptide, pe.sequence, pe.modifier, pe.score as pep_score" +
						",pr.entry, pr.score as prot_score " +
						"FROM `clustered_emrt` AS ce " +
						"INNER JOIN best_peptides_for_quantification AS bp USING(cluster_average_index) " +
						"LEFT JOIN workflow as w ON ce.workflow_index=w.`index` " +
						"LEFT JOIN query_mass as qm ON ce.low_energy_index=qm.low_energy_index " +
						"LEFT JOIN peptide as pe ON pe.query_mass_index=qm.`index` " +
						"LEFT JOIN protein as pr ON pe.protein_index=pr.`index`";
		db.executeSQL(sql);
		return listQuery("SELECT * FROM temp_report");
	}

	/**
	 * collect result from a query into a list of rows,<br>
	 * first row is a list of column names,<br>
	 * all other following rows are values ordered by first row meaning
	 * @param sql
	 * @return result data
	 */
	public List<List<String>> listQuery(String sql)
	{
		List<List<String>> res = new ArrayList<List<String>>();
		List<String> caption = new ArrayList<String>();
		res.add(caption);
		ResultSet rs = db.executeSQL(sql);
		try
		{
			ResultSetMetaData rsMetaData = rs.getMetaData();
			int colSize = rsMetaData.getColumnCount();
			for (int i = 1; i <= colSize; i++)
			{
				caption.add(rsMetaData.getColumnName(i));
			}
			while (rs.next())
			{
				List<String> line = new ArrayList<String>();
				for (int i = 1; i <= colSize; i++)
				{
					line.add(rs.getString(i));
				}
				res.add(line);
			}
		}
		catch (Exception e)
		{
			System.err.println(sql);
			e.printStackTrace();
		}
		return res;
	}

	public List<List<String>> getWorkflowDetails()
	{
		if (!db.tableExists("workflow_report"))
			db.executeSQLFile(ReportPreparator.sql_file_workflow_info, MySQL.defExecListener);
		return listQuery("SELECT * FROM workflow_report ORDER BY workflow_index ASC");
	}

	public List<String> getPeptideTypes()
	{
		List<String> res = new ArrayList<String>();
		String sql = "SELECT `type` " +
				" FROM `peptide` as pep LEFT JOIN `query_mass` as qm " +
				" ON pep.`query_mass_index`=qm.`index` " +
				" GROUP BY `type` ORDER BY sum(`intensity`) DESC";
		ResultSet rs = db.executeSQL(sql);
		try
		{
			while (rs.next())
			{
				res.add(rs.getString("type"));
			}
		}
		catch (Exception e)
		{
			System.err.println(sql);
			e.printStackTrace();
		}
		return res;
	}

	public Map<String, Map<Integer, Double>> getSumOfIntensitiesFromPeptideTypesPerWorkflow()
	{
		List<String> types = getPeptideTypes();
		String sql =
				"SELECT pep.`workflow_index`, `type`, sum(`intensity`) as sum_of_inten" +
						" FROM `peptide` as pep LEFT JOIN `query_mass` as qm " +
						" ON pep.`query_mass_index`=qm.`index` " +
						" GROUP BY pep.`workflow_index`, `type` " +
						" ORDER BY pep.`workflow_index` ASC ";
		Map<String, Map<Integer, Double>> res = new HashMap<String, Map<Integer, Double>>();
		for (String t : types)
			res.put(t, new HashMap<Integer, Double>());
		ResultSet rs = db.executeSQL(sql);
		try
		{
			while (rs.next())
			{
				Map<String, Double> line = new HashMap<String, Double>();
				res.get(rs.getString("type")).put(rs.getInt("workflow_index"), rs.getDouble("sum_of_inten"));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}

	public List<List<String>> getProteinMasterList()
	{
		db.dropTable("sq");
		db.executeSQL(
				"CREATE TEMPORARY TABLE sq " +
						"	SELECT `workflow_index`, sum(`aq_ngrams`) as sum_aq_ngrams " +
						"	FROM `protein` WHERE aq_ngrams > 0 " +
						"	GROUP BY `workflow_index`");
		db.executeSQL("ALTER TABLE sq ADD INDEX(workflow_index)");
		return listQuery(
		"SELECT " +
				"	p.`workflow_index`, `entry`, `accession`, `description`, `mw`, " +
				"	`pi`, `sequence`, `peptides`, `products`, `coverage`, `score`, " +
				"	`rms_mass_error_prec`, `rms_mass_error_frag`, `rms_rt_error_frag`, " +
				"	`stat_count_samples`, `stat_count_workflows`, `stat_count_peptides`, " +
				"	`stat_count_unique_peptides`, `stat_count_peptides_per_sample`, " +
				"	`stat_count_unique_peptides_per_sample`, `stat_count_peptides_per_workflow`, " +
				"	`stat_count_unique_peptides_per_workflow`, `aq_fmoles`, `aq_ngrams`, " +
				// "	if(p.`aq_ngrams`>0,p.`aq_ngrams` / sum_aq_ngrams * 1000000, 0) as ppm "
// +
				"	(aq_ngrams / sum_aq_ngrams * 1000000) as ppm " +
				" FROM " +
				"	protein as p LEFT JOIN sq USING(workflow_index) " +
				" ORDER BY `entry` ");
	}

	public List<List<String>> getPLGSPivotFMOL()
	{
		List<Workflow> runs = getWorkflows();
		List<List<String>> res = new ArrayList<List<String>>();
		ResultSet rs = db.executeSQL(
				"SELECT UPPER(accession) as accession, UPPER(entry) as entry, " +
						" GROUP_CONCAT(`workflow_index`, ':', ROUND(aq_fmoles, 2) ORDER BY `workflow_index` ASC SEPARATOR ';') as fmol " +
						" FROM protein GROUP BY entry ORDER BY `entry` ASC "
				);
		List<String> titleRow = new ArrayList<String>();
		titleRow.add("accession");
		titleRow.add("entry");
		for (Workflow r : runs)
			titleRow.add(r.replicate_name);
		res.add(titleRow);
		try
		{
			while (rs.next())
			{
				List<String> row = new ArrayList<String>();
				row.add(rs.getString("accession"));
				row.add(rs.getString("entry"));
				Map<Integer, Double> fmol = extractRunGroupedValuesMap(rs.getString("fmol"));
				for (Workflow r : runs)
				{
					row.add((fmol.containsKey(r.index)) ? fmol.get(r.index) + "" : "");
				}
				res.add(row);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}

	public List<List<String>> getPLGSPivotPPM()
	{
		List<Workflow> runs = getWorkflows();
		List<List<String>> res = new ArrayList<List<String>>();
		db.dropTable("sq");
		db.executeSQL(
				"CREATE TEMPORARY TABLE sq " +
						"	SELECT `workflow_index`, sum(`aq_ngrams`) as sum_aq_ngrams " +
						"	FROM `protein` WHERE aq_ngrams > 0 " +
						"	GROUP BY `workflow_index`");
		db.executeSQL("ALTER TABLE sq ADD INDEX(workflow_index)");
		ResultSet rs = db
				.executeSQL(
				"SELECT "
						+
						" UPPER(accession) as accession, UPPER(entry) as entry, "
						+
						" GROUP_CONCAT(`workflow_index`, ':', ROUND(aq_ngrams / sum_aq_ngrams * 1000000, 2) ORDER BY `workflow_index` ASC SEPARATOR ';') as ppm "
						+
						" FROM protein as p LEFT JOIN sq USING(workflow_index) " +
						" GROUP BY entry " +
						" ORDER BY `entry` "
				);
		List<String> titleRow = new ArrayList<String>();
		titleRow.add("accession");
		titleRow.add("entry");
		for (Workflow r : runs)
			titleRow.add(r.replicate_name);
		res.add(titleRow);
		try
		{
			while (rs.next())
			{
				List<String> row = new ArrayList<String>();
				row.add(rs.getString("accession"));
				row.add(rs.getString("entry"));
				Map<Integer, Double> ppms = extractRunGroupedValuesMap(rs.getString("ppm"));
				for (Workflow r : runs)
				{
					row.add((ppms.containsKey(r.index)) ? ppms.get(r.index) + "" : "");
				}
				res.add(row);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}

	private Map<Integer, Double> extractRunGroupedValuesMap(String string)
	{
		Map<Integer, Double> res = new HashMap<Integer, Double>();
		try
		{
			if (string.endsWith(";")) string = string.substring(0, string.length() - 1);
			String[] elements = string.split(";");
			for (String e : elements)
			{
				String[] kv = e.split(":");
				res.put(Integer.parseInt(kv[0]), Double.parseDouble(kv[1]));
			}
		}
		catch (Exception e)
		{}
		return res;
	}

	public DBProject getProject()
	{
		DBProject p = new DBProject();
		String sql = "SELECT * FROM `project`";
		ResultSet rs = db.executeSQL(sql);
		try
		{
			if (rs.next())
			{
				p.data.id = rs.getString( "id" );
				p.data.title = rs.getString( "title" );
				p.data.root = rs.getString( "root" );
				p.data.db = rs.getString( "db" );
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return p;
	}

	/**
	 * @param runIndex
	 * @return 
	 */
	public List<Double> getRTW(int runIndex)
	{
		List<Double> res = new ArrayList<Double>();
		ResultSet rs = db.executeSQL("SELECT (ref - ret) as drt FROM rrtw WHERE run=" + runIndex);
		try
		{
			while (rs.next())
			{
				res.add(rs.getDouble(1));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}

	public List<Double> getRTWRef()
	{
		db.executeSQL("DROP TABLE IF EXISTS rrtw");
		db.executeSQL(
				"CREATE TEMPORARY TABLE rrtw " +
						"SELECT run, ROUND(time, 1) as ret, AVG(ref_rt)  as ref " +
						"FROM rtw GROUP BY run, ret ORDER BY ret ASC "
				);
		List<Double> res = new ArrayList<Double>();
		ResultSet rs = db.executeSQL("SELECT DISTINCT ret FROM rrtw ORDER BY ret ASC");
		try
		{
			while (rs.next())
			{
				res.add(rs.getDouble(1));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}

	public boolean extraQntTableExists()
	{
		return db.tableExists("finalquant_extended");
	}

	/**
	 * 
	 */
	public List<List<String>> getProteinHomology()
	{
		if (!db.tableExists("protein_homology") || !db.tableExists("peptides_in_proteins_stats")) return null;
		List<List<String>> res = new ArrayList<List<String>>();
		res.add(
				Arrays.asList(
						new String[] {
								"reported protein",
								"unique peptides",
								"razor peptides",
								"shared peptides",
								"proteins sharing peptides"
						}
						)
				);
		ResultSet rs = db.executeSQL(
				"SELECT " +
						"	entryLeft as id, " +
						"	unique_peptides, razor_peptides, shared_peptides, " +
						"	GROUP_CONCAT(DISTINCT entryRemoved SEPARATOR ',') as net \n" +
						"FROM finalquant JOIN protein_homology ON entry=entryLeft JOIN peptides_in_proteins_stats USING(entry)\n" +
						"GROUP BY entryLeft ORDER BY entryLeft"
				);
		try
		{
			while (rs.next())
			{
				res.add(Arrays.asList(
						new String[] {
								rs.getString("id"),
								rs.getString("unique_peptides"),
								rs.getString("razor_peptides"),
								rs.getString("shared_peptides"),
								rs.getString("net"),
						}
						));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}
}
