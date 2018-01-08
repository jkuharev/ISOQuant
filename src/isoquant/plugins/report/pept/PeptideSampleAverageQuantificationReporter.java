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
/** ISOQuant, isoquant.plugins.report.mpqs, 12.12.2011*/
package isoquant.plugins.report.pept;

import java.io.File;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mz.jk.jsix.libs.XFiles;
import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.utilities.Settings;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.db.IQDBUtils;
import isoquant.kernel.plugin.SingleActionPlugin4DB;
import isoquant.plugins.benchmark.mpqs.MPQS_Peptide_Reporter;

/**
 * <h3>{@link MPQS_Peptide_Reporter}</h3>
 * @author kuharev
 * @version 12.12.2011 12:28:16
 */
public class PeptideSampleAverageQuantificationReporter extends SingleActionPlugin4DB
{
	private String quoteChar = "\"";
	private String colSep = ",";
	private String decPoint = ".";

	public PeptideSampleAverageQuantificationReporter(iMainApp app)
	{
		super(app);
	}
	private File outDir = null;

	@Override public void loadSettings(Settings cfg)
	{
		outDir = new File(app.getSettings().getStringValue("setup.report.dir", iMainApp.appDir.replace("\\\\", "/"), false));
		decPoint = app.getSettings().getStringValue("setup.report.csv.decimalPoint", "'" + decPoint + "'", false);
		colSep = app.getSettings().getStringValue("setup.report.csv.columnSeparator", "'" + colSep + "'", false);
		quoteChar = app.getSettings().getStringValue("setup.report.csv.textQuote", "'" + quoteChar + "'", false);
		decPoint = XJava.stripQuotation(decPoint);
		colSep = XJava.stripQuotation(colSep);
		quoteChar = XJava.stripQuotation(quoteChar);
	}

	@Override public String getMenuItemIconName()
	{
		return "csv";
	}

	@Override public String getMenuItemText()
	{
		return "Peptide Sample Average Quantification Report";
	}

	@Override public String getPluginName()
	{
		return "Peptide Sample Average Quantification Reporter";
	}

	@Override public int getExecutionOrder()
	{
		return 256;
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		progressListener.startProgress("creating sample quantification report");
		File hint = new File( p.data.title.replaceAll( "\\W+", "_" ) + "_peptide_sample_quant.csv" );
		File file = XFiles.chooseFile("please choose target CSV file", true, hint, outDir, "csv", app.getGUI());
		if (file != null && (!file.exists() || XFiles.overwriteFileDialog(app.getGUI(), this.getPluginIcon())))
		{
			outDir = file.getParentFile();
			app.getSettings().setValue("setup.report.dir", outDir.getAbsolutePath());
			createReport(p, file);
		}
		progressListener.endProgress();
	}

	/**
	 * @param p
	 * @param file
	 * @throws Exception 
	 */
	private void createReport(DBProject p, File file) throws Exception
	{
		MySQL db = p.mysql;
		List<Integer> sampleIndexes = db.getIntegerValues("SELECT DISTINCT sample_index FROM finalquant ORDER BY sample_index ASC");
		Map<Integer, String> sampleI2N = IQDBUtils.getSampleIndexToNameMap(p.mysql);
		ResultSet rs = db.executeSQL(
				"SELECT \n" +
						"	cluster_average_index, " +
						"	intensities, " +
						"	pro.*, " +
						"	pep.*\n" +
						"FROM " +
						"	`cluster_info_samples` \n" +
						"JOIN cluster_info_proteins as pro USING(`cluster_average_index`)\n" +
						"JOIN cluster_info_peptide as pep USING(`cluster_average_index`)"
				);
		ResultSetMetaData meta = rs.getMetaData();
		int n = meta.getColumnCount();
		PrintStream out = new PrintStream(file);
		for (int i = 1; i <= n; i++)
		{
			if (i == 2)
			{
				for (int s : sampleIndexes)
				{
					String sn = sampleI2N.containsKey(s) ? sampleI2N.get(s) : "";
					printTxtCell(out, sn.length() > 0 ? sn : "s_" + s);
				}
			}
			else
			{
				printTxtCell(out, meta.getColumnName(i));
			}
		}
		out.println();
		while (rs.next())
		{
			for (int i = 1; i <= n; i++)
			{
				if (i == 2)
				{
					Map<Integer, String> map = extractKeyValueMap(rs.getString(i));
					for (int s : sampleIndexes)
					{
						printNumCell(out, (map.containsKey(s)) ? map.get(s) : "0");
					}
				}
				else
				{
					printCell(out, rs.getString(i));
				}
			}
			out.println();
		}
		out.flush();
		out.close();
	}

	private void printCell(PrintStream out, String value)
	{
		try
		{
			Double.parseDouble(value);
			printNumCell(out, value);
		}
		catch (Exception e)
		{
			printTxtCell(out, value);
		}
	}

	private void printTxtCell(PrintStream out, String value)
	{
		out.print(quoteChar);
		try
		{
			String txt = XJava.decURL(value);
			out.print(txt.replaceAll(quoteChar, " ").replaceAll("\n", ""));
		}
		catch (Exception e)
		{}
		out.print(quoteChar);
		out.print(colSep);
	}

	private void printNumCell(PrintStream out, String value)
	{
		try
		{
			out.print(value.replaceAll("\\.", decPoint));
		}
		catch (Exception e)
		{}
		out.print(colSep);
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
		out.print(colSep);
	}

	private void printNumCell(PrintStream out, ResultSet rs, String col)
	{
		try
		{
			String txt = rs.getString(col);
			out.print(txt.replaceAll("\\.", decPoint));
		}
		catch (Exception e)
		{}
		out.print(colSep);
	}

	private Map<Integer, String> extractKeyValueMap(String string)
	{
		if (string.endsWith(";")) string = string.substring(0, string.length() - 1);
		Map<Integer, String> res = new HashMap<Integer, String>();
		String[] elements = string.split(";");
		for (String e : elements)
		{
			String[] kv = e.split(":");
			if (kv.length > 1) res.put(Integer.parseInt(kv[0]), kv[1]);
		}
		return res;
	}
}
