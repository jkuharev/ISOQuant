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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.plgs.data.Workflow;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.db.IQDBUtils;
import isoquant.kernel.plugin.SingleActionPlugin4DBExportToCSV;

/**
 * <h3>{@link PeptideQuantificationReporter}</h3>
 * create a CSV Peptide Report
 * @author Joerg Kuharev
 * @version 16.09.2011 09:22:08
 */
public class PeptideQuantificationReporter extends SingleActionPlugin4DBExportToCSV
{
	public PeptideQuantificationReporter(iMainApp app)
	{
		super(app);
	}
	private boolean showFDR = false;

	@Override public void runExportAction(DBProject p, File file)
	{
		PrintStream out = null;
		Bencher t = new Bencher(true);
		System.out.println( "creating peptide quantification report for project '" + p.data.title + "' ..." );
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
		System.out.println("peptide quantification report creation duration: [" + t.stop().getSecString() + "]");
	}

	private void createCSVReport(DBProject p, PrintStream out)
	{
		MySQL db = p.mysql;
		List<Workflow> workflowList = IQDBUtils.getWorkflows(p);
		/* add average mobility if not there */
		if (!db.columnExists("cluster_info_runs", "avg_drift_time"))
		{
			db.addColumn("cluster_info_runs", "avg_drift_time", "FLOAT DEFAULT 0 AFTER avg_ref_rt", true);
			db.executeSQL("UPDATE cluster_info_runs JOIN " +
					" (SELECT `cluster_average_index`, AVG(Mobility) as avg_mobility " +
					" FROM clustered_emrt GROUP BY `cluster_average_index`) as XX " +
					" USING(`cluster_average_index`)" +
					" SET avg_drift_time=avg_mobility");
		}
		if (db.tableExists("pep_fpr"))
		{
			showFDR = true;
			if (db.columnExists("pep_fpr", "modifier"))
				db.executeSQLFile(getPackageResource("peptide_quantification_report.sql"));
			else
				db.executeSQLFile( getPackageResource( "peptide_quantification_report_no_modifier.sql" ) );
		}
		else
		{
			showFDR = false;
			db.executeSQLFile(getPackageResource("peptide_quantification_report_no_fdr.sql"));
		}
		try
		{
			ResultSet rs = db.getStatement().getResultSet();
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

	private void printDataLine(PrintStream out, ResultSet rs, List<Workflow> workflowList) throws Exception
	{
		printNumCell(out, rs, "cluster");
		printNumCell(out, rs, "avg_mass");
		printNumCell(out, rs, "avg_rt");
		printNumCell(out, rs, "avg_ref_rt");
		printNumCell(out, rs, "avg_drift_time");
		Map<Integer, Double> r2i = extractRunIntensityMap(rs.getString("intensities"));
		for (Workflow w : workflowList)
		{
			if (r2i.containsKey(w.index))
			{
				out.print(r2i.get(w.index).longValue() + colSep);
			}
			else
			{
				out.print(colSep);
			}
		}
		printNumCell(out, rs, "workflow_index");
		printTxtCell(out, rs, "sequence");
		printTxtCell(out, rs, "modifier");
		printTxtCell(out, rs, "type");
		printNumCell(out, rs, "start");
		printNumCell(out, rs, "end");
		printTxtCell(out, rs, "frag_string");
		printNumCell(out, rs, "products");
		printNumCell(out, rs, "peptide_annotated_max_score");
		printNumCell(out, rs, "peptide_overall_max_score");
		if (showFDR) printNumCell(out, rs, "peptide_fdr_level");
		printNumCell(out, rs, "peptide_annotated_replication_rate");
		printNumCell(out, rs, "peptide_overall_replication_rate");
		printNumCell(out, rs, "signal_mass");
		printNumCell(out, rs, "signal_intensity");
		printNumCell(out, rs, "signal_charge");
		printNumCell(out, rs, "signal_z");
		printNumCell(out, rs, "signal_fwhm");
		printNumCell(out, rs, "signal_rt");
		printNumCell(out, rs, "signal_liftoffrt");
		printNumCell(out, rs, "signal_infuprt");
		printNumCell(out, rs, "signal_infdown_rt");
		printNumCell(out, rs, "signal_touchdownrt");
		printTxtCell(out, rs, "pre_homology_entries");
		printTxtCell(out, rs, "pre_homology_accessions");
		printTxtCell(out, rs, "post_homology_entries");
		printTxtCell(out, rs, "post_homology_accessions");
		printTxtCell(out, rs, "entry");
		printTxtCell(out, rs, "accession");
		printTxtCell(out, rs, "description");
		printNumCell(out, rs, "mw");
		printNumCell(out, rs, "pi");
		printNumCell(out, rs, "protein_score");
		printNumCell(out, rs, "coverage");
		printNumCell(out, rs, "protein_replication_rate");
		printNumCell(out, rs, "protein_assigned_peptides_rate");
		printNumCell( out, rs, "protein_unique_paptides_rate", false );
		out.println();
	}

	private Map<Integer, Double> extractRunIntensityMap(String string)
	{
		if (string.endsWith(";")) string = string.substring(0, string.length() - 1);
		Map<Integer, Double> res = new HashMap<Integer, Double>();
		String[] elements = string.split(";");
		for (String e : elements)
		{
			String[] kv = e.split(":");
			res.put(Integer.parseInt(kv[0]), Double.parseDouble(kv[1]));
		}
		return res;
	}

	/*
	cluster 
	avg_mass 
	avg_rt 
	avg_ref_rt
	avg_drift_time
	intensities 
	workflow_index 
	sequence 
	modifier 
	type 
	start 
	end 
	frag_string 
	products 
	peptide_annotated_max_score 
	peptide_overall_max_score 
	peptide_fdr_level
	peptide_annotated_replicate_rate 
	peptide_overall_replication_rate 
	signal_mass 
	signal_intensity 
	signal_charge 
	signal_z
	signal_fwhm 
	signal_rt 
	signal_liftoffrt 
	signal_infuprt 
	signal_infdown_rt 
	signal_touchdownrt 
	pre_homology_entries 
	pre_homology_accessions 
	post_homology_entries 
	post_homology_accessions 
	entry 
	accession 
	description 
	mw 
	pi 
	protein_score 
	coverage 
	protein_replication_rate 
	protein_assigned_peptides_rate 
	protein_unique_paptides_rate 
	 */
	private void printHeadLine(PrintStream out, List<Workflow> workflowList)
	{
		out.print(quoteChar + "cluster" + quoteChar + colSep);
		out.print(quoteChar + "avg_mass" + quoteChar + colSep);
		out.print(quoteChar + "avg_rt" + quoteChar + colSep);
		out.print(quoteChar + "avg_ref_rt" + quoteChar + colSep);
		out.print(quoteChar + "avg_drift_time" + quoteChar + colSep);
		for (Workflow w : workflowList)
		{
			out.print(
					quoteChar +
							"intensity in " + XJava.decURL(w.replicate_name.replaceAll(quoteChar, " "))
							+ quoteChar + colSep);
		}
		out.print(quoteChar + "workflow_index" + quoteChar + colSep);
		out.print(quoteChar + "sequence" + quoteChar + colSep);
		out.print(quoteChar + "modifier" + quoteChar + colSep);
		out.print(quoteChar + "type" + quoteChar + colSep);
		out.print(quoteChar + "start" + quoteChar + colSep);
		out.print(quoteChar + "end" + quoteChar + colSep);
		out.print(quoteChar + "frag_string" + quoteChar + colSep);
		out.print(quoteChar + "products" + quoteChar + colSep);
		out.print(quoteChar + "peptide_annotated_max_score" + quoteChar + colSep);
		out.print(quoteChar + "peptide_overall_max_score" + quoteChar + colSep);
		if (showFDR) out.print(quoteChar + "peptide_fdr_level" + quoteChar + colSep);
		out.print(quoteChar + "peptide_annotated_replication_rate" + quoteChar + colSep);
		out.print(quoteChar + "peptide_overall_replication_rate" + quoteChar + colSep);
		out.print(quoteChar + "signal_mass" + quoteChar + colSep);
		out.print(quoteChar + "signal_intensity" + quoteChar + colSep);
		out.print(quoteChar + "signal_charge" + quoteChar + colSep);
		out.print(quoteChar + "signal_z" + quoteChar + colSep);
		out.print(quoteChar + "signal_fwhm" + quoteChar + colSep);
		out.print(quoteChar + "signal_rt" + quoteChar + colSep);
		out.print(quoteChar + "signal_liftoffrt" + quoteChar + colSep);
		out.print(quoteChar + "signal_infuprt" + quoteChar + colSep);
		out.print(quoteChar + "signal_infdown_rt" + quoteChar + colSep);
		out.print(quoteChar + "signal_touchdownrt" + quoteChar + colSep);
		out.print(quoteChar + "pre_homology_entries" + quoteChar + colSep);
		out.print(quoteChar + "pre_homology_accessions" + quoteChar + colSep);
		out.print(quoteChar + "post_homology_entries" + quoteChar + colSep);
		out.print(quoteChar + "post_homology_accessions" + quoteChar + colSep);
		out.print(quoteChar + "entry" + quoteChar + colSep);
		out.print(quoteChar + "accession" + quoteChar + colSep);
		out.print(quoteChar + "description" + quoteChar + colSep);
		out.print(quoteChar + "mw" + quoteChar + colSep);
		out.print(quoteChar + "pi" + quoteChar + colSep);
		out.print(quoteChar + "protein_score" + quoteChar + colSep);
		out.print(quoteChar + "coverage" + quoteChar + colSep);
		out.print(quoteChar + "protein_replication_rate" + quoteChar + colSep);
		out.print(quoteChar + "protein_assigned_peptides_rate" + quoteChar + colSep);
		out.println( quoteChar + "protein_unique_peptides_rate" + quoteChar );
	}

	@Override public String getMenuItemText()
	{
		return "extended peptide quantification (CSV)";
	}

	@Override public String getPluginName()
	{
		return "Peptide Quantification Reporter";
	}

	@Override public String getHintFileNameSuffix()
	{
		return "_peptide_quantification_report";
	}
}
