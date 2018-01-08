package isoquant.plugins.processing.expression.clustering.preclustering;

import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.mysql.SQLBatchExecutionListener;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.jsix.utilities.Settings;
import de.mz.jk.ms.clust.com.GeneralPeakClusteringConfiguration;
import de.mz.jk.ms.clust.method.hierarchical.HierarchicalClusteringConfig;
import isoquant.app.Defaults;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.db.IQDBUtils;
import isoquant.kernel.log.LogEntry;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

public class DynamicPreClusterer extends SingleActionPlugin4DB
{
	public static enum PreclusteringMode
	{
		/** normal mode */
		IN_PROJECT,
		/** no clusters spanning over different samples */
		IN_SAMPLE,
		/** do nothing */
		SKIP,
		/** join all peaks to a single cluster */ 
		JOIN,
		/** split into single peak clusters */
		SPLIT;
		
		public static PreclusteringMode fromString(String mode)
		{
			mode = mode.toUpperCase();
			if(mode.contains("JOIN")) return JOIN;
			if(mode.contains("SKIP")) return SKIP;
			if(mode.contains("SPLIT")) return SPLIT;
			if(mode.contains("SAMPLE")) return IN_SAMPLE;
			return IN_PROJECT;
		}
	}
	 
	private PreclusteringMode mode = PreclusteringMode.IN_PROJECT;
	
	private float mzRes = GeneralPeakClusteringConfiguration.DEFAULT_MASS_RESOLUTION;
	private float rtRes = GeneralPeakClusteringConfiguration.DEFAULT_TIME_RESOLUTION;
	private float dtRes = GeneralPeakClusteringConfiguration.DEFAULT_DRIFT_RESOLUTION;
	private float distanceTreshold = HierarchicalClusteringConfig.DEFAULT_MAX_ALLOWED_MIN_CLUSTER_DISTANCE;
	private float distanceFactor = 1.01f;
	
	private float maxDeltaMassPPM = mzRes * distanceTreshold * distanceFactor;
	private float maxDeltaTime = rtRes * distanceTreshold * distanceFactor;
	private float maxDeltaDrift = dtRes * distanceTreshold * distanceFactor;

//	private int bigClusterResolverCriticalClusterSizeByRunMultiplier = 4;
	private int bigClusterCriticalSize = 1000;
	private int bigClusterResolverMaxIterations = 50;
	private float bigClusterResolverScaleFactor = 0.7f;

	private String file_INIT_IN_PROJECT = getPackageResource("_init_in_project.sql");
	private String file_INIT_IN_SAMPLE = getPackageResource("_init_in_sample.sql");
	private String file_INIT_BIG_CLUSTERS = getPackageResource("_init_big_clusters.sql");
	
	private String file_COLLAPSE_CLUSTERS = getPackageResource("_collapse_clusters.sql");
	private String file_EXPAND_CLUSTERS = getPackageResource("_expand_clusters.sql");	
	
	private String file_SHIFT_CE = getPackageResource("_shift_ce.sql");
	
	private String file_STORE_CE = getPackageResource("_store_ce.sql");	
	
	private String file_BACKUP_RESULTS = getPackageResource("_backup.sql");
	
	private String file_CLUSTER_BY_MASS = getPackageResource("mass.sql");
	private String file_CLUSTER_BY_TIME = getPackageResource("time.sql");
	private String file_CLUSTER_BY_DRIFT = getPackageResource("drift.sql");
	
	private String file_JOIN_ALL = getPackageResource("_join_all.sql");
	private String file_SPLIT_ALL = getPackageResource("_split_all.sql");
		
	private String preclusteringSequence = "MTMTMT";
	
	private ClusteringSQLListener sqlListener = null;

	public DynamicPreClusterer(iMainApp app)
	{
		super(app);
	}

