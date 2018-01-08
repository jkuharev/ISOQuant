package isoquant.plugins.exporting.MzIdentMLReporter;

import uk.ac.ebi.jmzidml.model.mzidml.*;
import uk.ac.ebi.jmzidml.xml.io.MzIdentMLUnmarshaller;
import uk.ac.ebi.jmzidml.MzIdentMLElement;

import java.net.URL;
import java.util.List;

public class CaseStudyForParser {
    public static void main(String[] args) {
          try {

              // Open the input mzIdentML file for parsing
               URL xmlFileURL = CaseStudyForParser.class.getClassLoader().getResource("/Users/napedro/Dropbox/issues/self_mzIdentML_for_IsoQuant/doc/Mascot_MSMS_example.mzid"); 

                if (xmlFileURL != null) {

                   boolean aUseSpectrumCache = true;
                   MzIdentMLUnmarshaller unmarshaller = new MzIdentMLUnmarshaller(xmlFileURL);

                   DataCollection dc =  unmarshaller.unmarshal(DataCollection.class);
                   AnalysisData ad = dc.getAnalysisData();

                   // Get the list of SpectrumIdentification elements
                   List<SpectrumIdentificationList> sil = ad.getSpectrumIdentificationList();

                   for (SpectrumIdentificationList sIdentList : sil) {
                        for (SpectrumIdentificationResult spectrumIdentResult
                                : sIdentList.getSpectrumIdentificationResult()) {

                            // Get the name of SpectrumIdentificationResult
                            String spectrumID =  spectrumIdentResult.getSpectrumID();

                            for (SpectrumIdentificationItem spectrumIdentItem
                                 : spectrumIdentResult.getSpectrumIdentificationItem()) {

                                // Get the following information for SpectrumIdentificationItem element
                                String spectrumIdItem = spectrumIdentItem.getId();
                                Double calculatedMassToCharge =  spectrumIdentItem.getCalculatedMassToCharge();
                                Double experimentalMassToCharge = spectrumIdentItem.getExperimentalMassToCharge();
                                int rank = spectrumIdentItem.getRank();
                                int charge = spectrumIdentItem.getChargeState();

                                System.out.println("Spectrum Identification ID = " + spectrumIdItem);
                                System.out.println("Calculated Mass/Charge = " + calculatedMassToCharge);
                                System.out.println("Experimental Mass/Charge = " + experimentalMassToCharge);
                                System.out.println("Search rank = " + rank);
                                System.out.println("Charge = " + charge);

                                // If the auto-resolve mechanism is activated for SpectrumIdentificationItem
                                // then automatically resolve the Peptide Object
                                if (MzIdentMLElement.SpectrumIdentificationItem.isAutoRefResolving()
                                        && spectrumIdentItem.getPeptideRef() != null) {
                                     Peptide peptide = spectrumIdentItem.getPeptide();
                                     String peptideSequence = peptide.getPeptideSequence();

                                    System.out.println("Pepetide Sequence = " + peptideSequence);
                                }

                                System.out.println("\n");

                            } // end spectrum identification item
                        } // end spectrum identification results
                   }
                }
               }catch(Exception e) {
                    e.printStackTrace(); }
        }

}