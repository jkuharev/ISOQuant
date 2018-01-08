/** ISOQuant_1.0, isoquant.plugins.processing.expression, 04.02.2011 */
package isoquant.plugins.processing.expression;

import java.io.File;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.jsix.utilities.Settings;
import de.mz.jk.ms.align.com.abstraction.AbstractionMethod;
import isoquant.app.Defaults;
import isoquant.interfaces.iMainApp;
import isoquant.interfaces.log.iLogManager;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.db.IQDBUtils;
import isoquant.kernel.log.LogEntry;
import isoquant.kernel.plugin.SingleActionPlugin4DB;
import isoquant.plugins.processing.expression.align.MultiRunAlignmentProcedure;
import isoquant.plugins.processing.expression.align.context.AlignmentMethod;
import isoquant.plugins.processing.expression.align.context.ReferenceRunSelectionMethod;
import isoquant.plugins.processing.expression.align.methods.DynamicProgrammingPathMode;

/**
 * <h3>{@link RetentionTimeAlignmentPlugin}</h3>
 * @author Joerg Kuharev
 * @version 04.02.2011 13:55:18
 */
public class RetentionTimeAlignmentPlugin extends SingleActionPlugin4DB
{
	public RetentionTimeAlignmentPlugin(iMainApp app)
	{
		super(app);
	}

	private int OPTION_MIN_PEAKS_FOR_ITERATIVE_ALIGNMENT = 5000;
	private AbstractionMethod OPTION_PREALIGNMENT_ABSTRACTION_METHOD = AbstractionMethod.MASS_IN_RT_WINDOWS;
	private double OPTION_PATH_REFINEMENT_RADIUS = 500;

	private double OPTION_MAX_ALLOWED_DELTA_MASS_PPM = 10.0;
	private double OPTION_MAX_ALLOWED_DELTA_DRIFT_TIME = 2.0;

	private int OPTION_PROCESS_FORKING_DEPTH = Math.max( (int) Math.sqrt(Defaults.availableProcessors), 2);
	private int OPTION_NUMBER_OF_PROCESSES = Math.max( Defaults.availableProcessors, 2 );

	private AlignmentMethod OPTION_ALIGNMENT_METHOD = AlignmentMethod.PARALLEL_FAST_RECURSIVE;

	private boolean OPTION_NORMALIZE_REFERENCE_TIME=false;
	
	private double OPTION_MIN_PEAK_MASS=800.0;
	private int OPTION_MIN_PEAK_INTENSITY=1000;
	
	private DynamicProgrammingPathMode OPTION_PATH_MODE = DynamicProgrammingPathMode.BOTH;
	
	// process.emrt.rt.alignment.method.iterative.growthFactor=2
	private float OPTION_ITERATION_GROWTH = 2.0f;
	// process.emrt.rt.alignment.method.iterative.growthFactorLimit=2.5
	private float OPTION_ITERATION_GROWTH_LIM = 2.5f;
	
	// how to select the reference run
	private ReferenceRunSelectionMethod OPTION_REFERENCE_RUN_SELECTION_METHOD = ReferenceRunSelectionMethod.AUTO;
	private Integer OPTION_REFERENCE_RUN_INDEX = 0;
	private String OPTION_DEBUG_LOG_DIR = "rtw_log";
	private boolean OPTION_DEBUG_LOG_ENABLED = false;
	private boolean OPTION_DEBUG_PLOT_ENABLED = false;

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		Bencher t = new Bencher().start();
		MySQL db = p.mysql;
		
		loadSettings(app.getSettings());
		
		if (IQDBUtils.hasMixedMobility(db) && OPTION_MAX_ALLOWED_DELTA_DRIFT_TIME < 200)
		{
			System.out.println(
					"The processed project contains mixed data with and without ion mobility.\n" +
							"RT Alignment will temporarily relax ion mobility settings."
					);
			OPTION_MAX_ALLOWED_DELTA_DRIFT_TIME = 200.0;
		}
		
		logParameters(p.log);
		
