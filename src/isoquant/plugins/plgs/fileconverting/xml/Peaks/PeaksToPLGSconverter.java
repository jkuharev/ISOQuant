package isoquant.plugins.plgs.fileconverting.xml.Peaks;

import com.opencsv.CSVReader;
import de.mz.jk.jsix.libs.XFiles;
import isoquant.plugins.plgs.fileconverting.xml.massspectrum.XMassSpectrum;
import isoquant.plugins.plgs.fileconverting.xml.massspectrum.internal.XFormat;
import isoquant.plugins.plgs.fileconverting.xml.massspectrum.internal.XMs;
import isoquant.plugins.plgs.fileconverting.xml.massspectrum.internal.XProcessingParameters;
import isoquant.plugins.plgs.fileconverting.xml.workflow.XWorkflow;
import isoquant.plugins.plgs.fileconverting.xml.workflow.internal.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by napedro on 03/02/16.
 */
public class PeaksToPLGSconverter {

    protected XWorkflow wf;
    protected XMassSpectrum massSpectrum;
    protected String resultID;
    protected String peaksFile;
    protected String targetPath;
    protected List<Peaks> peaksRows;

    protected HashMap<String, String> peaksModifications;
    protected HashMap<String, XModifier> xModifiers;
    protected HashMap<String, XModifier> xModifiersFound;
    HashMap<Integer, String> peptides;
    HashMap<Integer, String> proteins;

    private int peptidesCurrentId;
    private int proteinsCurrentId;

    private float protonMass = 1.007276F;

    private String workflowTitle;

    public PeaksToPLGSconverter(String fileName, String targetPath, String ID, boolean CAM_Cys, String wfTitle){

        this.peaksFile = fileName;
        this.targetPath = targetPath;
        this.resultID = ID;
        this.workflowTitle = wfTitle;

        peaksModifications = new HashMap<String, String>();
        xModifiers = new HashMap<String, XModifier>();
        xModifiersFound = new HashMap<String, XModifier>();

        peptides = new HashMap<Integer, String>();
        proteins = new HashMap<Integer, String>();
        peptidesCurrentId = 0;
        proteinsCurrentId = 0;

        //TODO: write more modifications here
        peaksModifications.put("S(+79.97)","Phosphorylation+S");
        peaksModifications.put("T(+79.97)","Phosphorylation+T");
        peaksModifications.put("Y(+79.97)","Phosphorylation+Y");
        peaksModifications.put("M(+15.99)","Oxidation+M");

        for (String key : peaksModifications.keySet()) {
            String md = peaksModifications.get(key);
            String aa = Character.toString(md.charAt(md.length()-1));
            int deltam_start = key.indexOf("(") + 1;
            int deltam_end = key.indexOf(")");
            float deltam = Float.parseFloat(key.substring(deltam_start, deltam_end));

            XModifier mod1 = new XModifier();

            mod1.setName(md);
            mod1.setStatus(XModifier.modStatus.VARIABLE);
            mod1.setApplies_to(aa);
            mod1.setDelta_mass(deltam);
            mod1.setMcat_reagent(false);
            mod1.setType("SIDECHAIN");

            xModifiers.put(md, mod1);
        }

        if(CAM_Cys){
            String md = "Carbamidomethyl+C";
            peaksModifications.put("C(+57.02)", md);
            XModifier mod1 = new XModifier();
            mod1.setName(md);
            mod1.setStatus(XModifier.modStatus.FIXED);
            mod1.setApplies_to("C");
            mod1.setDelta_mass(57.02F);
            mod1.setMcat_reagent(false);
            mod1.setType("SIDECHAIN");

            xModifiers.put(md, mod1);

        }

    }


