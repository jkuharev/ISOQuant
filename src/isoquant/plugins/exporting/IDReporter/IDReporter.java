/** ISOQuant, isoquant.plugins.exporting, 18.12.2012 */
package isoquant.plugins.exporting.IDReporter;

import java.io.File;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import de.mz.jk.jsix.libs.XExcel;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.plgs.data.Workflow;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.db.IQDBUtils;
import isoquant.kernel.plugin.SingleActionPlugin4DBExportToFile;
import isoquant.plugins.processing.annotation.AnnotationFilter;

/**
 * <h3>{@link IDReporter}</h3>
 * @author kuharev
 * @version 18.12.2012 14:12:36
 */
public class IDReporter extends SingleActionPlugin4DBExportToFile
{
	boolean printColNames = false;

	/**
	 * @param app
	 */
	public IDReporter(iMainApp app)
	{
		super(app);
	}
	public AnnotationFilter AF = new AnnotationFilter();

	@Override public void runExportAction(DBProject prj, File tarFile)
	{
		AF.loadSettings(app.getSettings());
		MySQL db = prj.mysql;
// db.executeSQLFile( getPackageResource("00_prepare.sql"), AF );
		XExcel xls = new XExcel(tarFile);
		List<Workflow> runs = IQDBUtils.getWorkflows(prj);
		Sheet sumSheet = xls.getSheet("summary");
		Row row = xls.getRow(sumSheet, 0);
		int col = 0;
		xls.setCell(row, col++, "DBProject");
		xls.setCell(row, col++, "Sample");
		xls.setCell(row, col++, "Run");
		xls.setCell(row, col++, "PLGS 1+ proteins");
		xls.setCell(row, col++, "PLGS 1+ FP prots");
		xls.setCell(row, col++, "PLGS 2+ proteins");
		xls.setCell(row, col++, "PLGS 2+ FP prots");
		xls.setCell(row, col++, "PLGS 1+ peptides");
		xls.setCell(row, col++, "PLGS 1+ FP pepts");
		xls.setCell(row, col++, "PLGS 2+ peptides");
// xls.setCell(row, col++, "PLGS 2+ FP pepts");
		xls.setCell(row, col++, "IQ 1+ proteins");
		xls.setCell(row, col++, "IQ 1+ FP");
		xls.setCell(row, col++, "IQ 2+ proteins");
		xls.setCell(row, col++, "IQ 2+ FP");
		xls.setCell(row, col++, "IQ 1+ peptides");
		xls.setCell(row, col++, "IQ 1+ FP pepts");
		xls.setCell(row, col++, "IQ 2+ peptides");
// xls.setCell(row, col++, "IQ 2+ FP pepts");
		int nRuns = runs.size();
		System.out.println("reporting " + nRuns + " runs ...");
		Bencher ba = new Bencher(true);
		// list ids for each run separately
		for (int i = 0; i < nRuns; i++)
		{
			Bencher b = new Bencher(true);
			System.out.print("	" + (i + 1) + ": . ");
			Workflow run = runs.get(i);
			row = xls.getRow(sumSheet, i + 1);
			col = 0;
			xls.setCell( row, col++, prj.data.title );
			xls.setCell(row, col++, run.sample_description);
			xls.setCell(row, col++, i + 1);
			xls.setCell(row, col++, plotPLGSProteinsBy1(db, run, xls, i));
			System.out.print(" . ");
			xls.setCell(row, col++, countPLGSProteinsBy1FP(db, run));
			System.out.print(" . ");
			xls.setCell(row, col++, plotPLGSProteinsBy2(db, run, xls, i));
			System.out.print(" . ");
			xls.setCell(row, col++, countPLGSProteinsBy2FP(db, run));
			System.out.print(" . ");
			xls.setCell(row, col++, plotPLGSPeptidesBy1(db, run, xls, i));
			System.out.print(" . ");
			xls.setCell(row, col++, countPLGSPeptidesBy1FP(db, run));
			System.out.print(" . ");
			xls.setCell(row, col++, plotPLGSPeptidesBy2(db, run, xls, i));
			System.out.print(" . ");
			xls.setCell(row, col++, plotIQProteinsBy1(db, run, xls, i));
			System.out.print(" . ");
			xls.setCell(row, col++, countIQProteinsBy1FP(db, run));
			System.out.print(" . ");
			xls.setCell(row, col++, plotIQProteinsBy2(db, run, xls, i));
			System.out.print(" . ");
			xls.setCell(row, col++, countIQProteinsBy2FP(db, run));
			System.out.print(" . ");
			xls.setCell(row, col++, plotIQPeptidesBy1(db, run, xls, i));
			System.out.print(" . ");
			xls.setCell(row, col++, countIQPeptidesBy1FP(db, run));
			System.out.print(" . ");
			xls.setCell(row, col++, plotIQPeptidesBy2(db, run, xls, i));
			System.out.print(" . ");
			System.out.println("[" + b.stop().getSecString() + "]");
		}
		for (int c = 0; c < col; c++)
			sumSheet.autoSizeColumn(c);
		xls.save();
		System.out.println("report created! [" + ba.stop().getSecString() + "]");
	}