		MultiRunAlignmentProcedure mra = new MultiRunAlignmentProcedure( db, OPTION_ALIGNMENT_METHOD );
		mra
				.setReferenceRunSelectionMethod( OPTION_REFERENCE_RUN_SELECTION_METHOD )
				.setPreferredReferenceRunIndex( OPTION_REFERENCE_RUN_INDEX )
				.setNormalizeRefRT( OPTION_NORMALIZE_REFERENCE_TIME )
				.setMaxProcesses( OPTION_NUMBER_OF_PROCESSES )
				.setMaxProcessForkingDepth( OPTION_PROCESS_FORKING_DEPTH )
				.setMaxDeltaMassPPM( OPTION_MAX_ALLOWED_DELTA_MASS_PPM )
				.setMaxDeltaDrift( OPTION_MAX_ALLOWED_DELTA_DRIFT_TIME )
				.setMinMass( OPTION_MIN_PEAK_MASS )
				.setMinIntensity( OPTION_MIN_PEAK_INTENSITY )
				.setAlignmentPathMode( OPTION_PATH_MODE )
				.setPrealignmentPathRefinementRadius( OPTION_PATH_REFINEMENT_RADIUS )
				.setPrealignmentMinPeaks( OPTION_MIN_PEAKS_FOR_ITERATIVE_ALIGNMENT )
				.setPrealignmentAbstractionMethod( OPTION_PREALIGNMENT_ABSTRACTION_METHOD )
				.setPrealignmentGrowthFactor( OPTION_ITERATION_GROWTH )
				.setPrealignmentGrowthFactorLimit( OPTION_ITERATION_GROWTH_LIM )
				// .setDebugLogDir( OPTION_DEBUG_LOG_DIR )
				.setDebugLogDir( new File( OPTION_DEBUG_LOG_DIR, "rtw_" + XJava.timeStamp( "yyyyMMddHHmm" ) ).toString() )
				.setDebugLogEnabled( OPTION_DEBUG_LOG_ENABLED )
		;
		mra.run();
		
