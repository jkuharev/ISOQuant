package isoquant.kernel.dependency;

import isoquant.interfaces.dependency.iDependency;
import isoquant.interfaces.dependency.iDependencyManager;

import java.util.List;

import de.mz.jk.jsix.mysql.MySQL;

public class DependencyManager implements iDependencyManager
{
	private MySQL db = null;
	
	@Override public void setDB(MySQL db)
	{
		this.db = db;
	}
	
	@Override public MySQL getDB()
	{
		return db;
	}

	@Override public boolean check(List<iDependency> dps){
		return false;
	}

	@Override public boolean check(iDependency dp){
		return false;
	}
}
