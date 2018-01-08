package isoquant.kernel.dependency;

import isoquant.interfaces.dependency.iDependency;
import isoquant.interfaces.dependency.iDependencyOnDatabase;

import java.util.List;

import de.mz.jk.jsix.mysql.MySQL;

public class DependencyOnColumn implements iDependencyOnDatabase
{
	private State state = State.unknown;
	private String column = "";
	private DependencyOnTable tabDep = null;

	public DependencyOnColumn(String table, String column) 
	{
		tabDep = new DependencyOnTable(table);
		this.column = column;
	}
	
	public String getColumn(){return column;}
	
	@Override public boolean ok(MySQL db)
	{
		if( tabDep.ok(db) )
		{
			List<String> cols = db.listColumns( tabDep.getTable() );
			for(String col : cols)
			{
				if(col.equalsIgnoreCase(column))
				{
					state = State.fullfilled;
					break;
				}
			}
		}
		return ok();
	}

	@Override public boolean equals(iDependency otherDependency)
	{
		if (otherDependency instanceof DependencyOnColumn)
		{
			DependencyOnColumn od = (DependencyOnColumn)otherDependency;
			return 
				od.tabDep.getTable().equals(tabDep.getTable()) &&
				od.getColumn().equalsIgnoreCase(getColumn());
			
		}
		return false;
	}

	@Override public State getState(){return state;}
	@Override public boolean ok(){return state.ok();}

}
