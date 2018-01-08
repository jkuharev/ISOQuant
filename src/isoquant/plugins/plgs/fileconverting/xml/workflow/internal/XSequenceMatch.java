package isoquant.plugins.plgs.fileconverting.xml.workflow.internal;

import org.jdom.Element;

/**
 * Created by napedro on 25/01/16.
 */
public class XSequenceMatch {

    protected int id;
    protected int start;
    protected int end;

    public void setId(int id) {
        this.id = id;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public Element getXMLElement(){

        Element seqMatch = new Element("SEQUENCE_MATCH");
        seqMatch.setAttribute("END", Integer.toString(this.end));
        seqMatch.setAttribute("ID", Integer.toString(this.id));
        seqMatch.setAttribute("START", Integer.toString(this.start));

        return seqMatch;
    }

}
