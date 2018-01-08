/** ISOQuant_1.0, isoquant.kernel.plugin, 03.03.2011*/
package isoquant.kernel.plugin;

import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;

/**
 * <h3>{@link HeadlessPlugin4FS}</h3>
 * @author Joerg Kuharev
 * @version 03.03.2011 09:58:40
 */
public abstract class HeadlessPlugin4FS extends HeadlessPlugin
{
	public HeadlessPlugin4FS(iMainApp app)
	{
		super(app);
	}

	@Override public void runPluginAction() throws Exception
	{
		for(DBProject p : app.getSelectedFSProjects())
		{
			runPluginAction( p );
		}
	}
}
