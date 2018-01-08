package isoquant.plugins.processing.statistics;

import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.mysql.SQLBatchExecutionListener;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.jsix.utilities.Settings;
import isoquant.app.Defaults;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.log.LogEntry;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

public class ProteinStatisticsMaker extends SingleActionPlugin4DB implements SQLBatchExecutionListener 
{
	public ProteinStatisticsMaker(iMainApp app) 
	{
		super(app);
	}
	
	private boolean doSequenceSearch = false;
	
	@Override public void loadSettings(Settings cfg)
	{
		doSequenceSearch = cfg.getBooleanValue("process.peptide.statistics.doSequenceSearch", doSequenceSearch, !Defaults.DEBUG);
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		MySQL db = p.mysql;
		Bencher t = new Bencher().start();
		progressListener.startProgress("creating statistics for '"+p.data.title+"'");

		db.executeSQLFile( getPackageResource("00_init.sql"), this );
		
		p.log.add( LogEntry.newParameter("process.peptide.statistics.doSequenceSearch", doSequenceSearch) );
		
		if(doSequenceSearch)
			db.executeSQLFile( getPackageResource("01_pep2pro_seq_search.sql"), this );
		else
			db.executeSQLFile( getPackageResource("01_pep2pro_plgs.sql"), this );
			
		db.executeSQLFile( getPackageResource("02_pep.sql"), this );
		db.executeSQLFile( getPackageResource("03_prot.sql"), this );
		
		progressListener.endProgress("statistics creation done");
		p.log.add( LogEntry.newEvent("protein statistics collected", t.stop().getSecString()) );
	}

	@Override public String getMenuItemIconName(){return "statistics";}
	@Override public String getMenuItemText(){return "calculate protein statistics";}
	@Override public String getPluginName(){return "Protein and Peptide Statistics Creator";}
	@Override public int getExecutionOrder(){return 3;}

	@Override public String processSQLStatementBeforeExecution(String sql){return sql;}
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
