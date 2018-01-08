/** ISOQuant, isoquant.plugins.collections, 01.10.2012 */
package isoquant.plugins.collections;

import isoquant.app.Defaults;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.plugin.PluginCollection;
import isoquant.plugins.benchmark.ParameterBencher;
import isoquant.plugins.exporting.*;
import isoquant.plugins.exporting.protein_inference.ProteinInferenceExporter;
import isoquant.plugins.report.prot.csv.SimpleProteinRunQuantificationByGroupReporter;

/**
 * <h3>{@link DeveloperPluginsCollection}</h3>
 * @author kuharev
 * @version 01.10.2012 10:42:50
 */
public class DeveloperPluginsCollection extends PluginCollection
{
	public DeveloperPluginsCollection(iMainApp app)
	{
		super(app);
	}

	@Override public String getPluginName()
	{
		return "Collection of Reporting Plugins For Debugging";
	}

	@Override public String getMenuItemText()
	{
		return "developer";
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
						RTWExporter.class.getName()
						, Warp2DExporter.class.getName()
						, SimpleRTW2CSVExporter.class.getName()
						, DetailedEMRTTableExporter.class.getName()
						, SimpleEMRTTableExporter.class.getName()
						, ParameterBencher.class.getName()
						, ProteinInferenceExporter.class.getName()
						, SimpleProteinRunQuantificationByGroupReporter.class.getName()
				}
				: new String[] {};
	}
}