	private int countIQProteinsBy1FP(MySQL db, Workflow run)
	{
		return db.getFirstInt(
				"SELECT count(DISTINCT entry) FROM `finalquant` \n" +
						"WHERE workflow_index='" + run.index + "' AND (entry LIKE '%REVERSE%' OR entry LIKE '%RANDOM%')",
				1);
	}

	private int countIQProteinsBy2FP(MySQL db, Workflow run)
	{
		return db.getFirstInt(
				"SELECT count(DISTINCT entry) FROM (\n" +
						"SELECT entry,COUNT(DISTINCT sequence, modifier) as rpc \n" +
						"FROM finalquant JOIN `report_peptide_quantification` as rep USING(entry) \n" +
						"WHERE finalquant.workflow_index=" + run.index + " AND (entry LIKE '%REVERSE%' OR entry LIKE '%RANDOM%') \n" +
						"GROUP BY entry HAVING rpc>1 \n" +
						") as x",
				1);
	}

	private int countIQPeptidesBy1FP(MySQL db, Workflow run)
	{
		return db.getFirstInt(
				"SELECT count(DISTINCT sequence) FROM emrt4quant \n" +
						"WHERE workflow_index='" + run.index + "' AND (entry LIKE '%REVERSE%' OR entry LIKE '%RANDOM%')"
				, 1
				);
	}

	private int plotIQProteinsBy2(MySQL db, Workflow run, XExcel xls, int col)
	{
		Sheet sheet = xls.getSheet("IQ Proteins 2+");
		int ri = 0;
		if (printColNames) xls.setCell(xls.getRow(sheet, ri++), col, col + ": " + run.replicate_name);
		List<String> ids = db.getStringValues(
				"SELECT entry,COUNT(DISTINCT sequence, modifier) as rpc \n" +
						"FROM finalquant JOIN `report_peptide_quantification` as rep USING(entry) \n" +
						"WHERE finalquant.workflow_index=" + run.index + " GROUP BY entry HAVING rpc>1"
				);
		for (String id : ids)
		{
			xls.setCell(xls.getRow(sheet, ri++), col, id);
		}
		return ids.size();
	}

	private int plotIQProteinsBy1(MySQL db, Workflow run, XExcel xls, int col)
	{
		Sheet sheet = xls.getSheet("IQ Proteins 1+");
		int ri = 0;
		if (printColNames) xls.setCell(xls.getRow(sheet, ri++), col, col + ": " + run.replicate_name);
		List<String> ids = db.getStringValues("SELECT DISTINCT entry FROM `finalquant` WHERE workflow_index='" + run.index + "'");
		for (String id : ids)
		{
			xls.setCell(xls.getRow(sheet, ri++), col, id);
		}
		return ids.size();
	}

	private int plotIQPeptidesBy1(MySQL db, Workflow run, XExcel xls, int col)
	{
		Sheet sheet = xls.getSheet("IQ 1+ Peptides");
		int ri = 0;
		if (printColNames) xls.setCell(xls.getRow(sheet, ri++), col, col + ": " + run.replicate_name);
		List<String> ids = db.getStringValues(
				"SELECT CONCAT(sequence, ' ', modifier) \n" +
						"FROM best_peptides_for_quantification JOIN clustered_emrt USING(`cluster_average_index`)\n" +
						"WHERE workflow_index='" + run.index + "' GROUP BY sequence, modifier"
				);
		for (String id : ids)
		{
			xls.setCell(xls.getRow(sheet, ri++), col, id);
		}
		return ids.size();
	}

