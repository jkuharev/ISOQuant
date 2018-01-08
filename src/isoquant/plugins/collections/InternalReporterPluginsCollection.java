/** ISOQuant, isoquant.plugins.collections, 01.10.2012 */
package isoquant.plugins.collections;

import isoquant.app.Defaults;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.plugin.PluginCollection;
import isoquant.plugins.benchmark.fdr.FeatureFDRCalculator;
import isoquant.plugins.benchmark.lc.PeakCapacityReport;
import isoquant.plugins.benchmark.mpqs.Reporter_IQ_ProteinQuantification_LFQBench_CSV;
import isoquant.plugins.benchmark.mpqs.Reporter_PLGS_ProteinQuantification_LFQbench_CSV;
import isoquant.plugins.benchmark.warping.TimeWarpingExporter;
import isoquant.plugins.exporting.IDReporter.IDReporter;
import isoquant.plugins.report.plgs.Reporter_PLGS_RunBasedComprehensiveIdentificationAndQuantification;

/**
 * <h3>{@link InternalReporterPluginsCollection}</h3>
 * @author kuharev
 * @version 01.10.2012 10:42:50
 */
public class InternalReporterPluginsCollection extends PluginCollection
{
	public InternalReporterPluginsCollection(iMainApp app)
	{
		super(app);
	}

	@Override public String getPluginName()
	{
		return "Collection of Reporting Plugins";
	}

	@Override public String getMenuItemText()
	{
		return "internal";
	}

	@Override public String getMenuItemIconName()
	{
		return "immu";
	}

	@Override public String[] getPluginClassNames()
	{
		return (Defaults.DEBUG || Defaults.TEST)
				? new String[]
				{
						Reporter_PLGS_RunBasedComprehensiveIdentificationAndQuantification.class.getName()
						, Reporter_IQ_ProteinQuantification_LFQBench_CSV.class.getName()
						, Reporter_PLGS_ProteinQuantification_LFQbench_CSV.class.getName()
						, PeakCapacityReport.class.getName()
						, IDReporter.class.getName()
						, FeatureFDRCalculator.class.getName()
						, TimeWarpingExporter.class.getName()
				}
				: new String[] {};
	}
}
