/** ISOQuant_1.0, isoquant.interfaces.plugin, 14.02.2011*/
package isoquant.interfaces.plugin;

import java.util.List;

import isoquant.kernel.db.DBProject;

/**
 * <h3>{@link iProjectListChangingPlugin}</h3>
 * @author Joerg Kuharev
 * @version 14.02.2011 14:25:48
 */
public interface iProjectListChangingPlugin extends iPlugin
{
	public List<DBProject> getOriginalProjectList();
	public List<DBProject> getChangedProjectList();
}