    public void readPEAKSfile() throws IOException{

        System.out.println("reading PEAKS file...");

        //Create CSVReader object
        CSVReader reader = new CSVReader(new FileReader(peaksFile), ',');

        peaksRows = new ArrayList<Peaks>();


        //read all lines at once
        List<String[]> records = reader.readAll();

        Iterator<String[]> iterator = records.iterator();

        //skip header row
        iterator.next();

        // For DB search PSM.csv reports
/*        while (iterator.hasNext()){
            String[] record = iterator.next();
            Peaks peaksrow = new Peaks();
            peaksrow.setPeptide(record[0]);
            peaksrow.setLogP(record[1]);
            peaksrow.setMass( String.valueOf(Float.parseFloat(record[2]) + protonMass) );
            //Length(record[3]);
            peaksrow.setPpm(record[4]);
            peaksrow.setMz(record[5]);
            peaksrow.setRt(record[6]);
            //Area(record[7]);
            peaksrow.setScan(record[8]);
            peaksrow.setAccession(record[9]);
            peaksrow.setPtm(record[10]);
            //AScore(record[11]);
            peaksRows.add(peaksrow);
        }*/

        String pattern_start = "^.\\.";
        String pattern_end = "\\..$";

        while (iterator.hasNext()){
            String[] record = iterator.next();
            Peaks peaksrow = new Peaks();
            // If the sequence is reported enclosed in periods (pre-aa and post-aa), remove periods!
            String peptide = record[3].replaceAll(pattern_start,"");
            peptide = peptide.replaceAll(pattern_end,"");

            peaksrow.setPeptide(peptide);
            peaksrow.setLogP(record[5]);
            peaksrow.setMass( String.valueOf(Float.parseFloat(record[6]) + protonMass) );
            //Length(record[3]);
            peaksrow.setPpm(record[8]);
            peaksrow.setMz(record[9]);
            peaksrow.setRt(record[11]);
            //Area(record[7]);
            peaksrow.setScan(record[13]);
            peaksrow.setAccession(record[2]);
            peaksrow.setPtm(record[17]);
            //AScore(record[11]);
            peaksRows.add(peaksrow);
        }


        reader.close();
    }

