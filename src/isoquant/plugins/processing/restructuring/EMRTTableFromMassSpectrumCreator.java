/** ISOQuant, isoquant.plugins.processing.restructuring, 14.04.2011*/
package isoquant.plugins.processing.restructuring;

import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.mysql.SQLBatchExecutionListener;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.jsix.utilities.Settings;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.log.LogEntry;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

/**
 * <h3>{@link EMRTTableFromMassSpectrumCreator}</h3>
 * delete all contents from tables cluster_average and clustered_emrt
 * and refill clustered_emrt using data from table mass_spectrum<br> 
 * @author Joerg Kuharev
 * @version 14.04.2011 16:24:17
 */
public class EMRTTableFromMassSpectrumCreator extends SingleActionPlugin4DB implements SQLBatchExecutionListener
{
	/** minimum acceptable intensity */
	private int minIntensityCutOff = 1000;
	private int minMassCutOff = 500;
	private float zeroDriftTime = 10f;
	private float massOfDriftGas = 28.0134f;
	
	public EMRTTableFromMassSpectrumCreator(iMainApp app)
	{
		super(app);
	}

	@Override public void loadSettings(Settings cfg)
	{
		minIntensityCutOff = 	cfg.getIntValue("process.emrt.minIntensity", minIntensityCutOff, false);
		minMassCutOff = 		cfg.getIntValue("process.emrt.minMass", minMassCutOff, false);
//		zeroDriftTime = 		cfg.getValue("process.emrt.ionMobility.correction.zeroDriftTime", zeroDriftTime, true);
//		massOfDriftGas = 		cfg.getValue("process.emrt.ionMobility.correction.driftGasMass", massOfDriftGas, true);
	}

	@Override public String getMenuItemIconName(){	return "restructure_db"; }
	@Override public String getMenuItemText(){ return "create emrt table from mass spectrum data"; }
	@Override public String getPluginName(){return "EMRT Table from Mass Spectrum Data Creator";}
	
	@Override public void runPluginAction(DBProject p) throws Exception
	{
		MySQL db = p.mysql;
		Bencher t = new Bencher().start();
		
		progressListener.setStatus( "creating EMRT Table for '" + p.data.title + "'" );
			
			// work around for old versions without mobility
			// create column mobility in clustered_emrt 
			if( db.tableExists("clustered_emrt") && !db.columnExists("clustered_emrt", "Mobility") )
			{
				db.executeSQL("ALTER TABLE clustered_emrt ADD COLUMN (Mobility DOUBLE DEFAULT 00.0000)");
				db.createIndex("clustered_emrt", "Mobility");
			}
			db.executeSQLFile( getPackageResource("ms2ce.sql"), this );
			
			p.log.add( LogEntry.newParameter("process.emrt.minIntensity", minIntensityCutOff) );
			p.log.add( LogEntry.newParameter("process.emrt.minMass", minMassCutOff) );
			
		progressListener.setStatus("EMRT Table created");
		
		p.log.add( LogEntry.newEvent("EMRT Table created", t.stop().getSecString()) );
	}

	@Override public int getExecutionOrder(){ return 2; }

	@Override public String processSQLStatementBeforeExecution(String template)
	{
		return 
			template
			.replaceAll("%MIN_INTENSITY%", minIntensityCutOff+"")
			.replaceAll("%MIN_MASS%", minMassCutOff+"")
			.replaceAll("%MASS_OF_DRIFT_GAS%", massOfDriftGas + "")
			.replaceAll("%ZERO_DRIFT_TIME%", zeroDriftTime + "")
		;
	}
	@Override public void sqlStatementExecutedNotification(String sql, long ms){}
	@Override public void sqlStatementFailedNotification(String sql, Exception e){}
	@Override public void sqlCommentNotification(String comment)
	{
		if( comment.matches("--\\s*@\\w*\\s+.*") )
		{
			// String commentType = comment.replaceFirst("--\\s*@", "").replaceFirst("\\s+.*", "");
			// String commentContent = comment.replaceFirst("--\\s*@\\w*\\s+", "");
			System.out.println( comment.replaceFirst("--\\s*@\\w*\\s+", "") );
		}
	}
}
