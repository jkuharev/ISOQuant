/** ISOQuant, isoquant.plugins.plot, 06.08.2013 */
package isoquant.plugins.plot;

import java.util.List;

import de.mz.jk.jsix.plot.pt.SQLPlotter;
import de.mz.jk.plgs.data.Sample;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.db.IQDBUtils;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

/**
 * <h3>{@link ProteinSampleQuantLogRatioPlotter}</h3>
 * @author kuharev
 * @version 06.08.2013 10:50:05
 */
public class ProteinSampleQuantLogRatioPlotter extends SingleActionPlugin4DB
{
	public ProteinSampleQuantLogRatioPlotter(iMainApp app)
	{
		super(app);
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		List<Sample> samples = IQDBUtils.getSamples(p);
		int n = samples.size();
		for (int i = 0; i < n; i++)
		{
			Sample a = samples.get(i);
			for (int j = i + 1; j < n; j++)
			{
				Sample b = samples.get(j);
				plotL2R(a, b, p);
			}
		}
	}

	/**
	 * @param a
	 * @param b
	 * @param p
	 */
	private void plotL2R(Sample a, Sample b, DBProject p)
	{
		//
		p.mysql.dropTable("fqa");
		p.mysql.executeSQL("CREATE TEMPORARY TABLE fqa " +
				"SELECT entry, AVG(ppm) as ppmA " +
				"FROM finalquant_extended " +
				"WHERE sample_index=" + a.index + " " +
				"GROUP BY entry");
		p.mysql.executeSQL("ALTER TABLE fqa ADD PRIMARY KEY(entry)");
		//
		p.mysql.dropTable("fqb");
		p.mysql.executeSQL("CREATE TEMPORARY TABLE fqb " +
				"SELECT entry, AVG(ppm) as ppmB " +
				"FROM finalquant_extended " +
				"WHERE sample_index=" + b.index + " " +
				"GROUP BY entry");
		p.mysql.executeSQL("ALTER TABLE fqb ADD PRIMARY KEY(entry)");
		//
		SQLPlotter pl = new SQLPlotter(p.mysql, 640, 480);
		pl.setPlotTitle("protein quantification between samples");
		pl.setYAxisLabel("Log2('" + a.name + "' / '" + b.name + "')");
		pl.setXAxisLabel("log2(ppm of '" + a.name + "')");
		pl.setPointStyle("dots");
		pl.plot("SELECT log2(ppmA) as ppm, log2(ppmA/ppmB) as log2ratio " +
				"FROM fqa JOIN fqb USING(entry) ORDER BY ppmA ASC", false);
	}

	@Override public String getMenuItemText()
	{
		return "plot protein quantification log-ratios";
	}

	@Override public String getMenuItemIconName()
	{
		return "plot";
	}
}
