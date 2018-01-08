package isoquant.plugins.plgs.fileconverting.xml.workflow.internal;

import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by napedro on 25/01/16.
 */
public class XQueryMass {

    /*<QUERY_MASS CHARGE="2.0" DISSOCIATION_MODE="0" DRIFT_TIME="-2.14748365E9" FRACTION="0" ID="383" MASS="546.2407" NUM_FRAC="0" RETENTION_TIME="42.69">
                <MASS_MATCH AUTO_QC="2" CURATED="2" ID="11" QUERY_ID="_14387863078100_5874193099902615" SCORE="158.3426" SECONDARY_SCORE="55.5556"/>
    </QUERY_MASS>
    */

    protected int charge;
    protected int dissociation_mode; // who knows the code for this????
    protected float drift_time;
    protected int fraction;
    protected int id;
    protected float mass;
    protected int num_frac;
    protected float retention_time;

    protected List<XMassMatch> massMatches;

    public XQueryMass(){
        massMatches = new ArrayList<XMassMatch>();

    }

    public void setCharge(int charge) {
        this.charge = charge;
    }

    public void setDissociation_mode(int dissociation_mode) {
        this.dissociation_mode = dissociation_mode;
    }

    public void setDrift_time(float drift_time) {
        this.drift_time = drift_time;
    }

    public void setFraction(int fraction) {
        this.fraction = fraction;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public void setNum_frac(int num_frac) {
        this.num_frac = num_frac;
    }

    public void setRetention_time(float retention_time) {
        this.retention_time = retention_time;
    }

    public void addMassMatch(XMassMatch mm){
        massMatches.add(mm);
    }

    public Element getXMLElement(){

        /*<QUERY_MASS CHARGE="2.0" DISSOCIATION_MODE="0" DRIFT_TIME="-2.14748365E9" FRACTION="0" ID="383" MASS="546.2407" NUM_FRAC="0" RETENTION_TIME="42.69">
                    <MASS_MATCH AUTO_QC="2" CURATED="2" ID="11" QUERY_ID="_14387863078100_5874193099902615" SCORE="158.3426" SECONDARY_SCORE="55.5556"/>
        </QUERY_MASS>
        */

        Element qm = new Element("QUERY_MASS");
        qm.setAttribute("CHARGE", Integer.toString(charge));
        qm.setAttribute("DISSOCIATION_MODE", Integer.toString(dissociation_mode));
        qm.setAttribute("DRIFT_TIME", Float.toString(drift_time));
        qm.setAttribute("FRACTION", Integer.toString(fraction));
        qm.setAttribute("ID", Integer.toString(id));
        qm.setAttribute("MASS", Float.toString(mass));
        qm.setAttribute("NUM_FRAC", Integer.toString(num_frac));
        qm.setAttribute("RETENTION_TIME", Float.toString(retention_time));

        for (XMassMatch massMatch : massMatches) {
            qm.addContent(massMatch.getXMLElement());
        }

        return qm;
    }
}
