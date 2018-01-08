/** ISOQuant, isoquant.plugins.collections, 01.10.2012 */
package isoquant.plugins.collections;

import isoquant.app.Defaults;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.plugin.PluginCollection;
import isoquant.plugins.benchmark.mpqs.idcor.TripleDBDecoder;
import isoquant.plugins.db.ProjectDBCloner;
import isoquant.plugins.db.ProjectSchemaFinder;
import isoquant.plugins.db.WorkflowRemover;

/**
 * <h3>{@link PrivatePluginsCollection}</h3>
 * @author kuharev
 * @version 01.10.2012 10:42:50
 */
public class PrivatePluginsCollection extends PluginCollection
{
	public PrivatePluginsCollection(iMainApp app)
	{
		super(app);
	}

	@Override public String getMenuItemText()
	{
		return "debug";
	}

	@Override public String getMenuItemIconName()
	{
		return "debug";
	}

	@Override public String[] getPluginClassNames()
	{
		return (Defaults.DEBUG || Defaults.TEST)
				? new String[]
				{
						InternalReporterPluginsCollection.class.getName()
						, DeveloperPluginsCollection.class.getName()
						, PlottingPluginsCollection.class.getName()
						, ConfigPluginsCollection.class.getName()
						, ProjectDBCloner.class.getName()
						, WorkflowRemover.class.getName()
						, TripleDBDecoder.class.getName()
						, ProjectSchemaFinder.class.getName()
				}
				: new String[] {};
	}
}
