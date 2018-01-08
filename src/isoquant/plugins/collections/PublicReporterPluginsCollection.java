/** ISOQuant, isoquant.plugins.report, 29.08.2012 */
package isoquant.plugins.collections;

import isoquant.interfaces.iMainApp;
import isoquant.kernel.plugin.PluginCollection;
import isoquant.plugins.exporting.EMRTTableExporter;
import isoquant.plugins.report.MzIdentML.MzIdentMLReporter;
import isoquant.plugins.report.pept.PeptideQuantificationReporter;
import isoquant.plugins.report.project.ProjectSummaryReporter;
import isoquant.plugins.report.prot.csv.SimpleProteinRunQuantificationReporter;
import isoquant.plugins.report.prot.csv.SimpleProteinSampleAverageQuantificationReporter;
import isoquant.plugins.report.prot.excel.ExcelReporter;
import isoquant.plugins.report.prot.html.HTMLMultiPageReporter;
import isoquant.plugins.report.prot.html.HTMLOnePageReporter;

/**
 * <h3>{@link PublicReporterPluginsCollection}</h3>
 * @author kuharev
 * @version 29.08.2012 13:33:25
 */
public class PublicReporterPluginsCollection extends PluginCollection
{
	public PublicReporterPluginsCollection(iMainApp app)
	{
		super(app);
	}

	@Override public String getPluginName()
	{
		return "Collection of Reporting Plugins";
	}

	@Override public String getMenuItemText()
	{
		return "create report";
	}

	@Override public String getMenuItemIconName()
	{
		return "printer";
	}

	@Override public String[] getPluginClassNames()
	{
		return new String[] {
				ExcelReporter.class.getName()
				, PeptideQuantificationReporter.class.getName()
				, ProjectSummaryReporter.class.getName()
				, HTMLOnePageReporter.class.getName()
				, HTMLMultiPageReporter.class.getName()
				// , PeptideIdentificationReporter.class.getName()
				, SimpleProteinRunQuantificationReporter.class.getName()
				, SimpleProteinSampleAverageQuantificationReporter.class.getName()
				, EMRTTableExporter.class.getName()
				, MzIdentMLReporter.class.getName()
		};
	}
}
