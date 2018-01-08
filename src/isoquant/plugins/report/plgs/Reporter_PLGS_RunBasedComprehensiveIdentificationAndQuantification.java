/** ISOQuant, isoquant.plugins.report.plgs, 25.06.2013 */
package isoquant.plugins.report.plgs;

import java.io.File;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mz.jk.jsix.libs.XFiles;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.plgs.data.Workflow;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.db.IQDBUtils;
import isoquant.kernel.plugin.SingleActionPlugin4DBExportToCSV;

/**
 * <h3>{@link Reporter_PLGS_RunBasedComprehensiveIdentificationAndQuantification}</h3>
 * @author kuharev
 * @version 25.06.2013 15:10:19
 */
public class Reporter_PLGS_RunBasedComprehensiveIdentificationAndQuantification extends SingleActionPlugin4DBExportToCSV
{
	/**
	 * @param app
	 */
	public Reporter_PLGS_RunBasedComprehensiveIdentificationAndQuantification(iMainApp app)
	{
		super(app);
	}

	@Override public String getPluginName()
	{
		return "PLGS Run Based Coprehensive Identification and Quantification Report";
	}

	@Override public String getMenuItemText()
	{
		return "PLGS Run Based Coprehensive Identification and Quantification";
	}

	@Override public String getHintFileNameSuffix()
	{
		return "_PLGS_Report";
	}

