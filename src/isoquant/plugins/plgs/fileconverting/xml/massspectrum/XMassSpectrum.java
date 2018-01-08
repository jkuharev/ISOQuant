package isoquant.plugins.plgs.fileconverting.xml.massspectrum;

import isoquant.plugins.plgs.fileconverting.xml.XMLutil;
import isoquant.plugins.plgs.fileconverting.xml.massspectrum.internal.XFormat;
import isoquant.plugins.plgs.fileconverting.xml.massspectrum.internal.XMs;
import isoquant.plugins.plgs.fileconverting.xml.massspectrum.internal.XProcessingParameters;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by napedro on 14/01/16.
 */

public class XMassSpectrum {

    protected Document doc;

    protected Element massSpectrum;

    protected Element processing_parameters;
    protected List<Element> formats;
    protected Element ms;

    public String XMLfileName;

    public XMassSpectrum(String fileName){

        XMLfileName = fileName;

        formats = new ArrayList<Element>();

        massSpectrum = new Element("MASS_SPECTRUM");

        ms = new Element("MS");

    }

    public void addFormat(XFormat format1){

        Element f1 = format1.getXMLElement();
        //f1.detach();
        formats.add(f1);

    }

    public void setMS(XMs newMS){
        ms = newMS.getXMLElement();
    }

    public void setProcessing_parameters(XProcessingParameters newProcParameters){
        processing_parameters = newProcParameters.getXMLElement();

    }

    public Element getXMLElement(boolean keepDataPoints){
        //Return the MassSpectrum xml as an xml element so that it can be appended to other xml files (like the
        // workflow.xml)

        massSpectrum.detach();
        Element msspectrum = XMLutil.deepCopy(massSpectrum, true);

        msspectrum.detach();

        //add PROCESSING_PARAMETERS tag
        Element pcp= XMLutil.deepCopy(processing_parameters, true);
        msspectrum.addContent(pcp);

        // add FORMAT tags
        for (Element format : formats) {
            format.detach();
            msspectrum.addContent(XMLutil.deepCopy(format, true));
        }

        msspectrum.addContent(XMLutil.deepCopy(ms, true));

        if(!keepDataPoints){
            Element ms_empty = (Element)msspectrum.getChild("MS").getChild("DATA").clone();
            ms_empty.setText("");
            Element dt = ms_empty.getChild("DATA");
            msspectrum.getChild("MS").removeChild("DATA");
            msspectrum.getChild("MS").addContent(ms_empty);
            msspectrum.addContent(new Element("DATA"));
        }

        return msspectrum;
    }

    public void writeXML(){

        //Build the XML document
        massSpectrum.detach();
        Document docXML = new Document();
        docXML.setRootElement(massSpectrum);

        //add PROCESSING_PARAMETERS tag
        processing_parameters.detach();
        docXML.getRootElement().addContent(processing_parameters);

        // add FORMAT tags
        for (Element format : formats) {
            format.detach();
            docXML.getRootElement().addContent(format);

        }

        // add MS tag
        ms.detach();
        docXML.getRootElement().addContent(ms);

        try{

            XMLOutputter xmlOutput = new XMLOutputter();

            Format msFormat = Format.getPrettyFormat();
            msFormat.setTextMode(Format.TextMode.PRESERVE);

            xmlOutput.setFormat(msFormat);
            xmlOutput.output(docXML, new FileWriter(XMLfileName));

            System.out.println("File " + XMLfileName + " saved.");

        }catch (IOException io){

            System.out.println(io.getMessage());
        }

    }




}
