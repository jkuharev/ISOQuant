package isoquant.plugins.plgs.fileconverting.xml;

import org.jdom.Attribute;
import org.jdom.Element;

import java.util.List;

/**
 * Created by napedro on 22/01/16.
 */
public class XMLutil {

    public static Element deepCopy(Element srcElement, boolean keepTextContents){

        Element trgEl = new Element(srcElement.getName());

        List attlist = srcElement.getAttributes();

        for (int i = 0; i < attlist.size(); i++) {
            Attribute at = (Attribute) attlist.get(i);
            //at.detach();
            trgEl.setAttribute(at.getName(), at.getValue());
        }
        //trgEl.setAttributes(attlist);

        trgEl.setContent(srcElement.cloneContent());

        return trgEl;
    }
}
