package isoquant.plugins.plgs.fileconverting.xml.workflow.internal;

import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by napedro on 25/01/16.
 */
public class XDBsearchParameters {
   /*
    <DATABANK_SEARCH_QUERY_PARAMETERS>
        <SEARCH_ENGINE_TYPE VALUE="PLGS"/>
        <DISSOCIATION_MODE VALUE="CID"/>
        <SEARCH_DATABASE NAME="Deacpoda_REVERSE-1.0"/>
        <ANALYSIS_DIGESTOR MISSED_CLEAVAGES="2">
            <AMINO_ACID_SEQUENCE_DIGESTOR NAME="Asp-N" UUID="05b0e77f-8a94-44ec-9e57-3b47c8ba57ff">
                <CLEAVES_AT AMINO_ACID="D" POSITION="N-TERM">
                    <EXCLUDES AMINO_ACID="S" POSITION="C-TERM"/>
                </CLEAVES_AT>
            </AMINO_ACID_SEQUENCE_DIGESTOR>
        </ANALYSIS_DIGESTOR>
        <SEARCH_TYPE NAME="MSMS"/>
        <PEP_MASS_TOL UNIT="ppm">30</PEP_MASS_TOL>
        <PEP_FRAG_TOL UNIT="Da">0.05</PEP_FRAG_TOL>
        <MAX_HITS_TO_RETURN>20</MAX_HITS_TO_RETURN>
        <MIN_MASS_STANDARD_DEVIATION UNIT="Da">0.005</MIN_MASS_STANDARD_DEVIATION>
        <MIN_PEPS_TO_MATCH_PROTEIN>2</MIN_PEPS_TO_MATCH_PROTEIN>
        <PROTEIN_MW FROM="0" TO="2000000"/>
        <PROTEIN_PI FROM="0" TO="14"/>
        <ANALYSIS_MODIFIER STATUS="FIXED">
            <MODIFIER MCAT_REAGENT="No" NAME="Carbamidomethyl+C">
                <MODIFIES APPLIES_TO="C" DELTA_MASS="57.0215" TYPE="SIDECHAIN"/>
            </MODIFIER>
        </ANALYSIS_MODIFIER>
        <ANALYSIS_MODIFIER STATUS="VARIABLE">
            <MODIFIER MCAT_REAGENT="No" NAME="Deamidation+N">
                <MODIFIES APPLIES_TO="N" DELTA_MASS=".9840" TYPE="SIDECHAIN"/>
            </MODIFIER>
        </ANALYSIS_MODIFIER>
        <ANALYSIS_MODIFIER STATUS="VARIABLE">
            <MODIFIER MCAT_REAGENT="No" NAME="Deamidation+Q">
                <MODIFIES APPLIES_TO="Q" DELTA_MASS=".9840" TYPE="SIDECHAIN"/>
            </MODIFIER>
        </ANALYSIS_MODIFIER>
        <ANALYSIS_MODIFIER STATUS="VARIABLE">
            <MODIFIER MCAT_REAGENT="No" NAME="Oxidation+M">
                <MODIFIES APPLIES_TO="M" DELTA_MASS="15.9949" TYPE="SIDECHAIN"/>
            </MODIFIER>
        </ANALYSIS_MODIFIER>
        <VALIDATE_RESULTS VALIDATE="true"/>
        <ESTIMATED_PROT_SAMPLE>1000</ESTIMATED_PROT_SAMPLE>
        <ESTIMATED_PROT_PROTEOME>10000</ESTIMATED_PROT_PROTEOME>
        <MIN_CONFIDENCE>0.0</MIN_CONFIDENCE>
    </DATABANK_SEARCH_QUERY_PARAMETERS>
    * */

    protected List<XModifier> modifiers;

    protected String searchEngineType;
    protected String dissociationMode;
    protected String searchDBName;
    protected String searchType;
    protected float pepMassTol;
    protected massUnit pepMassTolUnit;
    protected float pepFragTol;
    protected massUnit pepFragTolUnit;
    protected int maxHitsReturn;
    protected float minMassSD;
    protected massUnit minMassSDUnit;
    protected int minPepsProtein;
    protected float minProteinMW;
    protected float maxProteinMW;
    protected float minProteinPI;
    protected float maxProteinPI;
    protected boolean validateResults;
    protected float estimatedProtSample;
    protected float estimatedProtProteome;
    protected float minConfidence;

    protected int missedCleavages;
    protected Enzyme enzyme;

    public class Enzyme {

        protected String name;
        protected String uuid;

        protected List<Cleave> cleavesat;
        protected List<Cleave> exclude;

