/** ISOQuant_1.0, isoquant.kernel.plugin, 14.02.2011*/
package isoquant.interfaces.plugin;

import isoquant.kernel.db.DBProject;

/**
 * <h3>{@link ProjectProcessingPlugin}</h3>
 * this interface enables determination if 
 * a plugin is able to process single projects 
 * @author Joerg Kuharev
 * @version 14.02.2011 11:13:35
 */
public interface iProjectProcessingPlugin extends iPlugin
{
	/**
	 * run this plugin's action for a single project
	 * @param p the project to be initialized
	 */
	public void runPluginAction( DBProject p ) throws Exception;	
}
