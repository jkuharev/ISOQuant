/** ISOQuant, isoquant.plugins.processing.expression.align.io, 11.05.2011*/
package isoquant.plugins.processing.expression.align.io;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.ms.align.com.IMSPeak;
import isoquant.plugins.processing.normalization.xy.XYPointList;

/**
 * <h3>{@link MassSpectrumPeakStorage}</h3>
 * @author Joerg Kuharev
 * @version 11.05.2011 13:19:09
 */
public class MassSpectrumPeakStorage extends PeakStorage
{
	public MassSpectrumPeakStorage( MySQL _db )
	{
		super(_db);
		
		this.db.executeSQL("OPTIMIZE TABLE mass_spectrum");
		this.db.executeSQL("OPTIMIZE TABLE clustered_emrt");
		
		findMaxRT();
	}

	/**
	 * all run indexes descending ordered by number of peaks
	 * @return list of indexes
	 */
	synchronized public List<Integer> getRunIndexes() 
	{
		return db.getIntegerValues(
			"SELECT `workflow_index` FROM `clustered_emrt` " +
			"GROUP BY `workflow_index` ORDER BY COUNT(`index`) DESC"
		);
	}
	
	/**
	 * run indexes in a single sample descending ordered by number of peaks
	 * @param sampleIndex
	 * @return list of indexes
	 */
	synchronized public List<Integer> getRunIndexes( int sampleIndex ) 
	{
		return db.getIntegerValues(
			"SELECT `workflow_index` " +
			"FROM `clustered_emrt` as ce JOIN workflow as w ON ce.workflow_index=w.`index` " +
			"WHERE w.sample_index="+sampleIndex+" " +
			"GROUP BY `workflow_index` ORDER BY COUNT(ce.`index`) DESC"
		);
	}
	
	/**
	 * sample indexes
	 * @return list of indexes
	 */
	synchronized public List<Integer> getParentUnitIndexes() 
	{
		return db.getIntegerValues("SELECT DISTINCT sample_index FROM workflow ORDER BY sample_index ASC");
	}
	