        public Enzyme(String name, String uuid){
            this.name = name;
            this.uuid = uuid;
            cleavesat = new ArrayList<Cleave>();
            exclude = new ArrayList<Cleave>();
        }

        public void addCleave(String aminoacid, String position){
            Cleave cleave = new Cleave(aminoacid, position);
            cleavesat.add(cleave);
        }

        public void addExclude(String aminoacid, String position){
            Cleave cleave = new Cleave(aminoacid, position);
            exclude.add(cleave);
        }



        public class Cleave{
            protected String aminoacid;
            protected String position;

            public Cleave(String aminoacid, String position){
                this.aminoacid = aminoacid;
                this.position = position;
            }
        }

    }

    public enum digestorEnzime {Trypsin, Chymotrypsin, LysC, LysN, AspN};

    public enum massUnit { ppm, Da};

    public XDBsearchParameters(){
        modifiers = new ArrayList<XModifier>();
    }

    public List<XModifier> getModifiers() {
        return modifiers;
    }

    public void clearModifiers(){
        modifiers.clear();
    }

    public void setDigestionEnzyme(Enzyme de){
        this.enzyme = de;
    }

    public void setDigestionEnzyme(digestorEnzime de){

        UUID idOne = UUID.randomUUID();

        Enzyme enzyme1 = new Enzyme("Trypsin", idOne.toString());

        if(de == digestorEnzime.Trypsin){
            enzyme1 = new Enzyme("Trypsin", idOne.toString());
            enzyme1.addCleave("R", "C-TERM");
            enzyme1.addCleave("K", "C-TERM");
            enzyme1.addExclude("P", "N-TERM");
        }

        if(de == digestorEnzime.AspN){
            enzyme1 = new Enzyme("AspN", idOne.toString());
            enzyme1.addCleave("D", "N-TERM");
            enzyme1.addExclude("S", "C-TERM");
        }

        if(de == digestorEnzime.Chymotrypsin){
            enzyme1 = new Enzyme("Chymotrypsin", idOne.toString());
            enzyme1.addCleave("F", "C-TERM");
            enzyme1.addCleave("W", "C-TERM");
            enzyme1.addCleave("Y", "C-TERM");
            enzyme1.addExclude("P", "N-TERM");
        }
        if(de == digestorEnzime.LysC){
            enzyme1 = new Enzyme("LysC", idOne.toString());
            enzyme1.addCleave("K", "C-TERM");
            enzyme1.addExclude("P", "N-TERM");
        }
        if(de == digestorEnzime.LysN){
            enzyme1 = new Enzyme("LysC", idOne.toString());
            enzyme1.addCleave("K", "N-TERM");
            enzyme1.addExclude("K", "N-TERM");
        }

        this.setDigestionEnzyme(enzyme1);
    }


    public void setSearchEngineType(String searchEngineType) {
        this.searchEngineType = searchEngineType;
    }

    public void setDissociationMode(String dissociationMode) {
        this.dissociationMode = dissociationMode;
    }

