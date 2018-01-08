package isoquant.plugins.plgs.fileconverting.xml.workflow.internal;

import org.jdom.Element;

/**
 * Created by napedro on 25/01/16.
 */
public class XMassMatch {

    /*
     <MASS_MATCH AUTO_QC="2" CURATED="2" ID="11" QUERY_ID="_14387863078100_5874193099902615" SCORE="158.3426" SECONDARY_SCORE="55.5556"/>
    * */

    protected int auto_qc;
    protected int curated;
    protected int id;
    protected String query_id;
    protected float score;
    protected float secondary_score;


    /**
     * @param id
     */
    public XMassMatch(int id){this.id = id;}

    public void setAuto_qc(int auto_qc) {
        this.auto_qc = auto_qc;
    }

    public void setCurated(int curated) {
        this.curated = curated;
    }

//    public void setId(int id) {
//        this.id = id;
//    }

    public int getId(){
        return this.id;
    }

    public void setQuery_id(String query_id) {
        this.query_id = query_id;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public void setSecondary_score(float secondary_score) {
        this.secondary_score = secondary_score;
    }

    public Element getXMLElement(){
            /*
     <MASS_MATCH AUTO_QC="2" CURATED="2" ID="11" QUERY_ID="_14387863078100_5874193099902615" SCORE="158.3426" SECONDARY_SCORE="55.5556"/>
    * */

        Element mm = new Element("MASS_MATCH");
        mm.setAttribute("AUTO_QC", Integer.toString(auto_qc));
        mm.setAttribute("CURATED", Integer.toString(curated));
        mm.setAttribute("ID", Integer.toString(id));

        mm.setAttribute("QUERY_ID", query_id);

        mm.setAttribute("SCORE", Float.toString(score));
        mm.setAttribute("SECONDARY_SCORE", Float.toString(secondary_score));

        return mm;
    }
}
