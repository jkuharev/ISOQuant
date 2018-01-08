package isoquant.plugins.report.MzIdentML;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

//import uk.ac.ebi.pride.term.*;


/**
 * Created by napedro on 04/08/14.
 */
public enum CvReference {


        PSI_MS("PSI-MS","2.25.0","PSI-MS","http://psidev.cvs.sourceforge.net/viewvc/*checkout*/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo"),
        UNIMOD("UNIMOD",null,"UNIMOD","http://www.unimod.org/obo/unimod.obo"),
        UNIT_ONTOLOGY("UNIT-ONTOLOGY",null,"UO","http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/phenotype/unit.obo");

        private final String id;
        private final String version;
        private final String fullName;
        private final String uri;

        private CvReference(String id, String version, String fullName, String uri) {
            this.id = id;
            this.version = version;
            this.fullName = fullName;
            this.uri = uri;
        }

        public String getId() {
            return id;
        }
        public String getUri() {
            return uri;
        }

        public String getVersion() {
            return version;
        }

        public String getFullName() {
            return fullName;
        }

        /**
         * Get Cv term by accession.
         *
         * @param id CV id.
         * @return CvReference  Cv.
         */
        public static CvReference getCvRefById(String id) {
            CvReference cvTerm = null;

            CvReference[] cvTerms = CvReference.values();
            for (CvReference cv : cvTerms) {
                if (cv.getId().equals(id)) {
                    cvTerm = cv;
                }
            }

            return cvTerm;
        }

        /**
         * Check whether the accession exists in the enum.
         *
         * @param id controlled vocabulary accession
         * @return boolean  true if exists
         */
        public static boolean hasId(String id) {
            boolean result = false;

            CvReference[] cvTerms = CvReference.values();
            for (CvReference cv : cvTerms) {
                if (cv.getId().equals(id)) {
                    result = true;
                }
            }

            return result;
        }

    }