	/**
	 * get all peaks of a run
	 * @param workflowIndex run identification
	 * @param rtCol one of ["LiftOffRT", "InfUpRT", "RT", "InfDownRT", "TouchDownRT"]
	 * @param maxPeaks maximum number of peaks
	 * @return
	 * @throws Exception
	 */
	@Override
	synchronized public List<IMSPeak> getAllPeaks(int workflowIndex)
	{
		List<IMSPeak> res = new ArrayList<IMSPeak>();
		try
		{
			ResultSet rs = db.executeSQL( 
				"SELECT `index`, Mass, RT, Mobility, Intensity " +
				" FROM `mass_spectrum` WHERE " +
				" `workflow_index`="+workflowIndex+
				" ORDER BY RT ASC"
			);
			while( rs.next() )
			{
				res.add(
						new IMSPeak(
								rs.getInt( "index" ),
								rs.getFloat( "Mass" ),
								rs.getFloat( "RT" ),
								rs.getFloat( "Mobility" ),
								rs.getFloat( "Intensity" ) 
						)
				);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}
	
	/**
	 * get limited number of peaks
	 * @param workflowIndex run identification
	 * @param rtCol one of ["LiftOffRT", "InfUpRT", "RT", "InfDownRT", "TouchDownRT"]
	 * @param maxPeaks maximum number of peaks
	 * @param byMass if true the resulting peaks are selected by highest mass otherwise by highest intensity 
	 * @return
	 * @throws Exception
	 */
	synchronized public List<IMSPeak> getAbstractedPeaks(int workflowIndex, int maxPeaks, double minMass, int minInten, boolean byMass)
	{
		List<IMSPeak> res = new ArrayList<IMSPeak>();
		try
		{
			db.optimizeTable( "mass_spectrum" );
			String table = "tmp_" + XJava.timeStamp();
			db.dropTable( table );
			db.executeSQL(
					"CREATE TEMPORARY TABLE `" + table
							+ "` (`index` INTEGER, Mass DOUBLE, RT DOUBLE, Mobility DOUBLE, Intensity DOUBLE, PRIMARY KEY(`index`), KEY(`RT`)); " );
			db.executeSQL(
					"INSERT INTO `" + table + "` " +
							" SELECT `index`, Mass, RT, Mobility, Intensity " +
							" FROM `mass_spectrum` " +
							" WHERE workflow_index=" + workflowIndex +
							" AND Mass >= " + minMass +
							" AND Intensity >= " + minInten +
							" ORDER BY `" + ( byMass ? "Mass" : "Intensity" ) + "` DESC LIMIT " + maxPeaks + " " );
			db.optimizeTable( table );
			ResultSet rs = db.executeSQL( "SELECT * FROM `" + table + "` ORDER BY RT ASC" );
			while (rs.next())
			{
				res.add(
						new IMSPeak(
								rs.getInt( "index" ),
								rs.getFloat( "Mass" ),
								rs.getFloat( "RT" ),
								rs.getFloat( "Mobility" ),
								rs.getFloat( "Intensity" ) ) );
			}
			db.dropTable( table );
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}
	
	/**
	 * retrieve peaks from given mass range 
	 * @param workflowIndex  run identification
	 * @param minMass minimum mass
	 * @param minInten minimum intensity
	 * @return
	 * @throws Exception
	 */
	synchronized public List<IMSPeak> getPeaksByMinMassAndIntensity(int workflowIndex, double minMass, int minInten)
	{
		List<IMSPeak> res = new ArrayList<IMSPeak>();
		try
		{
			ResultSet rs = db.executeSQL( 
				"SELECT `index`, Mass, RT, Mobility, Intensity  FROM mass_spectrum " +
				" WHERE workflow_index="+workflowIndex+
				" AND Mass >= "+minMass+
				" AND Intensity >= " + minInten +
				" ORDER BY RT ASC"
			);
			while( rs.next() )
			{
				res.add(
						new IMSPeak(
								rs.getInt( "index" ),
								rs.getFloat( "Mass" ),
								rs.getFloat( "RT" ),
								rs.getFloat( "Mobility" ),
								rs.getFloat( "Intensity" ) 
						)
				);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}
	
	/**
	 * find and retrieve maximum known retention time from mass_spectrum table 
	 * @return
	 */
	synchronized public double findMaxRT()
	{
		maxRT = Math.max(maxRT, getMaxColValue("RT", null) + 2.0 ); 
		return maxRT;
	}
	
	/**
	 * get maximum value for a column from table mass_spectrum
	 * @param colName
	 * @param runIndex run index for a single run or null for all runs
	 * @return max value of given column or 0.0 on error
	 */
	synchronized public double getMaxColValue(String colName, Integer runIndex)
	{
		try{
			return Double.parseDouble( 
				db.getFirstValue("mass_spectrum", "max(`"+colName+"`)", 
				(runIndex!=null) ? "workflow_index='"+runIndex+"'" : "1") 
			);
		}catch (Exception e) {
			return 0.0;
		}		
	}

	/**
	 * get peaks in a mass window
	 * @param runIndex
	 * @param fromMass
	 * @param toMass
	 * @return
	 */
	synchronized public List<IMSPeak> getPeaksInMassWindow(int runIndex, double fromMass, double toMass)
	{
		List<IMSPeak> res = new ArrayList<IMSPeak>();
		try
		{
			ResultSet rs = db.executeSQL(
					"SELECT `index`, Mass, RT, Mobility, Intensity FROM mass_spectrum " +
							" WHERE workflow_index=" + runIndex + " AND " +
							" Mass BETWEEN " + fromMass + " AND " + toMass +
							" ORDER BY RT ASC" );
			while (rs.next())
			{
				res.add(
						new IMSPeak(
								rs.getInt( "index" ),
								rs.getFloat( "Mass" ),
								rs.getFloat( "RT" ),
								rs.getFloat( "Mobility" ),
								rs.getFloat( "Intensity" ) 
						)
				);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * get retention times from pairwise identified peptides in a run (the run) and the reference run (ref run)  
	 * @param theRunIndex
	 * @param refRunIndex
	 * @return XYPointList having 
	 * 		the retention times of the runIndex as x and 
	 * 		the retention times of the reference run as y values,
	 * 		the points are ordered by increasing x-values
	 */
	public XYPointList getPairwiseIdentifiedPeptideRTs(int theRunIndex, int refRunIndex)
	{
		XYPointList res = new XYPointList();
		String sql = "SELECT run.`retention_time_rounded` as rt, ref.`retention_time_rounded` as ref_rt FROM \n" +
				"(SELECT `sequence`,`modifier`,`retention_time_rounded` FROM `peptide` as pep \n" +
				"JOIN query_mass as qm ON pep.`query_mass_index`=qm.`index` \n" +
				"JOIN low_energy as le ON qm.`low_energy_index`=le.`index` \n" +
				"WHERE pep.workflow_index=" + theRunIndex + " GROUP BY le.`index`) as run JOIN \n" +
				"(SELECT `sequence`,`modifier`,`retention_time_rounded` FROM `peptide` as pep \n" +
				"JOIN query_mass as qm ON pep.`query_mass_index`=qm.`index` \n" +
				"JOIN low_energy as le ON qm.`low_energy_index`=le.`index` \n" +
				"WHERE pep.workflow_index=" + refRunIndex + "  GROUP BY le.`index`) as ref \n" +
				" USING(`sequence`, `modifier`) ORDER BY rt";
		try
		{
			ResultSet rs = db.executeSQL( sql );
			while (rs.next())
			{
				res.addPoint( rs.getDouble( 1 ), rs.getDouble( 2 ) );
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}

	@Override 
	public int countAllPeaks(int runIndex)
	{
		String sql = "SELECT COUNT(*) FROM mass_spectrum WHERE workflow_index=" + runIndex;
		return db.getFirstInt( sql, 1 );
	}

	public int countPeaksOverThreshold(int runIndex, double minMass, int minInten)
	{
		String sql = "SELECT Count(*) FROM mass_spectrum " +
				" WHERE workflow_index="+runIndex+
				" AND Mass >= "+minMass+
				" AND Intensity >= " + minInten;
		return db.getFirstInt( sql, 1 );
	}

}
