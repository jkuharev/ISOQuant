package isoquant.interfaces.plugin;

import isoquant.kernel.plugin.Plugin;

import java.util.List;

public interface iPluginManager 
{
	/**
	 * @return all available plugins
	 */
	public abstract List<Plugin> getPlugins();
	
	/**
	 * @param id
	 * @return plugins identified by id
	 */
	public abstract List<Plugin> getPlugins(String id);

	/**
	 * add a plugin by its class name
	 * @param className
	 * @return initialized plugin object
	 */
	public Plugin addPlugin(String className);

	/**
	 * @param plugin
	 */
	public void addPlugin(Plugin plugin);
}
