/** ISOQuant, isoquant.plugins.report, 07.07.2011*/
package isoquant.plugins.exporting;

import java.io.File;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.util.List;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.plgs.data.Workflow;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.db.IQDBUtils;
import isoquant.kernel.plugin.SingleActionPlugin4DBExportToFolder;

/**
 * <h3>{@link Warp2DExporter}</h3>
 * @author Joerg Kuharev
 * @version 07.07.2011 14:13:36
 */
public class Warp2DExporter extends SingleActionPlugin4DBExportToFolder
{
	public Warp2DExporter(iMainApp app)
	{
		super(app);
	}
	
	@Override public String getMenuItemIconName(){	return "export";	}
	@Override public String getMenuItemText(){	return "export peak lists for Warp2D"; }
	@Override public int getExecutionOrder(){ return 256; }
	@Override public String getPluginName(){ return "Warp2D Exporter"; }

	@Override public String getFolderSelectionDialogTitle()
	{
		return "choose a target folder for peak lists exporting";
	}

	@Override public void runExportAction(DBProject p, File tarDir)
	{
		List<Workflow> ws = IQDBUtils.getWorkflows( p );
		
		Bencher t = new Bencher().start();
		
		System.out.println("exporting peak list for " + ws.size() + " runs ...");
		System.out.println("output folder: " + tarDir.getAbsolutePath());
		for(Workflow w : ws)
		{
			System.out.print("\t"+w.index+": DB -> ");
			exportPeaks(p.mysql, w, tarDir);			
		}
		
		System.out.println( "Peak list export duration: " + t.stop().getSecString() );
	}
	
	private void exportPeaks(MySQL db, Workflow w, File dir)
	{
		Bencher t = new Bencher().start();
		String fileName =  XJava.numString(w.index, 3) + "_run_"+w.acquired_name;
		File pksFile = new File(dir, fileName + ".pks");
		
		ResultSet rs = db.executeSQL("SELECT Mass, RT, Intensity FROM mass_spectrum WHERE workflow_index="+w.index+" ORDER BY RT ASC");
		
		System.out.print( "\t file: " + fileName + " ... ");
		int peaks = 0;
		
		try
		{
			PrintStream out = new PrintStream( pksFile );
			out.println("Mass, RT, Intensity");
			while( rs.next() )
			{
				peaks++;
				out.println(
					rs.getDouble(1) + " " +
					rs.getDouble(2) + " " +
					rs.getDouble(3)
				);
			}
			out.flush();
			out.close();			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		System.out.println(" ["+peaks+" peaks, "+t.stop().getSecString()+"]");
	}
}
