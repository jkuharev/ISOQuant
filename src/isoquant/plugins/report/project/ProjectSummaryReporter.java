/** ISOQuant, isoquant.plugins.report.project, Apr 1, 2015*/
package isoquant.plugins.report.project;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import de.mz.jk.jsix.libs.XExcel;
import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.plgs.data.Workflow;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.db.IQDBUtils;
import isoquant.kernel.plugin.SingleActionPlugin4DBExportToFile;
import isoquant.plugins.report.prep.ReportPreparator;

/**
 * <h3>{@link ProjectSummaryReporter}</h3>
 * @author kuharev
 * @version Apr 1, 2015 3:55:02 PM
 */
public class ProjectSummaryReporter extends SingleActionPlugin4DBExportToFile
{
	private final int firstRowShift = 0, firstColShift = 0;

	public static final String sql_file_workflow_info = XJava.getPackageResource( ReportPreparator.class, "prepare_workflow_info.sql" );

	public ProjectSummaryReporter(iMainApp app)
	{
		super( app );
	}

	@Override public String getHintFileNameSuffix()
	{
		return "_project_summary_report";
	}

	@Override public String getFileChooserTitle()
	{
		return "select excel file for " + getPluginName();
	}

	@Override public String[] getFileExtensions()
	{
		return "xls,xlsx".split( "," );
	}

	@Override public String getMenuItemText()
	{
		return "Experiment Summary Report";
	}

	@Override public String getMenuItemIconName()
	{
		return "xls";
	}

	@Override public void runExportAction(DBProject p, File tarFile) throws Exception
	{
		// if (!p.mysql.tableExists( "workflow_report" ))
		p.mysql.executeSQLFile( sql_file_workflow_info );
		XExcel xls = new XExcel( tarFile );
		
		if (p.mysql.tableExists( "workflow_report" ))
		{
			List<List<String>> reportData = p.mysql.listQuery( "SELECT * FROM workflow_report", true );
			xls.createSheetFromDataList( "project summary", reportData, 0, 0, xls.getCellStyle( "hlab" ) );
		}

		if (p.mysql.tableExists( "workflow_metadata" ))
		{
			System.out.println( "reading project metadata ..." );
			List<String> paramNames = p.mysql.getStringValues( "SELECT DISTINCT `name` FROM `workflow_metadata`" );
			List<Workflow> runs = IQDBUtils.getWorkflows( p );
			Map<String, String> paramValues = p.mysql.getMap(
					"SELECT `name`, GROUP_CONCAT(`workflow_index`, ':', `value` SEPARATOR ';') as `values`\n" +
							"FROM  `workflow_metadata` GROUP BY `name`", 1, 2 );
			
			Sheet sheet = xls.getSheet( "run metadata" );
			int ri = firstRowShift, ci = firstColShift;
			Row r = sheet.createRow( ri );
			// write labels
			xls.setCell( r, ci, "parameter name", xls.getCellStyle( "hlab" ) );
			for ( Workflow run : runs )
			{
				xls.setCell( r, ++ci, XJava.decURL( run.replicate_name ), xls.getCellStyle( "hlab" ) );
			}
			xls.autoSizeColumns( sheet, firstColShift + 1, firstColShift + 1 + runs.size() );
			for ( String param : paramNames )
			{
				ci = firstColShift;
				r = sheet.createRow( ++ri );
				xls.setCell( r, ci, XJava.decURL( param ) );
				Map<Integer, String> runValues = IQDBUtils.extractI2SMap( paramValues.get( param ), ";", ":" );
				for ( Workflow run : runs )
				{
					xls.setCell( r, ++ci,
							runValues.containsKey( run.index )
									? XJava.decURL( runValues.get( run.index ) )
									: "" );
				}
			}
			sheet.autoSizeColumn( firstColShift );
			System.out.println( "reading project metadata ... [done!]" );
		}
		System.out.println( "writing report to file ... " );
		System.out.println( "	" + xls.getXlsFile() );
		xls.save();
		System.out.println( "[done!]" );
	}
}