    public void convertToPLGSFiles(){

        //Create new file structure within the selected project (targetPath)
        String peaksConvertedFolder = new File(targetPath, resultID).getAbsolutePath(); // targetPath + File.pathSeparator + resultID;
        String peaksConvertedWorkflowFolder = new File(peaksConvertedFolder, resultID + "_WorkflowResults").getAbsolutePath(); // peaksConvertedFolder + File.pathSeparator + resultID + "_Workflow";
        XFiles.mkDir(peaksConvertedWorkflowFolder);


        //Create PLGS XML objects
        String wf_ID =  workflowTitle;//UUID.randomUUID().toString();
        String wf_filename = wf_ID + ".xml";
        String wf_filepath = new File(peaksConvertedWorkflowFolder, wf_filename).getAbsolutePath(); // peaksConvertedWorkflowFolder + File.pathSeparator + wf_filename;
        String ms_filepath = new File(peaksConvertedFolder, "MassSpectrum.xml").getAbsolutePath();
        wf = new XWorkflow(wf_filepath, wf_ID, workflowTitle);
        wf.setTemplate_ref(resultID);

        massSpectrum = new XMassSpectrum(ms_filepath);

        XProcessingParameters procParams = new XProcessingParameters("QTOF-MSMS", true);

        massSpectrum.setProcessing_parameters(procParams);

        XFormat fm0 = new XFormat(XFormat.templates.DDA0);
        XFormat fm1 = new XFormat(XFormat.templates.DDA1);

        massSpectrum.addFormat(fm0);
        massSpectrum.addFormat(fm1);

        XMs ms = new XMs();

        LinkedList<String> dps = new LinkedList<>();

        ms.setDataCorrected(false);

        XResult result = new XResult(resultID);

        int scanIdx = 1;

        for (Peaks peaksRow : peaksRows) {

            String peptide = peaksRow.getPeptide();
            String sequence = peaksRow.getSequence();
            float score = Float.parseFloat(peaksRow.getLogP());
            float mass = Float.parseFloat(peaksRow.getMass());
            float ppm = Float.parseFloat(peaksRow.getPpm());
            float mz = Float.parseFloat(peaksRow.getMz());
            float rt = Float.parseFloat(peaksRow.getRt());
            String scan = peaksRow.getScan();
            String accession = this.getParsedAccession(peaksRow.getAccession());
            String entry = this.getParsedEntry(peaksRow.getAccession());
            int z = guessChargeState(mz, mass);
            HashMap<Integer, String> modifications = peaksRow.getModifications();

            /*<FORMAT FRAGMENTATION_LEVEL="0">
            Mass Intensity Charge RT Function StartScan StopScan ETD
            </FORMAT>*/

            String msrow = peaksRow.getMass() + " 1000 " + z + " " + rt + " 0 " + scanIdx + " " + scanIdx + " 0";
            dps.add(msrow);
            ms.addFragmentation(scanIdx, true);

            /*
            <PEPTIDE ID="6" MASS="1783.9257" SEQUENCE="FALPPGVLEHFETATR"/>
            <QUERY_MASS CHARGE="2.0" DISSOCIATION_MODE="0" DRIFT_TIME="-2.14748365E9" FRACTION="0" ID="1" MASS="785.8425" NUM_FRAC="0" RETENTION_TIME="0.04"/>
            <QUERY_MASS CHARGE="2.0" DISSOCIATION_MODE="0" DRIFT_TIME="-2.14748365E9" FRACTION="0" ID="59" MASS="476.7386" NUM_FRAC="0" RETENTION_TIME="21.37">
            <MASS_MATCH AUTO_QC="2" CURATED="2" ID="1" QUERY_ID="_14387863059000_8757508004142684" SCORE="78.0454" SECONDARY_SCORE="60.0"/>
            </QUERY_MASS>
            */

            //Add peptide only if not already added
            if(!peptides.containsValue(peptide)){
                peptidesCurrentId++;
                peptides.put(peptidesCurrentId, peptide);

                XPeptide pep1 = new XPeptide();
                pep1.setId(peptidesCurrentId);
                pep1.setMass(mass);
                pep1.setSequence(sequence);

                for (Integer modPosition : modifications.keySet()) {
                    String peaksModDescription = modifications.get(modPosition);
                    String mod = peaksModifications.get(peaksModDescription);
                    pep1.addMatchModifier(new XMatchModifier(mod, modPosition));

                    //Add the modification to the list of modifications present in the file
                    if(!xModifiersFound.containsKey(mod)){
                        xModifiersFound.put(mod, xModifiers.get(mod));
                    }
                }

                result.addPeptide(pep1);
            }

            //we add as many query masses (including its corresponding mass match) as peaks MS identifications we have
            XQueryMass qm1 = new XQueryMass();
            qm1.setId(scanIdx);
            qm1.setMass(mass);
            qm1.setCharge(z);
            qm1.setDissociation_mode(0);
            qm1.setDrift_time(-Float.MIN_VALUE);
            qm1.setFraction(0);
            qm1.setNum_frac(0);
            qm1.setRetention_time(rt); //In minutes!!
            XMassMatch mm1 = new XMassMatch(peptidesCurrentId);  //The ID here is the peptide ID!
            mm1.setAuto_qc(2);
            mm1.setCurated(2);
            mm1.setQuery_id(resultID);
            mm1.setScore(score);
            mm1.setSecondary_score(score - 1.0F);
            qm1.addMassMatch(mm1);
            result.addQueryMass(qm1);

            // We add a hit for each new protein
            if(!proteins.containsValue(accession)){
                proteinsCurrentId++;
                proteins.put(proteinsCurrentId, accession);

                XHit hit1 = new XHit();
                hit1.setScore(56.9F);
                hit1.setCurated(2);
                hit1.setAutoqc(2);
                hit1.setConfidence_level(0.993F);
                hit1.setCoverage(0.56F);
                hit1.setRms_mass_error_frag(0.0F);
                hit1.setRms_mass_error_prec(0.0F);
                hit1.setRms_rt_error_frag(0.0F);
                hit1.setVar_mod_matches(0);
                hit1.setAccession(accession);
                hit1.setEntry(entry);
                hit1.setDescription(accession + "|" + entry);
                hit1.setMw(766543.65F);
                hit1.setPi(7.5F);
                hit1.setSequence("THISISAFAKESEQUENCE");

                result.addHit(hit1);
            }

            //Add sequence matches to the hit
            XHit hit = result.getHit(accession);
            Random rnd = new Random();
            int seq_start = rnd.nextInt();
            //TODO: fill with proper values when we got the more complete PEAKS report
            if(!hit.getSequenceMatches().containsValue(peptidesCurrentId)){
                hit.addSequenceMatch(peptidesCurrentId, seq_start, seq_start + sequence.length());
            }


            scanIdx++;
        }

        //Add the data points array to the massSpectrum.MS object
        ms.addDataPoints(dps);

        massSpectrum.setMS(ms);

        wf.setMassspectrum(massSpectrum, false);

        XProteinLynxQuery plq = new XProteinLynxQuery();
        plq.setMassspectrum(massSpectrum, false);

        XDBsearchParameters dbparams = new XDBsearchParameters();
        dbparams.setDefaultSearchParameters();
        dbparams.clearModifiers();

        plq.setDbparameters(dbparams.getXMLElement());

        for (XModifier xModifier : xModifiersFound.values()) {
            result.addModifier(xModifier);
            dbparams.addModifier(xModifier);
        }

        plq.setResult(result);
        wf.setProteinlynx_query(plq.getXMLElement());


        wf.writeXML();
        massSpectrum.writeXML();

    }

