package isoquant.plugins.plgs.fileconverting.xml.workflow.internal;

import org.jdom.Element;

/**
 * Created by napedro on 25/01/16.
 */
public class XModifier {

    /*
    <MODIFIER MCAT_REAGENT="No" NAME="Deamidation+N">
        <MODIFIES APPLIES_TO="N" DELTA_MASS=".9840" TYPE="SIDECHAIN"/>
    </MODIFIER>
    * */

    //So far, we consider that each MODIFIER contains only one MODIFIES tag, so that we integrate both in the same class

    protected boolean mcat_reagent;
    protected String name;
    protected String applies_to;
    protected float delta_mass;
    protected String type;
    protected modStatus status;

    public enum modStatus {FIXED, VARIABLE};

    public void setMcat_reagent(boolean mcat_reagent) {
        this.mcat_reagent = mcat_reagent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setApplies_to(String applies_to) {
        this.applies_to = applies_to;
    }

    public void setDelta_mass(float delta_mass) {
        this.delta_mass = delta_mass;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * @param status modStatus
     */
    public void setStatus(modStatus status) {
        this.status = status;
    }


    /**
     * @param partialXML
     * @return object converted to XML (object)
     */
    public Element getXMLElement(boolean partialXML){

        if(partialXML){ return getXMLElementPartial(); }
        else{ return getXMLElementComplete(); }
    }

    private Element getXMLElementComplete(){
        Element anMod = new Element("ANALYSIS_MODIFIER");
        anMod.setAttribute("STATUS", status.toString());
        Element mod = getXMLElementPartial();

        anMod.addContent(mod);

        return anMod;
    }

    private Element getXMLElementPartial(){
        Element mod = new Element("MODIFIER");
        mod.setAttribute("MCAT_REAGENT", this.mcat_reagent ? "Yes" : "No");
        mod.setAttribute("NAME", this.name);

        Element mds = new Element("MODIFIES");
        mds.setAttribute("APPLIES_TO", this.applies_to);
        mds.setAttribute("DELTA_MASS",Float.toString(this.delta_mass));
        mds.setAttribute("TYPE", this.type);

        mod.addContent(mds);

        return mod;
    }

}
