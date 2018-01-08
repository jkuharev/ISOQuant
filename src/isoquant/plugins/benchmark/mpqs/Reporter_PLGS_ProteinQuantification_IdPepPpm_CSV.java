/** ISOQuant, isoquant.plugins.benchmark.mpqs, 17.01.2013 */
package isoquant.plugins.benchmark.mpqs;

import java.io.File;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.plgs.data.Workflow;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.db.IQDBUtils;
import isoquant.kernel.plugin.SingleActionPlugin4DBExportToCSV;

/**
 * <h3>{@link Reporter_PLGS_ProteinQuantification_IdPepPpm_CSV}</h3>
 * @author kuharev
 * @version 17.01.2013 09:36:04
 */
public class Reporter_PLGS_ProteinQuantification_IdPepPpm_CSV extends SingleActionPlugin4DBExportToCSV
{
	public Reporter_PLGS_ProteinQuantification_IdPepPpm_CSV(iMainApp app)
	{
		super(app);
	}

	@Override public String getPluginName()
	{
		return "PLGS Protein Quantification ID-PEP-PPM Reporter";
	}

	@Override public String getMenuItemText()
	{
		return "PLGS Protein Quantification ID-PEP-PPM";
	}

	@Override public int getExecutionOrder()
	{
		return 256;
	}

	@Override public String getHintFileNameSuffix()
	{
		return "_plgs.id.pep.qnt";
	}

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
		MySQL db = p.mysql;
		List<Workflow> runs = IQDBUtils.getWorkflows( p );
		PrintStream out = new PrintStream(file);
		printHeadLine( out, runs );
		db.executeSQLFile( getPackageResource( "collect_plgs_protein_amounts.sql" ), MySQL.defExecListener );
		ResultSet rs = db.executeSQL(
				"SELECT "
						+ " entry, "
						+ " score, "
						+ " stat_count_peptides as peptides, "
						+ " GROUP_CONCAT(workflow_index, ':', ROUND(ppm, 4) ORDER BY workflow_index ASC SEPARATOR ';') as ppm "
						+ " FROM protein_plgs_amounts JOIN protein_info USING(entry) "
						+ " GROUP BY entry ORDER BY score DESC "
				);
		try
		{
			int i;
			for (i = 0; rs.next(); i++)
			{
				printDataLine( out, rs, runs );
			}
			System.out.println("\t" + i + " data rows written to file " + file.getName());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		out.flush();
		out.close();
	}

	private void printHeadLine(PrintStream out, List<Workflow> runList)
	{
		printTxtCell( out, "entry" );
		out.print( colSep );
		printTxtCell( out, "score" );
		out.print( colSep );
		printTxtCell( out, "peptides" );
		String names = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		for ( Workflow w : runList )
		{
			out.print(colSep);
			printTxtCell( out, w.replicate_name );
			// "" + names.charAt((w.index - 1) % names.length()));
		}
		out.println();
	}

	private void printDataLine(PrintStream out, ResultSet rs, List<Workflow> runList) throws Exception
	{
		printTxtCell( out, rs.getString( "entry" ) );
		out.print( colSep );
		printTxtCell( out, rs.getString( "score" ) );
		out.print( colSep );
		printTxtCell( out, rs.getString( "peptides" ) );
		Map<Integer, String> r2i = IQDBUtils.extractI2SMap( rs.getString( "ppm" ) );
		for ( Workflow w : runList )
		{
			out.print(colSep);
			printNumCell(out, r2i.containsKey(w.index) ? r2i.get(w.index) : "0");
		}
		out.println();
	}
}