	@Override public void runExportAction(DBProject prj, File tarFile)
	{
		System.out.println("creating PLGS Identification and Quantification report ...");
		Bencher b = new Bencher(true);
		Map<Integer, String> spls = IQDBUtils.getSampleIndexToNameMap(prj.mysql);
		List<Workflow> runs = IQDBUtils.getWorkflows(prj);
		try
		{
			System.out.println("printing PLGS run overview table ...");
			Map<Workflow, File> run2file = printRunOverview(runs, spls, tarFile);
			app.getProcessProgressListener().setProgressMaxValue(runs.size());
			for (int i = 0; i < runs.size(); i++)
			{
				app.getProcessProgressListener().setProgressValue(i);
				Workflow run = runs.get(i);
				System.out.println("	creating PLGS data report for run " + run.index + " ... ");
				printRunData(prj.mysql, run.index, run2file.get(run));
			}
			app.getProcessProgressListener().setProgressValue(0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("PLGS Identification and Quantification report created [" + b.stop().getSecString() + "]");
	}

	private void printRunData(MySQL db, int index, File file) throws Exception
	{
		PrintStream out = new PrintStream(file);
		ResultSet rs = db.executeSQL(
				"SELECT\n" +
						"	`mass_spectrum`.`Mobility`	as	`mass_spectrum.Mobility`	,\n" +
						"	`mass_spectrum`.`MassSD`	as	`mass_spectrum.MassSD`	,\n" +
						"	`mass_spectrum`.`RT`	as	`mass_spectrum.RT`	,\n" +
						"	`mass_spectrum`.`RTSD`	as	`mass_spectrum.RTSD`	,\n" +
						"	`mass_spectrum`.`FWHM`	as	`mass_spectrum.FWHM`	,\n" +
						"	`query_mass`.`intensity`	as	`query_mass.intensity`	,\n" +
						"	`low_energy`.`charge`	as	`low_energy.charge`	,\n" +
						"	`peptide`.`mass`	as	`peptide.mass`	,\n" +
						"	`peptide`.`sequence`	as	`peptide.sequence`	,\n" +
						"	`peptide`.`type`	as	`peptide.type`	,\n" +
						"	`peptide`.`modifier`	as	`peptide.modifier`	,\n" +
						"	`peptide`.`rms_mass_error_prod`	as	`peptide.rms_mass_error_prod`	,\n" +
						"	`peptide`.`rms_rt_error_prod`	as	`peptide.rms_rt_error_prod`	,\n" +
						"	`peptide`.`mass_error`	as	`peptide.mass_error`	,\n" +
						"	`peptide`.`mass_error_ppm`	as	`peptide.mass_error_ppm`	,\n" +
						"	`peptide`.`score`	as	`peptide.score`	,\n" +
						"	`protein`.`entry`	as	`protein.entry`	,\n" +
						"	`protein`.`accession`	as	`protein.accession`	,\n" +
						"	`protein`.`description`	as	`protein.description`	,\n" +
						"	`protein`.`coverage`	as	`protein.coverage`	,\n" +
						"	`protein`.`score`	as	`protein.score`	\n" +
						"FROM \n" +
						"	`peptide`\n" +
						"        LEFT JOIN `protein` ON `peptide`.`protein_index`=`protein`.`index`\n" +
						"        LEFT JOIN `query_mass` ON `peptide`.`query_mass_index`=`query_mass`.`index`\n" +
						"        LEFT JOIN `low_energy` ON `query_mass`.`low_energy_index`=`low_energy`.`index`\n" +
						"        LEFT JOIN `mass_spectrum` ON `low_energy`.`index`=`mass_spectrum`.`low_energy_index`\n" +
						"WHERE\n" +
						"	`peptide`.`workflow_index`=1\n" +
						"ORDER BY\n" +
						"	`protein.entry`, `peptide.sequence`");
		// print column labels
		ResultSetMetaData meta = rs.getMetaData();
		for (int i = 1; i <= meta.getColumnCount(); i++)
		{
			printTxtCell(out, meta.getColumnLabel(i));
			printColSep(out);
		}
		out.println();
		while (rs.next())
		{
			printNumCell(out, rs.getString("mass_spectrum.Mobility"));
			printColSep(out);
			printNumCell(out, rs.getString("mass_spectrum.MassSD"));
			printColSep(out);
			printNumCell(out, rs.getString("mass_spectrum.RT"));
			printColSep(out);
			printNumCell(out, rs.getString("mass_spectrum.RTSD"));
			printColSep(out);
			printNumCell(out, rs.getString("mass_spectrum.FWHM"));
			printColSep(out);
			printNumCell(out, rs.getString("query_mass.intensity"));
			printColSep(out);
			printNumCell(out, rs.getString("low_energy.charge"));
			printColSep(out);
			printNumCell(out, rs.getString("peptide.mass"));
			printColSep(out);
			printTxtCell(out, rs.getString("peptide.sequence"));
			printColSep(out);
			printTxtCell(out, rs.getString("peptide.type"));
			printColSep(out);
			printTxtCell(out, rs.getString("peptide.modifier"));
			printColSep(out);
			printNumCell(out, rs.getString("peptide.rms_mass_error_prod"));
			printColSep(out);
			printNumCell(out, rs.getString("peptide.rms_rt_error_prod"));
			printColSep(out);
			printNumCell(out, rs.getString("peptide.mass_error"));
			printColSep(out);
			printNumCell(out, rs.getString("peptide.mass_error_ppm"));
			printColSep(out);
			printNumCell(out, rs.getString("peptide.score"));
			printColSep(out);
			printTxtCell(out, rs.getString("protein.entry"));
			printColSep(out);
			printTxtCell(out, rs.getString("protein.accession"));
			printColSep(out);
			printTxtCell(out, rs.getString("protein.description"));
			printColSep(out);
			printNumCell(out, rs.getString("protein.coverage"));
			printColSep(out);
			printNumCell(out, rs.getString("protein.score"));
			out.println();
		}
		out.flush();
		out.close();
	}

	private Map<Workflow, File> printRunOverview(List<Workflow> runs, Map<Integer, String> spls, File tarFile) throws Exception
	{
		String tarFileBase = XFiles.getBaseName(tarFile);
		Map<Workflow, File> run2file = new HashMap<Workflow, File>();
		PrintStream out = new PrintStream(tarFile);
		printTxtCell(out, "run index");
		printColSep(out);
		printTxtCell(out, "sample name");
		printColSep(out);
		printTxtCell(out, "sample description");
		printColSep(out);
		printTxtCell(out, "run title");
		printColSep(out);
		printTxtCell(out, "replicate name");
		printColSep(out);
		printTxtCell(out, "input file");
		printColSep(out);
		printTxtCell(out, "acquired name");
		printColSep(out);
		printTxtCell(out, "report file name");
		printColSep(out);
		out.println();
		for (Workflow run : runs)
		{
			String runReportFileName = tarFileBase + "_run_" + run.index + ".csv";
			run2file.put(run, new File(tarFile.getParentFile(), runReportFileName));
			printNumCell(out, run.index + "");
			printColSep(out);
			printTxtCell(out, spls.get(run.index));
			printColSep(out);
			printTxtCell(out, run.sample_description);
			printColSep(out);
			printTxtCell(out, run.title);
			printColSep(out);
			printTxtCell(out, run.replicate_name);
			printColSep(out);
			printTxtCell(out, run.input_file);
			printColSep(out);
			printTxtCell(out, run.acquired_name);
			printColSep(out);
			printTxtCell(out, runReportFileName);
			printColSep(out);
			out.println();
		}
		out.flush();
		out.close();
		return run2file;
	}
}
