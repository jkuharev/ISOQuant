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
package isoquant.plugins.report.prot.html;

import java.io.File;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;

import de.mz.jk.jsix.libs.XFiles;
import de.mz.jk.jsix.utilities.Settings;
import de.mz.jk.plgs.data.Sample;
import de.mz.jk.plgs.data.Workflow;
import isoquant.interfaces.iMainApp;
import isoquant.interfaces.log.iLogEntry;
import isoquant.interfaces.plugin.iProjectReportingPlugin;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DBExportToFolder;

/**
 * <h3>{@link HTMLMultiPageReporter}</h3> create a single-html-page
 * quantification report
 * 
 * @author Joerg Kuharev
 * @version 20.01.2011 17:27:16
 */
public class HTMLMultiPageReporter extends SingleActionPlugin4DBExportToFolder implements iProjectReportingPlugin
{
	private File tarDir;
	private DBProject p = null;
	private String runQuantificationFileName = "run_quantification.html";
	private String sampleQuantificationFileName = "sample_quantification.html";
	private String proteinsFolderName = "proteins";
	private String historyFileName = "history.html";
	private String indexFile = "index.html";
	private PrintStream out = null;
	private List<Workflow> runs = null;
	private Map<Integer, List<Workflow>> groupedRuns = null;
	private List<Sample> samples = null;
	private List<String> entries = null;
	private Map<String, Map<Integer, Long>> wqData = null;
	private Map<String, Map<Integer, Long>> sqData = null;
	private String headerTemplateFile = getPackageResource("templates/parts/header.html");
	private String footerTemplateFile = getPackageResource("templates/parts/footer.html");
	private String indexTemplateFile = getPackageResource("templates/start/index.html");
	private String headerFrameTemplateFile = getPackageResource("templates/start/header_frame.html");
	private String guideFrameTemplateFile = getPackageResource("templates/start/guide_frame.html");
	private String guideFrameFile = "guide_frame.html";
	private String headerFrameFile = "header_frame.html";

	/**
	 * @param app
	 */
	public HTMLMultiPageReporter(iMainApp app)
	{
		super(app);
	}

	@Override public void loadSettings(Settings cfg)
	{
		outDir = new File(cfg.getStringValue("setup.report.dir", iMainApp.appDir.replace("\\\\", "/"), false));
	}

	@Override public String getFolderSelectionDialogTitle()
	{
		return "choose destination folder for report generation";
	}

	@Override public String getMenuItemIconName()
	{
		return "htmls";
	}

	@Override public String getMenuItemText()
	{
		return "multi page HTML";
	}

	@Override public String getPluginName()
	{
		return "Multi-Page HTML Report Generator";
	}

	@Override public void runExportAction(DBProject p, File tarDir)
	{
		this.p = p;
		this.tarDir = tarDir.exists() ? tarDir : tarDir.getParentFile();
		progressListener.startProgress("creating html report");
		try
		{
			createReport();
			createZip();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		progressListener.endProgress();
	}

	private void createZip() throws Exception
	{
		LinkedList<String> files = new LinkedList<String>();
		files.add(new File(tarDir, indexFile).getAbsolutePath());
		files.add(new File(tarDir, guideFrameFile).getAbsolutePath());
		files.add(new File(tarDir, headerFrameFile).getAbsolutePath());
		files.add(new File(tarDir, runQuantificationFileName).getAbsolutePath());
		files.add(new File(tarDir, sampleQuantificationFileName).getAbsolutePath());
		files.add(new File(tarDir, proteinsFolderName).getAbsolutePath());
		files.add(new File(tarDir, historyFileName).getAbsolutePath());
		XFiles.zipFiles( files.toArray( new String[0] ), tarDir.getAbsolutePath() + File.separatorChar + p.data.title + ".zip" );
	}
	private JFileChooser fc = null;
	private File outDir = null;

	public void createReport() throws Exception
	{
		runs = UtilsForHTMLReporter.getWorkflows(p.mysql);
		groupedRuns = UtilsForHTMLReporter.groupWorkflowsBySample(runs);
		samples = UtilsForHTMLReporter.getSamples(p.mysql);
		entries = UtilsForHTMLReporter.getQuantifiedProteinEntries(p.mysql);
		wqData = UtilsForHTMLReporter.getQuantifiedProteinsPerRun(entries, p.mysql);
		sqData = UtilsForHTMLReporter.getQuantifiedProteinsPerSample(entries, p.mysql);
		String htmlHeader = processTemplate(XFiles.readFile(headerTemplateFile));
		String htmlFooter = processTemplate(XFiles.readFile(footerTemplateFile));
		String indexFile = processTemplate(XFiles.readFile(indexTemplateFile));
		String guideFrameContent = processTemplate(XFiles.readFile(guideFrameTemplateFile));
		String headerFrameContent = processTemplate(XFiles.readFile(headerFrameTemplateFile));
		// #############
		File f = new File(tarDir, "index.html");
		out = new PrintStream(f);
		out.println(indexFile);
		// #############
		f = new File(tarDir, "guide_frame.html");
		out = new PrintStream(f);
		out.println(guideFrameContent);
		// #########
		f = new File(tarDir, "header_frame.html");
		out = new PrintStream(f);
		out.println(headerFrameContent);
		// ########
		f = new File(tarDir, runQuantificationFileName);
		out = new PrintStream(f);
		out.println(htmlHeader);
// out.println("<a name=\"top\"></a><h2>ISOQuant Quantification Report for<br><i>"
// + p.title + "</i></h2>");
//
// printSpace();
		printProteinQuantification();
		out.println(htmlFooter);
		// ##########
		f = new File(tarDir, sampleQuantificationFileName);
		out = new PrintStream(f);
		out.println(htmlHeader);
		printAverageProteinQuantification();
		out.println(htmlFooter);
		// ###########
		f = new File(tarDir, proteinsFolderName);
		if (!f.exists())
		{
			f.mkdir();
		}
		for (String entry : entries)
		{
			f = new File(tarDir, UtilsForHTMLReporter.getFileNameByEntryName(
					proteinsFolderName, entry));
			out = new PrintStream(f);
			out.println(htmlHeader);
			printProteinDetails(entry);
			out.println(htmlFooter);
			// printSpace();
		}
		// ##########
		f = new File(tarDir, historyFileName);
		out = new PrintStream(f);
		out.println(htmlHeader);
		printHistory();
		out.println(htmlFooter);
		out.close();
	}

	private String processTemplate(String originalTemplateString)
	{
		return originalTemplateString
				.replaceAll( "%PROJECT_TITLE%", p.data.title )
				.replaceAll( "%PROJECT_ID%", p.data.id )
				.replaceAll("%RUN_QUANTIFICATION_FILE%", runQuantificationFileName)
				.replaceAll("%SAMPLE_QUANTIFICATION_FILE%", sampleQuantificationFileName)
				.replaceAll("%HISTORY_FILE%", historyFileName)
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
				out.println("<tr class=\"" + ((odd = !odd) ? "odd" : "even")
						+ "\">");
				out.println("<td class=\"caption\">" + le.getTime() + "</td>");
				out.println("<td class=\"caption\">" + le.getValue() + "</td>");
				out.println("<td class=\"caption\">" + le.getNote() + "</td>");
				out.println("</tr>");
			}
		out.println("</table>");
	}

