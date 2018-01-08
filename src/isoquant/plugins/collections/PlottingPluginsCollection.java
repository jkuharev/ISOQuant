/** ISOQuant, isoquant.plugins.collections, 01.10.2012*/
package isoquant.plugins.collections;

import isoquant.app.Defaults;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.plugin.PluginCollection;
import isoquant.plugins.benchmark.InSampleProteinQualifier;
import isoquant.plugins.benchmark.clustering.ClusterChecker;
import isoquant.plugins.benchmark.rsd.ProteinQuantificationSampleWiseRSDDensityPlotter;
import isoquant.plugins.plot.ProteinSampleQuantLogRatioPlotter;
import isoquant.plugins.plot.RTWPlotter;
import isoquant.plugins.processing.expression.clustering.ClusterSizeInspector;

/**
 * <h3>{@link PlottingPluginsCollection}</h3>
 * @author kuharev
 * @version 01.10.2012 10:42:50
 */
public class PlottingPluginsCollection extends PluginCollection
{
	public PlottingPluginsCollection(iMainApp app){ super(app); }

	@Override public String getPluginName(){ return "Collection of Plotting Plugins"; }
	@Override public String getMenuItemText(){ return "plot"; }
	@Override public String getMenuItemIconName(){ return "chart"; }
	@Override public String[] getPluginClassNames()
	{
		return 
			( Defaults.DEBUG || Defaults.TEST ) 
				? new String[]
				{
					 RTWPlotter.class.getName()
					,InSampleProteinQualifier.class.getName() // AUQC between replicates
					,ClusterSizeInspector.class.getName() // cluster size distribution
					,ClusterChecker.class.getName() // clustering statistics like clusters per peptide, peptides per cluster, ...
					,ProteinQuantificationSampleWiseRSDDensityPlotter.class.getName()
					,ProteinSampleQuantLogRatioPlotter.class.getName()
				}
				: new String[]{}
		;
	}
}
