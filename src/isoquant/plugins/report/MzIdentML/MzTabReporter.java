/*******************************************************************************
 * THIS FILE IS PART OF ISOQUANT SOFTWARE PROJECT WRITTEN BY JOERG KUHAREV
 *
 * Copyright (c) 2009 - 2013, JOERG KUHAREV and STEFAN TENZER
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 * must display the following acknowledgment:
 * This product includes software developed by JOERG KUHAREV and STEFAN TENZER.
 * 4. Neither the name "ISOQuant" nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY JOERG KUHAREV ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL JOERG KUHAREV BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
/** ISOQuant, isoquant.plugins.report.MzIdentML, 23.10.2014 */

package isoquant.plugins.report.MzIdentML;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.plgs.data.Workflow;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.db.IQDBUtils;
import uk.ac.ebi.jmzidml.model.mzidml.Cv;
import uk.ac.ebi.jmzidml.model.mzidml.CvParam;
import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.jmztab.utils.convert.ConvertProvider;


public class MzTabReporter extends  ConvertProvider<DBProject, Void> {
    private Metadata mtd;
    private MZTabColumnFactory peh;

    private MZTabColumnFactory prh;   // Protein Header Column Factory
    private MZTabColumnFactory psh;   // PSM Header Column Factory.

    public static final String sql_mztab_proteinsection = "mzidentml_mztab_proteinsection.sql";
    public static final String db_search_engine_name = "[MS,MS:1000601,ProteinLynx Global Server,]";

    public String specie;
    public String dbname;
    public String ncbiTaxId;
    public String dbversion;

    private List<String> var_modifications;
    private List<String> fix_modifications;
    private Map<Param, Set<String>> variableModifications;

    private Map<Integer, Workflow> msRuns_wf;
    private Map<Integer, Integer> wfs_msRuns;

    public final Cv CvPSIMS = PSIStandardUtils.getthisCv(CvReference.PSI_MS);
    public final Cv CvUNIMOD = PSIStandardUtils.getthisCv(CvReference.UNIMOD);
    public final Cv CvUO = PSIStandardUtils.getthisCv(CvReference.UNIT_ONTOLOGY);


    private PeptideModsReference modificationReference = new PeptideModsReference();

    public MzTabReporter(DBProject p) {
        // Generate mzTab by manual, no convert source and parameters setting.
        super(p, null);
    }


