package isoquant.interfaces;

import java.util.List;

import de.mz.jk.jsix.mysql.MySQL;
import isoquant.kernel.db.DBProject;

/**
 * interface for a ProjectManager
 * <h3>{@link iProjectManager}</h3>
 * @author Joerg Kuharev
 * @version 24.02.2011 13:14:28
 */
public interface iProjectManager 
{
	/**
	 * close old database connection and switch to new one 
	 * @param db current database
	 */
	public abstract void setManagementDB(MySQL db);

	/**
	 * extract only new projects from given list<br>
	 * by removing from the list projects existing in database<br>
	 * @param projects list of projects
	 * @return list of new projects
	 */
	public abstract List<DBProject> getNewProjects(List<DBProject> projects);

	/**
	 * lists all existing projects
	 * @return list of projects
	 */
	public abstract List<DBProject> getProjects();

	/**
	 * removes a project from database by<br>
	 * - deleting its entry from project list and<br>
	 * - droping project's database.
	 * @param p project to remove
	 */
	public abstract void removeProject(DBProject p);

	/**
	 * adds new project to project list 
	 * and creates new database by saving its reference in p.mysql
	 * @param p the project
	 */
	public abstract void addProject(DBProject p);

	/**
	 * retrieve current management database 
	 * @return the management database
	 */
	public abstract MySQL getManagementDB();
		
	/**
	 * get project by its database (schema) name
	 * @param dbName the full schema name of project db
	 * @return project object or null if no such project exists
	 */
	public DBProject getProject(String dbName);
	
	/**
	 * unify schema names creation by suggesting a prefix from project's and expression analysis' ids
	 * @param projectID
	 * @param expressionAnalysisID
	 * @return 
	 */
	public String suggestSchemaNamePrefix(String projectID, String expressionAnalysisID);

	/**
	 * append next free enumerator to the schema name prefix 
	 * @param schemaNamePrefix
	 * @return free schema name built by appending anumerator to prefix
	 */
	public abstract String getNextSchemaNameForPrefix(String schemaNamePrefix);

	/**
	 * get the MySQL database object associated with given project
	 * @param p the project
	 * @return
	 */
	public MySQL getProjectDB(DBProject p);

	/**
	 * tell to mangager that db has been externally changed
	 */
	void onDBExternallyChangedAction();
	
	/** 
	 * @return list of basic project tables
	 */
	public String[] listBasicProjectTables();
}