	private void printProteinDetails(String entry)
	{
		out.println("<a class=\"proteinName\" name=\"Details_" + entry
				+ "\"></a>\n" + "<table>\n" + "<tr>\n" + "<td>[<a href=\".."
				+ File.separatorChar + runQuantificationFileName + "#" + entry
				+ "\" target=\"main\" \">show in run overview</a>]</td>\n"
				+ "<td>[<a  href=\".." + File.separatorChar + sampleQuantificationFileName
				+ "#" + entry
				+ "\" target=\"main\" > show in sample overview</a>]</td>\n"
				+ "</tr>\n" + "</table>\n"
				+ "<h3>Peptide quantification for protein " + entry + "</h3>");
		out.println("<table class=\"pepRunQuant\">");
		// out.println("<caption><a name=runQuant />Peptide quantification by replicate run</caption>");
		out.println("<tr>");
		out.println("<td colspan=\"9\" class=\"caption\">&nbsp;</td>");
		out.println("<th class=\"caption\" colspan=\"" + runs.size()
				+ "\">normalized intensity for replicate run</th>");
		out.println("<th class=\"caption\" colspan=\"" + runs.size()
				+ "\">redistributed intensity for replicate run</th>");
		out.println("<td class=\"spacer\" colspan=\"" + samples.size()
				+ "\"></td>");
		out.println("<td class=\"spacer\" colspan=\"" + samples.size()
				+ "\"></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=9 class=\"caption\">&nbsp;</td>");
		for (Sample s : samples)
			out.println("<th class=\"caption\" colspan=\""
					+ groupedRuns.get(s.index).size() + "\">" + s.name
					+ "</th>");
		for (Sample s : samples)
			out.println("<th class=\"caption\" colspan=\""
					+ groupedRuns.get(s.index).size() + "\">" + s.name
					+ "</th>");
		out.println("<th class=\"caption\" colspan=\"" + samples.size()
				+ "\">average normalized intensity for sample</th>");
		out.println("<th class=\"caption\" colspan=\"" + samples.size()
				+ "\">average redistributed intensity for replicate run</th>");
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
		List<Pep> peps = UtilsForHTMLReporter.getPeptides(entry, p.mysql);
		for (Pep p : peps)
		{
			out.println("<tr class=\"" + ((odd = !odd) ? "odd" : "even")
					+ "\">");
			out.println("<td class=\"text\">" + p.sequence + "</td>");
			out.println("<td class=\"text\">" + p.modifier + "</td>");
			out.println("<td class=\"text\">" + p.type + "</td>");
			out.println("<td class=\"text\">");
			for (String e : p.entries)
			{
				if (!e.equals(entry))
				{
					out.print("<a href=\""
							+ UtilsForHTMLReporter.getFileNameByEntryName(null,
									e) + "\">" + e + "</a> ");
				}
			}
			out.println("</td>");
			out.println("<td class=\"number\">" + p.razor_count + "</td>");
			out.println("<td class=\"number\">" + p.abs_count + "</td>");
			out.println("<td class=\"number\">" + p.cluster_average_index
					+ "</td>");
			out.println("<td class=\"number\">" + p.ave_ref_rt + "</td>");
			out.println("<td class=\"number\">" + p.ave_mass + "</td>");
			for (Sample s : samples)
			{
				String cssClass = "firstNumber";
				for (Workflow run : groupedRuns.get(s.index))
				{
					out.print("<td class=\"" + cssClass + "\">");
					out.print(((p.run_src_inten.containsKey(run.index))) ? p.run_src_inten
							.get(run.index) : "-");
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
					out.print(((p.run_dist_inten.containsKey(run.index))) ? p.run_dist_inten
							.get(run.index) : "-");
					out.println("</td>");
					cssClass = "number";
				}
			}
			for (Sample s : samples)
			{
				out.println("<td class=\"number\">"
						+ (((p.sample_src_inten.containsKey(s.index))) ? p.sample_src_inten
								.get(s.index) : "-") + "</td>");
			}
			for (Sample s : samples)
			{
				out.println("<td class=\"number\">"
						+ (((p.sample_dist_inten.containsKey(s.index))) ? p.sample_dist_inten
								.get(s.index) : "-") + "</td>");
			}
			out.print("</tr>");
		}
		out.println("</table>");
	}

