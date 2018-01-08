package isoquant.plugins.plgs.fileconverting.xml.massspectrum.internal;

import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Created by napedro on 14/01/16.
 */
public class XFormat {

    Element[] fields;
    int fragLevel;

    public enum templates{DDA0, DDA1};


    /**
     * @param frgLevel fragment level
     * @param numFields number of fields described in the format
     */
    public XFormat(int frgLevel, int numFields){
        fields = new Element[numFields];
        fragLevel = frgLevel;
    }

    public XFormat(templates template){
        if(template == templates.DDA0){
            fields = new Element[8];
            fragLevel = 0;

            this.fillField(1, "Mass");
            this.fillField(2, "Intensity");
            this.fillField(3, "Charge");
            this.fillField(4, "RT");
            this.fillField(5, "Function");
            this.fillField(6, "StartScan");
            this.fillField(7, "StopScan");
            this.fillField(8, "ETD");
        }
        else if(template == templates.DDA1){
            fields = new Element[2];
            fragLevel = 1;

            this.fillField(1, "Mass");
            this.fillField(2, "Intensity");

        }
    }

    public void fillField(int position, String name){

        Element field = new Element("FIELD");
        field.setAttribute(new Attribute("POSITION", Integer.toString(position)));
        field.setAttribute(new Attribute("NAME", name));

        //Fields are stored at fields at position - 1 index so that they are sorted when retrieved
        fields[position - 1] = field;
    }

    public Element getXMLElement(){

        Element format = new Element("FORMAT");

        format.setAttribute("FRAGMENTATION_LEVEL", Integer.toString(fragLevel));

        for (Element field : fields) {
            field.detach();
            format.addContent(field);
        }

        return format;
    }
}
