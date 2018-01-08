/** ISOQuant, isoquant.plugins.processing.expression.clustering.preclustering, 19.03.2012*/
package isoquant.plugins.processing.expression.clustering;

import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

import java.util.List;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.plot.pt.SQLPlotter;

/**
 * <h3>{@link ClusterSizeInspector}</h3>
 * @author kuharev
 * @version 19.03.2012 16:28:15
 */
public class ClusterSizeInspector extends SingleActionPlugin4DB
{
	/**
	 * @param app
	 */
	public ClusterSizeInspector(iMainApp app)
	{
		super(app);
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		p.mysql.dropTable("tmpce");
		p.mysql.executeSQL(
			"CREATE TABLE tmpce "+
			"SELECT count(*) size, cluster_average_index FROM `clustered_emrt` GROUP BY `cluster_average_index` ORDER BY size DESC"
		);
		p.mysql.createIndex("tmpce", "size");
		
		SQLPlotter pl = new SQLPlotter(p.mysql, 600, 400);
		pl.setPointStyle("bigdot");
		pl.setXAxisLabel("size");
		pl.setYAxisLabel("frequency");
//		pl.plot("SELECT `size`, log(`frequency`) FROM `preclustered_emrt_histogram`", true);
		
		String sql = "SELECT \n" + 
		"floor(size/power(10, floor( log10(size) ) )) * power(10, floor( log10(size) ) ) as cluster_size, " + 
		"count(`cluster_average_index`) as frequency \n" + 
		"FROM `tmpce` GROUP BY cluster_size";
		
		pl.plot(sql, true);//( "SELECT size, count(*) as freq FROM tmpce GROUP BY size ORDER BY size ASC", true);
		
		System.out.println("--------------------------------------------------------------------------------");
		List<List<String>> histData = p.mysql.listQuery(sql, true);
		for(List<String> row : histData)
		{
			System.out.println( XJava.joinList(row, "\t")  );
		}
		System.out.println("--------------------------------------------------------------------------------");
		
		p.mysql.dropTable("tmpce");
	}

	@Override public String getPluginName(){return "Cluster Size Inspector";}
	
	@Override public String getMenuItemText(){return "view cluster sizes";}
	@Override public String getMenuItemIconName(){return "plot";}
}
