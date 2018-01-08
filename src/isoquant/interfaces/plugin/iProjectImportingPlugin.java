/** ISOQuant_1.0, isoquant.interfaces.plugin, 14.02.2011*/
package isoquant.interfaces.plugin;

import java.util.List;

import isoquant.kernel.db.DBProject;

/**
 * <h3>{@link iProjectImportingPlugin}</h3>
 * interface for plugins which import a list of projects 
 * and know what plugins were imported
 * @author Joerg Kuharev
 * @version 14.02.2011 15:28:36
 */
public interface iProjectImportingPlugin extends iPlugin
{
	/** set projects for beeing imported */
	public void importProjects( List<DBProject> projects );
	
	/** returned plugins are already imported */
	public List<DBProject> getImportedProjects();
}
