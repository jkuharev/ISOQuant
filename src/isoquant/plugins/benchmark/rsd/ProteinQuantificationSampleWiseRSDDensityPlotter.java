/** ISOQuant, isoquant.plugins.benchmark.rsd, 07.08.2012*/
package isoquant.plugins.benchmark.rsd;

import java.util.List;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.math.KernelDensityEstimator;
import de.mz.jk.jsix.plot.pt.XYPlotter;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

/**
 * <h3>{@link ProteinQuantificationSampleWiseRSDDensityPlotter}</h3>
 * @author kuharev
 * @version 07.08.2012 10:43:45
 */
public class ProteinQuantificationSampleWiseRSDDensityPlotter extends SingleActionPlugin4DB
{
	/** @param app */
	public ProteinQuantificationSampleWiseRSDDensityPlotter(iMainApp app)
	{
		super(app);
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		String sql = "SELECT \n" + 
			"STD(`top3_avg_inten`)/AVG(`top3_avg_inten`) as rsd\n" +
			", entry\n" + 
			", `sample_index`\n" + 
			", count(`workflow_index`) as n\n" + 
			"FROM `finalquant`\n" + 
			"GROUP BY `sample_index`, `entry`\n" + 
			"HAVING n>1\n" + 
			"ORDER BY rsd DESC";
		
		List<Double> iqRSDs = p.mysql.getDoubleValues(sql);
		KernelDensityEstimator iqKDE = new KernelDensityEstimator(iqRSDs, 0.5);

		sql = "SELECT \n" + 
			"STD(`aq_ngrams`)/AVG(`aq_ngrams`) as rsd,\n" + 
			"entry,\n" + 
			"sample_index,\n" + 
			"count(DISTINCT workflow_index) as n\n" + 
			"FROM `protein` as p JOIN workflow as w ON p.`workflow_index`=w.`index`\n" + 
			"GROUP BY sample_index, entry\n" + 
			"HAVING n>1 \n"+
			"ORDER BY rsd DESC";
		List<Double> plgsRSDs = p.mysql.getDoubleValues(sql);
		KernelDensityEstimator plgsKDE = new KernelDensityEstimator(plgsRSDs, 0.5);
		
		XYPlotter plotter = new XYPlotter(app.getGUI().getWidth(), app.getGUI().getHeight());
		plotter.setPlotTitle( "Variance of Protein Quantification for " + p.data.title );
		plotter.setYAxisLabel("density");
		plotter.setXAxisLabel("value");
		
		List<Double> iqV = XJava.fillDoubleList(iqKDE.getMinX(), iqKDE.getMaxX(), .05);
		List<Double> iqD = iqKDE.getDensities(iqV);
		plotter.plotXY(iqV, iqD, "RSD by ISOQuant", true);
		
		List<Double> plgsV = XJava.fillDoubleList(plgsKDE.getMinX(), plgsKDE.getMaxX(), .05);
		List<Double> plgsD = iqKDE.getDensities(plgsV);
		plotter.plotXY(plgsV, plgsD, "RSD by PLGS", true);
	}

	@Override public String getPluginName(){ return "Protein In-Sample Quantity RSD Plotter"; }
	@Override public String getMenuItemText(){ return "plot protein quantification RSD"; }
	@Override public String getMenuItemIconName(){ return "benchmark"; }
	@Override public int getExecutionOrder(){return 0;}
}
