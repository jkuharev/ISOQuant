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
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import de.mz.jk.jsix.libs.XFiles;
import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.ui.CustomizableFileFilter;
import de.mz.jk.jsix.utilities.Settings;
import de.mz.jk.plgs.data.Sample;
import de.mz.jk.plgs.data.Workflow;
import isoquant.interfaces.iMainApp;
import isoquant.interfaces.log.iLogEntry;
import isoquant.interfaces.plugin.iProjectReportingPlugin;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

/**
 * <h3>{@link HTMLOnePageReporter}</h3>
 * create a single-html-page quantification report
 * @author Joerg Kuharev
 * @version 20.01.2011 17:27:16
 */
public class HTMLOnePageReporter extends SingleActionPlugin4DB implements iProjectReportingPlugin
{
	private DBProject p = null;
	private FileFilter htmlFF = new CustomizableFileFilter().get("html", "HTML File");

	/**
	 * @param app
	 */
	public HTMLOnePageReporter(iMainApp app)
	{
		super(app);
	}

	@Override public void loadSettings(Settings cfg)
	{
		outDir = new File(cfg.getStringValue("setup.report.dir", iMainApp.appDir.replace("\\\\", "/"), false));
	}

	@Override public String getMenuItemIconName()
	{
		return "html";
	}

	@Override public String getMenuItemText()
	{
		return "single page HTML";
	}

