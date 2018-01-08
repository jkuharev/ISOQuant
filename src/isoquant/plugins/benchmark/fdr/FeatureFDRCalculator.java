/** ISOQuant, isoquant.plugins.benchmark.fdr, 08.08.2013 */
package isoquant.plugins.benchmark.fdr;

import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

import java.sql.ResultSet;

/**
 * <h3>{@link FeatureFDRCalculator}</h3>
 * @author kuharev
 * @version 08.08.2013 16:21:13
 */
public class FeatureFDRCalculator extends SingleActionPlugin4DB
{
	/**
	 * @param app
	 */
	public FeatureFDRCalculator(iMainApp app)
	{
		super(app);
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		p.mysql.executeSQLFile(getPackageResource("00_init_fdr_stats.sql"));
		p.mysql.executeSQLFile(getPackageResource("01_feature_fdr_before_id_filter.sql"));
		p.mysql.executeSQLFile(getPackageResource("02_feature_fdr_after_id_filter.sql"));
		p.mysql.executeSQLFile(getPackageResource("03_feature_fdr_after_annotation.sql"));
		p.mysql.executeSQLFile(getPackageResource("04_feature_fdr_after_qnt_filter.sql"));
		ResultSet rs = p.mysql.executeSQL("SELECT * FROM feature_fdr_stats ORDER BY timepoint ASC");
		System.out.println("--------------------------------------------------------------------------------");
		System.out.println("project: " + p.toString());
		System.out.println("description	fdr");
		while (rs.next())
		{
			System.out.println(rs.getString("description") + "	" + rs.getString("fdr"));
		}
		System.out.println("--------------------------------------------------------------------------------");
	}

	@Override public String getMenuItemText()
	{
		return "calculate feature FDR";
	}

	@Override public String getMenuItemIconName()
	{
		return "timer";
	}
}
