package isoquant.plugins.batch;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;

import de.mz.jk.jsix.utilities.ResourceLoader;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.plugin.queue.PluginQueue;
import isoquant.plugins.plgs.importing.ProjectImporterByExpressionAnalysis;
import isoquant.plugins.plgs.importing.ProjectImporterByExpressionAnalysisAndBlusterResultsImporter;
import isoquant.plugins.plgs.importing.ProjectImporterByUserDesignMulti;
import isoquant.plugins.processing.annotation.EMRTClusterAnnotator;
import isoquant.plugins.processing.expression.EMRTClusteringPlugin;
import isoquant.plugins.processing.expression.RetentionTimeAlignmentPlugin;
import isoquant.plugins.processing.fractions.LCFractionsTimeShifter;
import isoquant.plugins.processing.fractions.LCFractionsTimeUnshifter;
import isoquant.plugins.processing.linkage.EMRTTableLinker;
import isoquant.plugins.processing.normalization.IntensityNormalizer;
import isoquant.plugins.processing.preprocessing.DataPreparator;
import isoquant.plugins.processing.quantification.TopXQuantifier;
import isoquant.plugins.processing.restructuring.EMRTTableFromMassSpectrumCreator;
import isoquant.plugins.processing.statistics.ProteinStatisticsMaker;
import isoquant.plugins.report.prep.ReportPreparator;

/**
 * <h3>{@link ImportAndProcessBatcher}</h3>
 * @author Joerg Kuharev
 * @version 02.02.2011 09:58:33
 */
public class ImportAndProcessBatcher extends PluginBatcher
{
	public ImportAndProcessBatcher(iMainApp app)
	{
		super(app);
	}

	@Override protected List<PluginQueue> getQueues()
	{
		List<PluginQueue> res = new ArrayList<PluginQueue>();
		res.add(
				new PluginQueue(
						"design project and run ISOQuant analysis",
						ResourceLoader.getIcon("queue_manual"),
						new String[] {
								ProjectImporterByUserDesignMulti.class.getName(),
								DataPreparator.class.getName(),
								ProteinStatisticsMaker.class.getName(),
								LCFractionsTimeShifter.class.getName(),
								EMRTTableFromMassSpectrumCreator.class.getName(),
								RetentionTimeAlignmentPlugin.class.getName(),
								EMRTClusteringPlugin.class.getName(),
								LCFractionsTimeUnshifter.class.getName(),
								EMRTClusterAnnotator.class.getName(),
								IntensityNormalizer.class.getName(),
								TopXQuantifier.class.getName(),
								ReportPreparator.class.getName()
						}
				)
				);
// res.add(
// new PluginQueue(
// "design project and run metabolite processing ...",
// ResourceLoader.getIcon( "queue_small" ),
// new String[] {
// ProjectImporterByUserDesignMulti.class.getName(),
// DataPreparator.class.getName(),
// ProjectCleaner.class.getName(),
// LCFractionsTimeShifter.class.getName(),
// EMRTTableFromMassSpectrumCreator.class.getName(),
// RetentionTimeAlignmentPlugin.class.getName(),
// EMRTClusteringPlugin.class.getName(),
// LCFractionsTimeUnshifter.class.getName(),
// IntensityNormalizer.class.getName()
// }
// )
// );
		res.add(
				new PluginQueue(
						"use PLGS Expression Analysis",
						ResourceLoader.getIcon("queue_plgs"),
						new String[] {
								ProjectImporterByExpressionAnalysisAndBlusterResultsImporter.class.getName(),
								DataPreparator.class.getName(),
								EMRTTableLinker.class.getName(),
								ProteinStatisticsMaker.class.getName(),
								EMRTClusterAnnotator.class.getName(),
								IntensityNormalizer.class.getName(),
								TopXQuantifier.class.getName(),
								ReportPreparator.class.getName()
						}
				)
				);
		res.add(
				new PluginQueue(
						"use PLGS-EA but recluster EMRTs",
						ResourceLoader.getIcon("queue_plgs"),
						new String[] {
								ProjectImporterByExpressionAnalysisAndBlusterResultsImporter.class.getName(),
								DataPreparator.class.getName(),
								EMRTTableLinker.class.getName(),
								ProteinStatisticsMaker.class.getName(),
								EMRTClusteringPlugin.class.getName(),
								EMRTClusterAnnotator.class.getName(),
								IntensityNormalizer.class.getName(),
								TopXQuantifier.class.getName(),
								ReportPreparator.class.getName()
						}
				)
				);
		res.add(
				new PluginQueue(
						"use PLGS-EA but realign RTs & recluster EMRTs",
						ResourceLoader.getIcon("queue_plgs"),
						new String[] {
								ProjectImporterByExpressionAnalysis.class.getName(),
								DataPreparator.class.getName(),
								ProteinStatisticsMaker.class.getName(),
								EMRTTableFromMassSpectrumCreator.class.getName(),
								RetentionTimeAlignmentPlugin.class.getName(),
								EMRTClusteringPlugin.class.getName(),
								EMRTClusterAnnotator.class.getName(),
								IntensityNormalizer.class.getName(),
								TopXQuantifier.class.getName(),
								ReportPreparator.class.getName()
						}
				)
				);
		return res;
	}

	@Override public List<Component> getDBMenuComponents()
	{
		return null; /* Collections.singletonList((Component)menu); */
	}

	@Override public List<Component> getFSMenuComponents()
	{
		return Collections.singletonList((Component) menu);
	}

	@Override protected String getMenuText()
	{
		return "import and process";
	}

	@Override public Icon getPluginIcon()
	{
		return ResourceLoader.getIcon("run");
	}

	@Override public String getPluginName()
	{
		return "Import and Process Batcher";
	}

	@Override public void runPluginAction() throws Exception
	{
		runBatch(currentQueue, app.getSelectedFSProjects());
	}
}
