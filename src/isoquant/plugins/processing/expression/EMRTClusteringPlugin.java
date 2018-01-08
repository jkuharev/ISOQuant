package isoquant.plugins.processing.expression;

import java.util.Collection;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.jsix.utilities.Settings;
import de.mz.jk.ms.clust.com.ClusteringPeak;
import de.mz.jk.ms.clust.com.GeneralPeakClusteringConfiguration;
import de.mz.jk.ms.clust.com.GeneralPeakClusteringThread;
import de.mz.jk.ms.clust.com.PipeLine;
import de.mz.jk.ms.clust.method.dbscan.DBSCANClusteringConfiguration;
import de.mz.jk.ms.clust.method.dbscan.DBSCANClusteringProcedureThread;
import de.mz.jk.ms.clust.method.hierarchical.HierarchicalClusteringConfig;
import de.mz.jk.ms.clust.method.hierarchical.HierarchicalClusteringProcedureThread;
import de.mz.jk.ms.clust.method.hierarchical.PeakClusterDistanceMetric;
import de.mz.jk.ms.clust.method.hierarchical.PeakClusterNeighborFinder;
import isoquant.app.Defaults;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.db.IQDBUtils;
import isoquant.kernel.log.LogEntry;
import isoquant.kernel.plugin.SingleActionPlugin4DB;
import isoquant.plugins.processing.expression.clustering.io.PeakListReader;
import isoquant.plugins.processing.expression.clustering.io.PeakListWriter;
import isoquant.plugins.processing.expression.clustering.preclustering.DynamicPreClusterer;

public class EMRTClusteringPlugin extends SingleActionPlugin4DB
{
	private int nThreads = Defaults.availableProcessors;
	private float mzRes = GeneralPeakClusteringConfiguration.DEFAULT_MASS_RESOLUTION;
	private float rtRes = GeneralPeakClusteringConfiguration.DEFAULT_TIME_RESOLUTION;
	private float dtRes = GeneralPeakClusteringConfiguration.DEFAULT_DRIFT_RESOLUTION;
	private float distanceTreshold = HierarchicalClusteringConfig.DEFAULT_MAX_ALLOWED_MIN_CLUSTER_DISTANCE;
	private boolean mergeClustersByIdenticalFeatures = true;
	private PeakClusterDistanceMetric.Description distanceMetric = PeakClusterDistanceMetric.Description.SINGLE_LINKAGE;
	private PeakClusterNeighborFinder.Description neighborFinder = PeakClusterNeighborFinder.Description.NEAREST_NEIGHBOR;
	private DynamicPreClusterer preClusteringPlugin = null;

	public static enum ClusteringMethod
	{
		HNH,
		DBSCAN,
		NONE;
		static ClusteringMethod fromString(String methodString)
		{
			methodString = methodString.toUpperCase();
			if (methodString.startsWith("H")) return HNH;
			if (methodString.contains("NONE")) return NONE;
			return DBSCAN;
		}
	}
	private ClusteringMethod clusteringMethod = ClusteringMethod.DBSCAN;
	private int minNeighborCount = 2;

	public EMRTClusteringPlugin(iMainApp app)
	{
		super(app);
		preClusteringPlugin = new DynamicPreClusterer(app);
	}

	@Override public void loadSettings(Settings cfg)
	{
		clusteringMethod = ClusteringMethod.fromString(cfg.getStringValue("process.emrt.clustering.method", clusteringMethod.toString(), !Defaults.DEBUG));
		mzRes = cfg.getFloatValue("process.emrt.clustering.distance.unit.mass.ppm", GeneralPeakClusteringConfiguration.DEFAULT_MASS_RESOLUTION * 1000000f,
				false);
		rtRes = cfg.getFloatValue("process.emrt.clustering.distance.unit.time.min", rtRes, false);
		dtRes = cfg.getFloatValue("process.emrt.clustering.distance.unit.drift.bin", dtRes, false);
		nThreads = cfg.getIntValue("process.emrt.clustering.maxProcesses", nThreads, false);
		distanceTreshold = cfg.getFloatValue("process.emrt.clustering.distance.threshold", distanceTreshold, !Defaults.DEBUG);
		distanceMetric = PeakClusterDistanceMetric.Description.fromString(cfg.getStringValue("process.emrt.clustering.hnh.distanceMetric",
				distanceMetric.toString(), !Defaults.DEBUG));
		neighborFinder = PeakClusterNeighborFinder.Description.fromString(cfg.getStringValue("process.emrt.clustering.hnh.joiningMethod",
				neighborFinder.toString(), !Defaults.DEBUG));
		minNeighborCount = cfg.getIntValue("process.emrt.clustering.dbscan.minNeighborCount", minNeighborCount, false);
		mergeClustersByIdenticalFeatures = cfg.getBooleanValue( "process.emrt.clustering.postclustering.mergeClustersByIdenticalFeatures",
				mergeClustersByIdenticalFeatures, false );
		// correct ppm to parts
		if (mzRes >= 1.0) mzRes = mzRes / 1000000f;
	}

	@Override public String getMenuItemIconName()
	{
		return "cluster";
	}

	@Override public String getMenuItemText()
	{
		return "run emrt clustering";
	}