	private int plotIQPeptidesBy2(MySQL db, Workflow run, XExcel xls, int col)
	{
		Sheet sheet = xls.getSheet("IQ 2+ Peptides");
		int ri = 0;
		if (printColNames) xls.setCell(xls.getRow(sheet, ri++), col, col + ": " + run.replicate_name);
		List<String> ids = db.getStringValues(
				"SELECT CONCAT(sequence, ' ', modifier) FROM emrt4quant JOIN \n" +
						"(SELECT sequence, modifier, count(DISTINCT workflow_index) as r FROM emrt4quant GROUP BY sequence, modifier) as x \n" +
						"USING(sequence, modifier) WHERE workflow_index='" + run.index + "' AND r>1 GROUP BY sequence, modifier"
				);
		for (String id : ids)
		{
			xls.setCell(xls.getRow(sheet, ri++), col, id);
		}
		return ids.size();
	}

	private int countPLGSProteinsBy1FP(MySQL db, Workflow run)
	{
		return db.getFirstInt(
				AF.processSQLStatementBeforeExecution(
						"SELECT count(entry) FROM (" +
								"SELECT DISTINCT `entry` FROM " +
								"	`peptide` as pep JOIN protein as pro ON pep.`protein_index`=pro.`index` " +
								"	WHERE \n" +
								"		pep.workflow_index='" + run.index + "' AND \n" +
								"		pro.aq_fmoles > 0 AND \n" +
								"		pep.`type` IN (%PEPTIDE_TYPE_FILTER_ADDITIONAL_TYPES%)\n" +
								"		AND length(pep.`sequence`) >= %PEPTIDE_SEQUENCE_LENGTH_MIN_VALUE% \n" +
								"		AND (entry LIKE '%REVERSE%' OR entry LIKE '%RANDOM%') \n" +
								") as x"
						)
				, 1
				);
	}

	private int countPLGSProteinsBy2FP(MySQL db, Workflow run)
	{
		return db.getFirstInt(
				AF.processSQLStatementBeforeExecution(
						"SELECT count(entry) FROM (" +
								"SELECT `entry`, count(pep.`index`) as idr FROM " +
								"	`peptide` as pep JOIN protein as pro ON pep.`protein_index`=pro.`index` " +
								"	WHERE \n" +
								"		pep.workflow_index='" + run.index + "' AND \n" +
								"		pro.aq_fmoles > 0 AND \n" +
								"		pep.`type` IN (%PEPTIDE_TYPE_FILTER_ADDITIONAL_TYPES%)\n" +
								"		AND length(pep.`sequence`) >= %PEPTIDE_SEQUENCE_LENGTH_MIN_VALUE% \n" +
								"		AND (entry LIKE '%REVERSE%' OR entry LIKE '%RANDOM%') \n" +
								"	GROUP BY `entry`" +
								"	HAVING idr > 1" +
								") as x"
						)
				, 1
				);
	}

	private int countPLGSPeptidesBy1FP(MySQL db, Workflow run)
	{
		return db.getFirstInt(
				AF.processSQLStatementBeforeExecution(
						"SELECT count(DISTINCT pep.`sequence`) FROM " +
								"		`peptide` as pep JOIN protein as pro ON pep.`protein_index`=pro.`index` \n" +
								"	WHERE pep.workflow_index='" + run.index + "' \n" +
								"		AND pep.`type` IN (%PEPTIDE_TYPE_FILTER_ADDITIONAL_TYPES%)\n" +
								"		AND length(pep.`sequence`) >= %PEPTIDE_SEQUENCE_LENGTH_MIN_VALUE% \n" +
								"		AND (entry LIKE '%REVERSE%' OR entry LIKE '%RANDOM%') "
						)
				, 1
				);
	}

	private int plotPLGSProteinsBy2(MySQL db, Workflow run, XExcel xls, int col)
	{
		Sheet sheet = xls.getSheet("PLGS Proteins 2+");
		int ri = 0;
		if (printColNames) xls.setCell(xls.getRow(sheet, ri++), col, col + ": " + run.replicate_name);
		List<String> ids = db.getStringValues(
				AF.processSQLStatementBeforeExecution(
						"SELECT `entry`, count(pep.`index`) as idr FROM " +
								"	peptide as pep JOIN protein as pro ON pep.`protein_index`=pro.`index` " +
								"	WHERE \n" +
								"		pep.workflow_index='" + run.index + "' AND \n" +
								"		pro.aq_fmoles > 0 AND \n" +
								"		pep.`type` IN (%PEPTIDE_TYPE_FILTER_ADDITIONAL_TYPES%)\n" +
								"		AND length(pep.`sequence`) >= %PEPTIDE_SEQUENCE_LENGTH_MIN_VALUE% " +
								"	GROUP BY `entry`" +
								"	HAVING idr > 1 "
						)
				);
		for (String id : ids)
		{
			xls.setCell(xls.getRow(sheet, ri++), col, id);
		}
		return ids.size();
	}

