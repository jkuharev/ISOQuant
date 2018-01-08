/** ISOQuant, isoquant.plugins.benchmark, 19.01.2012*/
package isoquant.plugins.benchmark;

import java.io.File;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Map;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.utilities.ResourceLoader;
import de.mz.jk.jsix.utilities.Settings;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DB;
import isoquant.kernel.plugin.queue.PluginQueue;
import isoquant.plugins.batch.ReprocessBatcher;
import isoquant.plugins.benchmark.mpqs.MPQS_IQ_Sample_TopX_Reporter;
import isoquant.plugins.db.ProjectCleaner;
import isoquant.plugins.processing.annotation.EMRTClusterAnnotator;
import isoquant.plugins.processing.expression.EMRTClusteringPlugin;
import isoquant.plugins.processing.normalization.IntensityNormalizer;
import isoquant.plugins.processing.quantification.TopXQuantifier;
import isoquant.plugins.report.prep.ReportPreparator;

/**
 * <h3>{@link ParameterBencher}</h3>
 * @author kuharev
 * @version 19.01.2012 09:34:19
 */
public class ParameterBencher extends SingleActionPlugin4DB implements AttributesIterator.PermutationListener 
{
	ReprocessBatcher batcher = null;
	MPQS_IQ_Sample_TopX_Reporter reporter = null;
	
	PluginQueue processQueue = new PluginQueue(			
		"run clean process",
		ResourceLoader.getIcon("queue_small"),
		new String[] {
			// ProjectCleaner.class.getName(),
			// EMRTTableFromMassSpectrumCreator.class.getName(),
			// ExpressionAnalysisPlugin.class.getName(),
			EMRTClusteringPlugin.class.getName(),
			EMRTClusterAnnotator.class.getName(),
			IntensityNormalizer.class.getName(),
			TopXQuantifier.class.getName(),
			ReportPreparator.class.getName()
		}
	);
	
	public ParameterBencher(iMainApp app)
	{
		super(app);
		batcher = new ReprocessBatcher( app );
		reporter = new MPQS_IQ_Sample_TopX_Reporter( app );
		cfg = app.getSettings();
	}
	
	String outDir = "/Volumes/OSXHome/UNIKLINIK/eclipse.projects/ISOQuant_Tuning/";
	Settings cfg = null;
	DBProject p = null;
	PrintStream parameterTable = null;
	String outPath = "";
	
	@Override public void runPluginAction(DBProject p) throws Exception
	{
		this.p = p;
		
		outDir = cfg.getStringValue("BENCHMARK_REPORT_DIR", outDir, false)
				+ XJava.timeStamp("yyMM") + "/" 
				+ XJava.timeStamp("yyMMdd") + "/";
		
		outPath = outDir + p.data.title + "/";

		// make sure output path exists
		File outPathFile = new File(outPath);
		if( !outPathFile.exists() ) outPathFile.mkdirs();
		
		// switch cleaner to batch mode
		ProjectCleaner.silentMode = true;
		
		parameterTable = new PrintStream( new File(outPath + XJava.timeStamp("yyMMdd") + "_parameters.csv") );
		
		String[] varNames = cfg.getArray("BENCHMARK_VARS", new String[] {"A","B","C"}, false	);

		// print title row
		parameterTable.println("id;" + XJava.joinArray(varNames, ";") );

		// read benchmark variable values
		String[][] varValues = new String[varNames.length][];
		for(int i = 0; i<varNames.length; i++)
		{
			String varName = varNames[i];
			String oldValue = cfg.getStringValue(varName, "", false);
			System.out.println(varName+"="+oldValue);
			String[] varVal = cfg.getArray( "BENCHMARK_"+varName, new String[]{oldValue+""}, false );
			System.out.println("BENCHMARK_"+varName+"="+XJava.joinArray(varVal, ";"));
			varValues[i] = varVal;
		}
		
		currentLoop = 0;
		loopsDone = cfg.getIntValue("BENCHMARK_LOOPS_DONE", 0, false);
		
		AttributesIterator ai = new AttributesIterator( varNames, varValues, this );
		ai.run();
				
		//--------//--------//--------//--------//--------//--------//--------//--------
		//--------//--------//--------//--------//--------//--------//--------//--------
		//--------//--------//--------//--------//--------//--------//--------//--------

	}
	
	private int currentLoop = 0;
	private int loopsDone = 0;
	
	@Override public void attributesChanged(String[] varNames, Map<String, String> varVals)
	{
		// run benchmark
		String loopID = new DecimalFormat("0000").format(currentLoop);
		
		System.out.println("running " + currentLoop + ". benchmark loop ...");
		
		// set current parameter values
		parameterTable.print( loopID + ";" );
		for(String var : varNames)
		{
			String val = varVals.get(var);
			cfg.setValue(var, val);
			parameterTable.print( val + ";" );
			System.out.println("	" + var + "=" + val);
		}			
		parameterTable.println();
		
		if( loopsDone>currentLoop++ )
		{
			System.out.println("skipping " + currentLoop + ". benchmark loop ... ");
		}
		else
		{
			// process data
			batcher.runBatch(processQueue, Collections.singletonList(p));	
			
			// store results
			try
			{
				report(p, outPath + loopID + ".csv");
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			
			cfg.setValue("BENCHMARK_LOOPS_DONE", currentLoop);
		}
	}
	

	@Override public String getPluginName()
	{
		return "MPQS based Parameter Benchmark Runner";
	}

	@Override public int getExecutionOrder()
	{
		return 0;
	}

	@Override public String getMenuItemText()
	{
		return "run parameter benchmark";
	}

	@Override public String getMenuItemIconName()
	{
		return "benchmark";
	}
	
	public void report(DBProject p, String file) throws Exception
	{
		reporter.createReport(p, new File(file));
		reporter.createLog(p, new File(file+".log"));
	}

}