    public void setSearchDBName(String searchDBName) {
        this.searchDBName = searchDBName;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public void setPepMassTol(float pepMassTol) {
        this.pepMassTol = pepMassTol;
    }

    public void setPepMassTolUnit(massUnit pepMassTolUnit) {
        this.pepMassTolUnit = pepMassTolUnit;
    }

    public void setPepFragTol(float pepFragTol) {
        this.pepFragTol = pepFragTol;
    }

    public void setPepFragTolUnit(massUnit pepFragTolUnit) {
        this.pepFragTolUnit = pepFragTolUnit;
    }

    public void setMaxHitsReturn(int maxHitsReturn) {
        this.maxHitsReturn = maxHitsReturn;
    }

    public void setMinMassSD(float minMassSD) {
        this.minMassSD = minMassSD;
    }

    public void setMinMassSDUnit(massUnit minMassSDUnit) {
        this.minMassSDUnit = minMassSDUnit;
    }

    public void setMinPepsProtein(int minPepsProtein) {
        this.minPepsProtein = minPepsProtein;
    }

    public void setMinProteinMW(float minProteinMW) {
        this.minProteinMW = minProteinMW;
    }

    public void setMaxProteinMW(float maxProteinMW) {
        this.maxProteinMW = maxProteinMW;
    }

    public void setMinProteinPI(float minProteinPI) {
        this.minProteinPI = minProteinPI;
    }

    public void setMaxProteinPI(float maxProteinPI) {
        this.maxProteinPI = maxProteinPI;
    }

    public void setValidateResults(boolean validateResults) {
        this.validateResults = validateResults;
    }

    public void setEstimatedProtSample(float estimatedProtSample) {
        this.estimatedProtSample = estimatedProtSample;
    }

    public void setEstimatedProtProteome(float estimatedProtProteome) {
        this.estimatedProtProteome = estimatedProtProteome;
    }

    public void setMinConfidence(float minConfidence) {
        this.minConfidence = minConfidence;
    }

    public void setMissedCleavages(int missedCleavages) {
        this.missedCleavages = missedCleavages;
    }

    public void addModifier(XModifier mod){
        this.modifiers.add(mod);
    }

    public void setDefaultSearchParameters(){
        // If parameters are not available, we can use mock parameters. We interpret a classic proteomics experiment,
        // with Trypsin, etc...

           /*
    <DATABANK_SEARCH_QUERY_PARAMETERS>
        <SEARCH_ENGINE_TYPE VALUE="PLGS"/>
        <DISSOCIATION_MODE VALUE="CID"/>
        <SEARCH_DATABASE NAME="Deacpoda_REVERSE-1.0"/>
        <ANALYSIS_DIGESTOR MISSED_CLEAVAGES="2">
            <AMINO_ACID_SEQUENCE_DIGESTOR NAME="Asp-N" UUID="05b0e77f-8a94-44ec-9e57-3b47c8ba57ff">
                <CLEAVES_AT AMINO_ACID="D" POSITION="N-TERM">
                    <EXCLUDES AMINO_ACID="S" POSITION="C-TERM"/>
                </CLEAVES_AT>
            </AMINO_ACID_SEQUENCE_DIGESTOR>
        </ANALYSIS_DIGESTOR>
        <SEARCH_TYPE NAME="MSMS"/>
        <PEP_MASS_TOL UNIT="ppm">30</PEP_MASS_TOL>
        <PEP_FRAG_TOL UNIT="Da">0.05</PEP_FRAG_TOL>
        <MAX_HITS_TO_RETURN>20</MAX_HITS_TO_RETURN>
        <MIN_MASS_STANDARD_DEVIATION UNIT="Da">0.005</MIN_MASS_STANDARD_DEVIATION>
        <MIN_PEPS_TO_MATCH_PROTEIN>2</MIN_PEPS_TO_MATCH_PROTEIN>
        <PROTEIN_MW FROM="0" TO="2000000"/>
        <PROTEIN_PI FROM="0" TO="14"/>
        <ANALYSIS_MODIFIER STATUS="FIXED">
            <MODIFIER MCAT_REAGENT="No" NAME="Carbamidomethyl+C">
                <MODIFIES APPLIES_TO="C" DELTA_MASS="57.0215" TYPE="SIDECHAIN"/>
            </MODIFIER>
        </ANALYSIS_MODIFIER>
        <ANALYSIS_MODIFIER STATUS="VARIABLE">
            <MODIFIER MCAT_REAGENT="No" NAME="Deamidation+N">
                <MODIFIES APPLIES_TO="N" DELTA_MASS=".9840" TYPE="SIDECHAIN"/>
            </MODIFIER>
        </ANALYSIS_MODIFIER>
        <ANALYSIS_MODIFIER STATUS="VARIABLE">
            <MODIFIER MCAT_REAGENT="No" NAME="Deamidation+Q">
                <MODIFIES APPLIES_TO="Q" DELTA_MASS=".9840" TYPE="SIDECHAIN"/>
            </MODIFIER>
        </ANALYSIS_MODIFIER>
        <ANALYSIS_MODIFIER STATUS="VARIABLE">
            <MODIFIER MCAT_REAGENT="No" NAME="Oxidation+M">
                <MODIFIES APPLIES_TO="M" DELTA_MASS="15.9949" TYPE="SIDECHAIN"/>
            </MODIFIER>
        </ANALYSIS_MODIFIER>
        <VALIDATE_RESULTS VALIDATE="true"/>
        <ESTIMATED_PROT_SAMPLE>1000</ESTIMATED_PROT_SAMPLE>
        <ESTIMATED_PROT_PROTEOME>10000</ESTIMATED_PROT_PROTEOME>
        <MIN_CONFIDENCE>0.0</MIN_CONFIDENCE>
    </DATABANK_SEARCH_QUERY_PARAMETERS>
    * */

        setDigestionEnzyme(digestorEnzime.Trypsin);
        setSearchEngineType("PEAKS");
        setDissociationMode("CID");
        setSearchDBName("database_1.0.0");
        setMissedCleavages(2);
        setSearchType("MSMS");
        setPepMassTol(30.0F);
        setPepMassTolUnit(massUnit.ppm);
        setPepFragTol(0.05F);
        setPepFragTolUnit(massUnit.Da);
        setMaxHitsReturn(20);
        setMinMassSD(0.005F);
        setMinMassSDUnit(massUnit.Da);
        setMinPepsProtein(2);
        setMinProteinMW(0.0F);
        setMaxProteinMW(2000000F);
        setMinProteinPI(0.0F);
        setMaxProteinPI(14.0F);

        // modifiers

        XModifier mod1 = new XModifier();
        mod1.setStatus(XModifier.modStatus.FIXED);
        mod1.setName("Carbamidomethyl+C");
        mod1.setApplies_to("C");
        mod1.setDelta_mass(57.0215F);
        mod1.setMcat_reagent(false);
        mod1.setType("SIDECHAIN");

        XModifier mod2 = new XModifier();
        mod2.setStatus(XModifier.modStatus.VARIABLE);
        mod2.setName("Oxidation+M");
        mod2.setApplies_to("C");
        mod2.setDelta_mass(57.0215F);
        mod2.setMcat_reagent(false);
        mod2.setType("SIDECHAIN");

        addModifier(mod1);
        addModifier(mod2);

        setValidateResults(true);
        setEstimatedProtSample(1000.0F);
        setEstimatedProtProteome(10000.0F);
        setMinConfidence(0.0F);

    }

    public Element getXMLElement(){

        //<DATABANK_SEARCH_QUERY_PARAMETERS>

        Element dbsp = new Element("DATABANK_SEARCH_QUERY_PARAMETERS");

        //<SEARCH_ENGINE_TYPE VALUE="PLGS"/>
        Element set = new Element("SEARCH_ENGINE_TYPE");
        set.setAttribute("VALUE", searchEngineType);
        dbsp.addContent(set);

//        <DISSOCIATION_MODE VALUE="CID"/>
        Element dsm = new Element("DISSOCIATION_MODE");
        dsm.setAttribute("VALUE", dissociationMode);
        dbsp.addContent(dsm);

//        <SEARCH_DATABASE NAME="Deacpoda_REVERSE-1.0"/>
        Element dbn = new Element("SEARCH_DATABASE");
        dbn.setAttribute("NAME", searchDBName);
        dbsp.addContent(dbn);

//        <ANALYSIS_DIGESTOR MISSED_CLEAVAGES="2">
        Element andg = new Element("ANALYSIS_DIGESTOR");
        andg.setAttribute("MISSED_CLEAVAGES", Integer.toString(missedCleavages));
//          <AMINO_ACID_SEQUENCE_DIGESTOR NAME="Asp-N" UUID="05b0e77f-8a94-44ec-9e57-3b47c8ba57ff">
        Element aasd = new Element("AMINO_ACID_SEQUENCE_DIGESTOR");
        aasd.setAttribute("NAME", enzyme.name);
        aasd.setAttribute("UUID",enzyme.uuid);
//              <CLEAVES_AT AMINO_ACID="D" POSITION="N-TERM">
        for (Enzyme.Cleave cleave : enzyme.cleavesat) {
            Element clv = new Element("CLEAVES_AT");
            clv.setAttribute("AMINO_ACID", cleave.aminoacid);
            clv.setAttribute("POSITION", cleave.position);

            //TODO: so far we consider that all exclusions are the same for each cleavage, which is not true.
//                  <EXCLUDES AMINO_ACID="S" POSITION="C-TERM"/>
            for (Enzyme.Cleave exclude : enzyme.exclude) {
                Element excl = new Element("EXCLUDES");
                excl.setAttribute("AMINO_ACID",exclude.aminoacid);
                excl.setAttribute("POSITION", exclude.position);
                clv.addContent(excl);
            }
//              </CLEAVES_AT>
            aasd.addContent(clv);
        }
//          </AMINO_ACID_SEQUENCE_DIGESTOR>
        andg.addContent(aasd);
//        </ANALYSIS_DIGESTOR>
        dbsp.addContent(andg);

//        <SEARCH_TYPE NAME="MSMS"/>
        Element st = new Element("SEARCH_TYPE");
        st.setAttribute("NAME", searchType);
        dbsp.addContent(st);

//        <PEP_MASS_TOL UNIT="ppm">30</PEP_MASS_TOL>
        Element pmt = new Element("PEP_MASS_TOL");
        pmt.setAttribute("UNIT", pepMassTolUnit.toString());
        pmt.setText(Float.toString(pepMassTol));
        dbsp.addContent(pmt);

//        <PEP_FRAG_TOL UNIT="Da">0.05</PEP_FRAG_TOL>
        Element pft = new Element("PEP_FRAG_TOL");
        pft.setAttribute("UNIT", pepFragTolUnit.toString());
        pft.setText(Float.toString(pepFragTol));
        dbsp.addContent(pft);

//        <MAX_HITS_TO_RETURN>20</MAX_HITS_TO_RETURN>
        Element mhr = new Element("MAX_HITS_TO_RETURN");
        mhr.setText(Integer.toString(maxHitsReturn));
        dbsp.addContent(mhr);

//        <MIN_MASS_STANDARD_DEVIATION UNIT="Da">0.005</MIN_MASS_STANDARD_DEVIATION>
        Element minMSD = new Element("MIN_MASS_STANDARD_DEVIATION");
        minMSD.setAttribute("UNIT", minMassSDUnit.toString());
        minMSD.setText(Float.toString(minMassSD));
        dbsp.addContent(minMSD);

//        <MIN_PEPS_TO_MATCH_PROTEIN>2</MIN_PEPS_TO_MATCH_PROTEIN>
        Element mptmp = new Element("MIN_PEPS_TO_MATCH_PROTEIN");
        mptmp.setText(Integer.toString(minPepsProtein));
        dbsp.addContent(mptmp);

//        <PROTEIN_MW FROM="0" TO="2000000"/>
        Element pmw = new Element("PROTEIN_MW");
        pmw.setAttribute("FROM", Float.toString(minProteinMW));
        pmw.setAttribute("TO", Float.toString(maxProteinMW));
        dbsp.addContent(pmw);

//        <PROTEIN_PI FROM="0" TO="14"/>
        Element ppi = new Element("PROTEIN_PI");
        ppi.setAttribute("FROM", Float.toString(minProteinPI));
        ppi.setAttribute("TO", Float.toString(maxProteinPI));
        dbsp.addContent(ppi);

//        <ANALYSIS_MODIFIER STATUS="FIXED">
//        <MODIFIER MCAT_REAGENT="No" NAME="Carbamidomethyl+C">
//        <MODIFIES APPLIES_TO="C" DELTA_MASS="57.0215" TYPE="SIDECHAIN"/>
//        </MODIFIER>
//        </ANALYSIS_MODIFIER>
//        <ANALYSIS_MODIFIER STATUS="VARIABLE">
//        <MODIFIER MCAT_REAGENT="No" NAME="Deamidation+N">
//        <MODIFIES APPLIES_TO="N" DELTA_MASS=".9840" TYPE="SIDECHAIN"/>
//        </MODIFIER>
//        </ANALYSIS_MODIFIER>
//        <ANALYSIS_MODIFIER STATUS="VARIABLE">
//        <MODIFIER MCAT_REAGENT="No" NAME="Deamidation+Q">
//        <MODIFIES APPLIES_TO="Q" DELTA_MASS=".9840" TYPE="SIDECHAIN"/>
//        </MODIFIER>
//        </ANALYSIS_MODIFIER>
//        <ANALYSIS_MODIFIER STATUS="VARIABLE">
//        <MODIFIER MCAT_REAGENT="No" NAME="Oxidation+M">
//        <MODIFIES APPLIES_TO="M" DELTA_MASS="15.9949" TYPE="SIDECHAIN"/>
//        </MODIFIER>
//        </ANALYSIS_MODIFIER>

        for (XModifier modifier : modifiers) {
            Element mod = modifier.getXMLElement(false);
            dbsp.addContent(mod);
        }

//        <VALIDATE_RESULTS VALIDATE="true"/>
        Element valres = new Element("VALIDATE_RESULTS");
        valres.setAttribute("VALIDATE", validateResults ? "true" : "false");
        dbsp.addContent(valres);

//        <ESTIMATED_PROT_SAMPLE>1000</ESTIMATED_PROT_SAMPLE>
        Element eps = new Element("ESTIMATED_PROT_SAMPLE");
        eps.setText(Float.toString(estimatedProtSample));
        dbsp.addContent(eps);

//        <ESTIMATED_PROT_PROTEOME>10000</ESTIMATED_PROT_PROTEOME>
        Element epp = new Element("ESTIMATED_PROT_PROTEOME");
        epp.setText(Float.toString(estimatedProtProteome));
        dbsp.addContent(epp);

//        <MIN_CONFIDENCE>0.0</MIN_CONFIDENCE>
        Element mc = new Element("MIN_CONFIDENCE");
        mc.setText(Float.toString(minConfidence));
        dbsp.addContent(mc);

//        </DATABANK_SEARCH_QUERY_PARAMETERS>

        return dbsp;
    }
}