    /**
     * Generate metadata section by manual.
     */
    @Override
    protected Metadata convertMetadata() {

        mtd = new Metadata();

        var_modifications = new ArrayList<>();
        fix_modifications = new ArrayList<>();
        variableModifications = new HashMap<Param, Set<String>>();
        msRuns_wf = new HashMap<Integer, Workflow>();
        wfs_msRuns = new HashMap<Integer, Integer>();

        // setting mzTab- description.
        mtd.setMZTabMode(MZTabDescription.Mode.Complete);
        mtd.setMZTabType(MZTabDescription.Type.Quantification);
		mtd.setMZTabID( source.data.id );
		mtd.setDescription( source.data.title );

        // create ms_run[1-n]-location
		source.data.samples = IQDBUtils.getSamples( source );
        List<Workflow> runs = IQDBUtils.getWorkflows(source);
        int nRuns = runs.size();
		int nSamples = source.data.samples.size();

        // MS_PROTEIN_LYNX_SCORE_LOG_L("MS","MS:1001570","ProteinLynx:Log Likelihood","MS:1001570")

        for (int i = 0; i < nRuns; i++) {
            Workflow run = runs.get(i);
            Map<String, String>  wfMetaInfo = IQDBUtils.getWorkflowMetaInfo(source, run, "Workflow");

            //mtd.addMsRunLocation(i + 1, MZTabUtils.parseURL(run.acquired_name));  //Careful: MsRun index can not be 0 in mzTab model.

            //ModificationParams
            // set fixed_mod[1] and variable_mod[1]
            //mtd.addFixedModParam(1, MZTabUtils.parseParam("[UNIMOD, UNIMOD:4, Carbamidomethyl, ]"));
            //mtd.addVariableModParam(1, MZTabUtils.parseParam("[UNIMOD, UNIMOD:35, Oxidation, ]"));

            if(wfMetaInfo.containsKey("MODIFICATIONS"))
            {
                String[] mods = XJava.decURL(wfMetaInfo.get("MODIFICATIONS")).split(";");
                for (String m : mods)
                {
                    Map<String, String> modinfo =  XJava.decMap(m, ",", ":");
                    String modName = modinfo.get("name").split("\\+")[0];
                    String modFixedMod = modinfo.get("status");

                    PeptideModification peptideMod = modificationReference.getPeptideModsByPLGScode(modName).get(1);
                    String unimod_id = peptideMod.getUnimod_Id();
                    String um_codename = peptideMod.getUnimod_code_name();
                    String modification = "[UNIMOD," + unimod_id + "," + um_codename + ", ]";
                    if (modFixedMod.equals("VARIABLE"))
                    {
                        if(!var_modifications.contains(unimod_id))
                        {
                            mtd.addVariableModParam(i+1, MZTabUtils.parseParam(modification));
                            var_modifications.add(unimod_id);
                        }
                    }
                    else
                    {
                        if(!fix_modifications.contains(unimod_id))
                        {
                            mtd.addFixedModParam(i + 1, MZTabUtils.parseParam(modification));
                            fix_modifications.add(unimod_id);
                        }
                    }
                }
            }
        }

        int runCounter = 1;
        for(int i=0; i < nSamples; i++)
        {
			de.mz.jk.plgs.data.Sample s = source.data.samples.get( i );
            //System.out.println(s.index + " : " + s.name);
            StudyVariable sv = new StudyVariable(i+1);
            sv.setDescription(s.name);
            mtd.addStudyVariableDescription(i+1, s.name);
            mtd.addStudyVariable(sv);
            s.workflows = IQDBUtils.getWorkflowsBySampleIndex(source, s.index);

            //System.out.println("Sample: " + s.index + " num workflows: " + s.workflows.size());
            for(Workflow wf: s.workflows)
            {
                MsRun msr = new MsRun(runCounter);
                msr.setLocation(MZTabUtils.parseURL(wf.acquired_name));
                //msr.set
                mtd.addMsRun(msr);
                String location = "//" + wf.acquired_name;
                // spectradata.getLocation() != null && !spectradata.getLocation().isEmpty())?spectradata.getLocation():spectradata.getName();
                if(location != null && !location.isEmpty() && !location.contains("file:")) location = "file:"+location;
                if(location == null) location="";
                try{
                    mtd.addMsRunLocation(runCounter, new URL(location));
                }catch (MalformedURLException e){
                    e.printStackTrace();
                }
                //mtd.addMsRunLocation(runCounter, MZTabUtils.parseURL(wf.acquired_name));
                mtd.addAssayQuantificationReagent(runCounter, new CVParam("PRIDE", "PRIDE:0000434", "Unlabeled sample", null));
                mtd.addStudyVariableAssay(i + 1, mtd.getAssayMap().get(runCounter));
                mtd.addAssayMsRun(runCounter, mtd.getMsRunMap().get(runCounter)); //In label-free, one assay per ms-run
                msRuns_wf.put(runCounter, wf);
                wfs_msRuns.put(wf.index, runCounter);
                System.out.println("msrun: " + runCounter + " name: " + location);

                runCounter++;
            }
        }

        //set search_engine_score[1]
        //mtd.addProteinSearchEngineScoreParam(1, new CVParam("MS", "MS:1001171", "Mascot:score", null));
        mtd.addProteinSearchEngineScoreParam(1,new UserParam("PLGS:score",null)); //It was originally 0
        mtd.addPeptideSearchEngineScoreParam(1, new UserParam("PLGS:score", null));

        mtd.addPsmSearchEngineScoreParam(1, new CVParam("MS", "MS:1001570", "ProteinLynx:Log Likelihood", null));
        //mtd.addPeptideSearchEngineScoreParam(1, new CVParam("MS", "MS:1001570", "ProteinLynx:Log Likelihood", null));
        //mtd.addProteinSearchEngineScoreParam(1, new CVParam("MS", "MS:1001570", "ProteinLynx:Log Likelihood", null));


        // set peptide-quantification_unit
        mtd.setPeptideQuantificationUnit(MZTabUtils.parseParam("[PRIDE, PRIDE:0000396, Copies per cell,]")); //TODO: request a PRIDE CV for ppm

        return mtd;
    }


