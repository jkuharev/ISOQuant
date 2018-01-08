package isoquant.kernel.adapter;

import isoquant.interfaces.iMainApp;
import isoquant.kernel.plugin.Plugin;

import java.awt.Component;
import java.util.List;

import javax.swing.Icon;

/**
 * <h3>{@link PluginAdapter}</h3>
 * empty iPlugin implementation,
 * all functions do nothing and return null
 * @author Joerg Kuharev
 * @version 11.02.2011 09:20:10
 */
public abstract class PluginAdapter extends Plugin
{
	public PluginAdapter(iMainApp app){super(app);}
	@Override public List<Component> getDBMenuComponents(){return null;}
	@Override public List<Component> getFSMenuComponents(){return null;}
	@Override public List<Component> getToolBarComponents(){return null;}
	@Override public Icon getPluginIcon(){return null;}
	@Override public String getPluginName(){return null;}
	@Override public void runPluginAction() throws Exception{}
}
