package isoquant.interfaces.dependency;


import java.util.List;

import de.mz.jk.jsix.mysql.MySQL;

public interface iDependencyManager
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
	 * check a single dependency
	 * @param dp
	 * @return true if dp.ok()==true after checking, or false if not
	 */
	public boolean check(iDependency dp);
	
	/**
	 * check a set of dependencies
	 * @param dps
	 * @return true if all checked depencies are ok, otherwise false
	 */
	public boolean check(List<iDependency> dps);
}