    @Override
    protected MZTabColumnFactory convertPSMColumnFactory()
    {
        //psh = MZTabColumnFactory.getInstance(Section.PSM_Header);

        psh = MZTabColumnFactory.getInstance(Section.PSM);
        psh.addDefaultStableColumns();
        psh.addSearchEngineScoreOptionalColumn(PSMColumn.SEARCH_ENGINE_SCORE,1, null);
        psh.addOptionalColumn(MZIdentMLUtils.OPTIONAL_DECOY_COLUMN, Integer.class);
        //psh.addOptionalColumn(MZIdentMLUtils.OPTIONAL_ID_COLUMN, String.class);
        psh.addOptionalColumn(MZIdentMLUtils.OPTIONAL_ION_MOBILITY_COLUMN, Double.class);

        return  psh;
    }

    /**
     * Generate peptide header line by manual.
     */
    @Override
    protected MZTabColumnFactory convertPeptideColumnFactory() {
        peh = MZTabColumnFactory.getInstance(Section.Peptide);
        peh.addDefaultStableColumns();

        // add best_search_engine_score column
        peh.addBestSearchEngineScoreOptionalColumn(ProteinColumn.BEST_SEARCH_ENGINE_SCORE, 1);

        // abundance optional columns: peptide_abundance_study_variable[1-2], peptide_abundance_stdev_study_variable[1-2] and peptide_abundance_std_error_study_variable[1-2]
        for (StudyVariable studyVariable : mtd.getStudyVariableMap().values()) {
            peh.addAbundanceOptionalColumn(studyVariable);
        }

        return peh;
    }


    /**
     * Generate protein header line by manual.
     */
    @Override
    protected MZTabColumnFactory convertProteinColumnFactory() {
        prh = MZTabColumnFactory.getInstance(Section.Protein);
        prh.addDefaultStableColumns();

        //prh.addDefaultStableColumns();
        prh.addBestSearchEngineScoreOptionalColumn(ProteinColumn.BEST_SEARCH_ENGINE_SCORE, 1);

        // optional columns: search_engine_score_ms_run[1-6], num_psms_ms_run[1-6], num_peptides_distinct_ms_run[1-6] and num_peptides_unique_ms_run[1-6]
        for (MsRun msRun : mtd.getMsRunMap().values()) {
            prh.addSearchEngineScoreOptionalColumn(ProteinColumn.SEARCH_ENGINE_SCORE, 1, msRun);
            //prh.addOptionalColumn(ProteinColumn.NUM_PSMS, msRun);
            prh.addOptionalColumn(ProteinColumn.NUM_PEPTIDES_DISTINCT, msRun);
            prh.addOptionalColumn(ProteinColumn.NUM_PEPTIDES_UNIQUE, msRun);
        }

        // abundance optional columns: protein_abundance_assay[1-6]
        for (Assay assay : mtd.getAssayMap().values()) {
            prh.addAbundanceOptionalColumn(assay);
        }

        // abundance optional columns: protein_abundance_study_variable[1-2], protein_abundance_stdev_study_variable[1-2] and protein_abundance_std_error_study_variable[1-2]
        for (StudyVariable studyVariable : mtd.getStudyVariableMap().values()) {
            prh.addAbundanceOptionalColumn(studyVariable);
        }

        return prh;
    }