	@Override public void loadSettings(Settings cfg)
	{
		distanceFactor=cfg.getFloatValue("process.emrt.clustering.preclustering.distanceFactor", distanceFactor, !Defaults.DEBUG );
		preclusteringSequence=cfg.getStringValue("process.emrt.clustering.preclustering.orderSequence", preclusteringSequence, !Defaults.DEBUG );
		
		mzRes = cfg.getFloatValue("process.emrt.clustering.distance.unit.mass.ppm", GeneralPeakClusteringConfiguration.DEFAULT_MASS_RESOLUTION*1000000f, false);
		rtRes = cfg.getFloatValue("process.emrt.clustering.distance.unit.time.min", rtRes, false);
		dtRes = cfg.getFloatValue("process.emrt.clustering.distance.unit.drift.bin", dtRes, false);

		// correct ppm to parts
		if(mzRes >= 1.0) mzRes = mzRes/1000000f;
		
		maxDeltaMassPPM = mzRes * distanceTreshold * distanceFactor;
		maxDeltaTime = rtRes * distanceTreshold * distanceFactor;
		maxDeltaDrift = dtRes * distanceTreshold * distanceFactor;
		
		
		bigClusterCriticalSize = cfg.getIntValue("process.emrt.clustering.preclustering.resolver.maxClusterSize", bigClusterCriticalSize, !Defaults.DEBUG);
		bigClusterResolverMaxIterations = cfg.getIntValue("process.emrt.clustering.preclustering.resolver.maxIterations", bigClusterResolverMaxIterations, !Defaults.DEBUG);
		bigClusterResolverScaleFactor = cfg.getFloatValue("process.emrt.clustering.preclustering.resolver.distanceScaleFactor", bigClusterResolverScaleFactor, !Defaults.DEBUG);
		
		mode = PreclusteringMode.fromString( cfg.getStringValue("process.emrt.clustering.preclustering.mode", mode.toString(), !Defaults.DEBUG) );
	}

	@Override public String getMenuItemIconName(){return "precluster";}
	@Override public String getMenuItemText(){return "run emrt pre-clustering";}
	@Override public String getPluginName(){return "Iterative Multi Dimensional Single Linkage Nearest Neighbor Preclustering";}
	@Override public int getExecutionOrder(){return 10;}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		loadSettings( app.getSettings() );
	
		if(mode.equals(PreclusteringMode.SKIP)) return;
		
		MySQL db = p.mysql;
		Bencher t = new Bencher().start();
		
//		int nRuns = Integer.parseInt(db.getFirstValue("SELECT count(*) FROM `workflow`", 1));
//		bigClusterCriticalSize = nRuns * bigClusterResolverCriticalClusterSizeByRunMultiplier;
		
		System.out.println("\tpreclustering parameters:");
		System.out.println( "\t\tdatabase schema name: '" + p.data.db + "'" );
		System.out.println("\t\tcurrent mode: " + mode);
		System.out.println("\t\tmax delta ppm: "+ maxDeltaMassPPM +"");
		System.out.println("\t\tmax delta time: "+ maxDeltaTime +"");
		System.out.println("\t\tmax delta drift: "+ maxDeltaDrift +"");
		System.out.println("\t\tcritical cluster size: "+ bigClusterCriticalSize +"");
		
		p.log.add( LogEntry.newParameter("process.emrt.clustering.preclustering.orderSequence", preclusteringSequence) );
		p.log.add( LogEntry.newParameter("process.emrt.clustering.preclustering.maxDistance.mass.ppm", maxDeltaMassPPM+"") );
		p.log.add( LogEntry.newParameter("process.emrt.clustering.preclustering.maxDistance.time.min", maxDeltaTime+"") );
		p.log.add( LogEntry.newParameter("process.emrt.clustering.preclustering.maxDistance.drift", maxDeltaDrift+"") );
		
		sqlListener = new ClusteringSQLListener(maxDeltaMassPPM, maxDeltaTime, maxDeltaDrift, bigClusterCriticalSize);
		preclusteringSequence = preclusteringSequence.toLowerCase();
		if (IQDBUtils.hasMixedMobility(db) && dtRes < 200)
		{
			System.out.println("\t\tremoving drift time separation from order sequence ...");
			preclusteringSequence = preclusteringSequence.replaceAll("d", "");
		}
		char[] seq = preclusteringSequence.toCharArray();
		
		if( mode.equals(PreclusteringMode.IN_PROJECT) )
		{
			db.executeSQLFile( file_INIT_IN_PROJECT , sqlListener);
			doClusteringBySequence(db, seq);			
			db.executeSQLFile(file_STORE_CE, sqlListener);
			splitBigClusters(db, sqlListener, seq);
		}
		else
		if( mode.equals(PreclusteringMode.IN_SAMPLE) )
		{
			db.executeSQLFile( file_INIT_IN_SAMPLE, sqlListener);
			doClusteringBySequence(db, seq);
			db.executeSQLFile(file_COLLAPSE_CLUSTERS, sqlListener);
			doClusteringBySequence(db, seq);
			db.executeSQLFile(file_EXPAND_CLUSTERS, sqlListener);
			db.executeSQLFile(file_STORE_CE, sqlListener);
			splitBigClusters(db, sqlListener, seq);
		}
		else
		if( mode.equals(PreclusteringMode.JOIN) )
		{
			db.executeSQLFile(file_JOIN_ALL, sqlListener);
		}
		else
		if( mode.equals(PreclusteringMode.SPLIT) )
		{
			db.executeSQLFile(file_SPLIT_ALL, sqlListener);
		}
		