	@Override public String getPluginName()
	{
		return "Single-HTML-Page Report Generator";
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		this.p = p;
		progressListener.startProgress("creating html report");
		try
		{
			File hint = new File( p.data.title.replaceAll( "\\W+", "_" ) + "_report.html" );
			File f = chooseReportFile(hint);
			if (f != null)
			{
				try
				{
					createReport(f);
				}
				catch (Exception e)
				{
					app.showErrorMessage(
							"HTML file could not be created!\n\n" +
									"an error occured while html file creation!"
							);
					throw e;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		progressListener.endProgress();
	}
	private JFileChooser fc = null;
	private File outDir = null;

	public File chooseReportFile(File hintFile)
	{
		if (fc == null)
		{
			fc = new JFileChooser();
			fc.setDialogTitle("Choose a name for html report file");
			fc.resetChoosableFileFilters();
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(htmlFF);
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
		fc.setCurrentDirectory(outDir);
		fc.setSelectedFile(hintFile);
		int state = fc.showSaveDialog(app.getGUI());
		File file = fc.getSelectedFile();
		if (state != JFileChooser.CANCEL_OPTION && file != null)
		{
			if (!fc.getFileFilter().accept(file))
			{
				file = new File(file.toString() + ".html");
			}
			outDir = file.getParentFile();
			System.out.println("file selected: " + file.getAbsolutePath());
			app.getSettings().setValue("setup.report.dir", outDir.getAbsolutePath().replace("\\\\", "/"));
			if (!file.exists() ||
					JOptionPane.showConfirmDialog
							(
									app.getGUI(),
									"<html>" +
											"you have selected an existing file<br> " +
											"do you want to overwrite it?" +
											"</html>",
									"overwrite file?",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.WARNING_MESSAGE,
									getPluginIcon()
							) == JOptionPane.YES_OPTION)
				return file.getAbsoluteFile();
		}
		else
		{
			System.out.println("file selection cancelled");
		}
		return null;
	}
	private PrintStream out = null;
	private List<Workflow> runs = null;
	private Map<Integer, List<Workflow>> groupedRuns = null;
	private List<Sample> samples = null;
	private List<String> entries = null;
	private Map<String, Map<Integer, Long>> wqData = null;
	private Map<String, Map<Integer, Long>> sqData = null;
// private String projectTitle = "";
	private String headerFile = getPackageResource("header.html");
	private String footerFile = getPackageResource("footer.html");

	public void createReport(File outFile) throws Exception
	{
		out = new PrintStream(outFile);
		runs = getWorkflows();
		groupedRuns = groupWorkflowsBySample(runs);
		samples = getSamples();
		entries = getQuantifiedProteinEntries();
		wqData = getQuantifiedProteinsPerRun(entries);
		sqData = getQuantifiedProteinsPerSample(entries);
		String htmlHeader = processTemplate(XFiles.readFile(headerFile));
		String htmlFooter = processTemplate(XFiles.readFile(footerFile));
		out.println(htmlHeader);
		out.println( "<a name=\"top\"></a><h2>ISOQuant Quantification Report for<br><i>" + p.data.title + "</i></h2>" );
		printSpace();
		printProteinQuantification();
		printSpace();
		printAverageProteinQuantification();
		printSpace();
		for (String entry : entries)
		{
			printProteinDetails(entry);
			printSpace();
		}
		printHistory();
		out.println(htmlFooter);
		out.close();
	}

	private String processTemplate(String originalTemplateString)
	{
		return originalTemplateString
				.replaceAll( "%PROJECT_TITLE%", p.data.title )
				.replaceAll( "%PROJECT_ID%", p.data.id )
// .replaceAll("%VAR_NAME%", "")
		;
	}

	private void printHistory()
	{
		List<iLogEntry> logEntries = p.log.get();
		out.println("<h3>Processing parameters:</h3>");
		out.println("<table class=\"parLog\">");
		out.println("<tr>");
		out.println("<th class=\"caption\">time</th>");
		out.println("<th class=\"caption\">parameter</th>");
		out.println("<th class=\"caption\">value</th>");
		out.println("</tr>");
		boolean odd = true;
		for (iLogEntry le : logEntries)
			if (le.getType().equals(iLogEntry.Type.parameter))
			{
				out.println("<tr class=\"" + ((odd = !odd) ? "odd" : "even") + "\">");
				out.println("<td class=\"caption\">" + le.getTime() + "</td>");
				out.println("<td class=\"caption\">" + le.getValue() + "</td>");
				out.println("<td class=\"caption\">" + le.getNote() + "</td>");
				out.println("</tr>");
			}
		out.println("</table>");
	}

	private void printProteinDetails(String entry)
	{
		out.println(
				"<a class=\"proteinName\" name=\"Details_" + entry + "\"></a>\n" +
						"<table>\n" +
						"<tr>\n" +
						"<td>[<a href=\"#top \">top</a>]</td>\n" +
						"<td>[<a href=\"#RunOverview_" + entry + "\" \">run overview</a>]</td>\n" +
						"<td>[<a  href=\"#SampleOverview_" + entry + "\">sample overview</a>]</td>\n" +
						"</tr>\n" +
						"</table>\n" +
						"<h3>Peptide quantification for protein " + entry + "</h3>"
				);
		out.println("<table class=\"pepRunQuant\">");
		// out.println("<caption><a name=runQuant />Peptide quantification by replicate run</caption>");
		out.println("<tr>");
		out.println("<td colspan=\"9\" class=\"caption\">&nbsp;</td>");
		out.println("<th class=\"caption\" colspan=\"" + runs.size() + "\">normalized intensity for replicate run</th>");
		out.println("<th class=\"caption\" colspan=\"" + runs.size() + "\">redistributed intensity for replicate run</th>");
		out.println("<td class=\"spacer\" colspan=\"" + samples.size() + "\"></td>");
		out.println("<td class=\"spacer\" colspan=\"" + samples.size() + "\"></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=9 class=\"caption\">&nbsp;</td>");
		for (Sample s : samples)
			out.println("<th class=\"caption\" colspan=\"" + groupedRuns.get(s.index).size() + "\">" + s.name + "</th>");
		for (Sample s : samples)
			out.println("<th class=\"caption\" colspan=\"" + groupedRuns.get(s.index).size() + "\">" + s.name + "</th>");
		out.println("<th class=\"caption\" colspan=\"" + samples.size() + "\">average normalized intensity for sample</th>");
		out.println("<th class=\"caption\" colspan=\"" + samples.size() + "\">average redistributed intensity for replicate run</th>");
		out.println("</tr>");
		out.println("<tr class=\"captionRow\">");
		out.println("<th class=\"caption\">sequence</th>");
		out.println("<th class=\"caption\">modifier</th>");
		out.println("<th class=\"caption\">type</th>");
		out.println("<th class=\"caption\">additional entries</th>");
		out.println("<th class=\"caption\">razor peakCount</th>");
		out.println("<th class=\"caption\">absolute peakCount</th>");
		out.println("<th class=\"caption\">cluster index</th>");
		out.println("<th class=\"caption\">average time</th>");
		out.println("<th class=\"caption\">average mass</th>");
		for (Sample s : samples)
			for (Workflow run : groupedRuns.get(s.index))
			{
				out.print("<th class=\"longCaption\">");
				out.print(run.replicate_name.replaceAll(".*\\W", " "));
				out.println("</th>");
			}
		for (Sample s : samples)
			for (Workflow run : groupedRuns.get(s.index))
			{
				out.print("<th class=\"longCaption\">");
				out.print(run.replicate_name.replaceAll(".*\\W", " "));
				out.println("</th>");
			}
		for (Sample s : samples)
			out.println("<th class=\"caption\" >" + s.name + "</th>");
		for (Sample s : samples)
			out.println("<th class=\"caption\" >" + s.name + "</th>");
		out.println("</tr>");
		boolean odd = true;
		List<Pep> peps = getPeptides(entry);
		for (Pep p : peps)
		{
			out.println("<tr class=\"" + ((odd = !odd) ? "odd" : "even") + "\">");
			out.println("<td class=\"text\">" + p.sequence + "</td>");
			out.println("<td class=\"text\">" + p.modifier + "</td>");
			out.println("<td class=\"text\">" + p.type + "</td>");
			out.println("<td class=\"text\">");
			for (String e : p.entries)
			{
				if (!e.equals(entry))
				{
					out.print("<a href=\"#Details_" + e + "\">" + e + "</a> ");
				}
			}
			out.println("</td>");
			out.println("<td class=\"number\">" + p.razor_count + "</td>");
			out.println("<td class=\"number\">" + p.abs_count + "</td>");
			out.println("<td class=\"number\">" + p.cluster_average_index + "</td>");
			out.println("<td class=\"number\">" + p.ave_ref_rt + "</td>");
			out.println("<td class=\"number\">" + p.ave_mass + "</td>");
			for (Sample s : samples)
			{
				String cssClass = "firstNumber";
				for (Workflow run : groupedRuns.get(s.index))
				{
					out.print("<td class=\"" + cssClass + "\">");
					out.print(((p.run_src_inten.containsKey(run.index))) ? p.run_src_inten.get(run.index) : "-");
					out.println("</td>");
					cssClass = "number";
				}
			}
			for (Sample s : samples)
			{
				String cssClass = "firstNumber";
				for (Workflow run : groupedRuns.get(s.index))
				{
					out.print("<td class=\"" + cssClass + "\">");
					out.print(((p.run_dist_inten.containsKey(run.index))) ? p.run_dist_inten.get(run.index) : "-");
					out.println("</td>");
					cssClass = "number";
				}
			}
			for (Sample s : samples)
			{
				out.println(
						"<td class=\"number\">" +
								(((p.sample_src_inten.containsKey(s.index))) ? p.sample_src_inten.get(s.index) : "-") +
								"</td>"
						);
			}
			for (Sample s : samples)
			{
				out.println(
						"<td class=\"number\">" +
								(((p.sample_dist_inten.containsKey(s.index))) ? p.sample_dist_inten.get(s.index) : "-") +
								"</td>"
						);
			}
			out.print("</tr>");
		}
		out.println("</table>");
	}

	/**
	 	out.println("<a href=\"#top\">to top</a><hr>");
	 */
	private void printSpace()
	{
		out.println("<div class=\"spaceBetweenTables\">");
		out.println("<hr>");
		out.println("</div>");
	}

	private void printProteinQuantification()
	{
		out.println("<table class=\"protRunQuant\">");
		out.println("<caption><a name=\"protRunQuant\" />Protein quantification by replicate run</caption>");
		out.println("<tr>");
		out.println("<td class=\"caption\">&nbsp;</td>");
		out.println("<th class=\"caption\" colspan=\"" + runs.size() + "\">estimated expression level for replicate run</th>");
		out.println("<td class=\"caption\">&nbsp;</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td class=\"caption\">&nbsp;</td>");
		for (Sample s : samples)
		{
			out.println("<th class=\"caption\" colspan=\"" + groupedRuns.get(s.index).size() + "\">" + s.name + "</th>");
		}
		out.println("<td class=\"caption\">&nbsp;</td>");
		out.println("</tr>");
		out.println("<tr class=\"captionRow\">");
		out.println("<th class=\"caption\">Protein</th>");
		for (Sample s : samples)
			for (Workflow run : groupedRuns.get(s.index))
			{
				out.print("<th class=\"longCaption\">");
				out.print(run.replicate_name.replaceAll(".*\\W", " "));
				out.println("</th>");
			}
		out.println("<th class=\"caption\">Protein</th>");
		out.println("</tr>");
		boolean odd = true;
		for (String entry : entries)
		{
			out.println("<tr class=\"" + ((odd = !odd) ? "odd" : "even") + "\">");
			out.println("<th class=\"name\"><a class=\"proteinName\" href=\"#Details_" + entry + "\" name=\"RunOverview_" + entry + "\" >" + entry
					+ "</a></th>");
			Map<Integer, Long> reData = wqData.get(entry);
			for (Sample s : samples)
			{
				String cssClass = "firstNumber";
				for (Workflow run : groupedRuns.get(s.index))
				{
					out.print("<td class=\"" + cssClass + "\">");
					out.print(((reData.containsKey(run.index))) ? reData.get(run.index) : "-");
					out.println("</td>");
					cssClass = "number";
				}
			}
			out.println("<th class=\"name\"><a class=\"proteinName\" href=\"#Details_" + entry + "\" name=\"RunOverview_" + entry + "\" >" + entry
					+ "</a></th>");
			out.print("</tr>");
		}
		out.println("</table>");
	}

	private void printAverageProteinQuantification()
	{
		out.println("<table class=\"protSampleQuant\">");
		out.println("<caption><a name=\"protSampleQuant\" />Protein quantification by sample</caption>");
		out.println("<tr>");
		out.println("<td class=\"caption\">&nbsp;</td>");
		out.println("<th class=\"caption\" colspan=\"" + samples.size() + "\">average expression level for sample</th>");
		out.println("<td class=\"caption\">&nbsp;</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th class=\"caption\">Protein</th>");
		for (Sample s : samples)
		{
			out.println("<th class=\"longCaption\">" + s.name + "</th>");
		}
		out.println("<th class=\"caption\">Protein</th>");
		out.println("</tr>");
		boolean odd = true;
		for (String entry : entries)
		{
			out.println("<tr class=\"" + ((odd = !odd) ? "odd" : "even") + "\">");
			out.println("<th class=\"name\"><a class=\"proteinName\" href=\"#Details_" + entry + "\" name=\"SampleOverview_" + entry + "\" >" + entry
					+ "</a></th>");
			Map<Integer, Long> seData = sqData.get(entry);
			for (Sample s : samples)
			{
				out.print("<td class=\"firstNumber\">");
				if (seData.containsKey(s.index))
					out.print(seData.get(s.index));
				else out.print("-");
				out.println("</td>");
			}
			out.println("<th class=\"name\"><a class=\"proteinName\" href=\"#Details_" + entry + "\" name=\"SampleOverview_" + entry + "\" >" + entry
					+ "</a></th>");
			out.print("</tr>");
		}
		out.println("</table>");
	}

	// -----------------------------------------------------------------------------
/*
CREATE TABLE IF NOT EXISTS `report_peptide_quantification` (
  `entry` varchar(255) DEFAULT NULL,
  `sequence` varchar(255) DEFAULT NULL,
  `modifier` varchar(255) DEFAULT NULL,
  `type` varchar(50) DEFAULT NULL,
  `cluster_average_index` int(11) DEFAULT NULL,
  `ave_ref_rt` double(19,2) DEFAULT NULL,
  `ave_mass` double(21,4) DEFAULT NULL,
  `entries` longtext,
  `razor_count` bigint(21) NOT NULL DEFAULT '0',
  `abs_count` smallint(6) DEFAULT '0',
  `sample_src_inten` longblob,
  `sample_dist_inten` longblob,
  `run_src_inten` longblob,
  `run_dist_inten` longblob,
  KEY `entry` (`entry`)
) 
 */
	private class Pep
	{
		String sequence;
		String modifier;
		String type;
		int cluster_average_index;
		float ave_ref_rt;
		float ave_mass;
		int razor_count;
		int abs_count;
		String[] entries;
		Map<Integer, Long> sample_src_inten;
		Map<Integer, Long> sample_dist_inten;
		Map<Integer, Long> run_src_inten;
		Map<Integer, Long> run_dist_inten;
	}

	private List<Pep> getPeptides(String entry)
	{
		List<Pep> res = new ArrayList<Pep>();
		String sql =
				"SELECT " +
						"`sequence`, `modifier`, `type`, `cluster_average_index`, " +
						"`ave_ref_rt`, `ave_mass`, `razor_count`, `abs_count`, `entries`," +
						"`sample_src_inten`, `sample_dist_inten`, `run_src_inten`, `run_dist_inten` " +
						" FROM `report_peptide_quantification` WHERE entry='" + entry + "'";
		ResultSet rs = p.mysql.executeSQL(sql);
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
// separator should be used in report_prepare.sql
				p.sample_src_inten = getMapFromDPSemString(rs.getString("sample_src_inten"));
				p.sample_dist_inten = getMapFromDPSemString(rs.getString("sample_dist_inten"));
				p.run_src_inten = getMapFromDPSemString(rs.getString("run_src_inten"));
				p.run_dist_inten = getMapFromDPSemString(rs.getString("run_dist_inten"));
				res.add(p);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * create a map from string formatted as key1:value1;key2:value2;key3:value3 ... 
	 * @param data
	 * @return
	 */
	private Map<Integer, Long> getMapFromDPSemString(String data)
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

	// -----------------------------------------------------------------------------
	private List<String> getQuantifiedProteinEntries()
	{
		List<String> entries = new ArrayList<String>();
		String sql = "SELECT DISTINCT `entry` FROM finalquant ORDER BY entry";
		ResultSet rs = p.mysql.executeSQL(sql);
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

	/**
	 * Map<String, Map<Integer, Float>>  data = getQuantifiedProteinsPerSample(List<String> entries);
	 * float intensity = data.get("entry").get("sample");
	 * 
	 * @param entries
	 * @return
	 */
	private Map<String, Map<Integer, Long>> getQuantifiedProteinsPerSample(List<String> entries)
	{
		Map<String, Map<Integer, Long>> res = new HashMap<String, Map<Integer, Long>>();
		for (String entry : entries)
			res.put(entry, new HashMap<Integer, Long>());
		String sql =
				"SELECT `sample_index`, `entry`, FLOOR(AVG(`top3_avg_inten`)) as inten " +
						"FROM finalquant GROUP BY entry, sample_index ORDER BY entry, sample_index ASC";
		ResultSet rs = p.mysql.executeSQL(sql);
		try
		{
			while (rs.next())
			{
				res.get(rs.getString("entry")).put(rs.getInt("sample_index"), rs.getLong("inten"));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * Map<String, Map<Integer, Float>>  data = getQuantifiedProteins(List<String> entries);
	 * float intensity = data.get("entry").get("run");
	 * 
	 * @param entries
	 * @return
	 */
	private Map<String, Map<Integer, Long>> getQuantifiedProteinsPerRun(List<String> entries)
	{
		Map<String, Map<Integer, Long>> res = new HashMap<String, Map<Integer, Long>>();
		for (String entry : entries)
			res.put(entry, new HashMap<Integer, Long>());
		String sql =
				"SELECT `sample_index`, `workflow_index`, `entry`, `top3_avg_inten` " +
						"FROM finalquant ORDER BY entry, sample_index ASC, workflow_index ASC";
		ResultSet rs = p.mysql.executeSQL(sql);
		try
		{
			while (rs.next())
			{
				res.get(rs.getString("entry"))
						.put(rs.getInt("workflow_index"),
								(long)rs.getDouble( "top3_avg_inten" ) );
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return res;
	}

	// -----------------------------------------------------------------------------
	public List<Workflow> getWorkflows()
	{
		String sql = "SELECT * FROM `workflow` ORDER BY sample_index ASC, `index` ASC";
		List<Workflow> res = new ArrayList<Workflow>();
		try
		{
			ResultSet rs = p.mysql.executeSQL(sql);
			while (rs.next())
			{
				Workflow w = new Workflow();
				w.index = rs.getInt("index");
				w.sample_index = rs.getInt("sample_index");
				w.sample_description = XJava.decURL(rs.getString("sample_description"));
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

	// -----------------------------------------------------------------------------
	/**
	 * group workflows by sample_index
	 */
	private Map<Integer, List<Workflow>> groupWorkflowsBySample(List<Workflow> runs)
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

	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------
	private List<Sample> getSamples()
	{
		String sql = "SELECT * FROM `sample` ORDER BY `index` ASC";
		List<Sample> res = new ArrayList<Sample>();
		try
		{
			ResultSet rs = p.mysql.executeSQL(sql);
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

	// -----------------------------------------------------------------------------
	/**
	 * @param workflowIndex
	 * @return HashMap with entry->intensity pairs
	 */
	public Map<String, Double> getQuantData(int workflowIndex)
	{
		Map<String, Double> res = new HashMap<String, Double>();
		String sql =
				"SELECT entry, top3_avg_inten FROM finalquant " +
						"WHERE workflow_index=" + workflowIndex + " ORDER BY entry ";
		try
		{
			ResultSet rs = p.mysql.executeSQL(sql);
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

	/**
	 * assign average of top3-intensities within sample for each protein
	 * @param sampleIndex
	 * @return HashMap with entry->intensity pairs
	 */
	public Map<String, Double> getQuantDataForSample(int sampleIndex)
	{
		Map<String, Double> res = new HashMap<String, Double>();
		String sql =
				"SELECT entry, AVG(top3_avg_inten) as inten" +
						" FROM finalquant WHERE sample_index=" + sampleIndex +
						" GROUP BY `entry` ORDER BY entry";
		try
		{
			ResultSet rs = p.mysql.executeSQL(sql);
			while (rs.next())
			{
				res.put(
						rs.getString("entry"),
						rs.getDouble("inten")
						);
			}
		}
		catch (Exception e)
		{
			System.err.println(sql);
			e.printStackTrace();
		}
		return res;
	}

	@Override public int getExecutionOrder()
	{
		return 0;
	}
}
