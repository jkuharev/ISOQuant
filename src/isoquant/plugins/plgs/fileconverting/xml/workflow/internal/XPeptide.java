package isoquant.plugins.plgs.fileconverting.xml.workflow.internal;

import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by napedro on 25/01/16.
 */
public class XPeptide {

    /*
    <PEPTIDE ID="3" MASS="1042.4501" SEQUENCE="DGGNAVNHLM">
                <MATCH_MODIFIER NAME="Oxidation M" POS="10"/>
    </PEPTIDE>
    * */

    protected int id;
    protected float mass;
    protected String sequence;
    protected List<XMatchModifier> massModifiers;

    public XPeptide(){
        massModifiers = new ArrayList<XMatchModifier>();
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public void addMatchModifier(XMatchModifier mmod){
        massModifiers.add(mmod);
    }

    public Element getXMLElement(){
        Element pep = new Element("PEPTIDE");

        pep.setAttribute("ID", Integer.toString(this.id));
        pep.setAttribute("MASS", Float.toString(this.mass));
        pep.setAttribute("SEQUENCE", this.sequence);

        for (XMatchModifier massModifier : massModifiers) {
            pep.addContent(massModifier.getXMLElement());
        }

        return pep;
    }
}
