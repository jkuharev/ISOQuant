/** ISOQuant, isoquant.plugins.exporting, 22.01.2013*/
package isoquant.plugins.exporting;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.plgs.data.Workflow;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.db.IQDBUtils;
import isoquant.kernel.plugin.SingleActionPlugin4DBExportToCSV;

/**
 * <h3>{@link SimpleRTW2CSVExporter}</h3>
 * @author kuharev
 * @version 22.01.2013 17:01:05
 */
public class SimpleRTW2CSVExporter extends SingleActionPlugin4DBExportToCSV
{
	public SimpleRTW2CSVExporter(iMainApp app){	super(app);	}

	@Override public String getPluginName(){return "RTW to CSV Exporter";}

	@Override public void runExportAction(DBProject prj, File tarFile)
	{
		try
		{
			createReport(prj, tarFile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void createReport(DBProject p, File file) throws Exception
	{
		PrintStream out = new PrintStream(file);
		List<Workflow> runs = IQDBUtils.getWorkflows(p);
		
		printTxtCell(out, "original time");
		for(Workflow r : runs)
		{
			printColSep(out);
			printTxtCell(out, r.replicate_name);
		}
		out.println();
		
		MySQL db = p.mysql;
		List<String> lines = db.getStringValues(
			"SELECT\n" + 
			"	CONCAT(`time`, ';', GROUP_CONCAT(ROUND(`ref_rt`,3) ORDER BY `run` ASC SEPARATOR ';')) \n" + 
			"FROM \n" + 
			"	`rtw`\n" + 
			"GROUP BY \n" + 
			"	`time`\n" + 
			"ORDER BY `time` ASC"		
		);
		for(String row : lines)
		{
			String[] cells = row.split(";");
			for(int i=0; i<cells.length; i++)
			{
				if(i>0) printColSep(out);
				printNumCell(out, cells[i]);
			}
			out.println();
		}
		out.flush();
		out.close();
	}

	@Override public String getMenuItemText(){return "export RTW to CSV";}
	@Override public String getHintFileNameSuffix(){return "_rtw_origin2ref";}
}
