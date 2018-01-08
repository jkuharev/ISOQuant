package isoquant.kernel.log;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.mysql.MySQL;
import isoquant.interfaces.log.iLogEntry;
import isoquant.interfaces.log.iLogEntry.Type;
import isoquant.interfaces.log.iLogManager;

/**
 * <h3>LogManager</h3>
 * managing of history entries for each project database
 * @author Joerg Kuharev
 * @version 30.12.2010 11:51:21
 */
public class LogManager implements iLogManager
{	
	private MySQL db = null;

	/**
	 * create LogManager for database
	 * @param db
	 */
	public LogManager(MySQL db) 
	{
		setDB(db);
		initDB();
	}
	
	/**
	 * create LogManager for database connection
	 * @param con
	 */
	public LogManager(Connection con)
	{
		this( new MySQL(con) );
	}
	
	/**
	 * create history table if it not already exists 
	 */
	public void initDB()
	{
		db.executeSQL(
			"CREATE TABLE IF NOT EXISTS `history` ("+
			"	`id` INT NOT NULL AUTO_INCREMENT,"+
			"	`time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"+
			"	`type` ENUM('event', 'parameter', 'message') DEFAULT 'message',"+
			"	`value` TEXT NOT NULL,"+
			"	`note` TEXT NOT NULL,"+
			"	PRIMARY KEY (`id`)) "
		);
		db.closeConnection( false );
	}
	
	@Override public void setDB(MySQL db){this.db = db;}
	@Override public MySQL getDB(){return db;}
	
	@Override public void add(List<iLogEntry> logEntries){for(iLogEntry e : logEntries) add(e);}
	
	@Override public void add(iLogEntry entry)
	{
		if(!db.tableExists("history")) initDB();
		
		String sql = 
			"INSERT INTO history SET " +
			" type='"+entry.getType()+"'," +
			" value='"+XJava.encURL( entry.getValue() )+"',"+
			" note='"+XJava.encURL( entry.getNote() )+"'";
		db.executeSQL(sql);
	}
	
	@Override public List<iLogEntry> get()
	{
		if(!db.tableExists("history")) initDB();
		
		List<iLogEntry> res = new ArrayList<iLogEntry>();
		ResultSet rs = db.executeSQL("SELECT `id`,DATE_FORMAT(`time`, '%Y-%m-%d %H:%i:%S') as `time`,`type`,`value`,`note` FROM history");
		
		try {
			while( rs!=null && rs.next() )
			{
				res.add(
					new LogEntry().set(
						rs.getInt("id"), 
						rs.getString("time"), 
						Type.fromString( rs.getString("type") ), 
						XJava.decURL( rs.getString("value") ),
						XJava.decURL( rs.getString("note") )
					)
				);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}
}
