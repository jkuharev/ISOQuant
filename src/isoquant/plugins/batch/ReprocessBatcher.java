package isoquant.plugins.batch;

import java.awt.Component;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;

import de.mz.jk.jsix.utilities.ResourceLoader;
import isoquant.app.Defaults;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.plugin.queue.PluginQueue;
import isoquant.plugins.db.ProjectCleaner;
import isoquant.plugins.processing.annotation.EMRTClusterAnnotator;
import isoquant.plugins.processing.expression.EMRTClusteringPlugin;
import isoquant.plugins.processing.expression.RetentionTimeAlignmentPlugin;
import isoquant.plugins.processing.fractions.LCFractionsTimeShifter;
import isoquant.plugins.processing.fractions.LCFractionsTimeUnshifter;
import isoquant.plugins.processing.normalization.IntensityNormalizer;
import isoquant.plugins.processing.preprocessing.DataPreparator;
import isoquant.plugins.processing.quantification.TopXQuantifier;
import isoquant.plugins.processing.restructuring.EMRTTableFromMassSpectrumCreator;
import isoquant.plugins.processing.statistics.ProteinStatisticsMaker;
import isoquant.plugins.report.prep.ReportPreparator;

/**
 * <h3>{@link ReprocessBatcher}</h3>
 * @author Joerg Kuharev
 * @version 02.02.2011 09:58:33
 */
public class ReprocessBatcher extends PluginBatcher
{
	public ReprocessBatcher(iMainApp app)
	{
		super(app);
	}

	@Override protected List<PluginQueue> getQueues()
	{
		List<PluginQueue> res = new ArrayList<PluginQueue>();
		res.add(
				new PluginQueue(
						"run complete ISOQuant analysis",
						ResourceLoader.getIcon("queue_small"),
						new String[] {
								DataPreparator.class.getName(),
								ProteinStatisticsMaker.class.getName(),
								ProjectCleaner.class.getName(),
								LCFractionsTimeShifter.class.getName(),
								EMRTTableFromMassSpectrumCreator.class.getName(),
								RetentionTimeAlignmentPlugin.class.getName(),
								EMRTClusteringPlugin.class.getName(),
								LCFractionsTimeUnshifter.class.getName(),
								IntensityNormalizer.class.getName(),
								EMRTClusterAnnotator.class.getName(),
								TopXQuantifier.class.getName(),
								ReportPreparator.class.getName()
						}
				)
				);
		res.add(
				new PluginQueue(
						"align retention time ...",
						ResourceLoader.getIcon("queue_small"),
						new String[] {
								ProjectCleaner.class.getName(),
								LCFractionsTimeShifter.class.getName(),
								EMRTTableFromMassSpectrumCreator.class.getName(),
								RetentionTimeAlignmentPlugin.class.getName(),
								EMRTClusteringPlugin.class.getName(),
								LCFractionsTimeUnshifter.class.getName(),
								IntensityNormalizer.class.getName(),
								EMRTClusterAnnotator.class.getName(),
								TopXQuantifier.class.getName(),
								ReportPreparator.class.getName()
						}
				)
				);
		res.add(
				new PluginQueue(
						"cluster peaks ...",
						ResourceLoader.getIcon("queue_small"),
						new String[] {
								EMRTClusteringPlugin.class.getName(),
								IntensityNormalizer.class.getName(),
								EMRTClusterAnnotator.class.getName(),
								TopXQuantifier.class.getName(),
								ReportPreparator.class.getName()
						}
				)
				);
		res.add(
				new PluginQueue(
						"annotate + normalize ...",
						ResourceLoader.getIcon("queue_small"),
						new String[] {
								IntensityNormalizer.class.getName(),
								EMRTClusterAnnotator.class.getName(),
								TopXQuantifier.class.getName(),
								ReportPreparator.class.getName()
						}
				)
				);
		res.add(
				new PluginQueue(
						"annotate ...",
						ResourceLoader.getIcon("queue_small"),
						new String[] {
								EMRTClusterAnnotator.class.getName(),
								TopXQuantifier.class.getName(),
								ReportPreparator.class.getName()
						}
				)
				);
		res.add(
				new PluginQueue(
						"normalize ...",
						ResourceLoader.getIcon("queue_small"),
						new String[] {
								IntensityNormalizer.class.getName(),
								TopXQuantifier.class.getName(),
								ReportPreparator.class.getName()
						}
				)
				);
		res.add(
				new PluginQueue(
						"quantify ...",
						ResourceLoader.getIcon("queue_small"),
						new String[] {
								TopXQuantifier.class.getName(),
								ReportPreparator.class.getName()
						}
				)
				);
// res.add(
// new PluginQueue(
// "run metabolite processing ...",
// ResourceLoader.getIcon( "queue_small" ),
// new String[] {
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
		if (Defaults.DEBUG)
			res.add(
					new ManualSelectionQueue(
							"run manually selected plugins",
							ResourceLoader.getIcon("manual_processing"),
							new String[] {
									DataPreparator.class.getName(),
									ProteinStatisticsMaker.class.getName(),
									ProjectCleaner.class.getName(),
									LCFractionsTimeShifter.class.getName(),
									EMRTTableFromMassSpectrumCreator.class.getName(),
									RetentionTimeAlignmentPlugin.class.getName(),
									EMRTClusteringPlugin.class.getName(),
									LCFractionsTimeUnshifter.class.getName(),
									IntensityNormalizer.class.getName(),
									EMRTClusterAnnotator.class.getName(),
									TopXQuantifier.class.getName(),
									ReportPreparator.class.getName()
							},
							(Frame) app.getGUI()
					)
					);
		return res;
	}

	@Override public List<Component> getFSMenuComponents()
	{
		return null; /* Collections.singletonList((Component)menu); */
	}

	@Override public List<Component> getDBMenuComponents()
	{
		return Collections.singletonList((Component) menu);
	}

	@Override protected String getMenuText()
	{
		return "reprocess";
	}

	@Override public Icon getPluginIcon()
	{
		return ResourceLoader.getIcon("chip");
	}

	@Override public String getPluginName()
	{
		return "Reprocessing Batcher";
	}

	@Override public void runPluginAction() throws Exception
	{
		runBatch(currentQueue, app.getSelectedDBProjects());
	}
}
