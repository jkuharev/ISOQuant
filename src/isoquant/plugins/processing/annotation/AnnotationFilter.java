package isoquant.plugins.processing.annotation;

import java.util.ArrayList;
import java.util.List;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.mysql.StdOutSQLBatchExecutionAdapter;
import de.mz.jk.jsix.utilities.Settings;
import isoquant.interfaces.log.iLogManager;
import isoquant.kernel.log.LogEntry;

public class AnnotationFilter extends StdOutSQLBatchExecutionAdapter
{
	public float minPeptideReplication = 2; // peptide in min n workflows
	public int minPeptideLength = 6; // min sequence length
	public double minPeptideScore = 0.0; // peptide's score is more than x
	public double minOverallMaxScore = 3.0; // peptide's max stat score is
// more than x
	public int maxCountPerEMRTCluster = 1; //
	// calculated raplication rates
	public int minPeptideReplicationRateValue = (int) minPeptideReplication;
	/** should homologous proteins be filtered (reduced to the protein with highest probability) */
	public boolean useHomologyFilter = true;
	/** 
	 * if true add type name enclosed by '' to %PEPTIDE_TYPE_FILTER_ADDITIONAL_TYPES%
	 * including a leading comma
	 * e.g. ", 'IN_SOURCE', 'PEP_FRAG_2'" ...
	 */
	public boolean
			FILTER_PEP_FRAG_1 = true,
 			FILTER_DDA = true,
			FILTER_IN_SOURCE = false,
			FILTER_MISSING_CLEAVAGE = false,
			FILTER_NEUTRAL_LOSS_H20 = false,
			FILTER_PEP_FRAG_2 = false,
			FILTER_NEUTRAL_LOSS_NH3 = false,
			FILTER_VAR_MOD = false,
			FILTER_PTM = false;
	/** peptide filter list like ", 'IN_SOURCE', 'PEP_FRAG_2'"  */
	public String peptideFilterAdditionalTypes = "";
	/** peptide False Positive Rate level */
	public double maxPeptideFDR = 0.01;

	public void loadSettings(Settings cfg)
	{
// System.out.print("loading annotation settings . . . ");
		minPeptideReplication = cfg.getFloatValue("process.identification.peptide.minReplicationRate", minPeptideReplication, false);
		minPeptideScore = cfg.getDoubleValue("process.identification.peptide.minScore", minPeptideScore, false);
		minOverallMaxScore = cfg.getDoubleValue("process.identification.peptide.minOverallMaxScore", minPeptideScore, false);
		minPeptideLength = cfg.getIntValue("process.identification.peptide.minSequenceLength", minPeptideLength, false);
		maxCountPerEMRTCluster = cfg.getIntValue("process.annotation.peptide.maxSequencesPerEMRTCluster", maxCountPerEMRTCluster, false);
		useHomologyFilter = cfg.getBooleanValue("process.annotation.protein.resolveHomology", useHomologyFilter, false);
		
		// FPR is not FDR!!!
		// remove this lines in some moment
		if(cfg.isSet( "process.annotation.peptide.maxFPR" ))
		{
			maxPeptideFDR = cfg.getDoubleValue("process.annotation.peptide.maxFPR", maxPeptideFDR, true);
			cfg.remove( "process.annotation.peptide.maxFPR" );
		}
		maxPeptideFDR = cfg.getDoubleValue( "process.annotation.peptide.maxFDR", maxPeptideFDR, false );
		
		FILTER_PEP_FRAG_1 = cfg.getBooleanValue( "process.identification.peptide.acceptType.PEP_FRAG_1", FILTER_PEP_FRAG_1, false );
		FILTER_DDA = cfg.getBooleanValue( "process.identification.peptide.acceptType.DDA", FILTER_DDA, false );
		
		FILTER_IN_SOURCE = cfg.getBooleanValue("process.identification.peptide.acceptType.IN_SOURCE", FILTER_IN_SOURCE, false);
		FILTER_MISSING_CLEAVAGE = cfg.getBooleanValue("process.identification.peptide.acceptType.MISSING_CLEAVAGE", FILTER_MISSING_CLEAVAGE, false);
		FILTER_NEUTRAL_LOSS_H20 = cfg.getBooleanValue("process.identification.peptide.acceptType.NEUTRAL_LOSS_H20", FILTER_NEUTRAL_LOSS_H20, false);
		FILTER_NEUTRAL_LOSS_NH3 = cfg.getBooleanValue("process.identification.peptide.acceptType.NEUTRAL_LOSS_NH3", FILTER_NEUTRAL_LOSS_NH3, false);
		FILTER_PEP_FRAG_2 = cfg.getBooleanValue("process.identification.peptide.acceptType.PEP_FRAG_2", FILTER_PEP_FRAG_2, false);
		FILTER_VAR_MOD = cfg.getBooleanValue("process.identification.peptide.acceptType.VAR_MOD", FILTER_VAR_MOD, false);
		FILTER_PTM = cfg.getBooleanValue("process.identification.peptide.acceptType.PTM", FILTER_PTM, false);
		
		peptideFilterAdditionalTypes = listPeptideTypes();
		// System.out.println("[done]");
	}