	/**
	 * out.println("<a href=\"#top\">to top</a>
	 * <hr>
	 * ");
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
		out.println("<thead>");
		out.println("<tr>");
		out.println("<td class=\"caption\">&nbsp;</td>");
		out.println("<th class=\"caption\" colspan=\"" + runs.size()
				+ "\">estimated expression level for replicate run</th>");
		out.println("<td class=\"caption\">&nbsp;</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td class=\"caption\">&nbsp;</td>");
		for (Sample s : samples)
		{
			out.println("<th class=\"caption\" colspan=\""
					+ groupedRuns.get(s.index).size() + "\">" + s.name
					+ "</th>");
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
		out.println("</thead>");
		boolean odd = true;
		out.println("<tbody class=\"scrollContent\">");
		for (String entry : entries)
		{
			out.println("<tr class=\"" + ((odd = !odd) ? "odd" : "even")
					+ "\">");
			// out.println("<th class=\"name\"><a class=\"proteinName\" href=\"#Details_"+entry+"\" name=\"RunOverview_"+entry+"\" >"+entry+"</a></th>");
			out.println("<th class=\"name\"><a class=\"proteinName\" href=\""
					+ UtilsForHTMLReporter.getFileNameByEntryName(
							proteinsFolderName, entry)
					+ "\" target =\"details\" name=\"" + entry + "\" >" + entry
					+ "</a></th>");
			Map<Integer, Long> reData = wqData.get(entry);
			for (Sample s : samples)
			{
				String cssClass = "firstNumber";
				for (Workflow run : groupedRuns.get(s.index))
				{
					out.print("<td class=\"" + cssClass + "\">");
					out.print(((reData.containsKey(run.index))) ? reData
							.get(run.index) : "-");
					out.println("</td>");
					cssClass = "number";
				}
			}
			out.println("<th class=\"name\"><a class=\"proteinName\" href=\""
					+ UtilsForHTMLReporter.getFileNameByEntryName(
							proteinsFolderName, entry)
					+ "\" target =\"details\" name=\"" + entry + "\" >" + entry
					+ "</a></th>");
			out.print("</tr>");
		}
		out.println("</tbody>");
		out.println("</table>");
	}

	private void printAverageProteinQuantification()
	{
		out.println("<table class=\"protSampleQuant\">");
		out.println("<caption><a name=\"protSampleQuant\" />Protein quantification by sample</caption>");
		out.println("<thead>");
		out.println("<tr>");
		out.println("<td class=\"caption\">&nbsp;</td>");
		out.println("<th class=\"caption\" colspan=\"" + samples.size()
				+ "\">average expression level for sample</th>");
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
		out.println("</thead>");
		out.println("<tbody>");
		boolean odd = true;
		for (String entry : entries)
		{
			out.println("<tr class=\"" + ((odd = !odd) ? "odd" : "even")
					+ "\">");
			out.println("<th class=\"name\"><a class=\"proteinName\" href=\""
					+ UtilsForHTMLReporter.getFileNameByEntryName(
							proteinsFolderName, entry)
					+ "\" target =\"details\" name=\"" + entry + "\" >" + entry
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
			out.println("<th class=\"name\"><a class=\"proteinName\" href=\""
					+ UtilsForHTMLReporter.getFileNameByEntryName(
							proteinsFolderName, entry)
					+ "\" target =\"details\" name=\"" + entry + "\" >" + entry
					+ "</a></th>");
			out.print("</tr>");
		}
		out.println("<tbody>");
		out.println("</table>");
	}

	// -----------------------------------------------------------------------------
	@Override public int getExecutionOrder()
	{
		return 0;
	}
}
