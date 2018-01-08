/** ISOQuant, isoquant.plugins.benchmark.warping, Nov 3, 2014*/
package isoquant.plugins.benchmark.warping;

import java.io.File;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import de.mz.jk.plgs.data.Workflow;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.db.IQDBUtils;
import isoquant.kernel.plugin.SingleActionPlugin4DBExportToCSV;

/**
 * <h3>{@link TimeWarpingExporter}</h3>
 * @author kuharev
 * @version Nov 3, 2014 3:32:51 PM
 */
public class TimeWarpingExporter extends SingleActionPlugin4DBExportToCSV
{
	public TimeWarpingExporter(iMainApp app)
	{
		super( app );
	}

	@Override public void runExportAction(DBProject p, File csvFile) throws Exception
	{
		try
		{
			createReport( p, csvFile );
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void createReport(DBProject p, File file) throws Exception
	{
		List<Workflow> runs = IQDBUtils.getWorkflows( p );
		PrintStream out = new PrintStream( file );
		printHeadLine( out, runs );
		ResultSet rs = p.mysql.executeSQL(
				"SELECT time, GROUP_CONCAT( CONCAT(run, ':', ref_rt) ORDER BY run ASC SEPARATOR ';') FROM rtw GROUP BY time ORDER BY time ASC"
				);
		try
		{
			while (rs.next())
			{
				printDataLine( out, rs, runs );
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		out.flush();
		out.close();
	}

	private void printHeadLine(PrintStream out, List<Workflow> runs)
	{
		printTxtCell( out, "time" );
		for ( Workflow w : runs )
		{
			out.print( colSep );
			printTxtCell( out, w.replicate_name );
		}
		out.println();
	}

	private void printDataLine(PrintStream out, ResultSet rs, List<Workflow> runs) throws Exception
	{
		Float time = rs.getFloat( 1 );
		printNumCell( out, time.toString() );
		Map<Integer, Double> r2i = IQDBUtils.extractI2DMap( rs.getString( 2 ) );
		for ( Workflow w : runs )
		{
			out.print( colSep );
			printNumCell( out, "" + ( r2i.containsKey( w.index ) ? r2i.get( w.index ) : time ) );
		}
		out.println();
	}

	@Override public String getMenuItemText()
	{
		return "Retention Time Warping Shifts";
	}
}
