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
/** ISOQuant, isoquant.plugins.exporting, 16.09.2011 */
package isoquant.plugins.report.prot.csv;

import java.io.File;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import de.mz.jk.jsix.libs.XFiles;
import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.plgs.data.Workflow;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.db.IQDBUtils;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

/**
 * <h3>{@link SimpleProteinRunQuantificationByGroupReporter}</h3>
 * create a CSV Protein Report
 * @author Joerg Kuharev
 * @version 16.09.2011 09:22:08
 */
public class SimpleProteinRunQuantificationByGroupReporter extends SingleActionPlugin4DB
{
	private File outDir = null;
	private String quoteChar = "\"";
	private String colSep = ",";
	private String decPoint = ".";

	public SimpleProteinRunQuantificationByGroupReporter(iMainApp app)
	{
		super(app);
		loadConfig();
	}

	private void loadConfig()
	{
		outDir = new File(app.getSettings().getStringValue("setup.report.dir", iMainApp.appDir.replace("\\\\", "/"), false));
		decPoint = app.getSettings().getStringValue("setup.report.csv.decimalPoint", "'" + decPoint + "'", false);
		colSep = app.getSettings().getStringValue("setup.report.csv.columnSeparator", "'" + colSep + "'", false);
		quoteChar = app.getSettings().getStringValue("setup.report.csv.textQuote", "'" + quoteChar + "'", false);
		decPoint = XJava.stripQuotation(decPoint);
		colSep = XJava.stripQuotation(colSep);
		quoteChar = XJava.stripQuotation(quoteChar);
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		loadConfig();
		Bencher t = new Bencher(true);
		System.out.println( "creating protein quantification report for project '" + p.data.title + "' ..." );
		try
		{
			File file = XFiles.chooseFile(
					"choose file for csv protein report",
					true,
					new File( outDir, p.data.title.replace( "\\W", "_" ) + "_finalquant.csv" ),
					outDir,
					"csv",
					app.getGUI()
					);
			if (file == null) return;
			if (file.exists() && !XFiles.overwriteFileDialog(app.getGUI(), getPluginIcon())) return;
			createCSVReports(p, file);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("report creation done! [" + t.stop().getSecString() + "]");
	}

	/**
	 * @param p
	 * @param file
	 */
	private void createCSVReports(DBProject p, File file)
	{
		Bencher b = new Bencher(true);
		String fileBasePath = file.getAbsolutePath().replaceAll("\\.csv$", "");
		// get all-workflows list
		List<Workflow> workflowList = IQDBUtils.getWorkflows(p);
		System.out.println("\twriting protein quantification data for all runs ...");
		System.out.println("\t\t" + file);
		createCSVReport(file, p.mysql, workflowList);
// get sample groups indices
		List<Integer> groupIndices = p.mysql.getIntegerValues("SELECT `index` FROM `group` ORDER BY `index` ASC");
		for (int groupIndex : groupIndices)
		{
			String groupName =
					groupIndex + ", " +
							p.mysql.getFirstValue("SELECT `name` FROM `group` WHERE `index`=" + groupIndex, 1).replaceAll("\\W", " ");
			workflowList = IQDBUtils.getWorkflowsByGroupIndex(p, groupIndex);
			File f = new File(fileBasePath + "(" + groupName + ").csv");
			System.out.println("\twriting protein quantification data for runs of group (" + groupName + ") ...");
			System.out.println("\t\t" + f);
			createCSVReport(f, p.mysql, workflowList);
		}
		System.out.println("repor files creation duration: [" + b.stop().getSecString() + "s]");
	}

	private void createCSVReport(File file, MySQL db, List<Workflow> workflowList)
	{
		PrintStream out = null;
		try
		{
			out = new PrintStream(file);
			printHeadLine(out, workflowList);
			String wis = "";
			for (Workflow w : workflowList)
				wis += (wis.length() > 0 ? "," : "") + w.index;
			ResultSet rs = db.executeSQL(
					"SELECT \n" +
							"entry,\n" +
							"GROUP_CONCAT(`workflow_index`, \":\", FLOOR(`top3_avg_inten`) SEPARATOR \";\") as intensities\n" +
							"FROM finalquant WHERE workflow_index IN (" + wis + ") GROUP BY entry"
					);
			try
			{
				while (rs.next())
				{
					printDataLine(out, rs, workflowList);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (out != null) out.close();
		}
	}

	private void printDataLine(PrintStream out, ResultSet rs, List<Workflow> workflowList) throws Exception
	{
		printTxtCell(out, rs, "entry");
		Map<Integer, Double> r2i = IQDBUtils.extractI2DMap(rs.getString("intensities"));
		for (Workflow w : workflowList)
		{
			out.print(colSep);
			out.print(
					(r2i.containsKey(w.index))
							? (r2i.get(w.index).longValue())
							: "0"
					);
		}
		out.println();
	}

	private void printHeadLine(PrintStream out, List<Workflow> workflowList)
	{
		out.print(quoteChar + "entry" + quoteChar);
		for (Workflow w : workflowList)
		{
			String name = XJava.decURL(w.replicate_name);
			if (!quoteChar.equals("")) name.replaceAll(quoteChar, "_");
			out.print(colSep + quoteChar + name + quoteChar);
		}
		out.println();
	}

	private void printTxtCell(PrintStream out, ResultSet rs, String col)
	{
		out.print(quoteChar);
		try
		{
			String txt = XJava.decURL(rs.getString(col));
			out.print(txt.replaceAll(quoteChar, " ").replaceAll("\n", ""));
		}
		catch (Exception e)
		{}
		out.print(quoteChar);
	}

	@Override public String getMenuItemIconName()
	{
		return "csv";
	}

	@Override public String getMenuItemText()
	{
		return "simple protein quantification for runs by group (CSV)";
	}

	@Override public String getPluginName()
	{
		return "Simple Protein Quantification by Group Reporter";
	}

	@Override public int getExecutionOrder()
	{
		return 0;
	}
}
