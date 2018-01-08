/** ISOQuant, isoquant.plugins.processing.quantification, 19.07.2013 */
package isoquant.plugins.processing.quantification;

import java.util.ArrayList;
import java.util.List;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.mysql.StdOutSQLBatchExecutionAdapter;
import de.mz.jk.jsix.utilities.Settings;
import isoquant.interfaces.log.iLogManager;
import isoquant.kernel.log.LogEntry;

/**
 * <h3>{@link QuantificationFilter}</h3>
 * @author kuharev
 * @version 19.07.2013 10:39:02
 */
public class QuantificationFilter extends StdOutSQLBatchExecutionAdapter
{
	public boolean
			FILTER_IN_SOURCE = false,
			FILTER_MISSING_CLEAVAGE = false,
			FILTER_NEUTRAL_LOSS_H20 = false,
			FILTER_NEUTRAL_LOSS_NH3 = false,
			FILTER_DDA = true,
			FILTER_PEP_FRAG_1 = true,
            FILTER_PEP_FRAG_2 = false,
            FILTER_VAR_MOD = false,
			FILTER_PTM = false;
	/** peptide filter list like ", 'IN_SOURCE', 'PEP_FRAG_2'"  */
	public String peptideFilterAdditionalTypes = "";
	public double minPeptideMaxScorePerCluster = 0.0;

	public void loadSettings(Settings cfg)
	{
		minPeptideMaxScorePerCluster = cfg.getDoubleValue("process.quantification.peptide.minMaxScorePerCluster", minPeptideMaxScorePerCluster, false);
		FILTER_IN_SOURCE = cfg.getBooleanValue("process.quantification.peptide.acceptType.IN_SOURCE", FILTER_IN_SOURCE, false);
		FILTER_MISSING_CLEAVAGE = cfg.getBooleanValue("process.quantification.peptide.acceptType.MISSING_CLEAVAGE", FILTER_MISSING_CLEAVAGE, false);
		FILTER_NEUTRAL_LOSS_H20 = cfg.getBooleanValue("process.quantification.peptide.acceptType.NEUTRAL_LOSS_H20", FILTER_NEUTRAL_LOSS_H20, false);
		FILTER_NEUTRAL_LOSS_NH3 = cfg.getBooleanValue("process.quantification.peptide.acceptType.NEUTRAL_LOSS_NH3", FILTER_NEUTRAL_LOSS_NH3, false);
		FILTER_PEP_FRAG_1 = cfg.getBooleanValue( "process.quantification.peptide.acceptType.PEP_FRAG_1", FILTER_PEP_FRAG_1, false );
		FILTER_PEP_FRAG_2 = cfg.getBooleanValue("process.quantification.peptide.acceptType.PEP_FRAG_2", FILTER_PEP_FRAG_2, false);
        FILTER_DDA = cfg.getBooleanValue("process.quantification.peptide.acceptType.DDA", FILTER_DDA, false);
        FILTER_VAR_MOD = cfg.getBooleanValue("process.quantification.peptide.acceptType.VAR_MOD", FILTER_VAR_MOD, false);
		FILTER_PTM = cfg.getBooleanValue("process.quantification.peptide.acceptType.PTM", FILTER_PTM, false);
		peptideFilterAdditionalTypes = listPeptideTypes();
	}

	private String listPeptideTypes()
	{
		List<String> types = new ArrayList<String>();
		if (FILTER_PEP_FRAG_1) types.add( "'PEP_FRAG_1'" );
		if (FILTER_IN_SOURCE) types.add( "'IN_SOURCE'" );
		if (FILTER_MISSING_CLEAVAGE) types.add( "'MISSING_CLEAVAGE'" );
		if (FILTER_NEUTRAL_LOSS_H20) types.add( "'NEUTRAL_LOSS-H20'" );
		if (FILTER_NEUTRAL_LOSS_NH3) types.add( "'NEUTRAL_LOSS-NH3'" );
		if (FILTER_PEP_FRAG_2) types.add( "'PEP_FRAG_2'" );
		if (FILTER_DDA) types.add( "'DDA'" );
		if (FILTER_VAR_MOD) types.add( "'VAR_MOD'" );
		if (FILTER_PTM) types.add( "'PTM'" );
		// ensure peptideFilter has at least one type
		if (types.size() < 1) types.add( "'PEP_FRAG_1'" );
		return XJava.joinList( types, "," );
	}

	/** @param log */
	public void logParameters(iLogManager log)
	{
		log.add(LogEntry.newParameter("process.quantification.peptide.minMaxScorePerCluster", minPeptideMaxScorePerCluster + ""));
		log.add(LogEntry.newParameter("process.quantification.peptide.acceptType.IN_SOURCE", FILTER_IN_SOURCE + ""));
		log.add(LogEntry.newParameter("process.quantification.peptide.acceptType.MISSING_CLEAVAGE", FILTER_MISSING_CLEAVAGE + ""));
		log.add(LogEntry.newParameter("process.quantification.peptide.acceptType.NEUTRAL_LOSS_H20", FILTER_NEUTRAL_LOSS_H20 + ""));
		log.add(LogEntry.newParameter("process.quantification.peptide.acceptType.NEUTRAL_LOSS_NH3", FILTER_NEUTRAL_LOSS_NH3 + ""));
		log.add( LogEntry.newParameter( "process.quantification.peptide.acceptType.PEP_FRAG_1", FILTER_PEP_FRAG_1 + "" ) );
		log.add(LogEntry.newParameter("process.quantification.peptide.acceptType.PEP_FRAG_2", FILTER_PEP_FRAG_2 + ""));
		log.add(LogEntry.newParameter("process.quantification.peptide.acceptType.VAR_MOD", FILTER_VAR_MOD + ""));
		log.add(LogEntry.newParameter("process.quantification.peptide.acceptType.PTM", FILTER_PTM + ""));
        log.add(LogEntry.newParameter("process.quantification.peptide.acceptType.DDA", FILTER_DDA + ""));
	}

	@Override public String processSQLStatementBeforeExecution(String sql)
	{
		return sql
				.replaceAll("%PEPTIDE_MIN_MAX_SCORE_PER_CLUSTER%", minPeptideMaxScorePerCluster + "")
				.replaceAll("%PEPTIDE_TYPE_FILTER_ADDITIONAL_TYPES%", peptideFilterAdditionalTypes);
	}
}