	@Override public String getPluginName()
	{
		return "EMRT Clusterer";
	}

	@Override public int getExecutionOrder()
	{
		return 11;
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		MySQL db = p.mysql;
		Bencher t = new Bencher().start();
		loadSettings(app.getSettings());
		if (IQDBUtils.hasMixedMobility(db) && dtRes < 200)
		{
			System.out.println(
					"The processed project contains mixed data with and without ion mobility.\n" +
							"EMRT Clustering will temporarily relax ion mobility settings."
					);
			dtRes = 200f;
		}
		System.out.println("starting emrt clustering for project '" + p.toString() + "'");
		preClusteringPlugin.runPluginAction(p);
		logClusteringParameters(p);
		if (!clusteringMethod.equals(ClusteringMethod.NONE))
		{
			Bencher tc = new Bencher().start();
			// make i/o pipelines
			PipeLine<Collection<ClusteringPeak>> inQ = new PipeLine<Collection<ClusteringPeak>>();
			PipeLine<Collection<ClusteringPeak>> outQ = new PipeLine<Collection<ClusteringPeak>>();
			// make reader/writer
			PeakListReader reader = new PeakListReader(db, inQ, app.getProcessProgressListener());
			PeakListWriter writer = new PeakListWriter(db, outQ);
			// make clustering thread array
			GeneralPeakClusteringThread[] hct = new GeneralPeakClusteringThread[nThreads];
			System.out.println( "	clustering ... " );
			// make and run clustering threads
			for (int i = 0; i < hct.length; i++)
			{
				if (clusteringMethod.equals(ClusteringMethod.HNH))
				{
					hct[i] = new HierarchicalClusteringProcedureThread(
							inQ, outQ,
							new HierarchicalClusteringConfig(
									mzRes, rtRes, dtRes, distanceTreshold,
									distanceMetric.newInstance(),
									neighborFinder.newInstance()
							)
							);
				}
				else
				{
					hct[i] = new DBSCANClusteringProcedureThread(
							inQ, outQ,
							new DBSCANClusteringConfiguration(mzRes, rtRes, dtRes, distanceTreshold, minNeighborCount)
							);
				}
				hct[i].start();
			}
			// wait until all clustering threads end
			for (int i = 0; i < hct.length; i++)
			{
				try
				{
					hct[i].join();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			System.out.print(" ... ");
			// send end signal to output pipeline
			outQ.shutdown();
			// wait until writer finishes
			try
			{
				writer.join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			
			if(mergeClustersByIdenticalFeatures)
			{
				System.out.println( "	postclustering ... " );
				p.mysql.executeSQLFile( XJava.getPackageResource( this.getClass(), "clustering/postclustering/merge_clusters_by_identical_features.sql" ) );
			}
			
			// show run time info
			System.out.println(" [" + tc.stop().getSecString() + "]");
			p.log.add(LogEntry.newEvent("hierachical emrt clustering done", t.stop().getSecString()));
		}
		else
		{
			System.out.println("skipping cluster refinement ...");
		}
		System.out.println("cumulated clustering duration: " + t.getSecString());
	}

	/**
	 * @param p
	 */
	private void logClusteringParameters(DBProject p)
	{
		System.out.println("	parameters:");
		System.out.println( "		database schema name: '" + p.data.db + "'" );
		System.out.println("		parallelization degree: '" + nThreads + "'");
		System.out.println("		time resolution: '" + rtRes + "'");
		System.out.println("		mass resolution: '" + mzRes + "'");
		System.out.println("		drift resolution: '" + dtRes + "'");
		System.out.println("		distance threshold: '" + distanceTreshold + "'");
		System.out.println("		clustering method: '" + clusteringMethod + "'");
		if (clusteringMethod.equals(ClusteringMethod.NONE))
		{
		}
		else if (clusteringMethod.equals(ClusteringMethod.HNH))
		{
			System.out.println("			neighbor finder: '" + neighborFinder + "'");
			System.out.println("			distance metric: '" + distanceMetric + "'");
		}
		else
		{
			System.out.println("			min neighbor count: '" + minNeighborCount + "'");
		}
		p.log.add(LogEntry.newParameter("process.emrt.clustering.distance.unit.mass.ppm", mzRes));
		p.log.add(LogEntry.newParameter("process.emrt.clustering.distance.unit.time.min", rtRes));
		p.log.add(LogEntry.newParameter("process.emrt.clustering.distance.unit.drift.bin", dtRes));
// p.log.add(
// LogEntry.newParameter("process.emrt.clustering.distance.threshold",
// distanceTreshold) );
// p.log.add( LogEntry.newParameter("process.emrt.clustering.method",
// clusteringMethod));
		if (clusteringMethod.equals(ClusteringMethod.NONE))
		{
		}
		else if (clusteringMethod.equals(ClusteringMethod.HNH))
		{
			p.log.add(LogEntry.newParameter("process.emrt.clustering.hnh.joiningMethod", neighborFinder));
			p.log.add(LogEntry.newParameter("process.emrt.clustering.hnh.distanceMetric", distanceMetric));
		}
		else
		{
			p.log.add(LogEntry.newParameter("process.emrt.clustering.dbscan.minNeighborCount", minNeighborCount));
		}
	}
}
