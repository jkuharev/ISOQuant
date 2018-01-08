package isoquant.plugins.plgs.fileconverting.xml.workflow.internal;

import org.jdom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by napedro on 25/01/16.
 */
public class XResult {

    /*
    <RESULT ENTRIES_SEARCHED="105108" ID="_14387863078100_5874193099902615" TIME_CALCULATED="1438786309290">
    <MODIFIER MCAT_REAGENT="No" NAME="Deamidation+N">
        <MODIFIES APPLIES_TO="N" DELTA_MASS=".9840" TYPE="SIDECHAIN"/>
    </MODIFIER>
    <MODIFIER MCAT_REAGENT="No" NAME="Deamidation+Q">
        <MODIFIES APPLIES_TO="Q" DELTA_MASS=".9840" TYPE="SIDECHAIN"/>
    </MODIFIER>

    * */

    protected int entries_searched;
    protected String id;
    protected float time_calculated;

    protected List<XModifier> modifiers;
    protected List<XPeptide> peptides;
    protected List<XQueryMass> queryMasses;
    //protected List<XHit> hits;
    protected HashMap<String, XHit> hits;
    protected List<String> hits_entries;

    public XResult(String id){
        this.id = id;

        modifiers = new ArrayList<XModifier>();
        peptides = new ArrayList<XPeptide>();
        queryMasses = new ArrayList<XQueryMass>();
        hits = new HashMap<String, XHit>();
        hits_entries = new ArrayList<String>();

        this.setDefaultValues();
    }



    public void addModifier(XModifier mod){
        modifiers.add(mod);
    }

    public void addPeptide(XPeptide pep){
        peptides.add(pep);
    }

    public void addQueryMass(XQueryMass qm){
        queryMasses.add(qm);
    }

    public void addHit(XHit hit){

        hits.put(hit.accession, hit);
        hits_entries.add(hit.getEntry());
    }

    public XHit getHit(String entry){
        if(!hits.containsKey(entry))
            return  null;

        return hits.get(entry);
    }

    public void setEntries_searched(int entries_searched) {
        this.entries_searched = entries_searched;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTime_calculated(float time_calculated) {
        this.time_calculated = time_calculated;
    }

    public void setDefaultValues(){
        this.entries_searched = 10000;
        this.time_calculated = 1000.0F;
    }

    public Element getXMLElement(){

        Element res = new Element("RESULT");

        res.setAttribute("ENTRIES_SEARCHED", Integer.toString(this.entries_searched));
        res.setAttribute("ID", this.id);
        res.setAttribute("TIME_CALCULATED", Float.toString(this.time_calculated));

        for (XModifier modifier : modifiers) {
            res.addContent(modifier.getXMLElement(true));
        }
        for (XPeptide peptide : peptides) {
            res.addContent(peptide.getXMLElement());
        }
        for (XQueryMass queryMass : queryMasses) {
            res.addContent(queryMass.getXMLElement());
        }
        for (XHit hit : hits.values()) {
            res.addContent(hit.getXMLElement());
        }

        return res;
    }

}
