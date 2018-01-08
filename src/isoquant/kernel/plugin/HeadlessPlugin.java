/** ISOQuant_1.0, isoquant.kernel.plugin, 03.03.2011*/
package isoquant.kernel.plugin;

import isoquant.interfaces.iMainApp;
import isoquant.interfaces.plugin.iProjectProcessingPlugin;

import java.awt.Component;
import java.util.List;

/**
 * <h3>{@link HeadlessPlugin}</h3>
 * @author Joerg Kuharev
 * @version 03.03.2011 09:38:14
 */
public abstract class HeadlessPlugin extends Plugin implements iProjectProcessingPlugin
{
	public HeadlessPlugin(iMainApp app)
	{
		super(app);
	}
	
	@Override public List<Component> getDBMenuComponents(){return null;}
	@Override public List<Component> getFSMenuComponents(){return null;}
	@Override public List<Component> getToolBarComponents(){return null;}
}
