/** ISOQuant, isoquant.plugins.plgs.importing, 14.04.2011*/
package isoquant.plugins.plgs.importing;

import isoquant.interfaces.iMainApp;
import isoquant.plugins.plgs.importing.expression.ExpressionSelectingImporterPlugin;

/**
 * <h3>{@link ProjectImporterByExpressionAnalysis}</h3>
 * @author Joerg Kuharev
 * @version 14.04.2011 15:05:08
 */
public class ProjectImporterByExpressionAnalysis extends ExpressionSelectingImporterPlugin
{
	public ProjectImporterByExpressionAnalysis(iMainApp app)
	{
		super(app);
	}

	@Override protected boolean shouldImportExpressionAnalysisResults()
	{
		return false;
	}

	@Override protected boolean shouldReadExpressionAnalysisFromFileSystem()
	{
		return true;
	}
	
	@Override public String getMenuItemText(){return "import Workflow data using Expression Analysis structure";}
	
	@Override public String getPluginName(){return "Expression Analysis oriented Workflow Data Importer";}
}
