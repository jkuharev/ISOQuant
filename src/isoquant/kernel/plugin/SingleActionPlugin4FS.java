package isoquant.kernel.plugin;

import isoquant.interfaces.iMainApp;
import isoquant.interfaces.plugin.iProjectProcessingPlugin;
import isoquant.kernel.db.DBProject;

import java.awt.Component;
import java.util.Collections;
import java.util.List;

/**
 * <h3>SingleActionPlugin4FS</h3>
 * abstract plugin implementing common functionality of plugins on
 * the file system side of ISOQuant
 * @author Joerg Kuharev
 * @version 19.01.2011 16:58:35
 *
 */
public abstract class SingleActionPlugin4FS extends SingleActionPlugin implements iProjectProcessingPlugin
{
	public SingleActionPlugin4FS(iMainApp app) 
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

	@Override public List<Component> getFSMenuComponents()
	{
		return Collections.singletonList((Component)menu);
	}
	
	@Override public List<Component> getDBMenuComponents(){return null;}
	@Override public List<Component> getToolBarComponents(){return null;}
}
