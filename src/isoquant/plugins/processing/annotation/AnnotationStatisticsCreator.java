/** ISOQuant, isoquant.plugins.processing.annotation, 16.01.2013*/
package isoquant.plugins.processing.annotation;

import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

/**
 * <h3>{@link AnnotationStatisticsCreator}</h3>
 * @author kuharev
 * @version 16.01.2013 08:57:41
 */
public class AnnotationStatisticsCreator extends SingleActionPlugin4DB
{
	public AnnotationStatisticsCreator(iMainApp app){super(app);}
	@Override public void runPluginAction(DBProject p) throws Exception { p.mysql.executeSQLFile( getPackageResource("09_pep2pro_stat.sql") );}
	@Override public String getPluginName(){return "pep2pro stat calculator";}
	@Override public String getMenuItemText(){return "recalculate peptides in proteins statistics";}
	@Override public String getMenuItemIconName(){ return "debug";	}
	@Override public int getExecutionOrder(){return 21;}
}
