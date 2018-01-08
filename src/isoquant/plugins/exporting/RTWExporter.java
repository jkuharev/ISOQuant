/** ISOQuant, isoquant.plugins.report, 07.07.2011*/
package isoquant.plugins.exporting;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.math.interpolation.Interpolator;
import de.mz.jk.jsix.math.interpolation.LinearInterpolator;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.plgs.data.Workflow;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DBExportToFolder;

/**
 * <h3>{@link RTWExporter}</h3>
 * @author Joerg Kuharev
 * @version 07.07.2011 14:13:36
 */
public class RTWExporter extends SingleActionPlugin4DBExportToFolder
{
	public RTWExporter(iMainApp app)
	{
		super(app);
	}
	
	@Override public String getMenuItemIconName(){	return "export";	}
	@Override public String getMenuItemText(){	return "export RTW as linear interpolator objects"; }
	@Override public int getExecutionOrder(){ return 256; }
	@Override public String getPluginName(){ return "RTW Exporter"; }

	@Override public String getFolderSelectionDialogTitle()
	{
		return "choose a target folder for RTW exporting";
	}

	@Override public void runExportAction(DBProject p, File tarDir)
	{
		List<Workflow> ws = getWorkflows( p.mysql ); 
		
		Bencher t = new Bencher().start();
		
		System.out.println("exporting Retention Time Alignments for " + ws.size() + " runs ...");
		System.out.println("output folder: " + tarDir.getAbsolutePath());
		for(Workflow w : ws)
		{
			System.out.print("\t"+w.index+": DB -> ");
			exportRTW(p.mysql, w, tarDir);			
		}
		
		System.out.println("Retention Time Alignments export duration: " + t.stop().getSecString());
	}
	
	private void exportRTW(MySQL db, Workflow w, File dir)
	{
		Bencher t = new Bencher().start();
		String fileName =  XJava.numString(w.index, 3) + "_run_"+w.acquired_name;
		File sliFile = new File(dir, fileName + ".sli");
//		File csvFile = new File(dir, fileName + ".csv");
		
		ResultSet rs = db.executeSQL("SELECT time, ref_rt FROM rtw WHERE run="+w.index+" ORDER BY time ASC");
		List<Double> x = new ArrayList<Double>();
		List<Double> y = new ArrayList<Double>();

		try
		{
			while(rs.next())
			{
				x.add( rs.getDouble(1) );
				y.add( rs.getDouble(2) );
			}
		} catch (Exception e) 
		{ 
			e.printStackTrace(); 
		}
		
		System.out.print( x.size() + " peaks -> " + fileName + " ");
		Interpolator li = new LinearInterpolator(x, y, 0, 0, 1000, 1000);
		
		try
		{
			/*
			PrintStream out = new PrintStream( csvFile );
			out.println("time;ref_rt");
			for(int i=0; i<x.size(); i++)
			{
				out.println( x.get(i) + ";" + y.get(i) );	
			}
			out.flush();
			out.close();
			*/
			LinearInterpolator.serialize(li, sliFile);
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		System.out.println("["+t.stop().getSecString()+"]");
	}

	public synchronized List<Workflow> getWorkflows(MySQL db)
	{
		String sql="SELECT * FROM `workflow`";
		List<Workflow> res = new ArrayList<Workflow>();
		ResultSet rs = db.executeSQL(sql);
		try
		{
			while(rs.next())
			{
				Workflow w = new Workflow();
					w.index = rs.getInt("index");
					w.sample_index = rs.getInt("sample_index"); 
					w.sample_description = XJava.decURL( rs.getString("sample_description") );
					w.replicate_name = XJava.decURL( rs.getString("replicate_name") );
					w.acquired_name = rs.getString("acquired_name");
				res.add( w );
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}
}