		p.log.add( LogEntry.newEvent("retention time alignment done", t.stop().getSecString()) );
		System.gc();
	}

	/** log parameter values */
	private void logParameters(iLogManager log)
	{
		log.add( LogEntry.newParameter("process.emrt.rt.alignment.match.maxDeltaMass.ppm", OPTION_MAX_ALLOWED_DELTA_MASS_PPM+"") );
		log.add( LogEntry.newParameter("process.emrt.rt.alignment.match.maxDeltaDriftTime", OPTION_MAX_ALLOWED_DELTA_DRIFT_TIME+"") );
		log.add( LogEntry.newParameter("process.emrt.rt.alignment.normalizeReferenceTime", OPTION_NORMALIZE_REFERENCE_TIME) );
		log.add( LogEntry.newParameter("process.emrt.rt.alignment.maxProcesses", OPTION_NUMBER_OF_PROCESSES) );
		log.add( LogEntry.newParameter( "process.emrt.rt.alignment.referenceRun.selectionMethod", OPTION_REFERENCE_RUN_SELECTION_METHOD.name() ) );
		if (OPTION_REFERENCE_RUN_SELECTION_METHOD.equals( ReferenceRunSelectionMethod.MANUAL ))
			log.add( LogEntry.newParameter( "process.emrt.rt.alignment.referenceRun.preferredIndex", OPTION_REFERENCE_RUN_INDEX ) );

		if(Defaults.DEBUG)
		{
			log.add( LogEntry.newParameter( "process.emrt.rt.alignment.maxProcessForkingDepth", OPTION_PROCESS_FORKING_DEPTH ) );
			log.add( LogEntry.newParameter( "process.emrt.rt.alignment.method", OPTION_ALIGNMENT_METHOD.name() ) );
			log.add( LogEntry.newParameter( "process.emrt.rt.alignment.method.iterative.minPeaks", OPTION_MIN_PEAKS_FOR_ITERATIVE_ALIGNMENT ) );
			log.add( LogEntry.newParameter( "process.emrt.rt.alignment.method.iterative.abstractionMethod", OPTION_PREALIGNMENT_ABSTRACTION_METHOD.name() ) );
			log.add( LogEntry.newParameter( "process.emrt.rt.alignment.method.iterative.pathRefinementRadius", OPTION_PATH_REFINEMENT_RADIUS ) );
			log.add( LogEntry.newParameter( "process.emrt.rt.alignment.method.iterative.growthFactor", OPTION_ITERATION_GROWTH ) );
			log.add( LogEntry.newParameter( "process.emrt.rt.alignment.method.iterative.growthFactorLimit", OPTION_ITERATION_GROWTH_LIM ) );
		}
	}

	@Override public void loadSettings( Settings cfg )
	{
		OPTION_MIN_PEAKS_FOR_ITERATIVE_ALIGNMENT = app.getSettings().getIntValue( "process.emrt.rt.alignment.method.iterative.minPeaks",
				OPTION_MIN_PEAKS_FOR_ITERATIVE_ALIGNMENT, !Defaults.DEBUG );
		OPTION_PREALIGNMENT_ABSTRACTION_METHOD = AbstractionMethod.fromString(
				app.getSettings().getStringValue( "process.emrt.rt.alignment.method.iterative.abstractionMethod",
						OPTION_PREALIGNMENT_ABSTRACTION_METHOD.name(), !Defaults.DEBUG ) );
		OPTION_PATH_REFINEMENT_RADIUS = app.getSettings().getDoubleValue( "process.emrt.rt.alignment.method.iterative.pathRefinementRadius",
				OPTION_PATH_REFINEMENT_RADIUS, !Defaults.DEBUG );
		OPTION_ALIGNMENT_METHOD = AlignmentMethod
				.fromString( cfg.getStringValue( "process.emrt.rt.alignment.method", OPTION_ALIGNMENT_METHOD.name(), !Defaults.DEBUG ) );

		OPTION_PATH_MODE = DynamicProgrammingPathMode.fromString(
				cfg.getStringValue( "process.emrt.rt.alignment.method.pathMode", OPTION_PATH_MODE.toString(), !Defaults.DEBUG ) );

		OPTION_MAX_ALLOWED_DELTA_MASS_PPM = cfg.getDoubleValue( "process.emrt.rt.alignment.match.maxDeltaMass.ppm", OPTION_MAX_ALLOWED_DELTA_MASS_PPM, false );
		OPTION_MAX_ALLOWED_DELTA_DRIFT_TIME = cfg.getDoubleValue( "process.emrt.rt.alignment.match.maxDeltaDriftTime", OPTION_MAX_ALLOWED_DELTA_DRIFT_TIME, false );
		
		OPTION_MIN_PEAK_MASS = cfg.getDoubleValue( "process.emrt.rt.alignment.minMass", OPTION_MIN_PEAK_MASS, false );
		OPTION_MIN_PEAK_INTENSITY = cfg.getIntValue( "process.emrt.rt.alignment.minIntensity", OPTION_MIN_PEAK_INTENSITY, false );

		OPTION_PROCESS_FORKING_DEPTH = cfg.getIntValue( "process.emrt.rt.alignment.maxProcessForkingDepth", OPTION_PROCESS_FORKING_DEPTH, !Defaults.DEBUG );
		OPTION_NUMBER_OF_PROCESSES = cfg.getIntValue( "process.emrt.rt.alignment.maxProcesses", OPTION_NUMBER_OF_PROCESSES, false );

		OPTION_NORMALIZE_REFERENCE_TIME = cfg.getBooleanValue( "process.emrt.rt.alignment.normalizeReferenceTime", OPTION_NORMALIZE_REFERENCE_TIME, false );

		OPTION_ITERATION_GROWTH = cfg.getFloatValue( "process.emrt.rt.alignment.method.iterative.growthFactor", OPTION_ITERATION_GROWTH, !Defaults.DEBUG );
		OPTION_ITERATION_GROWTH_LIM = cfg.getFloatValue( "process.emrt.rt.alignment.method.iterative.growthFactorLimit", OPTION_ITERATION_GROWTH_LIM,
				!Defaults.DEBUG );

		// store a list of available methods
		if (Defaults.DEBUG) cfg.setValue( "process.emrt.rt.alignment.method.availableMethods", XJava.joinArray( AlignmentMethod.implementedMethodNames, "," ) );

		// check values
		if (OPTION_ITERATION_GROWTH < 1.1)
		{
			OPTION_ITERATION_GROWTH = 2.0f;
			cfg.setValue( "process.emrt.rt.alignment.method.iterative.growthFactor", OPTION_ITERATION_GROWTH );
			System.out.println( "growth factor corrected!" );
		}
		if (OPTION_ITERATION_GROWTH_LIM < OPTION_ITERATION_GROWTH)
		{
			OPTION_ITERATION_GROWTH_LIM = 1.5f * OPTION_ITERATION_GROWTH;
			cfg.setValue( "process.emrt.rt.alignment.method.iterative.growthFactorLimit", OPTION_ITERATION_GROWTH_LIM );
			System.out.println( "growth factor limit corrected!" );
		}
		OPTION_REFERENCE_RUN_SELECTION_METHOD = ReferenceRunSelectionMethod.fromString(
				cfg.getStringValue( "process.emrt.rt.alignment.referenceRun.selectionMethod", OPTION_REFERENCE_RUN_SELECTION_METHOD.toString(), false ) );

		OPTION_REFERENCE_RUN_INDEX = cfg.getIntValue( "process.emrt.rt.alignment.referenceRun.preferredIndex", OPTION_REFERENCE_RUN_INDEX, false );

		OPTION_DEBUG_LOG_DIR = cfg.getStringValue( "process.emrt.rt.alignment.debug.log.dir", OPTION_DEBUG_LOG_DIR, !Defaults.DEBUG );
		OPTION_DEBUG_LOG_ENABLED = cfg.getBooleanValue( "process.emrt.rt.alignment.debug.log.enabled", OPTION_DEBUG_LOG_ENABLED, !Defaults.DEBUG );
		OPTION_DEBUG_PLOT_ENABLED = cfg.getBooleanValue( "process.emrt.rt.alignment.debug.plot.enabled", OPTION_DEBUG_PLOT_ENABLED, !Defaults.DEBUG );
	}

	@Override public String getMenuItemIconName(){return "warp";}
	@Override public String getMenuItemText(){return "run retention time alignment";}
	@Override public String getPluginName(){return "Retention Time Aligner";}
	@Override public int getExecutionOrder(){return 10;}
}
