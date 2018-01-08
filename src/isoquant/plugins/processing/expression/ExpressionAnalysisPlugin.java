package isoquant.plugins.processing.expression;

import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DB;
import isoquant.plugins.processing.restructuring.EMRTTableFromMassSpectrumCreator;

/**
 * <h3>{@link ExpressionAnalysisPlugin}</h3>
 * @author Joerg Kuharev
 * @version 04.02.2011 13:03:53
 */
public class ExpressionAnalysisPlugin extends SingleActionPlugin4DB
{
	public ExpressionAnalysisPlugin(iMainApp app)
	{
		super(app);
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		// recreate clustered_emrt ??? 
		// solved using EMRTTableCreatorFromMassSpectrum Plugin!!!
		new EMRTTableFromMassSpectrumCreator( app ).runPluginAction( p );
		
		System.out.println( "running time alignment:" );
		new RetentionTimeAlignmentPlugin( app ).runPluginAction( p );
		
		System.out.println( "running emrt clustering:" );
		new EMRTClusteringPlugin( app ).runPluginAction( p );
		
		// merge multiple emrts per cluster per run
	}

	@Override public String getMenuItemIconName(){return "expression_analysis";}
	@Override public String getMenuItemText(){return "run expression analysis";}
	@Override public String getPluginName(){return "ISOQuant Expression Analysis";}

	@Override public int getExecutionOrder()
	{
		return 10;
	}
}
