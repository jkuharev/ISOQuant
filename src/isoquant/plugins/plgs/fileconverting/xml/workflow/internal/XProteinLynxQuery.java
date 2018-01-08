package isoquant.plugins.plgs.fileconverting.xml.workflow.internal;

import isoquant.plugins.plgs.fileconverting.xml.massspectrum.XMassSpectrum;
import org.jdom.Element;

/**
 * Created by napedro on 25/01/16.
 */
public class XProteinLynxQuery {

    protected Element massspectrum;
    protected Element dbparameters; //DATABANK_SEARCH_QUERY_PARAMETERS
    protected XResult result;

    public void setDbparameters(Element dbparameters) {
        this.dbparameters = dbparameters;
    }

    public void setResult(XResult result) {
        this.result = result;
    }

    public void setMassspectrum(XMassSpectrum ms, boolean keepDataPoints){
        this.massspectrum = ms.getXMLElement(keepDataPoints);
    }

    public Element getXMLElement()
    {
        Element prLynxQuery = new Element("PROTEINLYNX_QUERY");

        massspectrum.detach();
        prLynxQuery.addContent(massspectrum);
        prLynxQuery.addContent(dbparameters);
        prLynxQuery.addContent(result.getXMLElement());

        return prLynxQuery;
    }

}
