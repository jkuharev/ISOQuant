package isoquant.plugins.plgs.fileconverting.xml.workflow.internal;

import org.jdom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by napedro on 25/01/16.
 */
public class XHit {
    /* TODO: we interpret that hits contain only one protein tag (which is true for DDA data). In DIA it is
     possible to have multiple proteins within a hit tag, but we don't see the reason they are grouped together.
     In the future we should create separate classes for HIT and PROTEIN, so that a HIT may contain multiple PROTEIN
     tags.
     */

    /*
    <HIT>
        <PROTEIN AUTO_QC="2" CONFIDENCE_LEVEL="1" COVERAGE="9.8336" CURATED="2" RMS_MASS_ERROR_FRAG="0.0" RMS_MASS_ERROR_PREC="0.0" RMS_RT_ERROR_FRAG="0.0" SCORE="11.5627" VAR_MOD_MATCHES="0">
            <ENTRY>MAR99991.1</ENTRY>
            <ACCESSION>MAR99991.1</ACCESSION>
            <DESCRIPTION>Carcinus_maenas_Hc1_AA</DESCRIPTION>
            <MW>75649.9609</MW>
            <PI>5.436</PI>
            <SEQUENCE>MKWAAVAAILLVAAVAEGTDLAHKQQAVNRLLYRIYSPIPSAFAALKELSLSFDPRAHTGDCHDGGNAVNHLMAELDDERLLEQEHWFSLFNTRQREEALMLVDVLLNCQTFETFVGNAAFFRERMNEGEFVYAFYVAVTHSTLMHDVVLPPLYEVTPHMFTNAEVIDKAYAAKMVQRPGNFQMTFTGSKKNPEQRIAYFGEDIGMNSHHVHWHMDFPFWWHGDQIDRKGELFFWAHHQLTARFDAERLSNYLPLVDELYWDRPIKEGFAPHTSYKYGGEFPTRPDNKNFEDVDGVARIRDMKEMESRIRDAIAHGYVDKTDGSHINIDNDNGINVLGDAIESSTSSVNPAYYGALHNQAHRVLGSQADPHGKFNMPPGVMEHFETATRDPSFFRLHKYMDSIFKEHKDKLAPYTANELKYENVEITDIDVDELSTFFEDFEFDLGNALDTTENVNDVDVHATVSRLNHKPFHYNIHYHADHAEKVSVRVYLTPVRDQNGIKMDIDENRWGAILIDNFWTEVEAGTHNVRRSSFDSTVTIPDRTCFSDLMKEADDAVANSKELSMTISRSCGHPHNLLLPKGNKEGLEFWLNVHVTSGADAAHDDLHTNDYASNYGYCGIQGKEYPDKRPMGYPFERRIPDIRVIKSLPNFFGKVVHVYHK</SEQUENCE>
            <SEQUENCE_MATCH END="76" ID="9" START="64"/>
            <SEQUENCE_MATCH END="223" ID="11" START="216"/>
            <SEQUENCE_MATCH END="256" ID="10" START="245"/>
            <SEQUENCE_MATCH END="408" ID="2" START="401"/>
            <SEQUENCE_MATCH END="503" ID="5" START="497"/>
            <SEQUENCE_MATCH END="503" ID="1" START="497"/>
            <SEQUENCE_MATCH END="626" ID="8" START="610"/>
        </PROTEIN>
    </HIT>
    */

    protected String entry;
    protected String accession;
    protected String description;
    protected float mw;
    protected float pi;
    protected String sequence;
    protected int autoqc;
    protected float confidence_level;
    protected float coverage;
    protected int curated;
    protected float rms_mass_error_frag;
    protected float rms_mass_error_prec;
    protected float rms_rt_error_frag;
    protected float score;
    protected int var_mod_matches;

    protected HashMap<Integer, XSequenceMatch> sequenceMatches;

    // An accession index to check whether a sequence has been already introduced or not, when multiple matches to the
    // same sequence are possible (i.e. modified/unmodified peptides).
    public List<String> accession_indices; //This index is a string containing: START_END values. Example: 64_76


    public XHit(){
        sequenceMatches = new HashMap<Integer, XSequenceMatch>();
        accession_indices = new ArrayList<String>();

    }

    public void addSequenceMatch(int id, int start, int end){
        XSequenceMatch sequenceMatch = new XSequenceMatch();
        sequenceMatch.setId(id);
        sequenceMatch.setStart(start);
        sequenceMatch.setEnd(end);
        sequenceMatches.put(id, sequenceMatch);
    }

    public HashMap<Integer, XSequenceMatch> getSequenceMatches() {
        return sequenceMatches;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public String getEntry() {
        return entry;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMw(float mw) {
        this.mw = mw;
    }

    public void setPi(float pi) {
        this.pi = pi;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public void setAutoqc(int autoqc) {
        this.autoqc = autoqc;
    }

    public void setConfidence_level(float confidence_level) {
        this.confidence_level = confidence_level;
    }

    public void setCoverage(float coverage) {
        this.coverage = coverage;
    }

    public void setCurated(int curated) {
        this.curated = curated;
    }

    public void setRms_mass_error_frag(float rms_mass_error_frag) {
        this.rms_mass_error_frag = rms_mass_error_frag;
    }

    public void setRms_mass_error_prec(float rms_mass_error_prec) {
        this.rms_mass_error_prec = rms_mass_error_prec;
    }

    public void setRms_rt_error_frag(float rms_rt_error_frag) {
        this.rms_rt_error_frag = rms_rt_error_frag;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public void setVar_mod_matches(int var_mod_matches) {
        this.var_mod_matches = var_mod_matches;
    }

    public Element getXMLElement(){
        Element hit = new Element("HIT");

        Element protein = new Element("PROTEIN");
        protein.setAttribute("AUTO_QC",Integer.toString(this.autoqc));
        protein.setAttribute("CONFIDENCE_LEVEL",Float.toString(confidence_level));
        protein.setAttribute("COVERAGE",Float.toString(coverage));
        protein.setAttribute("CURATED", Integer.toString(curated));
        protein.setAttribute("RMS_MASS_ERROR_FRAG", Float.toString(rms_mass_error_frag));
        protein.setAttribute("RMS_MASS_ERROR_PREC", Float.toString(rms_mass_error_prec));
        protein.setAttribute("RMS_RT_ERROR_FRAG", Float.toString(rms_rt_error_frag));
        protein.setAttribute("SCORE", Float.toString(score));
        protein.setAttribute("VAR_MOD_MATCHES", Integer.toString(var_mod_matches));

        Element Eentry = new Element("ENTRY");
        Eentry.setText(this.entry);
        protein.addContent(Eentry);
        Element Eaccession = new Element("ACCESSION");
        Eaccession.setText(this.accession);
        protein.addContent(Eaccession);
        Element Edescription = new Element("DESCRIPTION");
        Edescription.setText(this.description);
        protein.addContent(Edescription);
        Element Emw = new Element("MW");
        Emw.setText(Float.toString(this.mw));
        protein.addContent(Emw);
        Element Epi = new Element("PI");
        Epi.setText(Float.toString(this.pi));
        protein.addContent(Epi);
        Element Esequence = new Element("SEQUENCE");
        Esequence.setText(this.sequence);
        protein.addContent(Esequence);

        for (XSequenceMatch sequenceMatch : sequenceMatches.values()) {
            protein.addContent(sequenceMatch.getXMLElement());
        }

        hit.addContent(protein);

        return hit;
    }
}
