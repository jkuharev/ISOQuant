package isoquant.kernel.db;

import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.plgs.data.Project;
import isoquant.interfaces.log.iLogManager;
import isoquant.kernel.log.LogManager;

/**
 * wrap the PLGS Project class and add data base functinality
 * <h3>{@link DBProject}</h3>
 * @author jkuharev
 * @version Nov 6, 2017 1:26:45 PM
 */
public class DBProject implements Comparable<DBProject>
{
	public Project data = new Project();

	/** corresponding mysql object */
	public MySQL mysql = null;
	/** project related log manager */
	public iLogManager log = null;

	public DBProject()
	{}

	public DBProject(Project plgsPrj)
	{
		this.data = plgsPrj;
	}

	/** the ability for a list of {@link DBProject}s to be ordered */
	@Override public int compareTo(DBProject p)
	{
		// compare by data project
		return this.data.compareTo( p.data );
	}

	public void setMySQL(MySQL _db)
	{
		if(_db==null) return;
		try
		{
			if (_db.getConnection( false ) == null)
				throw new Exception( "failed to connect the database '" + this.data.db + "'" );
			mysql = _db;
			log = new LogManager( _db );
			_db.closeConnection( false );
		}
		catch (Exception e)
		{
//			e.printStackTrace();
		}
	}
	
	/**
	 * clone project,
	 * no samples and no expression analyses copied!!!
	 */
	@Override public DBProject clone()
	{
		DBProject clone = new DBProject();
		clone.data = data.clone();
		if (this.mysql != null) clone.setMySQL( mysql.clone() );
		return clone;
	}

	public void dump()
	{
		data.dump();
	}

	@Override public String toString()
	{
		return data.toString();
	}
}