    private int guessChargeState(float mz, float mass){
        int z = Math.round(mass / mz);

        return z;
    }

    private String getParsedAccession(String accession){
        // P14873|MAP1B_MOUSE
        // Returns the first feature

        String[] acc_split = accession.split("\\|");
        String parsedAccession = accession;

        for(String mystr : acc_split){
            if(mystr.contains(":")){
                mystr = mystr.split(":")[1];
            }
            if(mystr.contains("_")){
                parsedAccession = mystr;
            }
        }

        return parsedAccession;
    }

    private String getParsedEntry(String accession){
        // P14873|MAP1B_MOUSE
        // Returns the second feature

        String[] acc_split = accession.split("\\|");
        String parsedEntry = accession;

        for(String mystr : acc_split){
            if(mystr.contains(":")){
                mystr = mystr.split(":")[1];
            }
            if(mystr.contains("_")){
                parsedEntry = mystr;
            }
        }

        return parsedEntry;
    }

    public static void main(String[] args) throws IOException{

        String peaksfile = "/Users/napedro/tmp_data/2015-036/PEAKS_exports/2015-036-01/2015-036-01_short.csv";


        String[] peaksfiles = new String[6];
        peaksfiles[0] = "/Users/napedro/tmp_data/2016-010 PEAKS_DDA_originalPEAKSsearches/2016-010_A1_S160201_08/protein-peptides_filtered.csv";
        peaksfiles[1] = "/Users/napedro/tmp_data/2016-010 PEAKS_DDA_originalPEAKSsearches/2016-010_A2_S160201_10/protein-peptides_filtered.csv";
        peaksfiles[2] = "/Users/napedro/tmp_data/2016-010 PEAKS_DDA_originalPEAKSsearches/2016-010_A3_S160201_12/protein-peptides_filtered.csv";
        peaksfiles[3] = "/Users/napedro/tmp_data/2016-010 PEAKS_DDA_originalPEAKSsearches/2016-010_B1_S160201_09/protein-peptides_filtered.csv";
        peaksfiles[4] = "/Users/napedro/tmp_data/2016-010 PEAKS_DDA_originalPEAKSsearches/2016-010_B2_S160201_11/protein-peptides_filtered.csv";
        peaksfiles[5] = "/Users/napedro/tmp_data/2016-010 PEAKS_DDA_originalPEAKSsearches/2016-010_B3_S160201_13/protein-peptides_filtered.csv";

        peaksfile = "/Users/napedro/tmp_data/2016-010 PEAKS_DDA_originalPEAKSsearches/2016-010_B3_S160201_13/DB search psm.csv";


        PeaksToPLGSconverter peaksConverter = new PeaksToPLGSconverter(peaksfiles[0], "PeaksConvertedFiles", "PEAKS_DDA_CONSENSUS_A1", true, "S160201_08");
        peaksConverter.readPEAKSfile();
        peaksConverter.convertToPLGSFiles();

        peaksConverter = new PeaksToPLGSconverter(peaksfiles[1], "PeaksConvertedFiles", "PEAKS_DDA_CONSENSUS_A2", true, "S160201_10");
        peaksConverter.readPEAKSfile();
        peaksConverter.convertToPLGSFiles();

        peaksConverter = new PeaksToPLGSconverter(peaksfiles[2], "PeaksConvertedFiles", "PEAKS_DDA_CONSENSUS_A3", true, "S160201_12");
        peaksConverter.readPEAKSfile();
        peaksConverter.convertToPLGSFiles();

        peaksConverter = new PeaksToPLGSconverter(peaksfiles[3], "PeaksConvertedFiles", "PEAKS_DDA_CONSENSUS_B1", true, "S160201_09");
        peaksConverter.readPEAKSfile();
        peaksConverter.convertToPLGSFiles();

        peaksConverter = new PeaksToPLGSconverter(peaksfiles[4], "PeaksConvertedFiles", "PEAKS_DDA_CONSENSUS_B2", true, "S160201_11");
        peaksConverter.readPEAKSfile();
        peaksConverter.convertToPLGSFiles();

        peaksConverter = new PeaksToPLGSconverter(peaksfiles[5], "PeaksConvertedFiles", "PEAKS_DDA_CONSENSUS_B3", true, "S160201_13");
        peaksConverter.readPEAKSfile();
        peaksConverter.convertToPLGSFiles();

/*        String[] peaksfiles = new String[4];
        peaksfiles[0] = "/Users/napedro/tmp_data/2016-055 Phospho IMAC DDA_PEAKS_originalSearches/T160506_37/protein-peptides.csv";
        peaksfiles[1] = "/Users/napedro/tmp_data/2016-055 Phospho IMAC DDA_PEAKS_originalSearches/T160506_39/protein-peptides.csv";
        peaksfiles[2] = "/Users/napedro/tmp_data/2016-055 Phospho IMAC DDA_PEAKS_originalSearches/T160506_41/protein-peptides.csv";
        peaksfiles[3] = "/Users/napedro/tmp_data/2016-055 Phospho IMAC DDA_PEAKS_originalSearches/206-055-03_all/protein-peptides.csv";


        PeaksToPLGSconverter peaksConverter = new PeaksToPLGSconverter(peaksfiles[0], "PeaksConvertedFiles", "PEAKS_DDA_consensus_Phospho_1", true, "T160506_37");
        peaksConverter.readPEAKSfile();
        peaksConverter.convertToPLGSFiles();

        peaksConverter = new PeaksToPLGSconverter(peaksfiles[1], "PeaksConvertedFiles", "PEAKS_DDA_consensus_Phospho_2", true, "T160506_39");
        peaksConverter.readPEAKSfile();
        peaksConverter.convertToPLGSFiles();

        peaksConverter = new PeaksToPLGSconverter(peaksfiles[2], "PeaksConvertedFiles", "PEAKS_DDA_consensus_Phospho_3", true, "T160506_41");
        peaksConverter.readPEAKSfile();
        peaksConverter.convertToPLGSFiles();

        peaksConverter = new PeaksToPLGSconverter(peaksfiles[3], "PeaksConvertedFiles", "PEAKS_DDA_consensus_Phospho_all", true, "206-055-03_all");
        peaksConverter.readPEAKSfile();
        peaksConverter.convertToPLGSFiles();*/


        /*String[] peaksfiles = new String[3];
        peaksfiles[0] = "/Users/napedro/tmp_data/Ligandome_PEAKS_originalSearches/2016-038-01_T160330_10/protein-peptides.csv";
        peaksfiles[1] = "/Users/napedro/tmp_data/Ligandome_PEAKS_originalSearches/2016-038-01_T160402_01/protein-peptides.csv";
        peaksfiles[2] = "/Users/napedro/tmp_data/Ligandome_PEAKS_originalSearches/2016-038-01_T160429_02/protein-peptides.csv";


        PeaksToPLGSconverter peaksConverter = new PeaksToPLGSconverter(peaksfiles[0], "PeaksConvertedFiles", "PEAKS_DDA_MHC_Control_1", true, "T160330_10");
        peaksConverter.readPEAKSfile();
        peaksConverter.convertToPLGSFiles();

        peaksConverter = new PeaksToPLGSconverter(peaksfiles[1], "PeaksConvertedFiles", "PEAKS_DDA_MHC_Control_2", true, "T160402_01");
        peaksConverter.readPEAKSfile();
        peaksConverter.convertToPLGSFiles();

        peaksConverter = new PeaksToPLGSconverter(peaksfiles[2], "PeaksConvertedFiles", "PEAKS_DDA_MHC_Control_3", true, "T160429_02");
        peaksConverter.readPEAKSfile();
        peaksConverter.convertToPLGSFiles();
*/



//        for (Peaks peaksRow : peaksConverter.peaksRows) {
//            System.out.println(peaksRow.toString());
//        }


    }

}
