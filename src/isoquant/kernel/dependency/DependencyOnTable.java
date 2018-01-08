package isoquant.kernel.dependency;

import isoquant.interfaces.dependency.iDependency;
import isoquant.interfaces.dependency.iDependencyOnDatabase;

import java.util.List;

import de.mz.jk.jsix.mysql.MySQL;

public class DependencyOnTable implements iDependencyOnDatabase
{
	private State state = State.unknown;
	private String table = "default";
	
	public DependencyOnTable(String table) 
	{
		this.table = table;
	}

	public String getTable(){return table;}

	@Override public State getState(){return state;}
	@Override public boolean ok(){return state.ok();}

	@Override public boolean ok(MySQL db)
	{
		List<String> tabs = db.listTables();
		for(String tab : tabs)
		{
			if( tab.equals(table) ) 
			{
				state = State.fullfilled;
				break;
			}
		}		
		return ok();
	}

	@Override public boolean equals(iDependency otherDependency)
	{
		return (otherDependency instanceof DependencyOnTable) 
			? ((DependencyOnTable) otherDependency).getTable().equals(table)
			: false;		
	}
}
