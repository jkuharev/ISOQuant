package isoquant.interfaces.dependency;

import de.mz.jk.jsix.mysql.MySQL;

public interface iDependencyOnDatabase extends iDependency 
{
	public boolean ok(MySQL db);
}
