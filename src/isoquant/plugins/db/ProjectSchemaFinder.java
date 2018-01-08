/** ISOQuant, isoquant.plugins.db, Dec 17, 2014*/
package isoquant.plugins.db;

import java.sql.ResultSet;
import java.util.List;

import de.mz.jk.jsix.mysql.MySQL;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

/**
 * <h3>{@link ProjectSchemaFinder}</h3>
 * @author kuharev
 * @version Dec 17, 2014 3:16:41 PM
 */
public class ProjectSchemaFinder extends SingleActionPlugin4DB
{
	/**
	 * @param app
	 */
	public ProjectSchemaFinder(iMainApp app)
	{
		super( app );
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		List<String> exists = app.getManagementDB().getStringValues( "SELECT lower(db) FROM project" );
		List<String> schemaList = app.getManagementDB().listDatabases();
		for ( String schema : schemaList )
		{
			MySQL _db = app.getManagementDB().getDB( schema );
			System.out.print("schema " + schema );
			if (!schema.equals( app.getManagementDB().getSchema() ) && _db.tableExists( "project" ))
			{
				if (exists.contains( schema.toLowerCase() ))
				{
					System.out.println(" is already in project list!");
				}
				else
				{
					System.out.println( " is new " );
					System.out.println( " is new, importing ..." );
					ResultSet rs = _db.executeSQL( "SELECT `id`, `title`, `root`, `db` FROM `project`" );
					rs.next();
					DBProject _p = new DBProject();
					_p.data.id = rs.getString( "id" );
					_p.data.title = rs.getString( "title" );
					_p.data.root = rs.getString( "root" );
					_p.data.db = schema;
					_db.executeSQL(
							"INSERT INTO mass_projects.project SET "
									+ "`id`='" + _p.data.id + "',"
									+ "`title`='" + _p.data.title + "',"
									+ "`root`='" + _p.data.root + "',"
									+ "`db`='" + _p.data.db + "'" );
					_db.closeConnection();
				}
			}
			else
			{
				System.out.println(" is not an ISOQuant project!");
			}
		}
	}

	@Override public String getMenuItemText()
	{
		return "Find existing project schemas in MySQL";
	}

	@Override public String getMenuItemIconName()
	{
		return "restructure_db";
	}
}