	private String listPeptideTypes()
	{
		List<String> types = new ArrayList<String>();
		if(FILTER_PEP_FRAG_1) types.add( "'PEP_FRAG_1'" );
		if(FILTER_IN_SOURCE) types.add( "'IN_SOURCE'" );
		if(FILTER_MISSING_CLEAVAGE) types.add( "'MISSING_CLEAVAGE'");
		if(FILTER_NEUTRAL_LOSS_H20) types.add( "'NEUTRAL_LOSS-H20'");
		if(FILTER_NEUTRAL_LOSS_NH3) types.add( "'NEUTRAL_LOSS-NH3'");
		if(FILTER_PEP_FRAG_2) types.add( "'PEP_FRAG_2'");
        if(FILTER_DDA) types.add( "'DDA'");
		if(FILTER_VAR_MOD) types.add( "'VAR_MOD'");
		if(FILTER_PTM) types.add( "'PTM'");
		
		// ensure peptideFilter has at least one type
		if (types.size() < 1) types.add( "'PEP_FRAG_1'" );
		
		return XJava.joinList( types, "," );
	}

	private static float one = 0.99999999f;

	/** @param db */
	public void checkReplicationRates(MySQL db)
	{
		// peakCount runs
		float nRuns = Float.parseFloat(db.getFirstValue("SELECT COUNT(DISTINCT `index`) FROM workflow", 1));
		System.out.print("checking replication rate boundaries ... ");
		// check for relative replication rate values (rate < 1)
		minPeptideReplicationRateValue = (int) (minPeptideReplication * ((minPeptideReplication < one) ? nRuns : 1));
		// enforce replication rates not to be greater than number of runs
		if (minPeptideReplicationRateValue > nRuns)
			minPeptideReplicationRateValue = (int) nRuns;
		System.out.println("[done]");
	}

	/** @param log */
	public void logParameters(iLogManager log)
	{
		// identification filter
		log.add(LogEntry.newParameter("process.identification.peptide.minReplicationRate", minPeptideReplication
				+ ((minPeptideReplication < one) ? " = " + minPeptideReplicationRateValue : "")));
		log.add(LogEntry.newParameter("process.identification.peptide.minScore", minPeptideScore + ""));
		log.add(LogEntry.newParameter("process.identification.peptide.minOverallMaxScore", minOverallMaxScore + ""));
		log.add(LogEntry.newParameter("process.identification.peptide.minSequenceLength", minPeptideLength + ""));
		// identification filter: peptide types
		log.add( LogEntry.newParameter( "process.identification.peptide.acceptType.PEP_FRAG_1", FILTER_PEP_FRAG_1 + "" ) );
		log.add(LogEntry.newParameter("process.identification.peptide.acceptType.IN_SOURCE", FILTER_IN_SOURCE + ""));
		log.add(LogEntry.newParameter("process.identification.peptide.acceptType.MISSING_CLEAVAGE", FILTER_MISSING_CLEAVAGE + ""));
		log.add(LogEntry.newParameter("process.identification.peptide.acceptType.NEUTRAL_LOSS_H20", FILTER_NEUTRAL_LOSS_H20 + ""));
		log.add(LogEntry.newParameter("process.identification.peptide.acceptType.NEUTRAL_LOSS_NH3", FILTER_NEUTRAL_LOSS_NH3 + ""));
		log.add(LogEntry.newParameter("process.identification.peptide.acceptType.PEP_FRAG_2", FILTER_PEP_FRAG_2 + ""));
        log.add(LogEntry.newParameter("process.identification.peptide.acceptType.DDA", FILTER_DDA + ""));
        log.add(LogEntry.newParameter("process.identification.peptide.acceptType.VAR_MOD", FILTER_VAR_MOD + ""));
		log.add(LogEntry.newParameter("process.identification.peptide.acceptType.PTM", FILTER_PTM + ""));
		// filter for EMRT cluster annotation
		log.add(LogEntry.newParameter("process.annotation.peptide.maxSequencesPerEMRTCluster", maxCountPerEMRTCluster + ""));
		log.add(LogEntry.newParameter("process.annotation.protein.resolveHomology", useHomologyFilter + ""));
		log.add( LogEntry.newParameter( "process.annotation.peptide.maxFDR", maxPeptideFDR + "" ) );
	}

	@Override public String processSQLStatementBeforeExecution(String template)
	{
		return template
				// identification
				.replaceAll("%PEPTIDE_STAT_COUNT_WORKFLOWS_MIN_VALUE%", minPeptideReplicationRateValue + "")
				.replaceAll("%PEPTIDE_MIN_SCORE%", minPeptideScore + "")
				.replaceAll("%PEPTIDE_MIN_STAT_MAX_SCORE%", minOverallMaxScore + "")
				.replaceAll("%PEPTIDE_SEQUENCE_LENGTH_MIN_VALUE%", minPeptideLength + "")
				.replaceAll("%PEPTIDE_MIN_MAX_STAT_SCORE%", minOverallMaxScore + "")
				.replaceAll("%PEPTIDE_SEQUENCE_LENGTH_MIN_VALUE%", minPeptideLength + "")
				.replaceAll("%PEPTIDE_TYPE_FILTER_ADDITIONAL_TYPES%", peptideFilterAdditionalTypes)
				// annotation
				.replaceAll("%MAX_ALLOWED_SEQUENCES_PER_EMRT_CLUSTER%", maxCountPerEMRTCluster + "")
				.replaceAll("%MAX_PEPTIDE_FDR%", maxPeptideFDR + "" );
	}
}
