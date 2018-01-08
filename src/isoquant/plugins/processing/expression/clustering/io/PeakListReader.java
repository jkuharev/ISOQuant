package isoquant.plugins.processing.expression.clustering.io;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.ui.iProcessProgressListener;
import de.mz.jk.ms.clust.com.ClusteringPeak;
import de.mz.jk.ms.clust.com.PipeLine;

/**
 * Thread for loading clustered peaks from clustered_emrt into a queue 
 * @author Jörg Kuharev
 */
public class PeakListReader extends Thread
{
	private MySQL db = null;
	private PipeLine<Collection<ClusteringPeak>> queue = null;
	private iProcessProgressListener progressListener = null;

	public PeakListReader(MySQL db, PipeLine<Collection<ClusteringPeak>> queue)
	{
		init(db, queue);
		start();
	}

	public PeakListReader(MySQL db, PipeLine<Collection<ClusteringPeak>> queue, iProcessProgressListener progressListener)
	{
		init(db, queue);
		setProgressListener(progressListener);
		start();
	}

	private void init(MySQL db, PipeLine<Collection<ClusteringPeak>> queue)
	{
		this.db = db.clone();
		this.db.getConnection(false);
		this.queue = queue;
	}

	/**
	 * @param progressListener the progressListener to set
	 */
	public void setProgressListener(iProcessProgressListener progressListener)
	{
		this.progressListener = progressListener;
	}

	public void run()
	{
		if (queue == null) return;
		List<Integer> clusterIndexes = getClusterIndexes();
		List<List<ClusteringPeak>> clusteredPeaks = null;
		int nClusters = clusterIndexes.size();
		int cStep = 1000;
		int nSteps = nClusters / cStep;
		int progressCounter = 0;
		float partSize = (float) (nSteps) / 78f;
		if (progressListener != null)
		{
			progressListener.setProgressMaxValue(nClusters);
			progressListener.setMessage( "reading " + nClusters + " preclusters ..." );
		}
		for (int i = 0; i < nClusters; i++)
		{
			clusteredPeaks = getPeaks(i, i + cStep);
			if (clusteredPeaks != null)
				for (List<ClusteringPeak> peaks : clusteredPeaks)
				{
					queue.put(peaks);
				}
			i += cStep;
			if (progressListener != null) progressListener.setProgressValue(i);
			if (++progressCounter > partSize)
			{
				if (progressListener != null) progressListener.setStatus(i + "/" + nClusters);
				progressCounter = 0;
				System.out.print(".");
			}
		}
		queue.shutdown();
		db.closeConnection(false);
		if (progressListener != null)
		{
			progressListener.setProgressValue(0);
			progressListener.setStatus("");
		}
	}

	List<Integer> getClusterIndexes()
	{
		return db.getIntegerValues(
				"SELECT DISTINCT `cluster_average_index` FROM `clustered_emrt` ORDER BY `cluster_average_index` ASC"
				);
	}

	private List<List<ClusteringPeak>> getPeaks(int fromClusterIndex, int toClusterIndex)
	{
		List<List<ClusteringPeak>> res = new ArrayList<List<ClusteringPeak>>();
		int oldC = -1;
		int cntC = -1;
		try
		{
			ResultSet rs = db.executeSQL(
					"SELECT `index`, `mass`, `ref_rt`, inten, Mobility, `cluster_average_index` FROM `clustered_emrt` " +
							" WHERE `cluster_average_index` BETWEEN " + fromClusterIndex + " AND " + toClusterIndex +
							" ORDER BY cluster_average_index ASC"
					);
			while (rs.next())
			{
				ClusteringPeak p = new ClusteringPeak();
				p.id = rs.getInt("index");
				p.mass = rs.getFloat("mass");
				p.time = rs.getFloat("ref_rt");
				p.inten = rs.getFloat("inten");
				p.drift = rs.getFloat("Mobility"); // patiently waiting for ion
// mobility
				p.cluster = rs.getInt("cluster_average_index");
				if (p.cluster != oldC)
				{
					cntC++;
					res.add(new ArrayList<ClusteringPeak>());
					oldC = p.cluster;
				}
				res.get(cntC).add(p);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}
}