    /**
     * Generate and fill on protein record into mzTab file.
     */
    private void fillProteinRecord() {

        source.mysql.executeSQL("SELECT * " +
                                "FROM mzidentml_mztab_protein_section " +
                                "ORDER BY entry, sample_index, workflow_index;");

        try {
            ResultSet rs = source.mysql.getStatement().getResultSet();
            String entry = "before_first_entry";
            String last_entry = "before_first_entry";

            Protein protein = new Protein(prh);
            while (rs.next()) {

                entry = rs.getString("entry");

                if(!last_entry.equals("before_first_entry") && !entry.equalsIgnoreCase(last_entry) && !rs.isFirst())
                {
                    proteins.add(protein);
                    protein = new Protein(prh);
                }
                last_entry = entry;

                int sample_index = rs.getInt("sample_index");
                int workflow_index = rs.getInt("workflow_index");
                double top3_avg_inten = rs.getDouble("top3_avg_inten");
                double rawAmount = rs.getDouble("rawAmount");
                double ppm = rs.getDouble("ppm");
                int unique_peptides = rs.getInt("unique_peptides");
                int razor_peptides = rs.getInt("razor_peptides");
                int shared_peptides = rs.getInt("shared_peptides");
                int num_peptides = rs.getInt("num_peptides");
                String accession = rs.getString("accession");
                String description = rs.getString("description");
                double score = rs.getDouble("score");
                double best_score = rs.getDouble("best_score");
                double coverage = rs.getDouble("coverage");
                double avg_top3_avg_inten = rs.getDouble("avg_top3_avg_inten");
                double avg_rawAmount = rs.getDouble("avg_rawAmount");
                double avg_ppm = rs.getDouble("avg_ppm");
                double stdev_top3_avg_inten = rs.getDouble("stdev_top3_avg_inten");
                double stdev_rawAmount = rs.getDouble("stdev_rawAmount");
                double stdev_ppm = rs.getDouble("stdev_ppm");
                double sem_top3_avg_inten = rs.getDouble("sem_top3_avg_inten");
                double sem_rawAmount = rs.getDouble("sem_rawAmount");
                double sem_ppm = rs.getDouble("sem_ppm");
                specie = PSIStandardUtils.readFromTempTable(source, "orgSciName");
                dbname = PSIStandardUtils.readFromTempTable(source, "dbname");
                ncbiTaxId = PSIStandardUtils.readFromTempTable(source, "ncbiTaxId");
                dbversion = PSIStandardUtils.readFromTempTable(source, "dbversion");

                protein.setAccession(accession);
                protein.setDescription(description);
                protein.setTaxid(ncbiTaxId);
                protein.setSpecies(specie);
                protein.setDatabase(dbname);
                protein.setDatabaseVersion(dbversion);
                protein.setSearchEngine(db_search_engine_name);
                protein.setBestSearchEngineScore(1, best_score);

                // add parameter value for search_engine_score_ms_run[1-6]
                // NOTICE: ms_run[1-6] and search_engine_score[1] SHOULD be defined in the metadata, otherwise throw exception.
                protein.setSearchEngineScore(1, mtd.getMsRunMap().get(workflow_index), score);

                // add parameter value for num_psms_ms_run[1-6]

                // add parameter value for num_peptides_distinct_ms_run[1-6]
                protein.setNumPeptidesDistinct(mtd.getMsRunMap().get(workflow_index), razor_peptides + shared_peptides);

                // add parameter value for num_peptides_unique_ms_run[1-6]
                protein.setNumPeptidesUnique(mtd.getMsRunMap().get(workflow_index), unique_peptides);

                protein.setModifications("0");//TODO: real values
                protein.setProteinConverage(coverage);

                // set value for  protein_abundance_assay[1-6]
                // NOTICE: assay[1-6] SHOULD be defined in the metadata, otherwise throw exception.
                protein.setAbundanceColumnValue(mtd.getAssayMap().get(workflow_index), top3_avg_inten);

                // set value for protein_abundance_study_variable[1-2], protein_abundance_stdev_study_variable[1-2] and protein_abundance_std_error_study_variable[1-2]
                // NOTICE: study_variable[1-2] SHOULD be defined in the metadata, otherwise throw exception.
                // NOTICE: in this demo, protein_abundance_stdev_study_variable[1] and protein_abundance_std_error_study_variable[1] value are "null"
                //protein.setAbundanceColumnValue(mtd.getStudyVariableMap().get(2), "18.76666667");              // protein_abundance_study_variable[2]
                protein.setAbundanceStdevColumnValue(mtd.getStudyVariableMap().get(sample_index), stdev_top3_avg_inten);         // protein_abundance_stdev_study_variable[2]
                protein.setAbundanceStdErrorColumnValue(mtd.getStudyVariableMap().get(sample_index), sem_top3_avg_inten);      // protein_abundance_std_error_study_variable[2]
                protein.setAbundanceColumnValue(mtd.getStudyVariableMap().get(sample_index), avg_top3_avg_inten);  // protein_abundance_study_variable[1]

                if(rs.isLast())
                    proteins.add(protein);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void fillPSMRecord()
    {

        source.mysql.executeSQL("SELECT * " +
                                "FROM mzidentml_SpectrumIdentificationItem sii " +
                                "JOIN mzidentml_pepevidence pepev USING (peptide_ref) " +
                                "JOIN mzidentml_peptide pep ON (pepev.peptide_ref = pep.`index`) " +
                                "ORDER BY peptide_ref;");

        try {
            ResultSet rs = source.mysql.getStatement().getResultSet();

            while (rs.next()) {

                PSM psm = new PSM(psh, mtd);
                /*
                [Term]
                id: MS:1002476
                name: ion mobility drift time
                def: "Drift time of an ion or spectrum of ions as measured in an ion mobility mass spectrometer. This time might refer to the central value of a bin into which all ions within a narrow range of drift time have been aggregated." [PSI:MS]
                xref: value-type:xsd\:float "The allowed value-type for this CV term."
                is_a: MS:1000455 ! ion selection attribute
                relationship: has_units UO:0000028 ! millisecond
                */

                String entry = rs.getString("entry");
                String sequence = rs.getString("sequence");
                int peptide_ref = rs.getInt("peptide_ref");
                entry = rs.getString("entry");
                double PLGS_score = rs.getDouble("PLGS_score");
                int query_mass_index = rs.getInt("query_mass_index");
                double experimental_MassToCharge = rs.getDouble("experimental_MassToCharge");
                double theoretical_MassToCharge = rs.getDouble("theoretical_MassToCharge");
                String pre = rs.getString("pre");
                String post = rs.getString("post");
                int start = rs.getInt("start");
                int end = rs.getInt("end");
                int charge = rs.getInt("Z");
                int is_decoy = rs.getInt("is_decoy");
                double rt = rs.getDouble("RT");
                double mobility = rs.getDouble("Mobility");
                int workflow_index = rs.getInt("workflow_index");
                int msrun = wfs_msRuns.get(workflow_index);
                int low_energy_index = rs.getInt("low_energy_index");
                String modifier = rs.getString("modifier");
                //ms_run[1]:scan=11665
                String spectraref = "ms_run[" + msrun + "]:scan=" + Integer.toString(low_energy_index -1);

                psm.setSequence(sequence);
                psm.setPSM_ID(query_mass_index);
                psm.setAccession(entry); //TODO: it should be accession
                psm.setUnique(MZBoolean.True); //TODO: real value?
                psm.setDatabase(dbname);
                psm.setDatabaseVersion(dbversion);
                psm.setSearchEngine(db_search_engine_name);
                psm.setSearchEngineScore(1, PLGS_score);
                psm.setSpectraRef(spectraref);
                psm.setRetentionTime(Double.toString(rt));
                psm.setCharge(charge);
                psm.setExpMassToCharge(experimental_MassToCharge);
                psm.setCalcMassToCharge(theoretical_MassToCharge);
                psm.setPre(pre);
                psm.setPost(post);
                psm.setStart(start);
                psm.setEnd(end);
                psm.setOptionColumnValue(MZIdentMLUtils.OPTIONAL_DECOY_COLUMN, is_decoy);
                psm.setOptionColumnValue(MZIdentMLUtils.OPTIONAL_ION_MOBILITY_COLUMN, mobility);

                //Modifications
                List<uk.ac.ebi.jmzidml.model.mzidml.Modification>  mods1 = null;
                if(modifier.length()>0)
                {
                    List<Modification> mods = modifications_list(sequence, modifier, true);
                    for(Modification mod : mods)
                        psm.addModification(mod);
                }
                psms.add(psm);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private List<Modification> modifications_list(String sequence, String modifier, boolean add_fixed_mods) {

        List<uk.ac.ebi.jmzidml.model.mzidml.Modification> mods_old;
        List<Modification> mods = new ArrayList<Modification>();

        mods_old = PSIStandardUtils.parseModificationsFromPLGS(modifier);


        for(uk.ac.ebi.jmzidml.model.mzidml.Modification oldMod : mods_old)
        {
            Modification mod = MZTabUtils.parseModification(Section.PSM, oldMod.getCvParam().get(0).getAccession());

            if(mod != null){
                mod.addPosition(oldMod.getLocation(), null);
                String site = null;
                if(oldMod.getLocation()-1 < 0)
                    site = "N-Term";
                else if(sequence.length() <= oldMod.getLocation() -1)
                    site = "C-Term";
                else
                    site = String.valueOf(sequence.charAt(oldMod.getLocation() - 1));
                Param param = convertParam(oldMod.getCvParam().get(0));
                if(!variableModifications.containsKey(param) || !variableModifications.get(param).contains(site)){
                    Set<String> sites = new HashSet<String>();
                    sites = (variableModifications.containsKey(param.getAccession()))?variableModifications.get(param.getAccession()):sites;
                    sites.add(site);
                    variableModifications.put(param, sites);
                }
                if(add_fixed_mods)
                    mods.add(mod);
                else if (!fix_modifications.contains(param.getAccession()))
                    mods.add(mod);
            }
            else{
                System.out.println("Your mzidentml contains an UNKNOWN modification which is not supported by mzTab format");
            }
            for(CvParam param: oldMod.getCvParam()) {
                if(param.getAccession().equalsIgnoreCase(MZIdentMLUtils.CVTERM_NEUTRAL_LOST)){
                    CVParam lost = convertParam(param);
                    Modification modNeutral = new Modification(Section.PSM,Modification.Type.NEUTRAL_LOSS, lost.getAccession());
                    modNeutral.setNeutralLoss(lost);
                    modNeutral.addPosition(oldMod.getLocation(), null);
                    mods.add(modNeutral);
                    //mod.setNeutralLoss(lost);
                }
            }
        }

        return mods;
    }


    private void fillPeptideRecord()
    {

        Map<Integer, Integer> wf_sample = new HashMap<Integer, Integer>();

        source.mysql.executeSQL("SELECT DISTINCT emq.sample_index, emq.workflow_index " +
                            "FROM emrt4quant emq;");
        try{
            ResultSet rs = source.mysql.getStatement().getResultSet();
            while(rs.next()){
                int sample = rs.getInt("sample_index");
                int wf = rs.getInt("workflow_index");
                wf_sample.put(wf, sample);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        source.mysql.executeSQL("SELECT * FROM mzidentml_mztab_peptide;");

        try {
            ResultSet rs = source.mysql.getStatement().getResultSet();

            while (rs.next()) {

                Peptide peptide = new Peptide(peh, mtd);

                String sequence = rs.getString("sequence");
                String modifier = rs.getString("modifier");
                String entry = rs.getString("entry");
                double max_score = rs.getDouble("stat_max_score");
                int charge = rs.getInt("charge");
                String wf_intensities = rs.getString("wf_intensities");
                String wf_lowEnergy = rs.getString("wf_lowEnergy");
                double theoretical_MassToCharge = rs.getDouble("theoretical_MassToCharge");
                double rt_seconds = rs.getDouble("rt_seconds");
                double rt_begin_seconds = rs.getDouble("rt_begin_seconds");
                double rt_end_seconds = rs.getDouble("rt_end_seconds");

                //double mobility = rs.getDouble("Mobility");

                String[] lowenergies = wf_lowEnergy.split(",");
                List<String> spectrarefs = new ArrayList<String>();
                for (String le: lowenergies){
                    if(le.contains(":")){
                    String[] wfindex_le = le.split(":");
                    int msrun = wfs_msRuns.get(Integer.parseInt(wfindex_le[0]));
                    int low_energy_index = Integer.parseInt(wfindex_le[1]);
                    String spectraref = "ms_run[" + msrun + "]:scan=" + Integer.toString(low_energy_index -1);
                    spectrarefs.add(spectraref);
                    }
                }
                String spectrarefs_concat = StringUtils.join(spectrarefs, "|");

                //Fill data
                peptide.setSequence(sequence);
                peptide.setAccession(entry);
                peptide.setUnique("0");
                peptide.setDatabase(dbname);
                peptide.setDatabaseVersion(dbversion);
                peptide.setSearchEngine(db_search_engine_name);

                // Yes, I know this is a dirty trick, but I really don't give a sh*t anymore about all these horrible checks
                // By the way, I retrieve the max score from SQL. I don't need to look for it again.
                for (MsRun msRun: mtd.getMsRunMap().values()){
                    peptide.setSearchEngineScore(1, msRun,max_score);
                }
                peptide.setBestSearchEngineScore(1, 20.0);

                peptide.setReliability("3");
                //peptide.setModifications("3[MS,MS:1001876, modification probability, 0.8]|4[MS,MS:1001876, modification probability, 0.2]-MOD:00412,8[MS,MS:1001876, modification probability, 0.3]-MOD:00412");
                peptide.setRetentionTime(Double.toString(rt_seconds));
                peptide.setRetentionTimeWindow(Double.toString(rt_begin_seconds) + "|" + Double.toString(rt_end_seconds));
                peptide.setCharge(charge);
                peptide.setMassToCharge(theoretical_MassToCharge);
                //peptide.setURI("http://www.ebi.ac.uk/pride/link/to/peptide");
                peptide.setSpectraRef(spectrarefs_concat);

                //Modifications
                List<uk.ac.ebi.jmzidml.model.mzidml.Modification>  mods1 = null;
                if(modifier.length()>0)
                {
                    List<Modification> mods = modifications_list(sequence, modifier, true);
                    for(Modification mod : mods)
                        peptide.addModification(mod);
                }

                // set value for peptide_abundance_study_variable[1-2], peptide_abundance_stdev_study_variable[1-2] and peptide_abundance_std_error_study_variable[1-2]
                // NOTICE: study_variable[1-2] SHOULD be defined in the metadata, otherwise throw exception.
                // NOTICE: in this demo, peptide_abundance_stdev_study_variable[1] and peptide_abundance_std_error_study_variable[1] value are "null"

                Map<Integer, List<Double>> sample_intensities = new HashMap<>();
                String[] intensities = wf_intensities.split(",");
                for(String in: intensities){
                    if(in.contains(":")){
                        String[] wf_intensity = in.split(":");
                        int wf = Integer.parseInt(wf_intensity[0]);
                        double intensity = Double.parseDouble(wf_intensity[1]);
                        int msrun = wfs_msRuns.get(wf);
                        int sample = wf_sample.get(wf);

                        if(sample_intensities.containsKey(sample)){
                            sample_intensities.get(sample).add(intensity);
                        }else{
                            sample_intensities.put(sample, new ArrayList<Double>());
                            sample_intensities.get(sample).add(intensity);
                        }
                    }
                }

                for(int sample_index: sample_intensities.keySet()){
                   List<Double> intens_s = sample_intensities.get(sample_index);
                    double[] intens_array = new double[intens_s.size()];
                    SummaryStatistics sms = new SummaryStatistics();
                    for (int i = 0; i < intens_array.length; i++) {
                        intens_array[i] = intens_s.get(i);                // java 1.5+ style (outboxing)
                        sms.addValue(intens_s.get(i));
                    }
                    sms.getMean();
                    sms.getStandardDeviation();
                    sms.getPopulationVariance();
                    sms.getSum();
                    peptide.setAbundanceColumnValue(mtd.getStudyVariableMap().get(sample_index), sms.getSum());
                    peptide.setAbundanceStdevColumnValue(mtd.getStudyVariableMap().get(sample_index), sms.getStandardDeviation());   //TODO: This is absurd if we use the sum of intensities...
                }

                //peptide.setAbundanceStdErrorColumnValue(sv2, "1.327905619");

                // NOTICE: should be add peptide into peptides Container, which defined in the ConvertProvider class.
                peptides.add(peptide);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private CVParam convertParam(CvParam param) {
        return new CVParam(param.getCvRef(), param.getAccession(), param.getName(), param.getValue());
    }

    @Override
    protected void fillData() {

        fillProteinRecord();
        fillPSMRecord();
        fillPeptideRecord();
    }

}