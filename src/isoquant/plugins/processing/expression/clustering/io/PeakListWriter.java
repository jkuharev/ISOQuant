package isoquant.plugins.processing.expression.clustering.io;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.ms.clust.com.ClusteringPeak;
import de.mz.jk.ms.clust.com.PipeLine;


/**
 * Thread for loading clustered peaks from clustered_emrt into a queue 
 * @author Jšrg Kuharev
 */
public class PeakListWriter extends Thread 
{
	private MySQL db = null;
	
	private PipeLine<Collection<ClusteringPeak>> queue = null;	
	private int clusterID = 0;

	public PeakListWriter(MySQL db, PipeLine<Collection<ClusteringPeak>> queue)
	{
		this.db = db.clone();
		this.db.getConnection(false);
		this.queue = queue;
		start();
	}
	
	public void run()
	{
		if(queue == null) return;
		
		// System.out.println("peak cluster writer started.");
		
		List<ClusteringPeak> peakCollector = new ArrayList<ClusteringPeak>();
		int commitOnCollectorSize = 5000;
		
		initStorage();
		
		// never ending loop
		while(true)
		{
			// take next peak list from queue
			Collection<ClusteringPeak> peaks = queue.take();
			
			// if end of queue not reached
			if(peaks!=null)
			{
				// increment cluster id
				clusterID++;
				// assign cluster id to peaks of a cluster
				for(ClusteringPeak p : peaks)
				{
					p.cluster = clusterID;
				}
				// collect peaks
				peakCollector.addAll(peaks);
				// store collected peaks if max buffer size reached 
				if(peakCollector.size()>commitOnCollectorSize)
				{
					storePeaks(peakCollector);
					// clear buffer
					peakCollector.clear();
				}
			}
			else
			{
				// submit if buffer not empty
				if(peakCollector.size()>0)
				{
					storePeaks(peakCollector);
				}
				// end loop
				break;
			}
		}
		
		// inject clustering results into clustered_emrt
		db.executeSQL(
			"UPDATE clustered_emrt as ce JOIN hc USING(`index`) " +
			"SET ce.cluster_average_index=hc.`cluster`"
		);
		
		db.closeConnection(false);
	}
	
	/**
	 * insert peaks into a temporary in-memory table
	 * and move temporary table' content into hc after all peaks inserted 
	 * @param peaks
	 */
	private void storePeaks(List<ClusteringPeak> peaks) 
	{
		String table = "`thc_" + System.currentTimeMillis()+"`";
		db.executeSQL("DROP TABLE IF EXISTS "+table);
		db.executeSQL("CREATE TABLE "+table+" (`index` INTEGER, `cluster` INTEGER, PRIMARY KEY(`index`)) ENGINE=MEMORY");
		
		int bufCnt = 0;
		int bufSize = 200;
		String sqlBuf = "";
		String sqlPrefix = "INSERT INTO "+table+" (`index`,`cluster`) VALUES ";
		
		for(ClusteringPeak p : peaks)
		{
			sqlBuf += ((bufCnt>0) ? "," : "") + "('"+p.id+"','"+p.cluster+"')";
			
			if(bufCnt++ > bufSize)
			{
				db.executeSQL(sqlPrefix + sqlBuf);
				bufCnt = 0;
				sqlBuf = "";
			}
		}
		
		if(bufCnt>0) db.executeSQL(sqlPrefix + sqlBuf);
		
		db.executeSQL("REPLACE INTO hc SELECT `index`,`cluster` FROM "+table);
		db.executeSQL("DROP TABLE IF EXISTS "+table);
	}
	
	/**
	 * CREATE TABLE hc(`index` INTEGER, `cluster` INTEGER, PRIMARY KEY(`index`))
	 */
	private void initStorage()
	{
		db.dropTable("hc");
		db.executeSQL("CREATE TEMPORARY TABLE hc(`index` INTEGER, `cluster` INTEGER, PRIMARY KEY(`index`))");
	}
}
