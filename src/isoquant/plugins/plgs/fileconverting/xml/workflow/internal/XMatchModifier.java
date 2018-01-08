package isoquant.plugins.plgs.fileconverting.xml.workflow.internal;

import org.jdom.Element;

/**
 * Created by napedro on 25/01/16.
 */
public class XMatchModifier {
    /*
        <MATCH_MODIFIER NAME="Oxidation M" POS="10"/>
    */

    protected String name;
    protected int pos;

    public XMatchModifier(String name, int pos){
        this.name = name;
        this.pos = pos;
    }

    public Element getXMLElement(){
        Element mm = new Element("MATCH_MODIFIER");
        mm.setAttribute("NAME", this.name);
        mm.setAttribute("POS", Integer.toString(this.pos));

        return mm;
    }

}
