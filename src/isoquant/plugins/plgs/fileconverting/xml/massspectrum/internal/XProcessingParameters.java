package isoquant.plugins.plgs.fileconverting.xml.massspectrum.internal;

import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by napedro on 14/01/16.
 */
public class XProcessingParameters {

/*
 * Mock Example
 *
    <PROCESSING_PARAMETERS INSTRUMENT_MODE="QTOF-MSMS">
        <LEVEL FRAGMENTATION_LEVEL="0">
            <MASS_MEASURE>
                <BACKGROUND_SUBTRACT BELOW_CURVE="35.0" POLYNOMIAL_ORDER="5" TYPE="Adaptive"/>
                <PEAK_DETECTION>
                    <SMOOTH METHOD="Savitzky-Golay" NUMBER="3" WINDOW="1"/>
                    <CENTROID CENTROID_TOP="80" MIN_PEAK_WIDTH="4"/>
                </PEAK_DETECTION>
            </MASS_MEASURE>
            <CALIBRATE LOCKSPRAY_AVG_SCANS="3" LOCK_MASS_EXT="785.8426" NP_MULTIPLIER="1" TOF_RESOLUTION="23000" TOLERANCE="0.5"/>
            <DEISOTOPE METHOD="Transform">
                <TRANSFORM/>
            </DEISOTOPE>
        </LEVEL>
        <LEVEL FRAGMENTATION_LEVEL="1">
            <MASS_MEASURE>
                <BACKGROUND_SUBTRACT BELOW_CURVE="35.0" POLYNOMIAL_ORDER="5" TYPE="Adaptive"/>
                <PEAK_DETECTION>
                    <SMOOTH METHOD="Savitzky-Golay" NUMBER="3" WINDOW="1"/>
                    <CENTROID CENTROID_TOP="80" MIN_PEAK_WIDTH="4"/>
                </PEAK_DETECTION>
            </MASS_MEASURE>
            <CALIBRATE LOCKSPRAY_AVG_SCANS="3" LOCK_MASS_EXT="785.8426" NP_MULTIPLIER="1" TOF_RESOLUTION="23000" TOLERANCE="0.5"/>
            <DEISOTOPE METHOD="Transform">
                <TRANSFORM/>
            </DEISOTOPE>
        </LEVEL>
    </PROCESSING_PARAMETERS>
*/

    public enum levelTemplate{DDA0, DDA1}

    private String instrumentMode;
    private List<XLevel> levels;

    /**
     * @param instMode Instrument mode. Tipically "QTOF-MSMS"
     * @param defaultValues values defined for current version of PLGS
     */
    public XProcessingParameters(String instMode, boolean defaultValues){
        instrumentMode = instMode;
        levels = new ArrayList<XLevel>();
        if(defaultValues){
            XLevel lev0 = new XLevel(levelTemplate.DDA0);
            XLevel lev1 = new XLevel(levelTemplate.DDA1);
            this.addLevel(lev0);
            this.addLevel(lev1);
        }
    }

    public void addLevel(XLevel lev){
        levels.add(lev);
    }

    public Element getXMLElement(){

        Element prcparams = new Element("PROCESSING_PARAMETERS");

        for (XLevel level : levels) {
            Element xlevel = level.getXMLElement();
            xlevel.detach();
            prcparams.addContent(xlevel);
        }

        return prcparams;
    }

    protected class XLevel {

        public int frag_level;

        public float massm_bg_belowcurve;
        public int massm_polyn_order;
        public String massm_type;

        public String peakd_method;
        public int peakd_number;
        public int peakd_window;
        public int peakd_top;
        public int peakd_minpeakwidth;
        public int cal_lockspray_avg_scans;
        public float cal_lockspray_lockmass;
        public int cal_npmultiplier;
        public int cal_tof_resolution;
        public float cal_tolerance;

        public String desisot_method;


        public XLevel(){}

        public XLevel(levelTemplate template){
            // We give here some dummy values when our source does not contain these values.
            // In case you have some or all values required, use the default constructor to get an empty object.

            if(template==levelTemplate.DDA0){
                frag_level = 0;
            }else if(template==levelTemplate.DDA1){
                frag_level = 1;
            }

            massm_bg_belowcurve = 35.0F;
            massm_polyn_order = 5;
            massm_type ="Adaptive";
            peakd_method = "Savitzky-Golay";
            peakd_number = 3;
            peakd_window = 1;
            peakd_top = 80;
            peakd_minpeakwidth = 4;
            cal_lockspray_avg_scans = 3;
            cal_lockspray_lockmass = 785.8426F;
            cal_npmultiplier = 1;
            cal_tof_resolution = 23000;
            cal_tolerance = 0.5F;
            desisot_method = "Transform";

        }

        public Element getXMLElement(){

            Element level = new Element("LEVEL");
            level.setAttribute("FRAGMENTATION_LEVEL", Integer.toString(frag_level));

            //<MASS_MEASURE>
            Element mm = new Element("MASS_MEASURE");

            //<BACKGROUND_SUBTRACT BELOW_CURVE="35.0" POLYNOMIAL_ORDER="5" TYPE="Adaptive"/>
            Element bgs = new Element("BACKGROUND_SUBTRACT");
            bgs.setAttribute("BELOW_CURVE", Float.toString(massm_bg_belowcurve));
            bgs.setAttribute("POLYNOMIAL_ORDER", Integer.toString(massm_polyn_order));
            bgs.setAttribute("TYPE", massm_type);

            mm.setContent(bgs);

            //<PEAK_DETECTION>
            Element pd = new Element("PEAK_DETECTION");

            //<SMOOTH METHOD="Savitzky-Golay" NUMBER="3" WINDOW="1"/>
            Element sm = new Element("SMOOTH");
            sm.setAttribute("METHOD", peakd_method);
            sm.setAttribute("NUMBER", Integer.toString(peakd_number));
            sm.setAttribute("WINDOW", Integer.toString(peakd_window));

            pd.addContent(sm);

            //<CENTROID CENTROID_TOP="80" MIN_PEAK_WIDTH="4"/>
            Element ct = new Element("CENTROID");
            ct.setAttribute("CENTROID_TOP", Integer.toString(peakd_top));
            ct.setAttribute("MIN_PEAK_WIDTH", Integer.toString(peakd_minpeakwidth));

            pd.addContent(ct);

            //</PEAK_DETECTION>
            mm.setContent(pd);
            //</MASS_MEASURE>
            level.addContent(mm);

            //<CALIBRATE LOCKSPRAY_AVG_SCANS="3" LOCK_MASS_EXT="785.8426" NP_MULTIPLIER="1" TOF_RESOLUTION="23000" TOLERANCE="0.5"/>
            Element cal = new Element("CALIBRATE");
            cal.setAttribute("LOCKSPRAY_AVG_SCANS", Integer.toString(cal_lockspray_avg_scans));
            cal.setAttribute("LOCK_MASS_EXT", Float.toString(cal_lockspray_lockmass));
            cal.setAttribute("NP_MULTIPLIER", Integer.toString(cal_npmultiplier));
            cal.setAttribute("TOF_RESOLUTION",Integer.toString(cal_tof_resolution));
            cal.setAttribute("TOLERANCE", Float.toString(cal_tolerance));
            level.addContent(cal);
            //<DEISOTOPE METHOD="Transform">
            Element desisot = new Element("DEISOTOPE");
            desisot.setAttribute("METHOD", desisot_method);
            //<TRANSFORM/>
            desisot.addContent(new Element("TRANSFORM"));
            //</DEISOTOPE>
            level.addContent(desisot);

            return level;
        }
    }
}
