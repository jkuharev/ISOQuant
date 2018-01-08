/** ISOQuant, isoquant.plugins.plot, 13.07.2011*/
package isoquant.plugins.plot;

import java.util.List;

import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.plot.pt.SQLPlotter;
import de.mz.jk.jsix.plot.pt.XYPlotter;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

/**
 * <h3>{@link RTWPlotter}</h3>
 * @author Joerg Kuharev
 * @version 13.07.2011 12:56:31
 */
public class RTWPlotter extends SingleActionPlugin4DB
{
	public static void main(String[] args)
	{
		MySQL db = new MySQL("localhost:3307", "Proj__13081411591570_12005088211057313_IQ_1", "root", "", true);
		SQLPlotter p = new SQLPlotter(db);
		for(int i : new int[]{36, 41})
			p.plot("SELECT time, (ref_rt-time) as `ref_rt - time for run_"+i+"` FROM rtw WHERE run="+i+" ORDER BY time ASC", true);
	}
	
	/**
	 * @param app
	 */
	public RTWPlotter(iMainApp app)
	{
		super(app);
	}

	@Override public void runPluginAction(DBProject p) throws Exception 
	{
		SQLPlotter plotter = new SQLPlotter(p.mysql);
		plotter.setPointStyle(XYPlotter.PointStyle.points);
		List<Integer> runIndexes = p.mysql.getIntegerValues("rtw", "DISTINCT run", null);
		for( int i : runIndexes  )
		{
			// plotter.plot("SELECT time, (ref_rt-time) as `ref_rt - time for run_"+i+"` FROM rtw WHERE run="+i+" ORDER BY time ASC", true);
			plotter.plot( "SELECT time, ref_rt as `ref time for run " + i + "` FROM rtw WHERE run=" + i + " ORDER BY time ASC", true );
		}
		plotter.getDB().closeConnection();
	}

	@Override public String getMenuItemIconName(){ return "plot"; }
	@Override public String getMenuItemText(){ return "plot RTW"; }
	
	@Override public int getExecutionOrder(){ return 256; }
	@Override public String getPluginName(){ return "Retention Time Warping Plotter"; }
}
