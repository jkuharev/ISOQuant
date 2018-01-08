/** ISOQuant, isoquant.kernel.db, 27.09.2012 */
package isoquant.kernel.db;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.plgs.data.Sample;
import de.mz.jk.plgs.data.Workflow;

/**
 * <h3>{@link IQDBUtils}</h3>
 * @author kuharev
 * @version 27.09.2012 15:34:44
 */
public class IQDBUtils
{
	/**
	 * extract the meta info for a run and a specific type,
	 * type is not a part of returned parameter name!
	 * @param p
	 * @param run
	 * @param metaInfoType
	 * @return
	 */
	public static Map<String, String> getWorkflowMetaInfo(DBProject p, Workflow run, String metaInfoType)
	{
		return p.mysql.getMap(
				"SELECT `name`, `value` "
						+ "FROM workflow_metadata "
						+ "WHERE `workflow_index`=" + run.index + " AND `type`='" + metaInfoType + "'", "name", "value" );
	}

	/**
	 * list Workflows of a project <br>
	 * the contained meta info is prefixied by type and a dot before the name of parameter,
	 * so info type is a part of returned parameter name, e.g. MassSpectrum.AcquiredDate, Workflow.WORKFLOW_ID, etc.!
	 * @param p
	 * @return
	 */
	public static List<Workflow> getWorkflows(DBProject p)
	{
		String sql = "SELECT * FROM `workflow` ORDER BY `sample_index` ASC, `index` ASC";
		List<Workflow> res = new ArrayList<Workflow>();
		synchronized (p.mysql)
		{
			ResultSet rs = p.mysql.executeSQL(sql);
			try
			{
				while (rs.next())
				{
					Workflow w = new Workflow();
					w.index = rs.getInt("index");
					w.sample_index = rs.getInt("sample_index");
					w.sample_description = XJava.decURL(rs.getString("sample_description"));
					w.replicate_name = XJava.decURL(rs.getString("replicate_name"));
					w.input_file = XJava.decURL(rs.getString("input_file"));
					w.acquired_name = rs.getString("acquired_name");
					res.add(w);
				}
				for ( Workflow run : res )
				{
					if (p.mysql.tableExists( "workflow_metadata" ))
					{
						run.metaInfo = p.mysql.getMap(
								"SELECT CONCAT(`type`,'.',`name`) as `key`, `value` "
										+ "FROM workflow_metadata "
										+ "WHERE `workflow_index`=" + run.index, "key", "value" );
					}
					else
					{
						System.err.println( "Meta information for project " + p.data.title + " is not available!" );
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return res;
	}

	/**
	 * list Workflows of a sample
	 * @param p
	 * @param sampleIndex
	 * @return
	 */
	public static List<Workflow> getWorkflowsBySampleIndex(DBProject p, int sampleIndex)
	{
		String sql = "SELECT * FROM `workflow` WHERE `sample_index`= " + sampleIndex + " ORDER BY `index`";
		List<Workflow> res = new ArrayList<Workflow>();
		synchronized (p.mysql)
		{
			ResultSet rs = p.mysql.executeSQL(sql);
			try
			{
				while (rs.next())
				{
					Workflow w = new Workflow();
					w.index = rs.getInt("index");
					w.sample_index = rs.getInt("sample_index");
					w.sample_description = XJava.decURL(rs.getString("sample_description"));
					w.replicate_name = XJava.decURL(rs.getString("replicate_name"));
					w.input_file = XJava.decURL(rs.getString("input_file"));
					w.acquired_name = rs.getString("acquired_name");
					res.add(w);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return res;
	}

	/**
	 * list Workflows of a sample
	 * @param p
	 * @param groupIndex
	 * @return
	 */
	public static List<Workflow> getWorkflowsByGroupIndex(DBProject p, int groupIndex)
	{
		String sql =
				"SELECT w.* " +
						"FROM `workflow` as w JOIN `sample` as s ON  w.`sample_index` = s.`index` " +
						"WHERE s.`group_index`= " + groupIndex + " ORDER BY w.`sample_index`, w.`index`";
		List<Workflow> res = new ArrayList<Workflow>();
		synchronized (p.mysql)
		{
			ResultSet rs = p.mysql.executeSQL(sql);
			try
			{
				while (rs.next())
				{
					Workflow w = new Workflow();
					w.index = rs.getInt("index");
					w.sample_index = rs.getInt("sample_index");
					w.sample_description = XJava.decURL(rs.getString("sample_description"));
					w.replicate_name = XJava.decURL(rs.getString("replicate_name"));
					w.input_file = XJava.decURL(rs.getString("input_file"));
					w.acquired_name = rs.getString("acquired_name");
					res.add(w);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return res;
	}

	/**
	 * list Samples of a project
	 * @param p
	 * @return
	 */
	public static List<Sample> getSamples(DBProject p)
	{
		String sql = "SELECT * FROM `sample` ORDER BY `index`";
		List<Sample> res = new ArrayList<Sample>();
		synchronized (p.mysql)
		{
			ResultSet rs = p.mysql.executeSQL(sql);
			try
			{
				while (rs.next())
				{
					Sample s = new Sample();
					s.index = rs.getInt("index");
					s.name = XJava.decURL(rs.getString("name"));
					s.id = rs.getString("id");
					res.add(s);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return res;
	}

	/**
	 * get sample names mapped to indices
	 * @param db
	 * @return
	 */
	public static Map<Integer, String> getSampleIndexToNameMap(MySQL db)
	{
		Map<Integer, String> res = new HashMap<Integer, String>();
		String sql = "SELECT * FROM `sample` ORDER BY `index`";
		synchronized (db)
		{
			ResultSet rs = db.executeSQL(sql);
			try
			{
				while (rs.next())
				{
					res.put(rs.getInt("index"), rs.getString("name"));
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return res;
	}

	public static int countMobilityRuns(MySQL db)
	{
		return db.getFirstInt(
				"SELECT (count(*)-sum(mse)) FROM (SELECT ( max(Mobility) = min(Mobility) ) as mse FROM `mass_spectrum` GROUP BY workflow_index) xxx",
				1);
	}

	public static int countNonMobilityRuns(MySQL db)
	{
		return db.getFirstInt(
				"SELECT sum(mse) FROM (SELECT ( max(Mobility) = min(Mobility) ) as mse FROM `mass_spectrum` GROUP BY workflow_index) xxx",
				1);
	}

	public static boolean hasMixedMobility(MySQL db)
	{
		return db.getFirstInt(
				"SELECT ( sum(mse) > 0 AND sum(mse) < count(*) ) as mixed FROM " +
						" (SELECT ( max(Mobility) = min(Mobility) ) as mse FROM `mass_spectrum` GROUP BY workflow_index) xxx",
				1) == 1;
	}

	/**
	 * extract an integer to string map from a string 
	 * @param k2vString grouped map string formatted as follows "key1:value1;k2:v2;k3:v3;..."
	 * @return
	 */
	public static Map<Integer, String> extractI2SMap(String k2vString)
	{
		return extractI2SMap( k2vString, ";", ":" );
	}

	/**
	 * extract an integer to string map from a string 
	 * @param k2vString grouped map string formatted as follows "key1:value1;k2:v2;k3:v3;..."
	 * @param groupSep group separator string e.g. ";"
	 * @param valueSep value separator string e.g. ":"
	 * @return
	 */
	public static Map<Integer, String> extractI2SMap(String k2vString, String groupSep, String valueSep)
	{
		if (k2vString.endsWith( groupSep )) k2vString = k2vString.substring( 0, k2vString.length() - 1 );
		Map<Integer, String> res = new HashMap<Integer, String>();
		String[] elements = k2vString.split( groupSep );
		for (String e : elements)
		{
			String[] kv = e.split( valueSep );
			if (kv.length > 1) res.put(Integer.parseInt(kv[0]), kv[1]);
		}
		return res;
	}

	/**
	 * extract an integer to double map from a string 
	 * @param k2vString grouped map string formatted as follows "key1:value1;k2:v2;k3:v3;..."
	 * @return
	 */
	public static Map<Integer, Double> extractI2DMap(String k2vString)
	{
		return extractI2DMap( k2vString, ";", ":" );
	}

	/**
	 * extract an integer to double map from a string
	 * @param k2vString grouped map string formatted as follows "key1:value1;k2:v2;k3:v3;..."
	 * @param groupSep group separator string e.g. ";"
	 * @param valueSep value separator string e.g. ":"
	 * @return
	 */
	public static Map<Integer, Double> extractI2DMap(String k2vString, String groupSep, String valueSep)
	{
		if (k2vString.endsWith( groupSep )) k2vString = k2vString.substring( 0, k2vString.length() - 1 );
		Map<Integer, Double> res = new HashMap<Integer, Double>();
		String[] elements = k2vString.split( groupSep );
		for (String e : elements)
		{
			String[] kv = e.split( valueSep );
			if (kv.length > 1) res.put(Integer.parseInt(kv[0]), Double.parseDouble(kv[1]));
		}
		return res;
	}

	public static List<DBProject> getProjects(MySQL db, boolean withDBConnection)
	{
		List<DBProject> res = new ArrayList<DBProject>();
		try
		{
			ResultSet rs = db.executeSQL( "SELECT * FROM `project` ORDER BY `index` ASC" );
			while (rs.next())
			{
				DBProject p = new DBProject();
				try
				{
					p.data.index = rs.getInt( "index" );
					p.data.id = rs.getString( "id" );
					p.data.title = XJava.decURL( rs.getString( "title" ) );
					p.data.root = XJava.decURL( rs.getString( "root" ) );
					p.data.state = rs.getString( "state" );
					p.data.db = rs.getString( "db" );
					res.add( p );
					if (withDBConnection && db.schemaExists( p.data.db ))
						p.setMySQL( db.getDB( p.data.db ) );
				}
				catch (Exception e)
				{
					System.out.println( "Problem occured while reading project '" + p.data.db + "'" );
					e.printStackTrace();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}
}
