/*******************************************************************************
 * THIS FILE IS PART OF ISOQUANT SOFTWARE PROJECT WRITTEN BY JOERG KUHAREV
 * 
 * Copyright (c) 2009 - 2013, JOERG KUHAREV and STEFAN TENZER
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgment:
 *    This product includes software developed by JOERG KUHAREV and STEFAN TENZER.
 * 4. Neither the name "ISOQuant" nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
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
package isoquant.plugins.report.prot.html;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.plgs.data.Sample;
import de.mz.jk.plgs.data.Workflow;

public class UtilsForHTMLReporter
{
	public static Map<String, Map<Integer, Long>> getQuantifiedProteinsPerRun(
		List<String> entries, MySQL mysql)
	{
		Map<String, Map<Integer, Long>> res = new HashMap<String, Map<Integer, Long>>();
		for (String entry : entries)
			res.put(entry, new HashMap<Integer, Long>());
		String sql = "SELECT `sample_index`, `workflow_index`, `entry`, `top3_avg_inten` "
				+ "FROM finalquant ORDER BY entry, sample_index ASC, workflow_index ASC";
		ResultSet rs = mysql.executeSQL(sql);
		try
		{
			while (rs.next())
			{
				res.get(rs.getString("entry")).put(rs.getInt("workflow_index"),
						(long)rs.getDouble( "top3_avg_inten" ) );
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return res;
	}

	public static List<Workflow> getWorkflows(MySQL mysql)
	{
		String sql = "SELECT * FROM `workflow` ORDER BY sample_index ASC, `index` ASC";
		List<Workflow> res = new ArrayList<Workflow>();
		try
		{
			ResultSet rs = mysql.executeSQL(sql);
			while (rs.next())
			{
				Workflow w = new Workflow();
				w.index = rs.getInt("index");
				w.sample_index = rs.getInt("sample_index");
				w.sample_description = XJava.decURL(rs
						.getString("sample_description"));
				w.replicate_name = XJava.decURL(rs.getString("replicate_name"));
				res.add(w);
			}
		}
		catch (Exception e)
		{
			System.err.println(sql);
		}
		return res;
	}

	public static Map<Integer, List<Workflow>> groupWorkflowsBySample(
		List<Workflow> runs)
	{
		Map<Integer, List<Workflow>> res = new HashMap<Integer, List<Workflow>>();
		for (Workflow run : runs)
		{
			if (!res.containsKey(run.sample_index))
			{
				res.put(run.sample_index, new ArrayList<Workflow>());
			}
			res.get(run.sample_index).add(run);
		}
		return res;
	}

	public static List<Sample> getSamples(MySQL mysql)
	{
		String sql = "SELECT * FROM `sample` ORDER BY `index` ASC";
		List<Sample> res = new ArrayList<Sample>();
		try
		{
			ResultSet rs = mysql.executeSQL(sql);
			while (rs.next())
			{
				Sample s = new Sample();
				s.index = rs.getInt("index");
				s.name = XJava.decURL(rs.getString("name"));
				res.add(s);
			}
		}
		catch (Exception e)
		{
			System.err.println(sql);
		}
		return res;
	}

	public static Map<String, Double> getQuantData(int workflowIndex,
		MySQL mysql)
	{
		Map<String, Double> res = new HashMap<String, Double>();
		String sql = "SELECT entry, top3_avg_inten FROM finalquant "
				+ "WHERE workflow_index=" + workflowIndex + " ORDER BY entry ";
		try
		{
			ResultSet rs = mysql.executeSQL(sql);
			while (rs.next())
			{
				res.put(rs.getString("entry"), rs.getDouble("top3_avg_inten"));
			}
		}
		catch (Exception e)
		{
			System.err.println(sql);
			e.printStackTrace();
		}
		return res;
	}

	public static Map<String, Double> getQuantDataForSample(int sampleIndex,
		MySQL mysql)
	{
		Map<String, Double> res = new HashMap<String, Double>();
		String sql = "SELECT entry, AVG(top3_avg_inten) as inten"
				+ " FROM finalquant WHERE sample_index=" + sampleIndex
				+ " GROUP BY `entry` ORDER BY entry";
		try
		{
			ResultSet rs = mysql.executeSQL(sql);
			while (rs.next())
			{
				res.put(rs.getString("entry"), rs.getDouble("inten"));
			}
		}
		catch (Exception e)
		{
			System.err.println(sql);
			e.printStackTrace();
		}
		return res;
	}

	public static Map<String, Map<Integer, Long>> getQuantifiedProteinsPerSample(
		List<String> entries, MySQL mysql)
	{
		Map<String, Map<Integer, Long>> res = new HashMap<String, Map<Integer, Long>>();
		for (String entry : entries)
			res.put(entry, new HashMap<Integer, Long>());
		String sql = "SELECT `sample_index`, `entry`, FLOOR(AVG(`top3_avg_inten`)) as inten "
				+ "FROM finalquant GROUP BY entry, sample_index ORDER BY entry, sample_index ASC";
		ResultSet rs = mysql.executeSQL(sql);
		try
		{
			while (rs.next())
			{
				res.get(rs.getString("entry")).put(rs.getInt("sample_index"),
						rs.getLong("inten"));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return res;
	}

	public static List<String> getQuantifiedProteinEntries(MySQL mysql)
	{
		List<String> entries = new ArrayList<String>();
		String sql = "SELECT DISTINCT `entry` FROM finalquant ORDER BY entry";
		ResultSet rs = mysql.executeSQL(sql);
		try
		{
			while (rs.next())
			{
				entries.add(rs.getString("entry"));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return entries;
	}

	public static Map<Integer, Long> getMapFromDPSemString(String data)
	{
		Map<Integer, Long> res = new HashMap<Integer, Long>();
		String[] pairs = data.split(";");
		for (String pair : pairs)
		{
			String[] kv = pair.split(":");
			if (kv.length > 1)
			{
				res.put(Integer.parseInt(kv[0]), Long.parseLong(kv[1]));
			}
		}
		return res;
	}

	public static List<Pep> getPeptides(String entry, MySQL mysql)
	{
		List<Pep> res = new ArrayList<Pep>();
		String sql = "SELECT "
				+ "`sequence`, `modifier`, `type`, `cluster_average_index`, "
				+ "`ave_ref_rt`, `ave_mass`, `razor_count`, `abs_count`, `entries`,"
				+ "`sample_src_inten`, `sample_dist_inten`, `run_src_inten`, `run_dist_inten` "
				+ " FROM `report_peptide_quantification` WHERE entry='" + entry
				+ "'";
		ResultSet rs = mysql.executeSQL(sql);
		try
		{
			while (rs.next())
			{
				Pep p = new Pep();
				p.sequence = rs.getString("sequence");
				p.modifier = rs.getString("modifier");
				p.type = rs.getString("type");
				p.cluster_average_index = rs.getInt("cluster_average_index");
				p.ave_ref_rt = rs.getFloat("ave_ref_rt");
				p.ave_mass = rs.getFloat("ave_mass");
				p.razor_count = rs.getInt("razor_count");
				p.abs_count = rs.getInt("abs_count");
				p.entries = rs.getString("entries").split(";"); // same
																// separator
																// should be
																// used in
																// report_prepare.sql
				p.sample_src_inten = UtilsForHTMLReporter.getMapFromDPSemString(rs
						.getString("sample_src_inten"));
				p.sample_dist_inten = getMapFromDPSemString(rs
						.getString("sample_dist_inten"));
				p.run_src_inten = getMapFromDPSemString(rs
						.getString("run_src_inten"));
				p.run_dist_inten = getMapFromDPSemString(rs
						.getString("run_dist_inten"));
				res.add(p);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return res;
	}

	public static String getFileNameByEntryName(String folderName, String entrieName)
	{
		// wenn folderName==null, soll nur entirieName ber?cksichtigt werden
		if (folderName == null)
			return entrieName + ".html";
		return folderName + File.separatorChar + entrieName + ".html";
	}
}