		// db.executeSQLFile(file_BACKUP_RESULTS, sqlListener);
		
		p.log.add( LogEntry.newMessage( "preclustering done", t.stop().getSecString() ) );
		
		System.out.println( "preclustering duration: " + t.stop().getSecString() );
		System.out.println(
				"preclustering resulted in " +
				db.getFirstInt("SELECT COUNT(DISTINCT cluster_average_index) FROM clustered_emrt;", 1) +
				" clusters."
		);
	}
	

	/**
	 * @param db
	 * @param sqlListener
	 * @param seq
	 */
	private void splitBigClusters(MySQL db, ClusteringSQLListener sqlListener, char[] seq)
	{
		// check for clusters having critical size	
		for(int i=1; i<=bigClusterResolverMaxIterations; i++)
		{
			db.executeSQLFile(file_INIT_BIG_CLUSTERS, sqlListener);
			
			int nBigClusters = Integer.parseInt( db.getFirstValue("SELECT count(*) FROM `big_clusters`", 1) );
			if(nBigClusters<1) break;
			
			System.out.println(":	"+i+":	" + nBigClusters + " big preclusters found, let's split them ...");
			sqlListener.scaleMaxDeltas(bigClusterResolverScaleFactor);
			doClusteringBySequence(db, seq);
			
			db.executeSQLFile(file_STORE_CE, sqlListener);
		}
	}

	/**
	 * @param db
	 * @param seq
	 */
	private void doClusteringBySequence(MySQL db, char[] seq)
	{
		for(char dim:seq)
		{
			switch(dim)
			{
				case 'm':
					db.executeSQLFile(file_CLUSTER_BY_MASS, sqlListener);
					break;
				case 't':
					db.executeSQLFile(file_CLUSTER_BY_TIME, sqlListener);		
					break;
				case 'd':
					db.executeSQLFile(file_CLUSTER_BY_DRIFT, sqlListener);		
					break;
				default:
					break;
			}
			System.out.println(
					":		preliminary preclustering result: " +
					db.getFirstInt("SELECT COUNT(DISTINCT nc) FROM ce;", 1) +
					" clusters."
			);
		}
	}

//	public Mode getPreclusteringMode(){	return mode; }
//	public void setPreclusteringMode(Mode mode){ this.mode = mode; }
	
	private class ClusteringSQLListener implements SQLBatchExecutionListener
	{
		private float MaxDeltaMassPPM = maxDeltaMassPPM;
		private float MaxDeltaTime = maxDeltaTime;
		private float MaxDeltaDrift = maxDeltaDrift;
		private int CriticalClusterSize = bigClusterCriticalSize;
		
		public ClusteringSQLListener(float deltaMassPPM, float deltaTime, float deltaDrift)
		{
			setMaxDeltaMassPPM(deltaMassPPM);
			setMaxDeltaTime(deltaTime);
			setMaxDeltaDrift(deltaDrift);
		}
		
		public ClusteringSQLListener(float deltaMassPPM, float deltaTime, float deltaDrift, int criticalClusterSize)
		{
			this(deltaMassPPM, deltaTime, deltaDrift);
			setCriticalClusterSize(criticalClusterSize);
		}
		
		public void setCriticalClusterSize(int criticalClusterSize){CriticalClusterSize = criticalClusterSize;}
		public void setMaxDeltaMassPPM(float maxDeltaMassPPM){MaxDeltaMassPPM = maxDeltaMassPPM;}
		public void setMaxDeltaTime(float maxDeltaTime){MaxDeltaTime = maxDeltaTime;}
		public void setMaxDeltaDrift(float maxDeltaDrift){MaxDeltaDrift = maxDeltaDrift;}
		
		public void scaleMaxDeltas(float scaleFactor)
		{
			MaxDeltaMassPPM *= scaleFactor;
			MaxDeltaTime *= scaleFactor;
			MaxDeltaDrift *= scaleFactor;
		}
		
		@Override public String processSQLStatementBeforeExecution(String template)
		{
			return template.
				replaceAll("%MAX_DELTA_MASS_PPM%", this.MaxDeltaMassPPM +"" ).
				replaceAll("%MAX_DELTA_TIME%", this.MaxDeltaTime+"").
				replaceAll("%MAX_DELTA_DRIFT%", this.MaxDeltaDrift+"").
				replaceAll("%CRITICAL_CLUSTER_SIZE%", this.CriticalClusterSize+"" )
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
}
