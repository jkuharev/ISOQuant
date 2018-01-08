package isoquant.interfaces.log;


import java.util.List;

import de.mz.jk.jsix.mysql.MySQL;

/**
 * <h3>iLogManager</h3>
 * 
 * @author Joerg Kuharev
 * @version 30.12.2010 11:59:21
 */
public interface iLogManager 
{
	/** 
	 * set current database
	 * @param db
	 */
	public void setDB(MySQL db);
	
	/** 
	 * get current database
	 */
	public MySQL getDB();

	/**
	 * add a new log entry
	 * @param entry
	 */
	public void add(iLogEntry logEntry);
	
	/**
	 * add a list of log entries
	 * @param logEntries
	 */
	public void add(List<iLogEntry> logEntries);
	
	/**
	 * get log entries
	 * @return list of log entries
	 */
	public List<iLogEntry> get();
}
