/** ISOQuant, isoquant.plugins.plgs.importing, 14.04.2011*/
package isoquant.plugins.plgs.importing;

import isoquant.interfaces.iMainApp;
import isoquant.plugins.plgs.importing.expression.ExpressionSelectingImporterPlugin;

/**
 * <h3>{@link ProjectImporterByExpressionAnalysisAndBlusterResultsImporter}</h3>
 * @author Joerg Kuharev
 * @version 14.04.2011 15:18:48
 */
public class ProjectImporterByExpressionAnalysisAndBlusterResultsImporter extends ExpressionSelectingImporterPlugin
{
	public ProjectImporterByExpressionAnalysisAndBlusterResultsImporter(iMainApp app)
	{
		super(app);
	}

	@Override protected boolean shouldImportExpressionAnalysisResults()
	{
		return true;
	}

	@Override protected boolean shouldReadExpressionAnalysisFromFileSystem()
	{
		return true;
	}

	@Override public String getMenuItemText()
	{
		return "import full Expression Analysis data";
	}

	@Override public String getPluginName()
	{
		return "Expression Analysis Data and Results Importer";
	}
}
