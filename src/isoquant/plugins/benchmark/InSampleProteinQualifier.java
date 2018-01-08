/** ISOQuant, isoquant.plugins.benchmark, 09.02.2012*/
package isoquant.plugins.benchmark;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import de.mz.jk.jsix.math.interpolation.Interpolator;
import de.mz.jk.jsix.math.interpolation.LinearInterpolator;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.plot.pt.XYPlotter;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

/**
 * <h3>{@link InSampleProteinQualifier}</h3>
 * @author kuharev
 * @version 09.02.2012 17:38:51
 */
public class InSampleProteinQualifier extends SingleActionPlugin4DB
{
	private String prepare_abs_logratio_file = getPackageResource("protein_auqc.sql"); 
	
	/**
	 * @param app
	 */
	public InSampleProteinQualifier(iMainApp app)
	{
		super(app);
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		MySQL db = p.mysql;
		
		db.executeSQLFile(prepare_abs_logratio_file, null);
		List<Integer> wis = db.getIntegerValues("SELECT DISTINCT workflow_index FROM finalquant_abs_logratios");
		
		System.out.println("------ Quality of LC-MS runs ------");
		System.out.println("run	auqc");
		
		XYPlotter plot = new XYPlotter(640, 480);
		plot.setPlotTitle("AUQC score based on final protein abundances");
		
		DecimalFormat df = new DecimalFormat( "0.0000" );
		for(int wi : wis )
		{
			List<Double> x = db.getDoubleValues(
				"SELECT abs_logratio FROM finalquant_abs_logratios " +
				" WHERE workflow_index="+wi+" AND abs_logratio BETWEEN 0 AND 2 " +
				" ORDER BY abs_logratio ASC "
			);
			
			int m = x.size();
			List<Double> y = new ArrayList<Double>(m);
			for(int i=1; i<=m; i++) y.add((double)i/m);
			plot.addPoints(wi, x, y, true);
			
			Interpolator f = new LinearInterpolator(x, y);
			
			int nSteps = 1000;
			double step = 2.0 / nSteps;
			double auqc = 0;
			for(int i=0; i<=nSteps; i++)
			{
				double _x = i * step;
				auqc += f.getY(_x)*step;
			}
			
			String _auqc = df.format( auqc );
			plot.setLegend(wi, "AUQC("+wi+") = " + _auqc );
			
			System.out.println(wi + "	" + _auqc);
		}
		System.out.println("-----------------------------------");
	}

	@Override public String getPluginName()
	{
		return "AUQC checker";
	}

	@Override public int getExecutionOrder() {
		return 256;
	}

	@Override public String getMenuItemText()
	{
		return "check quality";
	}

	@Override public String getMenuItemIconName()
	{
		return "benchmark";
	}

}
