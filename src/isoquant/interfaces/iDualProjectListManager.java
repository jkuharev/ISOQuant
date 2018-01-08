/** ISOQuant_1.0, isoquant.interfaces, 10.02.2011*/
package isoquant.interfaces;

import java.util.List;

import isoquant.kernel.db.DBProject;

/**
 * <h3>{@link iDualProjectListManager}</h3>
 * Set of operations to handle the dual project list paradigma<br>
 * that describes a container with a list of projects from file system
 * and a list of projects from database
 * @author Joerg Kuharev
 * @version 10.02.2011 16:17:13
 */
public interface iDualProjectListManager
{
	public void setFSProjects(List<DBProject> projects);
	public void setDBProjects(List<DBProject> projects);
	
	public List<DBProject> getFSProjects();
	public List<DBProject> getDBProjects();
	
	public List<DBProject> getSelectedFSProjects();
	public List<DBProject> getSelectedDBProjects();
	
	public void selectFSProjects(List<DBProject> projects);
	public void selectDBProjects(List<DBProject> projects);
	
	public void updateFSProjects();
	public void updateDBProjects();
}
