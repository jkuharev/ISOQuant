package isoquant.kernel.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.math.Hash;
import de.mz.jk.jsix.mysql.MySQL;
import isoquant.interfaces.iProjectManager;

/**
 * <h3>ProjectManager</h3>
 * management of projects in database
 * @author Joerg Kuharev
 * @version 29.12.2010 15:27:58
 */
public class ProjectManager implements iProjectManager 
{
	public static final String mgtDBName = "mass_projects";	
	private MySQL db = null;
	private List<DBProject> existingProjects = null;
	private boolean dbChanged = true;
	
	public final String STRUCTURE_SQL_FILE_NAME = "isoquant/kernel/db/management.sql";

	public static final String[] basicProjectTables = 
	{
			"project",
			"expression_analysis",
			"group",
			"sample",
			"workflow",
			"workflow_metadata",
//			"cluster_average",
//			"clustered_emrt",
			"low_energy",
			"mass_spectrum",
			"peptide",
			"protein",
			"protein_info",
			"query_mass"
	};
	
	/**
	 * initialize MySQL db and connect to it 
	 * @param db
	 * @throws Exception
	 */
	public ProjectManager( MySQL db )
	{
		setManagementDB( db );
	}
	
	@Override public void setManagementDB( MySQL db ) 
	{
		if(this.db!=null) this.db.closeConnection(true);
		this.db = db;
		db.setSchema(mgtDBName);
		db.getForcedConnection();
		db.executeSQLFile(STRUCTURE_SQL_FILE_NAME, null);
		dbChanged = true;
	}
	
	@Override public List<DBProject> getNewProjects(List<DBProject> projects)
	{
		List<DBProject> res = new ArrayList<DBProject>();
		List<DBProject> existing = getProjects();

		for(DBProject p : projects)
		{
			boolean isNew = true;
			for(DBProject ep: existing) 
				if (p.data.id.equals( ep.data.id ))
			{
				isNew = false;
				break;
			}
			if(isNew) res.add(p);
		}
		return res;
	}

	@Override public List<DBProject> getProjects()
	{
		if(existingProjects==null || dbChanged)
		{
			if(existingProjects!=null)
			{
				// close all connections!
				for ( DBProject p : existingProjects )
					if (p.mysql != null) p.mysql.closeConnection( false );
			}
			
			List<DBProject> res = new ArrayList<DBProject>();
			try
			{
				res = IQDBUtils.getProjects( db, true );
				
				for(DBProject p : res)
				{
					try{
						if (!db.schemaExists( p.data.db ))
							throw new Exception( "DBProject database not found!\nProject: '" + p.data.title + "'\ndb: '" + p.data.db + "'" );
						
						String num = p.data.db.substring( p.data.db.lastIndexOf( "_" ) + 1 );
						if (num.length() < 5) p.data.titleSuffix += " [" + num + "]";
						
						// get some info from the database
						
						// ExpressionAnalysis.name as project name suffix
						p.data.titleSuffix = " [" + XJava.decURL( p.mysql.getFirstValue( "expression_analysis", "name", null ) ) + "]";

						// ExpressionAnalysis.description as project.info
						p.data.info = XJava.decURL( p.mysql.getFirstValue( "expression_analysis", "description", null ) );
					}
					catch (Exception e) 
					{
						e.printStackTrace();
						p.data.titleSuffix += "	<-- THIS IS AN INCONSISTENT ENTRY, PLEASE REMOVE IT";
					}
					finally 
					{
						// close the database sonnection
						try{ p.mysql.closeConnection( false ); } catch (Exception e) {  }
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			existingProjects = res;
			dbChanged = false;
		}
		// // close them again
		// for ( DBProject p : existingProjects )
		// if (p.mysql != null) p.mysql.closeConnection( false );
		return existingProjects;
	}

	@Override public void removeProject(DBProject p)
	{
		db.executeSQL( "DELETE FROM project WHERE `index`='" + p.data.index + "';" );
		db.dropDatabase( p.data.db );
		dbChanged = true;
	}

	@Override public void addProject( DBProject p )
	{
		try{
			if (p.data.db.length() < 1) p.data.db = getNextSchemaNameForPrefix( suggestSchemaNamePrefix( p.data.id, "" ) );
			db.executeSQL( "DELETE FROM project WHERE db='" + p.data.db + "';" );
			db.executeSQL(
				"INSERT INTO project SET " +
							"id='" + p.data.id + "'," +
							"title='" + XJava.encURL( p.data.title ) + "'," +
							"root='" + XJava.encURL( p.data.root ) + "'," +
							"db='" + p.data.db + "'," +
							"state='" + p.data.state + "';"
			);
			
			db.createDatabase( p.data.db );
			
			DBProject _p = getProject( p.data.db );
			p.data.index = _p.data.index;
			
			p.setMySQL( db.getDB( p.data.db ) );
			
		}catch(Exception e){
			System.out.println("An error occured while storing new project.");
			e.printStackTrace();
		}
		dbChanged = true;
	}
	
	@Override public MySQL getManagementDB(){return this.db;}
	
	@Override public DBProject getProject(String dbName)
	{
		try {
			ResultSet rs = db.executeSQL("SELECT * FROM project WHERE db='"+dbName+"'");
			rs.next();
			DBProject p = new DBProject();
			p.data.index = rs.getInt( "index" );
			p.data.id = rs.getString( "id" );
			p.data.title = XJava.decURL( rs.getString( "title" ) );
			p.data.root = XJava.decURL( rs.getString( "root" ) );
			p.data.db = rs.getString( "db" );
			p.data.state = rs.getString( "state" );
			p.setMySQL( db.getDB( p.data.db ) );
			return p;
		} catch (SQLException e) {
			System.out.println("project database '"+dbName+"' not found.");
		}
		return null;
	}
	
	@Override public MySQL getProjectDB( DBProject p )
	{
		if (p.data.db.length() < 1)
		{
			p.data.db = getNextSchemaNameForPrefix(
					suggestSchemaNamePrefix( p.data.id,
							( p.data.expressionAnalysisIDs.size() > 0 ) ? p.data.expressionAnalysisIDs.get( 0 ) : "" ) );
		}		
		return db.getDB( p.data.db );
	}
	
	/**
	 * unify schema names by appending 
	 * project db name prefix + SHA1 hash from project id and expression analysis id
	 */
	@Override public String suggestSchemaNamePrefix(String projectID, String expressionAnalysisID)
	{
		return projectID + "_" + (Hash.getPearson(expressionAnalysisID) + 100);
	}

	@Override public String getNextSchemaNameForPrefix(String schemaNamePrefix)
	{
		List<DBProject> allPrjs = getProjects();
		List<DBProject> selPrjs = new ArrayList<DBProject>();
		for ( DBProject p : allPrjs )
			if (p.data.db.startsWith( schemaNamePrefix )) selPrjs.add( p );
		boolean found = false;
		String res = schemaNamePrefix;
		for(int i=1; !found; i++)
		{
			found = true;
			res = schemaNamePrefix + "_" + i; 
			for(DBProject p : selPrjs)
			{
				if (p.data.db.equalsIgnoreCase( res ))
				{
					found = false;
					break;
				}
			}
		}
		return res;
	}

	@Override public void onDBExternallyChangedAction()
	{
		this.dbChanged = true;
	}

	@Override public String[] listBasicProjectTables()
	{
		return basicProjectTables;
	}
}
