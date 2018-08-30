/** ISOQuant, isoquant.app, 15.07.2011 */
package isoquant.app;

import java.io.File;

import javax.swing.JOptionPane;

import de.mz.jk.jsix.utilities.ResourceLoader;
import de.mz.jk.jsix.utilities.Settings;
import isoquant.kernel.plugin.management.PluginManager;
import isoquant.plugins.batch.ImportAndProcessBatcher;
import isoquant.plugins.batch.ReprocessBatcher;
import isoquant.plugins.collections.PrivatePluginsCollection;
import isoquant.plugins.collections.PublicReporterPluginsCollection;
import isoquant.plugins.common.ProjectFinder;
import isoquant.plugins.common.ProjectSelectionSynchronizer;
import isoquant.plugins.configuration.ConfigurationEditor;
import isoquant.plugins.db.*;
import isoquant.plugins.exporting.ProjectDataExporter;
import isoquant.plugins.help.HelpWindowPresenter;
import isoquant.plugins.importing.ProjectFromFileImporter;
import isoquant.plugins.plgs.PLGSProjectExplorer;
import isoquant.plugins.plgs.PLGSProjectInfoViewer;
import isoquant.plugins.plgs.importing.ProjectImporterByExpressionAnalysis;
import isoquant.plugins.plgs.importing.ProjectImporterByExpressionAnalysisAndBlusterResultsImporter;
import isoquant.plugins.plgs.importing.ProjectImporterByUserDesignMulti;
import isoquant.plugins.plgs.plain.PlainPLGSDataImporter;
import isoquant.plugins.processing.annotation.AnnotationStatisticsCreator;
import isoquant.plugins.processing.annotation.EMRTClusterAnnotator;
import isoquant.plugins.processing.annotation.PeptideIdentificationFilter;
import isoquant.plugins.processing.expression.EMRTClusteringPlugin;
import isoquant.plugins.processing.expression.ExpressionAnalysisPlugin;
import isoquant.plugins.processing.expression.RetentionTimeAlignmentPlugin;
import isoquant.plugins.processing.expression.clustering.preclustering.DynamicPreClusterer;
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
 * static configuration defaults for application
 * <h3>{@link Defaults}</h3>
 * @author Joerg Kuharev
 * @version 15.07.2011 15:23:12
 */
public class Defaults
{
	private static boolean suppressVersionString = false;
	public static boolean DEBUG = false;
	public static boolean TEST = false;
	public final static String VERSION_MILESTONE = "1.8";
	public final static String VERSION_STATE = "beta";
	public final static int VERSION_BUILD_YEAR = 2018;
	public final static int VERSION_BUILD_MONTH = 8;
	public final static int VERSION_BUILD_DAY = 30;
	public final static String VERSION_BUILD_DATE =
			VERSION_BUILD_YEAR
					+ "-" + (VERSION_BUILD_MONTH < 10 ? "0" : "") + VERSION_BUILD_MONTH
					+ "-" + (VERSION_BUILD_DAY < 10 ? "0" : "") + VERSION_BUILD_DAY;
	public final static int availableProcessors = Runtime.getRuntime().availableProcessors();

	public static String version()
	{
		return suppressVersionString
				? ""
				: VERSION_MILESTONE + " " + (DEBUG || TEST ? "internal" : "public") + " " + VERSION_STATE + " (" + VERSION_BUILD_DATE + ")";
	}
	/** make plugins available to ISOQuant application */
	static
	{
		PluginManager.defaultPlugins = new String[]
		{
				PLGSProjectExplorer.class.getName()
				, PlainPLGSDataImporter.class.getName()
				, ProjectDBExplorer.class.getName()
				, ProjectFromFileImporter.class.getName()
				, ProjectFinder.class.getName()
				, ConfigurationEditor.class.getName()
				, HelpWindowPresenter.class.getName()
				, ProjectSelectionSynchronizer.class.getName()
				// ProjectDetailsViewer.class.getName()
				, PLGSProjectInfoViewer.class.getName()
				, ProjectDBInfoViewer.class.getName()
				, ProjectRenamer.class.getName()
				// TODO remove next line
				, ProjectImporterByUserDesignMulti.class.getName()
				, ProjectImporterByExpressionAnalysisAndBlusterResultsImporter.class.getName()
				, ProjectImporterByExpressionAnalysis.class.getName()
				, ImportAndProcessBatcher.class.getName()
				, ReprocessBatcher.class.getName()
				, PublicReporterPluginsCollection.class.getName()
				, ProjectDataExporter.class.getName()
				, ProjectRemover.class.getName()
				// single processing steps and DEBUG
				// TODO remove following lines
				, ProjectCleaner.class.getName()
				, DataPreparator.class.getName()
				, EMRTTableLinker.class.getName()
				, EMRTTableFromMassSpectrumCreator.class.getName()
				, ProteinStatisticsMaker.class.getName()
				, LCFractionsTimeShifter.class.getName()
				, ExpressionAnalysisPlugin.class.getName()
				, RetentionTimeAlignmentPlugin.class.getName()
				, DynamicPreClusterer.class.getName()
				, EMRTClusteringPlugin.class.getName()
				, LCFractionsTimeUnshifter.class.getName()
				, PeptideIdentificationFilter.class.getName()
				, EMRTClusterAnnotator.class.getName()
				, AnnotationStatisticsCreator.class.getName()
				, IntensityNormalizer.class.getName()
				, TopXQuantifier.class.getName()
				, ReportPreparator.class.getName()
				, PrivatePluginsCollection.class.getName()
		};
	}
	public static Settings config = null;
	private static File configFile = new File("isoquant.ini");
	static
	{
		try
		{
			if (!configFile.exists())
			{
				System.out.print("configuration file not found, creating new one ... ");
				configFile.createNewFile();
				System.out.println("[done]");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (!configFile.canRead())
		{
			showErrorMessage("ISOQuant is unable to read config file\n" +
					"'" + configFile.getAbsolutePath() + "'.\n\n" +
					"Please correct the file system permissions for this file\n" +
					"or use privileged user to execute the application.\n\n" +
					"ISOQuant will now exit.");
			System.err.println(
					"Please correct file system permissions for file " + configFile + "\n" +
							"or use privileged user to execute the application.");
			System.exit(0);
		}
		if (!configFile.canWrite())
		{
			showErrorMessage("ISOQuant is unable to write configuration changes to file\n" +
					"'" + configFile.getAbsolutePath() + "'.\n\n" +
					"Please correct the file system permissions for this file\n" +
					"or use privileged user to execute the application.\n\n" +
					"ISOQuant will use predefined configuration " +
					"or default configuration values!");
		}
		config = new Settings(configFile.getAbsolutePath(), "ISOQuant configuration");
		DEBUG = config.getBooleanValue("setup.debug", false, true);
		TEST = config.getBooleanValue("setup.test", false, true);
		double iconScaleFactor = config.getDoubleValue("setup.ui.iconScaleFactor", 1.0, false);
		if (iconScaleFactor < 0.2 || iconScaleFactor > 3)
		{
			iconScaleFactor = 1.0;
			config.setValue("setup.ui.iconScaleFactor", 1.0);
		}
		ResourceLoader.setIconDefaults("isoquant/resources/icons/", ".png", iconScaleFactor);
	}

	public static void showErrorMessage(String msg)
	{
		System.err.println(msg);
		JOptionPane.showMessageDialog(null, msg, "error", JOptionPane.ERROR_MESSAGE);
	}
}
