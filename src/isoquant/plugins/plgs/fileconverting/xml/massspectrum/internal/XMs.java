package isoquant.plugins.plgs.fileconverting.xml.massspectrum.internal;

import org.jdom.Element;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by napedro on 14/01/16.
 */
public class XMs {

    protected List<Element> fragmentations;
    protected Element data;
    protected boolean dataCorrected;

    public List<XLevel> levels;

    public XMs(){
        fragmentations = new ArrayList<Element>();
        data = new Element("DATA");
        dataCorrected = false;
        //data.setText("\n");
    }

    /**
     * @param dataList a string containing the data to add. It must be already formatted.
     */
    public void addDataPoints(LinkedList<String> dataList){

        String currText = "\n";
        for (String dp : dataList) {
            currText += dp + "\n";
        }
        currText += "\n";
        data.setText(currText);

    }

    public void setDataCorrected(boolean corrected){
        dataCorrected = corrected;
    }

    /**
     * WARNING: fragmentation data does not contain data points!
     *
     * @param precursorIndex
     * @param dataCorrected
     */
    public void addFragmentation(int precursorIndex, boolean dataCorrected){

        Element frg = new Element("FRAGMENTATION");
        frg.setAttribute("PRECURSOR_INDEX", Integer.toString(precursorIndex));

        Element frgData = new Element("DATA");
        frgData.setAttribute("CORRECTED", Boolean.toString(dataCorrected).toLowerCase());

        frg.addContent(frgData);

        fragmentations.add(frg);
    }

    public Element getXMLElement(){

        Element ms = new Element("MS");

        Element dataEmpty = new Element("DATA");

        data.setAttribute("CORRECTED", Boolean.toString(dataCorrected));
        dataEmpty.setAttribute("CORRECTED", Boolean.toString(dataCorrected));

        //Attach data (frag level 0)
        ms.addContent(data);

        for (Element fragmentation : fragmentations) {
            fragmentation.detach();
            ms.addContent(fragmentation);
        }

        return ms;
    }


}
