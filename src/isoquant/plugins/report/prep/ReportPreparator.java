/** ISOQuant_1.0, isoquant.plugins.report.prep, 03.03.2011*/
package isoquant.plugins.report.prep;

import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.log.LogEntry;
import isoquant.kernel.plugin.SingleActionPlugin4DB;
import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.mysql.SQLBatchExecutionListener;
import de.mz.jk.jsix.utilities.Settings;

/**
 * <h3>{@link ReportPreparator}</h3>
 * @author Joerg Kuharev
 * @version 03.03.2011 10:07:04
 */
public class ReportPreparator extends SingleActionPlugin4DB implements SQLBatchExecutionListener
{
	public static final String sql_file_protein_fpr = XJava.getPackageResource(ReportPreparator.class, "filter_finalquant_fpr.sql");
	public static final String sql_file_protein_info = XJava.getPackageResource(ReportPreparator.class, "prepare_protein_details.sql");
	public static final String sql_file_cluster_info = XJava.getPackageResource(ReportPreparator.class, "collect_cluster_info.sql");
	public static final String sql_file_workflow_info= XJava.getPackageResource(ReportPreparator.class, "prepare_workflow_info.sql");
	
	private double maxProteinFDR = 0.01;
	
	public ReportPreparator(iMainApp app)
	{
		super(app);
	}
	
	/**
	 * @TODO remove workaround in some moment 
	 */
	@Override public void loadSettings(Settings cfg)
	{
		// FPR is not FDR!!!
		if (cfg.isSet( "process.quantification.maxProteinFPR" ))
		{
			maxProteinFDR = cfg.getDoubleValue( "process.quantification.maxProteinFPR", maxProteinFDR, true );
			cfg.remove( "process.quantification.maxProteinFPR" );
		}
		maxProteinFDR = cfg.getDoubleValue( "process.quantification.maxProteinFDR", maxProteinFDR, false );
	}
	
	@Override public String getMenuItemIconName(){return "prepare_report";}
	@Override public String getMenuItemText(){return "prepare report";}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		p.log.add( LogEntry.newParameter( "process.quantification.maxProteinFDR", maxProteinFDR ) );
		p.mysql.executeSQLFile(sql_file_protein_fpr, this );
		p.mysql.executeSQLFile(sql_file_protein_info, this );
		p.mysql.executeSQLFile(sql_file_cluster_info, this );
		p.mysql.executeSQLFile(sql_file_workflow_info, this );
	}
	
	public String processSQLStatementBeforeExecution(String template)
	{
		return template.
				replaceAll( "%MAX_PROTEIN_FDR%", maxProteinFDR + "" )
		;	
	}
	
	@Override public void sqlStatementExecutedNotification(String sql, long ms){}
	@Override public void sqlStatementFailedNotification(String sql, Exception e){}
	@Override public void sqlCommentNotification(String comment)
	{
		if( comment.matches("--\\s*@\\w*\\s+.*") )
		{
			System.out.println( comment.replaceFirst("--\\s*@\\w*\\s+", "") );
		}
	}
	
	@Override public int getExecutionOrder(){return 200;}
	@Override public String getPluginName(){return "Report Preparator";}
}