	private int plotPLGSProteinsBy1(MySQL db, Workflow run, XExcel xls, int col)
	{
		Sheet sheet = xls.getSheet("PLGS Proteins 1+");
		int ri = 0;
		if (printColNames) xls.setCell(xls.getRow(sheet, ri++), col, col + ": " + run.replicate_name);
		List<String> ids = db.getStringValues(
				AF.processSQLStatementBeforeExecution(
						"SELECT DISTINCT `entry` FROM " +
								"	`peptide` as pep JOIN protein as pro ON pep.`protein_index`=pro.`index` " +
								"	WHERE \n" +
								"		pep.workflow_index='" + run.index + "' AND \n" +
								"		pro.aq_fmoles > 0 AND \n" +
								"		pep.`type` IN (%PEPTIDE_TYPE_FILTER_ADDITIONAL_TYPES%)\n" +
								"		AND length(pep.`sequence`) >= %PEPTIDE_SEQUENCE_LENGTH_MIN_VALUE% "
						)
				);
		for (String id : ids)
		{
			xls.setCell(xls.getRow(sheet, ri++), col, id);
		}
		return ids.size();
	}

	private int plotPLGSPeptidesBy1(MySQL db, Workflow run, XExcel xls, int col)
	{
		Sheet sheet = xls.getSheet("PLGS 1+ Peptides");
		int ri = 0;
		if (printColNames) xls.setCell(xls.getRow(sheet, ri++), col, col + ": " + run.replicate_name);
		List<String> ids = db.getStringValues(
				AF.processSQLStatementBeforeExecution(
						"SELECT DISTINCT `sequence` FROM `peptide` WHERE workflow_index='" + run.index + "' AND \n" +
								"		`type` IN (%PEPTIDE_TYPE_FILTER_ADDITIONAL_TYPES%)\n" +
								"		AND length(`sequence`) >= %PEPTIDE_SEQUENCE_LENGTH_MIN_VALUE% "
						)
				);
		for (String id : ids)
		{
			xls.setCell(xls.getRow(sheet, ri++), col, id);
		}
		return ids.size();
	}

	private int plotPLGSPeptidesBy2(MySQL db, Workflow run, XExcel xls, int col)
	{
		Sheet sheet = xls.getSheet("PLGS 2+ Peptides");
		int ri = 0;
		if (printColNames) xls.setCell(xls.getRow(sheet, ri++), col, col + ": " + run.replicate_name);
		List<String> ids = db
				.getStringValues(
				AF.processSQLStatementBeforeExecution(
						" SELECT DISTINCT sequence FROM peptide JOIN ( "
								+ "	SELECT sequence, count(DISTINCT workflow_index) as r FROM peptide "
										+ "	WHERE `type` IN (%PEPTIDE_TYPE_FILTER_ADDITIONAL_TYPES%) AND length(`sequence`) >= %PEPTIDE_SEQUENCE_LENGTH_MIN_VALUE% "
								+ "	GROUP BY sequence HAVING r > 1) as x USING(sequence) WHERE workflow_index='" + run.index + "'"
						)
				);
		for (String id : ids)
		{
			xls.setCell(xls.getRow(sheet, ri++), col, id);
		}
		return ids.size();
	}

	@Override public String getPluginName()
	{
		return "Identity Reporter";
	}

// @Override public String getHintFileName(DBProject p)
// {
// return p.title.replaceAll("\\W+", "_") + "_ID_Report.xlsx";
// }
	@Override public String getHintFileNameSuffix()
	{
		return "_id_report";
	}

	@Override public String getFileChooserTitle()
	{
		return "select file for ID-Report ";
	}

	@Override public String[] getFileExtensions()
	{
		return new String[] { "xlsx", "xls" };
	}

	@Override public String getMenuItemText()
	{
		return "report IDs";
	}

	@Override public String getMenuItemIconName()
	{
		return "printer";
	}
}
