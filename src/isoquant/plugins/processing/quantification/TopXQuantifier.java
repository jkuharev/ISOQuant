/**
 * 
 */
package isoquant.plugins.processing.quantification;

import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.mysql.SQLBatchExecutionListener;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.jsix.utilities.Settings;
import isoquant.app.Defaults;
import isoquant.interfaces.iMainApp;
import isoquant.interfaces.log.iLogManager;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.log.LogEntry;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

/**
 * <h3>{@link TopXQuantifier}</h3>
 * @author Joerg Kuharev
 * @version 20.01.2011 13:53:55
 */
public class TopXQuantifier extends SingleActionPlugin4DB implements SQLBatchExecutionListener
{
	private int topX = 3; // 3 = TOP3, 4 = TOP4, ...
	private boolean useDifferentPeptides = true;
	// private boolean useStandardProtein = true;
	private String standardProteinEntry = "ENO1_YEAST";
	private double standardProteinFmol = 50.0;
	private int minPeptideCount = 1;

	private String file_peptide_filter = getPackageResource( "00_filter_best_peptides.sql" );
	private String file_emrt4quant = getPackageResource( "01_emrt4quant.sql" );
	private String file_dist_prepare = getPackageResource( "02_dist_prepare.sql" );
//	private String file_dist_count_loops = getPackageResource( "03_dist_count_loops.sql" );
	private String file_dist_loop = getPackageResource( "04_dist_loop.sql" );
	private String file_remove_nonquantifieable_proteins = getPackageResource( "05_remove_zero_intensity_proteins.sql" );
	private String file_quant_per_run = getPackageResource( "06_finalquant_topx_per_run.sql" );
	private String file_quant_per_project = getPackageResource( "06_finalquant_topx_per_project.sql" );
	private String file_protein_fdr_filter = getPackageResource( "07_finalquant_fpr.sql" );
	private String file_absolute_quant = getPackageResource( "08_absolute_quantification.sql" );

	private QuantificationFilter qntFilter = new QuantificationFilter();

	/**
	 * @param app
	 */
	public TopXQuantifier(iMainApp app)
	{
		super(app);
	}

	@Override public String getMenuItemIconName()
	{
		return "quantification";
	}

	@Override public String getMenuItemText()
	{
		return "quantify proteins";
	}

	@Override public String getPluginName()
	{
		return "Redistributing TopX Quantifier";
	}

	@Override public void loadSettings(Settings cfg)
	{
		qntFilter.loadSettings(cfg);
		topX = cfg.getIntValue("process.quantification.topx.degree", topX, false);
		useDifferentPeptides = cfg.getBooleanValue( "process.quantification.topx.allowDifferentPeptides", useDifferentPeptides, false );
		minPeptideCount = cfg.getIntValue("process.quantification.minPeptidesPerProtein", minPeptideCount, false);
		// useStandardProtein =
// cfg.getBooleanValue("process.quantification.absolute.standard.used",
// useStandardProtein, false);
		standardProteinEntry = cfg.getStringValue("process.quantification.absolute.standard.entry", standardProteinEntry, false);
		standardProteinFmol = cfg.getDoubleValue("process.quantification.absolute.standard.fmol", standardProteinFmol, false);
		// peptide count bounds correction
		if (minPeptideCount > topX)
		{
			minPeptideCount = topX;
			cfg.setValue("process.quantification.minPeptidesPerProtein", minPeptideCount);
		}
	}

	private void logParameters(iLogManager log)
	{
		qntFilter.logParameters( log );
		log.add( LogEntry.newParameter( "process.quantification.topx.degree", topX ) );
		log.add( LogEntry.newParameter( "process.quantification.topx.allowDifferentPeptides", useDifferentPeptides ) );
		log.add( LogEntry.newParameter( "process.quantification.minPeptidesPerProtein", minPeptideCount ) );
		// log.add( LogEntry.newParameter(
// "process.quantification.absolute.standard.used", useStandardProtein ) );
		log.add( LogEntry.newParameter( "process.quantification.absolute.standard.entry", standardProteinEntry ) );
		log.add( LogEntry.newParameter( "process.quantification.absolute.standard.fmol", standardProteinFmol ) );
	}

