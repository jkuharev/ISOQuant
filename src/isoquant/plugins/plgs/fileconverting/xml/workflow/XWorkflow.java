package isoquant.plugins.plgs.fileconverting.xml.workflow;

import isoquant.plugins.plgs.fileconverting.xml.massspectrum.XMassSpectrum;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by napedro on 25/01/16.
 */
public class XWorkflow {

    protected Document doc;

    protected Element massspectrum;
    protected Element proteinlynx_query;
    protected Element result;
    protected Element template_ref;

    public String XMLfileName;

    public String ID;
    public String status;
    public String title;

    /**
     * @param fileName output XML file name
     */
    public XWorkflow(String fileName, String wf_ID, String title){

        XMLfileName = fileName;
        ID = wf_ID;
        this.title = title;
        this.status = "Finished";

        massspectrum = new Element("MASS_SPECTRUM");
        massspectrum.setAttribute("ID", this.ID);
        massspectrum.setAttribute("STATUS", "Finished");
        massspectrum.setAttribute("TITLE", this.title);

        proteinlynx_query = new Element("PROTEINLYNX_QUERY");
        template_ref = new Element("TEMPLATE_REF");

    }


    public void setMassspectrum(XMassSpectrum ms, boolean keepDataPoints){
        massspectrum = ms.getXMLElement(keepDataPoints);
    }

    //TODO: change it to XProteinLynxQuery object
    public void setProteinlynx_query(Element proteinlynx_query) {
        this.proteinlynx_query = proteinlynx_query;
    }

    public void setTemplate_ref(String id) {
        this.template_ref = new Element("TEMPLATE_REF");
        Element elref = new Element("ELEMENT_REF");
        elref.setAttribute("ID", id);
        elref.setAttribute("NAME", "WORKFLOW_TEMPLATE");
        this.template_ref.addContent(elref);
    }

    public void writeXML(){

        //Build the XML document
        Document docXML = new Document();
        Element wf_fresh = new Element("WORKFLOW");
        wf_fresh.setAttribute("ID", this.ID);
        wf_fresh.setAttribute("STATUS", this.status);
        wf_fresh.setAttribute("TITLE", this.title);

        docXML.setRootElement(wf_fresh);

        //add MASS_SPECTRUM tag
        massspectrum.detach();
        docXML.getRootElement().addContent(massspectrum);

        //add PROTEINLYNX_QUERY tag
        proteinlynx_query.detach();
        docXML.getRootElement().addContent(proteinlynx_query);

        // add TEMPLATE_REF tag
        template_ref.detach();
        docXML.getRootElement().addContent(template_ref);

        try{

            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(docXML, new FileWriter(XMLfileName));

            System.out.println("File " + XMLfileName + " saved.");

        }catch (IOException io){

            System.out.println(io.getMessage());
        }

    }
}
