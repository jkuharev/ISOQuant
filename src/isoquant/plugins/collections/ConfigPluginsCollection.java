/** ISOQuant, isoquant.plugins.collections, 10.07.2013*/
package isoquant.plugins.collections;

import isoquant.app.Defaults;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.plugin.PluginCollection;
import isoquant.plugins.configuration.AttachConfigParams;
import isoquant.plugins.configuration.LoadConfigParamsFromAttachement;
import isoquant.plugins.configuration.RestoreConfigParamsFromHistory;

/**
 * <h3>{@link ConfigPluginsCollection}</h3>
 * @author kuharev
 * @version 10.07.2013 14:56:10
 */
public class ConfigPluginsCollection extends PluginCollection
{
	/**
	 * @param app
	 */
	public ConfigPluginsCollection(iMainApp app)
	{
		super(app);
	}

	@Override public String getMenuItemText()
	{
		return "settings";
	}

	@Override public String getMenuItemIconName()
	{
		return "options";
	}

	@Override public String[] getPluginClassNames()
	{
		return (Defaults.DEBUG || Defaults.TEST)
				? new String[]
				{
						RestoreConfigParamsFromHistory.class.getName()
						, AttachConfigParams.class.getName()
						, LoadConfigParamsFromAttachement.class.getName()
				}
				: new String[] {};
	}
}