	public String processSQLStatementBeforeExecution(String template)
	{
		return template.
				replaceAll( "%TOPX_DEGREE%", topX + "" ).
				replaceAll( "%STANDARD_ENTRY%", standardProteinEntry ).
				replaceAll( "%STANDARD_FMOL%", standardProteinFmol + "" ).
				replaceAll( "%MIN_PEPTIDE_COUNT%", minPeptideCount + "" );
	}

	@Override public void sqlStatementExecutedNotification(String sql, long ms)
	{}

	@Override public void sqlStatementFailedNotification(String sql, Exception e)
	{}

	@Override public void sqlCommentNotification(String comment)
	{
		MySQL.defExecListener.sqlCommentNotification( comment );
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		MySQL db = p.mysql;
		Bencher t = new Bencher().start();
		loadSettings(app.getSettings());
		logParameters(p.log);

		// filter peptides for quantification
		progressListener.setStatus("filtering peptides ...");
		db.executeSQLFile(file_peptide_filter, qntFilter);
		
		progressListener.setStatus("redistributing intensities ...");
		db.executeSQLFile(file_emrt4quant, this);
		
		// prepare distribution loop
		db.executeSQLFile( file_dist_prepare, this );

		// count how many loops we are going to do
		int n = db.getFirstInt("SELECT count(*) FROM  `redist_src_prots`", 1);
		progressListener.setProgressMaxValue(n);

		// do n loops
		System.out.println( "distributing intenisites by " + n + " iterations ..." );
		for ( int i = 1; i <= n; i++ )
		{
			if (progressListener != null) progressListener.setProgressValue( n - i );
			Bencher lt = new Bencher(true);
			if (Defaults.DEBUG)
			{
				int g = db.getFirstInt( "SELECT min( `src_proteins` ) FROM `redist_src_prots`", 1 );
				System.out.print( "\t" + i + "\t ... processing sharing grade " + g + " ..." );
			}
			else
			{
				System.out.print( "\t" + i + "\t ... " );
			}
			db.executeSQLFile(file_dist_loop, this);
			System.out.println("[" + lt.stop().getSecString() + "]");
		}
		db.executeSQLFile( file_remove_nonquantifieable_proteins, this );
		p.log.add(LogEntry.newEvent("peptide intensities redistributed", "redistribution duration=" + t.stop().getSecString()));
		progressListener.setProgressValue(0);

		progressListener.setStatus("quantifying proteins ...");
		db.executeSQLFile((useDifferentPeptides) ? file_quant_per_run : file_quant_per_project, this);
		p.log.add(LogEntry.newParameter("process.quantification.topx.allowDifferentPeptides", useDifferentPeptides));
		db.executeSQLFile(file_protein_fdr_filter, this);
		p.log.add( LogEntry.newParameter( "process.quantification.absolute.standard.entry", standardProteinEntry ) );
		p.log.add( LogEntry.newParameter( "process.quantification.absolute.standard.fmol", standardProteinFmol ) );
		Bencher st = new Bencher( true );
		progressListener.setStatus( "standardizing protein amounts ..." );
		db.executeSQLFile( file_absolute_quant, this );
		p.log.add( LogEntry.newEvent( "absolute quantification standardized", "duration=" + st.stop().getSecString() ) );
		System.out.println("distribution and quantification duration: " + t.stop().getSecString());
		p.log.add(LogEntry.newEvent("proteins quantified", "quantification duration=" + t.getSecString()));
		progressListener.setStatus("quantification done");
	}

	@Override public int getExecutionOrder()
	{
		return 200;
	}
}
