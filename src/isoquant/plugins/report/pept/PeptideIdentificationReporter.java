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
package isoquant.plugins.report.pept;

import java.io.File;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.util.List;

import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.plgs.data.Workflow;
import isoquant.interfaces.iMainApp;
import isoquant.interfaces.plugin.iProjectReportingPlugin;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.db.IQDBUtils;
import isoquant.kernel.plugin.SingleActionPlugin4DBExportToCSV;

/**
 * <h3>{@link PeptideIdentificationReporter}</h3>
 * create a CSV Peptide Report
 * @author Joerg Kuharev
 * @version 16.09.2011 09:22:08
 */
public class PeptideIdentificationReporter extends SingleActionPlugin4DBExportToCSV implements iProjectReportingPlugin
{
	public PeptideIdentificationReporter(iMainApp app)
	{
		super(app);
	}

	@Override public void runExportAction(DBProject p, File file)
	{
		PrintStream out = null;
		Bencher t = new Bencher(true);
		System.out.println( "creating peptide identification report for project '" + p.data.title + "' ..." );
		try
		{
			out = new PrintStream(file);
			createCSVReport(p, out);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (out != null) out.close();
		}
		System.out.println("peptide identification report creation duration: [" + t.stop().getSecString() + "]");
	}

	private void createCSVReport(DBProject p, PrintStream out)
	{
		List<Workflow> workflowList = IQDBUtils.getWorkflows(p);
		p.mysql.executeSQLFile(
				getPackageResource(
				p.mysql.columnExists("pep_fpr", "modifier")
						? "peptide_identification_report.sql"
						: "peptide_identification_report_no_modifier.sql"
				));
		try
		{
			ResultSet rs = p.mysql.getStatement().getResultSet();
			printHeadLine(out, workflowList);
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

	/**
	sequence,
	modifier,
	score,
	fpr,
	src_proteins
	GROUP_CONCAT( entry ) AS entries,	
	 */
	private void printDataLine(PrintStream out, ResultSet rs, List<Workflow> workflowList) throws Exception
	{
		printTxtCell(out, rs, "sequence");
		printTxtCell(out, rs, "modifier");
		printNumCell(out, rs, "score");
		printNumCell(out, rs, "fpr");
		printNumCell(out, rs, "src_proteins");
		printTxtCell(out, rs, "entries");
		out.println();
	}

	/*
	sequence,
	modifier,
	score,
	fpr,
	src_proteins
	GROUP_CONCAT( entry ) AS entries,
	 */
	private void printHeadLine(PrintStream out, List<Workflow> workflowList)
	{
		printTxtCell(out, "sequence");
		printColSep(out);
		printTxtCell(out, "modifier");
		printColSep(out);
		printTxtCell(out, "max score");
		printColSep(out);
		printTxtCell(out, "FDR level");
		printColSep(out);
		printTxtCell(out, "number of source proteins");
		printColSep(out);
		printTxtCell(out, "source protein entries");
		out.println();
	}

	@Override public String getMenuItemText()
	{
		return "peptide identification (CSV)";
	}

	@Override public String getPluginName()
	{
		return "Peptide Identification Reporter";
	}

	@Override public String getHintFileNameSuffix()
	{
		return "_peptide_identification_report";
	}
}
