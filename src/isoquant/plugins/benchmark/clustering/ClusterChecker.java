/** ISOQuant, isoquant.plugins.benchmark.clustering, 31.05.2012*/
package isoquant.plugins.benchmark.clustering;

import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

import java.sql.ResultSet;

import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.plot.pt.SQLPlotter;

/**
 * <h3>{@link ClusterChecker}</h3>
 * @author kuharev
 * @version 31.05.2012 13:20:12
 */
public class ClusterChecker extends SingleActionPlugin4DB
{
	/** @param app */
	public ClusterChecker(iMainApp app)
	{
		super(app);
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		MySQL db = p.mysql;
		
		db.dropTable("clustering_benchmark");
		
		db.executeSQLFile( getPackageResource("clustering_benchmark.sql") );
		String now = db.getFirstValue("SELECT @jetzt", 1);
		
		System.out.println( "--------------------------------------------------------------------------------" );
		ResultSet rs = db.executeSQL( "SELECT `name`, `line`, `value` FROM clustering_benchmark WHERE series='" + now + " ORDER BY `name`'" );
		while (rs.next())
		{
			System.out.println( "" + rs.getString( "name" ) + " [" + rs.getString( "line" ) + "]: " + rs.getString( "value" ) );
		}
		System.out.println( "--------------------------------------------------------------------------------" );
		
		SQLPlotter P = new SQLPlotter(db);
		P.setPointStyle("bigdots");

		P.plot("SELECT `line`, `value` as `clusters per peptide` FROM clustering_benchmark WHERE series='"+now+"' AND name='clusters per peptide'", true);
		P.plot("SELECT `line`, `value` as `cluster sizes` FROM clustering_benchmark WHERE series='"+now+"' AND name='cluster sizes'", true);
		P.plot("SELECT `line`, `value` as `peptides per cluster` FROM clustering_benchmark WHERE series='"+now+"' AND name='peptides per cluster'", true);
		
		P.getDB().closeConnection();
	}

	@Override public String getPluginName(){return "Cluster Checker";}
	@Override public String getMenuItemText(){return "check peak clustering";}
	@Override public String getMenuItemIconName(){return "benchmark";}
}
