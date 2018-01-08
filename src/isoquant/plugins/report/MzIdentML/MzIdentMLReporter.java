/*******************************************************************************
 * THIS FILE IS PART OF ISOQUANT SOFTWARE PROJECT WRITTEN BY JOERG KUHAREV
 * 
 * Copyright (c) 2009 - 2013, JOERG KUHAREV and STEFAN TENZER
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 * must display the following acknowledgment:
 * This product includes software developed by JOERG KUHAREV and STEFAN TENZER.
 * 4. Neither the name "ISOQuant" nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY JOERG KUHAREV ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL JOERG KUHAREV BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
/** ISOQuant, isoquant.plugins.report.MzIdentML, 31.07.2014 */

package isoquant.plugins.report.MzIdentML;

import static junit.framework.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.jsix.utilities.Settings;
import de.mz.jk.plgs.data.Workflow;
import isoquant.app.Defaults;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.db.IQDBUtils;
import isoquant.kernel.plugin.SingleActionPlugin4DBExportToFile;
import uk.ac.ebi.jmzidml.model.mzidml.*;
import uk.ac.ebi.jmzidml.model.mzidml.params.*;
import uk.ac.ebi.jmzidml.xml.io.MzIdentMLMarshaller;

/**
 * @author napedro
 *
 */
public class MzIdentMLReporter extends SingleActionPlugin4DBExportToFile
{
	private PeptideModsReference modificationReference = new PeptideModsReference();
    private String researcher_lastName ="Doe";
    private String researcher_firstName ="John";
    private String researcher_organization = "Uni-Mainz";
    private String cfg_dbversion = null;
	private boolean keepTables = false;

    private String peptideFDR = "0.01";
    private String his_resolveHomology =  "true";
    private String his_maxSeqCluster = "1";
    private String his_useSharedPeptides = "all";
    private String dbversion = "";
    private String dbId = "SearchDB_1";
    private Long numDBsequences = 0L;
    private String orgSciName = "";
    private String ncbiTaxId = "";
    private String dbname = "";


    public static final String sql_mzid_DbSequence = "mzidentml_DBsequence.sql";
    public static final String sql_mzid_Peptide = "mzidentml_Peptide.sql";
    public static final String sql_mzid_PeptideEvidence = "mzidentml_PeptideEvidence.sql";
    public static final String sql_mzid_SpectrumIdentificationItem = "mzidentml_SpectrumIdentificationItem.sql";
    public static final String getSql_mzid_ProteinAmbiguityGroup = "mzidentml_ProteinAmbiguityGroup.sql";
	public static final String sql_cleanup_file = "mzidentml_clean_db.sql";

    public final Cv CvPSIMS = getthisCv(CvReference.PSI_MS);
    public final Cv CvUNIMOD = getthisCv(CvReference.UNIMOD);
    public final Cv CvUO = getthisCv(CvReference.UNIT_ONTOLOGY);

    public final CvTermReference cvProtDesc = CvTermReference.MS_PROTEIN_DESCRIPTION;
    public final CvTermReference cvSciName = CvTermReference.MS_TAXONOMY_SCIENTIFIC_NAME;
    public final CvTermReference cvTaxID = CvTermReference.MS_TAXONOMY_NCBI_TAXID;
    public final CvTermReference cvResearcher = CvTermReference.MS_RESEARCHER;
    public final CvTermReference cvTaxSciName = CvTermReference.MS_TAXONOMY_SCIENTIFIC_NAME;
    public final CvTermReference cvCoverage = CvTermReference.MS_SEQ_COVERAGE;
    public final CvTermReference cvNumSequences = CvTermReference.MS_DISTINCT_PEPTIDE_SEQUENCES;
    public final CvTermReference cvMGF = CvTermReference.MS_MASCOT_MGF_FILE;
    public final CvTermReference cvMultiplePeakFile = CvTermReference.MS_MULTIPLE_PEAK_FORMAT;
    public final CvTermReference cvPepFDR = CvTermReference.PEPTIDE_GLOBAL_FDR;

    public MzIdentMLReporter(iMainApp app) { super(app); }

    public Boolean checkTablesAnalysis(DBProject p) {
        MySQL db = p.mysql;

        List<String> tablestocheck = Arrays.asList(("protein,best_peptides_for_quantification," +
                "peptide,peptides_in_proteins_before_homology_filtering," +
                "protein_homology,protein_info,finalquant,peptides_in_proteins_after_homology_filtering," +
                "peptides_in_proteins_stats,mass_spectrum,query_mass").split(","));

        List<String> dbtables = db.listTables();
        return dbtables.containsAll(tablestocheck);
    }

    public void initGlobalvariables(DBProject p)
    {
        //TODO: substitute dummy fasta values for real values from workflow_metadata
		peptideFDR = p.mysql.getFirstValue( "history", "note", "`type` = 'parameter' AND value = 'process.annotation.peptide.maxFDR' " +
                "ORDER BY time desc;");
        his_resolveHomology =  p.mysql.getFirstValue("history", "note", "`type` = 'parameter' AND value = 'process.annotation.protein.resolveHomology' " +
                "ORDER BY time desc;");
        his_maxSeqCluster = p.mysql.getFirstValue("history", "note", "`type` = 'parameter' AND value = 'process.annotation.peptide.maxSequencesPerEMRTCluster' " +
                "ORDER BY time desc;");
        his_useSharedPeptides = p.mysql.getFirstValue("history", "note", "`type` = 'parameter' AND value = 'process.annotation.useSharedPeptides' " +
                "ORDER BY time desc;");


        List<Workflow> runs = IQDBUtils.getWorkflows(p);
        int nRuns = runs.size();
        Workflow run = runs.get(0);

        Map<String, String> wf_metainfo = IQDBUtils.getWorkflowMetaInfo(p, run, "Workflow");

        dbversion = cfg_dbversion;  //'SEARCH_DATABASE' name value type=Workflow
        if(wf_metainfo.containsKey("SEARCH_DATABASE"))
        {
            dbname =  wf_metainfo.get("SEARCH_DATABASE");
        }

        if(wf_metainfo.containsKey("ENTRIES_SEARCHED"))
            numDBsequences = Long.parseLong(wf_metainfo.get("ENTRIES_SEARCHED"));

		// System.out.println(peptideFDR);
		// System.out.println(his_maxSeqCluster);
		// System.out.println(his_resolveHomology);
		// System.out.println(his_useSharedPeptides);
		// System.out.println(dbversion);
		// System.out.println(numDBsequences);
		// System.out.println(orgSciName);
		// System.out.println(ncbiTaxId);
		// System.out.println(dbname);
    }

    public Boolean checkMetaDataExists(DBProject p) {
        MySQL db = p.mysql;

        return db.tableExists("workflow_metadata");
    }

    @Override public void loadSettings(Settings cfg)
    {
        researcher_firstName = cfg.getStringValue("setup.report.mzidentml.researcherFirstName", "John", false);
        researcher_lastName = cfg.getStringValue("setup.report.mzidentml.researcherLastName", "Doe", false);
        researcher_organization = cfg.getStringValue("setup.report.mzidentml.researcherOrganization", "Uni-Mainz", false);
        cfg_dbversion = cfg.getStringValue("setup.report.mzidentml.DBversion", "", false);
        orgSciName = cfg.getStringValue("setup.report.mzidentml.DBOrganismScientificName", "", false);
        ncbiTaxId = cfg.getStringValue("setup.report.mzidentml.DBNCBITaxID", "", false);
		keepTables = cfg.getBooleanValue( "setup.report.mzidentml.keepTables", keepTables, !Defaults.DEBUG );
    }
	
	@Override public String getFileChooserTitle() { return "Choose a file for generating mzIdentML report"; }

	@Override public String[] getFileExtensions() {
        return new String[] { "mzid; MzIdentML xml file" };
	}

	@Override public void runExportAction(DBProject p, File file) throws Exception {
		PrintStream out = null;
		Bencher t = new Bencher(true);

        initGlobalvariables(p);

        if(!checkTablesAnalysis(p))
        {
			app.showErrorMessage( "You can't execute this plugin before completing an ISOQuant analysis!" );
            return;
        }

        if(!checkMetaDataExists(p)){
			app.showErrorMessage( "This project has been generated with a previous version of ISOQuant. In order to " +
                    "generate mzIdentML reports, you need to re-import and re-process the data." +
                    "We are sorry for the inconvenience.");
            return;
        }

		System.out.println( "creating MZidentML file for project '" + p.data.title + "' ..." );
		try
		{
			out = new PrintStream(file);
			createMzIdentMLReport(p, out);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (out != null) out.close();
			if (!keepTables)
				p.mysql.executeSQLFile( getPackageResource( sql_cleanup_file ) );
		}
		System.out.println("peptide MZidentML file creation duration: [" + t.stop().getSecString() + "]");
	}

	
	private void createMzIdentMLReport(DBProject p, PrintStream out) throws IOException
	{
		MySQL db = p.mysql;
		// List<Workflow> workflowList = IQDBUtils.getWorkflows(p);

        MzIdentMLMarshaller m = new MzIdentMLMarshaller();
        assertNotNull(m);

        // mzIdentML
        // XML header
        out.print(m.createXmlHeader() + "\n");
        // mzIdentML start tag
		out.print( m.createMzIdentMLStartTag( XJava.encURL( p.data.title ) ) + "\n" );
        //     cvList
        out.print("\n");
        out.print("<cvList>");
        out.print("\n");
        List<Cv> cvList = getCVList(out);
        for (Cv cv: cvList)
        {
            m.marshal(cv, out);
            out.print("\n");
            out.flush();
        }
        out.print("</cvList>");
        out.print("\n");

        //     AnalysisSoftwareList
        out.print("\n");
        out.flush();
        out.print("<AnalysisSoftwareList>");
        out.print("\n");
        out.flush();
        List<AnalysisSoftware> analSoftList = getAnalysisSoftwareList(p);
        for (AnalysisSoftware as: analSoftList)
        {
            m.marshal(as, out);
            out.print("\n");
            out.flush();
        }
        out.print("\n");
        out.flush();
        out.print("</AnalysisSoftwareList>");
        out.print("\n");
        out.flush();
        //     Provider
        out.print("\n");
        out.flush();
        Provider myProvider = getProvider(p);
        m.marshal(myProvider, out);
        out.print("\n");
        out.flush();
        //     AuditCollection
        AuditCollection myAuditCollection = getAuditCollection(p);
        m.marshal(myAuditCollection, out);
        out.print("\n");
        out.flush();
        //     AnalysisSampleCollection
        out.print("\n");
        out.flush();
        AnalysisSampleCollection asc = getAnalysisSampleCollection(p);
        m.marshal(asc, out);
        out.print("\n");
        out.flush();
        //     SequenceCollection
        out.print("\n");
        out.flush();
        out.print("<SequenceCollection>");
        out.print("\n");
        out.flush();
        //          DBSequence
        List<DBSequence> dbsList = getDBSequenceList(p);
        for (DBSequence dbs: dbsList)
        {
            m.marshal(dbs, out);
            out.print("\n");
            out.flush();
        }

        //          Peptide
        List<Peptide> pepList = getPeptideList(p);
        for (Peptide pep : pepList)
        {
            m.marshal(pep, out);
            out.print("\n");
            out.flush();
        }
        //          PeptideEvidence
        List<PeptideEvidence> pepEvList = getPeptideEvidenceList(p);
        for (PeptideEvidence pepEv: pepEvList)
        {
            m.marshal(pepEv, out);
            out.print("\n");
            out.flush();
        }
        out.print("\n");
        out.flush();
        out.print("</SequenceCollection>");
        out.print("\n");
        out.flush();
        //     AnalysisCollection
        AnalysisCollection ac = getAnalysisCollection(p);
        m.marshal(ac, out);
        out.print("\n");
        out.flush();
        //     AnalysisProtocolCollection
        out.print("\n");
        out.flush();
        AnalysisProtocolCollection apc = getAnalysisProtocolCollection(p);
        m.marshal(apc,out);
        out.print("\n");
        out.flush();

        //     DataCollection
        out.print("\n");
        out.flush();
        DataCollection dc = getDataCollection(p);
        m.marshal(dc, out);
        out.print("\n");
        out.flush();
        //     BibliographicReference
        List<BibliographicReference> biblio = getBibliographicReference();
        out.print("\n");
        out.flush();
        for(BibliographicReference b : biblio)
        {
            m.marshal(b, out);
            out.print("\n");
            out.flush();
        }

        // /mzIdentML
        out.print("\n");
        out.flush();
        out.print(m.createMzIdentMLClosingTag());
        out.flush();

	}

    private AnalysisSampleCollection getAnalysisSampleCollection(DBProject p)
    {
        /*
        <AnalysisSampleCollection>
            <Sample id="sample1" name="name23">
                <ContactRole contact_ref="ORG_MSL">
                    <Role>
                        <cvParam accession="MS:1001267" name="software vendor" cvRef="PSI-MS"/>
                    </Role>
                </ContactRole>
                <ContactRole contact_ref="ORG_MSL">
                    <Role>
                        <cvParam accession="MS:1001267" name="software vendor" cvRef="PSI-MS"/>
                    </Role>
                </ContactRole>
                <userParam name="name29" value="value15" unitAccession="unitAccession15" unitName="unitName15" unitCvRef="UO"/>
                <cvParam cvRef="UO" accession="accession15" name="name31" value="value17" unitAccession="unitAccession17"
                unitName="unitName17" unitCvRef="UO"/>
            </Sample>
            <Sample id="sample2" name="name24">
            <SubSample sample_ref="sample1"/>
            </Sample>
        </AnalysisSampleCollection>
        */

        AnalysisSampleCollection asc = new AnalysisSampleCollection();

        List<Workflow> runs = IQDBUtils.getWorkflows(p);
        int nRuns = runs.size();

        for (int i = 0; i < nRuns; i++) {
            Workflow run = runs.get(i);
            Sample s = new Sample();
            s.setId("sample_" + Integer.toString(i));
            s.setName(XJava.decURL(run.sample_description));
            asc.getSample().add(s);
        }

        return asc;
    }

    private List<Cv> getCVList(PrintStream out)
	{
        MzIdentMLMarshaller m = new MzIdentMLMarshaller();

        List<Cv> cvList = new ArrayList<Cv>();

        cvList.add(CvPSIMS);
        cvList.add(CvUNIMOD);
        cvList.add(CvUO);

        return cvList;
	}

    /*
    private ContactRole getContactRoleKuhavev()
    {
        ContactRole cr = new ContactRole();
        cr.getContact().setId("Kuharev");
        cr.getContact().setName("Jörg Kuharev");

        cr.setContact()
        Role r = new Role();
        CvParam cvp_role = getthisCVparam(CvTermReference.MS_PROGRAMMER, getthisCv(CvReference.PSI_MS), "");
        r.setCvParam(cvp_role);

        return cr;
    }
    */

	private List<AnalysisSoftware> getAnalysisSoftwareList(DBProject p)
	{
		MySQL db = p.mysql;

        List<AnalysisSoftware> analSoftList = new ArrayList<AnalysisSoftware>();

        AnalysisSoftware as_plgs = new AnalysisSoftware();
        as_plgs.setVersion("version");
        as_plgs.setUri("url");
        as_plgs.setId("data");
        as_plgs.setName("PLGS");
        Param plgs_name = new Param();
        UserParam userp_plgsname = new UserParam();
        userp_plgsname.setName("PLGS");
        plgs_name.setParam(userp_plgsname);

        as_plgs.setSoftwareName(plgs_name);
        as_plgs.setCustomizations("no customizations");

        //Software from workflow_metadata
        String massspectrum_software_name = db.getFirstValue("workflow_metadata", "value", "name = 'PROGRAM_NAME' AND type='MassSpectrum'");
        List<String> massspectrum_software_versions = db.getStringValues("workflow_metadata", "value", "name = 'PROGRAM_VERSION' AND type='MassSpectrum' GROUP BY value");
        for(String v:massspectrum_software_versions)
        {
            AnalysisSoftware massspectrum_software = new AnalysisSoftware();
            massspectrum_software.setVersion(v);
            massspectrum_software.setId(massspectrum_software_name + "_" + v);
            massspectrum_software.setName(massspectrum_software_name);
            Param masssp_sfw_name = new Param();
            UserParam usp_masssp_sfw_name = new UserParam();
            usp_masssp_sfw_name.setName(massspectrum_software_name);
            masssp_sfw_name.setParam(usp_masssp_sfw_name);
            massspectrum_software.setSoftwareName(masssp_sfw_name);

            analSoftList.add(massspectrum_software);
        }

        String workflow_software_name = db.getFirstValue("workflow_metadata", "value", "name = 'PROGRAM_NAME' AND type='Workflow'");
        List<String> workflow_software_versions = db.getStringValues("workflow_metadata", "value", "name = 'PROGRAM_VERSION' AND type='Workflow' GROUP BY value");

        for(String v:workflow_software_versions)
        {
            AnalysisSoftware workflow_software = new AnalysisSoftware();
            workflow_software.setVersion(v);
            workflow_software.setId("as_peptideIdentificationSoftware");
            workflow_software.setName(workflow_software_name + "_" + v);
            Param wflow_sfw_name = new Param();
            UserParam usp_wflow_sfw_name = new UserParam();
            usp_wflow_sfw_name.setName(workflow_software_name);
            wflow_sfw_name.setParam(usp_wflow_sfw_name);
            workflow_software.setSoftwareName(wflow_sfw_name);

            analSoftList.add(workflow_software);
        }

        AnalysisSoftware as_IsoQuant = new AnalysisSoftware();

        as_IsoQuant.setVersion(Defaults.version());
        as_IsoQuant.setUri("http://www.isoquant.de");
        as_IsoQuant.setId("isoquant");
		as_IsoQuant.setName( "ISOQuant" );
        //as_IsoQuant.setContactRole(getContactRoleKuhavev());
        Param iq_name = new Param();
        UserParam usp_iqname = new UserParam();
		usp_iqname.setName( "ISOQuant" );
        iq_name.setParam(usp_iqname);
        as_IsoQuant.setSoftwareName(iq_name);

        //analSoftList.add(as_plgs);
        analSoftList.add(as_IsoQuant);

        return analSoftList;
	}
	
	private Provider getProvider(DBProject p)
	{
		/*
	    <!-- Provider of the document in here, this can be hard-coded, with a reference to a Person in AuditCollection (see below) -->
	    <Provider id="PROVIDER">
	        <ContactRole contact_ref="PERSON_DOC_OWNER">
	            <Role>
	                <cvParam accession="MS:1001271" cvRef="PSI-MS" name="researcher"/>
	            </Role>
	        </ContactRole>
	    </Provider>
	    */



        AbstractContact OwnerContact = new AbstractContact() {
            @Override
            public List<AbstractParam> getParamGroup() {
                return super.getParamGroup();
            }
        };

        OwnerContact.setId("PERSON_DOC_OWNER");
        OwnerContact.setName("researcher");

        Provider prv = new Provider();
        ContactRole cr = new ContactRole();
        Role rl = new Role();
        CvParam cvpRole = getthisCVparam(cvResearcher, CvPSIMS, researcher_firstName + " " + researcher_lastName);

        rl.setCvParam(cvpRole);
        cr.setContact(OwnerContact);
        cr.setRole(rl);

        prv.setContactRole(cr);
        prv.setId("PROVIDER");

        return prv;
	}
	
	private AuditCollection getAuditCollection(DBProject p)
	{
		/*
        <!-- Minimally insert contact details in here for the Provider of the document if known, otherwise provide dummy values -->
        <AuditCollection>
        <Person firstName="Andy" lastName="Jones" id="PERSON_DOC_OWNER">
        <Affiliation organization_ref="ORG_DOC_OWNER"/>
        </Person>
        <Organization name="University of Liverpool" id="ORG_DOC_OWNER"/>
        </AuditCollection>
        <SequenceCollection>
        */

        String odo = "ORG_DOC_OWNER";
        String pdo = "PERSON_DOC_OWNER";

        AuditCollection ac = new AuditCollection();
        Person dummy = new Person();
        dummy.setId(pdo);
        dummy.setName(researcher_firstName + " " + researcher_lastName);
        dummy.setFirstName(researcher_firstName);
        dummy.setLastName(researcher_lastName);
        //dummy.setMidInitials("M");
        Affiliation res_affiliation = new Affiliation();
        Organization org_doc_owner = new Organization();
        org_doc_owner.setId(odo);
        org_doc_owner.setName(researcher_organization);
        res_affiliation.setOrganization(org_doc_owner);
        ac.getOrganization().add(org_doc_owner);
        ac.getPerson().add(dummy);

        return ac;
	}

    private CvParam getthisCVparam(CvTermReference cvtermref, Cv termCv, String value)
    {
        CvParam myCvparam = new CvParam();

        myCvparam.setAccession(cvtermref.getAccession());
        myCvparam.setName(cvtermref.getName());
        if (value.length() > 0)
            myCvparam.setValue(value);
        myCvparam.setCv(termCv);

        return myCvparam;
    }

    private CvParam getthisCVparam(CvTermReference cvtermref, Cv termCv, CvTermReference unitCvTerm, String value)
    {
        CvParam myCvparam = new CvParam();

        myCvparam.setAccession(cvtermref.getAccession());
        myCvparam.setName(cvtermref.getName());
        myCvparam.setValue(value);
        myCvparam.setCv(termCv);
        myCvparam.setUnitAccession(unitCvTerm.getAccession());
        myCvparam.setUnitCv(termCv);
        myCvparam.setUnitName(unitCvTerm.getName());

        return myCvparam;
    }

    private UserParam getthisUserParam(String type, String name, String value)
    {
        UserParam up = new UserParam();

        up.setType(type);
        up.setName(name);
        up.setValue(value);

        return up;
    }

    private Cv getthisCv(CvReference cvref)
    {
        Cv myCv = new Cv();
        myCv.setId(cvref.getId());
        myCv.setFullName(cvref.getFullName());
        myCv.setVersion(cvref.getVersion());
        myCv.setUri(cvref.getUri());

        return myCv;
    }


    private SearchDatabase getDataBaseObj(String version, String dbname, String dbId, Long numDBsequences,
                                          String organismSciName, String ncbiTaxId, String proteinDesc ){

        SearchDatabase searchDB = new SearchDatabase();
        if(version!=null)
            searchDB.setVersion(version);
        if(dbname!=null)
            searchDB.setName(dbname);
        if(dbId!=null)
            searchDB.setId(dbId);

        Param dbparams = new Param();
        if (organismSciName!=null)
            dbparams.setParam(getthisCVparam(cvSciName, CvPSIMS, organismSciName));
        if (ncbiTaxId!=null)
            dbparams.setParam(getthisCVparam(cvTaxID, CvPSIMS, ncbiTaxId));
        if(proteinDesc!=null)
            dbparams.setParam(getthisCVparam(cvProtDesc, CvPSIMS, proteinDesc));
        if(numDBsequences>0)
            searchDB.setNumDatabaseSequences(numDBsequences);

        searchDB.setDatabaseName(dbparams);

        return searchDB;
    }

	private List<DBSequence> getDBSequenceList(DBProject p)
	{
        MySQL db = p.mysql;

		List<DBSequence> dbsList = new ArrayList<DBSequence>();

        // <DBSequence id="DBSeq_HSP7C_RAT" length="646" searchDatabase_ref="SDB_SwissProt" accession="HSP7C_RAT">
        // <Seq>MSKGPAVGIDLGTTYSCVGVFQHGKVEIIANDQGNRTTPSYVAFTDTERLIGDAAKNQVAMNPTNTVFDAKRLIGRRFDDAVVQSDMKHWPFMVVNDAGRPKVQVEYKGETKSFYPEEVSSMVLTKMKEIAEAYLGKTVTNAVVTVPAYFNDSQRQATKDAGTIAGLNVLRIINEPTAAAIAYGLDKKVGAERNVLIFDLGGGTFDVSILTIEDGIFEVKSTAGDTHLGGEDFDNRMVNHFIAEFKRKHKKDISENKRAVRRLRTACERAKRTLSSSTQASIEIDSLYEGIDFYTSITRARFEELNADLFRGTLDPVEKALRDAKLDKSQIHDIVLVGGSTRIPKIQKLLQDFFNGKELNKSINPDEAVAYGAAVQAAILSGDKSENVQDLLLLDVTPLSLGIETAGGVMTVLIKRNTTIPTKQTQTFTTYSDNQPGVLIQVYEGERAMTKDNNLLGKFELTGIPPAPRGVPQIEVTFDIDANGILNVSAVDKSTGKENKITITNDKGRLSKEDIERMVQEAEKYKAEDEKQRDKVSSKNSLESYAFNMKATVEDEKLQGKINDEDKQKILDKCNEIISWLDKNQTAEKEEFEHQQKELEKVCNPIITKLYQSAGGMPGGMPGGFPGGGAPPSGGASSGPTIEEVD</Seq>
        // <cvParam accession="MS:1001088" name="protein description" cvRef="PSI-MS" value="Heat shock cognate 71 kDa protein (Heat shock 70 kDa protein 8) - Rattus norvegicus (Rat)"/>
        // <cvParam accession="MS:1001469" name="taxonomy: scientific name" cvRef="PSI-MS" value="Rattus norvegicus"/>
        // <cvParam accession="MS:1001467" name="taxonomy: NCBI TaxID" cvRef="PSI-MS" value="10116"/>
        // </DBSequence>

        db.executeSQLFile(getPackageResource(sql_mzid_DbSequence));

        try
        {
            ResultSet rs = db.getStatement().getResultSet();
            while (rs.next())
            {
                DBSequence dbs = new DBSequence();
                String index = "DBS_" + Integer.toString(rs.getInt("index"));
				dbs.setSeq( rs.getString( "sequence" ) );
                dbs.setId(index);
				dbs.setLength( rs.getInt( "seqlength" ) );
				dbs.setAccession( rs.getString( "accession" ) );
                dbs.setName(index);
                String proteinDesc = rs.getString("description");
                SearchDatabase searchDB = getDataBaseObj(dbversion, dbname, dbId, numDBsequences, orgSciName, ncbiTaxId, proteinDesc);
                dbs.setSearchDatabase(searchDB);

                CvParam cvp_protdesc = getthisCVparam(cvProtDesc, CvPSIMS, proteinDesc);
                dbs.getCvParam().add(cvp_protdesc);

                if(ncbiTaxId!=""){
                    CvParam cvp_taxid = getthisCVparam(cvTaxID, CvPSIMS, ncbiTaxId);
                    dbs.getCvParam().add(cvp_taxid);
                }
                if (orgSciName!="") {
                    CvParam cvp_taxonomy = getthisCVparam(cvTaxSciName, CvPSIMS, orgSciName);
                    dbs.getCvParam().add(cvp_taxonomy);
                }

                dbsList.add(dbs);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return dbsList;
	}

    private Modification getthisModification(String plgs_code, Integer location)
    {
        /*
        <Modification location="1" residues="M" monoisotopicMassDelta="15.994919">
        <cvParam accession="UNIMOD:35" name="Oxidation" cvRef="UNIMOD"/>
        <cvParam accession="MS:1001524" name="fragment neutral loss" cvRef="PSI-MS" value="0"
        unitAccession="UO:0000221" unitName="dalton" unitCvRef="UO"/>
        */

        Cv CvUnimod = getthisCv(CvReference.UNIMOD);
        Cv CvUO = getthisCv(CvReference.UNIT_ONTOLOGY);

        Modification mod = new Modification();
		// TODO: something to change here, it should be a single instance for
		// the app

		List<PeptideModification> modList = modificationReference.getPeptideModsByPLGScode( plgs_code );
		if (modList.size() == 0)
		{
			System.err.println( "unknown modification: " + plgs_code );
		}
		PeptideModification pmod = ( modList.size() > 0 ) ? modList.get( 0 ) : modificationReference.undefinedModification;

		// System.out.println(plgs_code);
        ModificationCvParam modCv = new ModificationCvParam();
        modCv.setAccession(pmod.getUnimod_Id());
        modCv.setName(pmod.getUnimod_code_name());
        modCv.setCv(CvUnimod);
        mod.setMonoisotopicMassDelta(pmod.getMonoisotopicMassDelta());
        mod.setLocation(location);

        mod.getCvParam().add(modCv);

        return mod;
    }



    private List<Modification> parseModificationsFromPLGS(String modifier)
    {
        //modifier
        // 'Carbamidomethyl+C(21), Carbamidomethyl+C(24)'
        List<Modification> modList = new ArrayList<Modification>();

        String[] mods = modifier.split(",");

        for (String m: mods)
        {
			// String[] modpos = m.split("\\+");
			String[] modpos = m.split( "\\(" );
			String plgscode = modpos[0].trim().replaceAll( "\\s+.*", "" ).replaceAll( "\\+.*", "" );
			// String[] p = modpos[1].split("[\\(||\\)]");
			String p = modpos[1].replaceAll( "[^0-9]", "" );
			Integer pos = Integer.parseInt( p );
            Modification mzid_mod = getthisModification(plgscode, pos);
            modList.add(mzid_mod);
        }
        return modList;
    }



    private List<Peptide> getPeptideList(DBProject p)
	{
        /*
        <Peptide id="peptide_3_9">
        <PeptideSequence>MSKPAGSTSRILDIPCK</PeptideSequence>
        <Modification location="0" monoisotopicMassDelta="127.063324">
        <cvParam accession="UNIMOD:29" name="SMA" cvRef="UNIMOD"/>
        </Modification>
        <Modification location="1" residues="M" monoisotopicMassDelta="15.994919">
        <cvParam accession="UNIMOD:35" name="Oxidation" cvRef="UNIMOD"/>
        <cvParam accession="MS:1001524" name="fragment neutral loss" cvRef="PSI-MS" value="0"
        unitAccession="UO:0000221" unitName="dalton" unitCvRef="UO"/>
        </Modification>
        </Peptide>
        */

        MySQL db = p.mysql;

        List<Peptide> pepList = new ArrayList<Peptide>();

        db.executeSQLFile(getPackageResource(sql_mzid_Peptide));

        try
        {
            ResultSet rs = db.getStatement().getResultSet();
            while (rs.next())
            {
                Peptide pep = new Peptide();

                pep.setPeptideSequence(rs.getString("sequence"));

                //Modifications
                String modifier = rs.getString("modifier");
                List<Modification>  mods = null;
                if(modifier.length()>0)
                {
                    mods = parseModificationsFromPLGS(modifier);
                    for(Modification m : mods)
                    {
                        pep.getModification().add(m);
                    }
                }

                String pepId = "PEP_" + Integer.toString(rs.getInt("index"));
                pep.setId(pepId);

                pepList.add(pep);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

		return pepList;
	}
	
	private List<PeptideEvidence> getPeptideEvidenceList(DBProject p)
	{

		// table peptides_in_proteins_before_homology_filtering
        /*
        <PeptideEvidence isDecoy="false" post="L" pre="R" end="312" start="291"
        peptide_ref="KDLYGNVVLSGGTTMYEGIGER_1@14"
        dBSequence_ref="dbseq_psu|NC_LIV_020800"
        id="PE5_2_6"/>
        */

        MySQL db = p.mysql;

        List<PeptideEvidence> pepEvList = new ArrayList<PeptideEvidence>();

        db.executeSQLFile(getPackageResource(sql_mzid_PeptideEvidence));

        try
        {
            ResultSet rs = db.getStatement().getResultSet();
            while (rs.next()) {
                PeptideEvidence pe = new PeptideEvidence();
                String pepEvId = "PE_" + rs.getString("id");
                pe.setId(pepEvId);
                pe.setName(pepEvId);

                DBSequence dbSequence = new DBSequence();
                String dbSeqId = "DBS_" + Integer.toString(rs.getInt("dbSequence_ref"));
                dbSequence.setId(dbSeqId);
                pe.setDBSequence(dbSequence);

                pe.setIsDecoy(rs.getBoolean("is_decoy"));
                Peptide pep = new Peptide();
                String pepId = "PEP_" + Integer.toString(rs.getInt("peptide_ref"));
                pep.setId(pepId);
                pe.setPeptide(pep);

                pe.setPost(rs.getString("post"));
                pe.setPre(rs.getString("pre"));
                pe.setStart(rs.getInt("start"));
                pe.setEnd(rs.getInt("end"));

                pepEvList.add(pe);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

		return pepEvList;
	}

	
	private AnalysisCollection getAnalysisCollection(DBProject p)
	{
		/*
	    <!-- AnalysisCollection maps input and output data  -->
	    <AnalysisCollection>
	        <SpectrumIdentification spectrumIdentificationList_ref="SII_LIST_1"
	            spectrumIdentificationProtocol_ref="SearchProtocol_1" id="SpecIdent_1">
	            <InputSpectra spectraData_ref="SID_1"/>
	            <SearchDatabaseRef searchDatabase_ref="SearchDB_1"/>
	        </SpectrumIdentification>

	        <ProteinDetection proteinDetectionList_ref="PDL_1" proteinDetectionProtocol_ref="PDL1"
	            id="PD1">
	            <InputSpectrumIdentifications spectrumIdentificationList_ref="SII_LIST_1"/>
	        </ProteinDetection>

	    </AnalysisCollection>
	    */

        AnalysisCollection ac = new AnalysisCollection();
        ProteinDetection pd = new ProteinDetection();
		pd.setId( "PDP_ISOQuant" );
		pd.setName( "ISOQuant_ProteinHomology" );
        ProteinDetectionList pdl = new ProteinDetectionList();
        pdl.setId("PDL_1");
        pd.setProteinDetectionList(pdl);
        ProteinDetectionProtocol pdp = new ProteinDetectionProtocol();
        pdp.setId("PDP_1");
        pd.setProteinDetectionProtocol(pdp);

        List<Workflow> runs = IQDBUtils.getWorkflows(p);
        int nRuns = runs.size();

        for (int i = 0; i < nRuns; i++) {
            Workflow run = runs.get(i);
            SpectrumIdentification si = new SpectrumIdentification();
            si.setId("SI_" + run.index);
            SpectrumIdentificationList sil = new SpectrumIdentificationList();
            sil.setId("SIL_" + run.index);
            si.setSpectrumIdentificationList(sil);
            SpectrumIdentificationProtocol sip = new SpectrumIdentificationProtocol();
            sip.setId("SIP_" + run.index);

            AnalysisSoftware as = new AnalysisSoftware();
            as.setId("as_peptideIdentificationSoftware"); //TODO: in the future, adapt to multiple identification software (versions)
            sip.setAnalysisSoftware(as);

            si.setSpectrumIdentificationProtocol(sip);
            SearchDatabaseRef sdbr = new SearchDatabaseRef();
            SearchDatabase sdb = new SearchDatabase();
            sdb.setId(dbId); // TODO: in the future, we should change this to many possible DBs.
            sdbr.setSearchDatabase(sdb);
            si.getSearchDatabaseRef().add(sdbr);

            InputSpectra is = new InputSpectra();
            SpectraData sd = new SpectraData();
            sd.setId("SD_" + run.index);
            is.setSpectraData(sd);
            si.getInputSpectra().add(is);

            ac.getSpectrumIdentification().add(si);

            InputSpectrumIdentifications isi = new InputSpectrumIdentifications();
            isi.setSpectrumIdentificationList(sil);
            pd.getInputSpectrumIdentifications().add(isi);
        }

        ac.setProteinDetection(pd);

        return ac;
	}
	
	private AnalysisProtocolCollection getAnalysisProtocolCollection(DBProject p)
	{
        AnalysisProtocolCollection apc = new AnalysisProtocolCollection();

        //SpectrumIdentificationProtocol
        List<SpectrumIdentificationProtocol> sips = getSpectrumIdentificationProtocols(p);

        for(SpectrumIdentificationProtocol sip: sips)
        {
            apc.getSpectrumIdentificationProtocol().add(sip);
        }
        //ProteinDetectionProtocol
        ProteinDetectionProtocol pdp = getProteinDetectionProtocol(p);
        apc.setProteinDetectionProtocol(pdp);
        //apc.getSpectrumIdentificationProtocols().add();

        return apc;
	}


    private List<SpectrumIdentificationProtocol> getSpectrumIdentificationProtocols(DBProject p)
    {
        /*
        <SpectrumIdentificationProtocol analysisSoftware_ref="ID_software" id="SearchProtocol_1">
        <SearchType>
        <cvParam accession="MS:1001083" cvRef="PSI-MS" name="ms-ms search"/>
        </SearchType>
        <AdditionalSearchParams>
        <cvParam accession="MS:1001211" cvRef="PSI-MS" name="parent mass type mono"/>
        <cvParam accession="MS:1001256" cvRef="PSI-MS" name="fragment mass type mono"/>
        </AdditionalSearchParams>
        <ModificationParams>
        <SearchModification residues="C" massDelta="57.021465" fixedMod="true">
        <cvParam accession="UNIMOD:4" cvRef="UNIMOD" name="Carbamidomethyl"/>
        </SearchModification>
        <SearchModification residues="M" massDelta="15.994915" fixedMod="false">
        <cvParam accession="UNIMOD:35" cvRef="UNIMOD" name="Oxidation"/>
        </SearchModification>
        </ModificationParams>
        <Enzymes independent="false">
        <Enzyme missedCleavages="1" semiSpecific="false" cTermGain="OH" nTermGain="H"
        id="Enz1">
        <EnzymeName>
        <cvParam accession="MS:1001251" cvRef="PSI-MS" name="Trypsin"/>
        </EnzymeName>
        </Enzyme>
        </Enzymes>
        <FragmentTolerance>
        <cvParam accession="MS:1001412" cvRef="PSI-MS" unitCvRef="UO" unitName="dalton"
        unitAccession="UO:0000221" value="0.8" name="search tolerance plus value"/>
        <cvParam accession="MS:1001413" cvRef="PSI-MS" unitCvRef="UO" unitName="dalton"
        unitAccession="UO:0000221" value="0.8" name="search tolerance minus value"/>
        </FragmentTolerance>
        <ParentTolerance>
        <cvParam accession="MS:1001412" cvRef="PSI-MS" unitCvRef="UO" unitName="dalton"
        unitAccession="UO:0000221" value="1.5" name="search tolerance plus value"/>
        <cvParam accession="MS:1001413" cvRef="PSI-MS" unitCvRef="UO" unitName="dalton"
        unitAccession="UO:0000221" value="1.5" name="search tolerance minus value"/>
        </ParentTolerance>

        <!-- Must include a threshold value, even if no threshold. Also can include FDR, p-value etc. -->
        <Threshold>
        <cvParam accession="MS:1001494" cvRef="PSI-MS" name="no threshold"/>
        </Threshold>
        </SpectrumIdentificationProtocol>
        */

        //SpectrumIdentificationProtocol
            //SearchType
            //AdditionalSearchParams
                //<cvParam accession="MS:1001211" cvRef="PSI-MS" name="parent mass type mono"/>
                //<cvParam accession="MS:1001256" cvRef="PSI-MS" name="fragment mass type mono"/>
            //ModificationParams
                //SearchModification residues="C" massDelta="57.021465" fixedMod="true">
                    //cvParam accession="UNIMOD:4" cvRef="UNIMOD" name="Carbamidomethyl"/>
            //Enzymes independent="false"
                //Enzyme missedCleavages="1" semiSpecific="false" cTermGain="OH" nTermGain="H" id="Enz1">
                    //EnzymeName
                        //cvParam accession="MS:1001251" cvRef="PSI-MS" name="Trypsin"/>
            //FragmentTolerance
                //cvParam accession="MS:1001412" cvRef="PSI-MS" unitCvRef="UO" unitName="dalton" unitAccession="UO:0000221" value="0.8" name="search tolerance plus value"/>
                //<cvParam accession="MS:1001413" cvRef="PSI-MS" unitCvRef="UO" unitName="dalton" unitAccession="UO:0000221" value="0.8" name="search tolerance minus value"/>
            //ParentTolerance
                //cvParam accession="MS:1001412" cvRef="PSI-MS" unitCvRef="UO" unitName="dalton" unitAccession="UO:0000221" value="1.5" name="search tolerance plus value"/>
                //cvParam accession="MS:1001413" cvRef="PSI-MS" unitCvRef="UO" unitName="dalton" unitAccession="UO:0000221" value="1.5" name="search tolerance minus value"/>

            //<!-- Must include a threshold value, even if no threshold. Also can include FDR, p-value etc. -->
            //Threshold
                //cvParam accession="MS:1001494" cvRef="PSI-MS" name="no threshold"/>



        List<Workflow> runs = IQDBUtils.getWorkflows(p);
        int nRuns = runs.size();

        List<SpectrumIdentificationProtocol> sips = new ArrayList<>();

        for(int i=0; i<nRuns;i++)
        {
            Workflow run = runs.get(i);
            Map<String, String>  wfMetaInfo = IQDBUtils.getWorkflowMetaInfo(p, run, "Workflow");

            SpectrumIdentificationProtocol sip = new SpectrumIdentificationProtocol();
            sip.setId("SIP_" + run.index);
            sip.setName("SIP_" + run.acquired_name);
            AnalysisSoftware as = new AnalysisSoftware();
            as.setId("as_peptideIdentificationSoftware"); //TODO: in the future, adapt to multiple identification software (versions)
            sip.setAnalysisSoftware(as);
            //SearchType
            SearchTypeUserParam upSearchType = new SearchTypeUserParam();  //TODO: this should be a CvParam
            upSearchType.setType("xsd:string");
            upSearchType.setName("ion accounting search");
            Param upst = new Param();
            upst.setParam(upSearchType);
            sip.setSearchType(upst);

            //AdditionalSearchParams
            ParamList pl = new ParamList();
            if(wfMetaInfo.containsKey("NUM_BY_MATCH_FOR_PEPTIDE_MINIMUM"))
            {
                UserParam up = new UserParam();
                up.setType("xsd:unsignedInt");
                up.setName("NUM_BY_MATCH_FOR_PEPTIDE_MINIMUM");
                up.setValue(wfMetaInfo.get("NUM_BY_MATCH_FOR_PEPTIDE_MINIMUM"));
                pl.getUserParam().add(up);
            }
            if(wfMetaInfo.containsKey("NUM_BY_MATCH_FOR_PROTEIN_MINIMUM"))
            {
                UserParam up = new UserParam();
                up.setType("xsd:unsignedInt");
                up.setName("NUM_BY_MATCH_FOR_PROTEIN_MINIMUM");
                up.setValue(wfMetaInfo.get("NUM_BY_MATCH_FOR_PROTEIN_MINIMUM"));
                pl.getUserParam().add(up);
            }
            if(wfMetaInfo.containsKey("NUM_PEPTIDE_FOR_PROTEIN_MINIMUM"))
            {
                UserParam up = new UserParam();
                up.setType("xsd:unsignedInt");
                up.setName("NUM_PEPTIDE_FOR_PROTEIN_MINIMUM");
                up.setValue(wfMetaInfo.get("NUM_PEPTIDE_FOR_PROTEIN_MINIMUM"));
                pl.getUserParam().add(up);
            }
            sip.setAdditionalSearchParams(pl);

            //ModificationParams
            if(wfMetaInfo.containsKey("MODIFICATIONS"))
            {
                ModificationParams modParams = new ModificationParams();

                String[] mods = XJava.decURL(wfMetaInfo.get("MODIFICATIONS")).split(";");
                for (String m : mods)
                {
                    boolean cterm = false;
                    boolean nterm = false;
                    Map<String, String> modinfo =  XJava.decMap(m, ",", ":");
                    String modName = modinfo.get("name").split("\\+")[0];
                    String modResidues = modinfo.get("applies_to");
                    String modFixedMod = modinfo.get("status");
                    char[] modResidues_char = modResidues.toCharArray();

                    if(modResidues.equals("C-TERM"))
                    {
                        modResidues = "-";
                        cterm = true;
                    }

                    if(modResidues.equals("N-TERM"))
                    {
                        modResidues = "-";
                        nterm = true;
                    }

					/**
					 * TODO: change here bla
					 * getPeptideModsByPLGScode retreives multiple items
					 * but we need only one modification
					 * where is the mass shift???
					 */
                    List<PeptideModification> modList = modificationReference.getPeptideModsByPLGScode( modName );
					if (modList.size() == 0) System.err.println( "undefined modification: " + modName );
					PeptideModification peptideMod = modList.size() > 0 ? modList.get( 0 ) : modificationReference.undefinedModification;

                    try {
                        for(char mod_aa: modResidues_char)
                        {
							// problem not to have the modification
							if (peptideMod == null) throw new Exception( "undefined modification " + modName );

                            String unimod_id = peptideMod.getUnimod_Id();
                            String um_codename = peptideMod.getUnimod_code_name();

                            SearchModification searchMod = new SearchModification();
                            searchMod.setFixedMod(true);
                            if (modFixedMod.equals("VARIABLE"))
                                searchMod.setFixedMod(false);

                            searchMod.setMassDelta(Float.parseFloat(Double.toString(peptideMod.getMonoisotopicMassDelta())));
                            searchMod.getResidues().add(String.valueOf(mod_aa));

                            CvParam cvModUnimod = new CvParam();
                            cvModUnimod.setCv(CvUNIMOD);
                            cvModUnimod.setName(um_codename);
                            cvModUnimod.setAccession(unimod_id);
                            searchMod.getCvParam().add(cvModUnimod);

                            if(cterm | nterm)
                            {
                                SpecificityRules sprules = new SpecificityRules();
                                SpecificityRulesCvParam spcv = new SpecificityRulesCvParam();
                                spcv.setCv(getthisCv(CvReference.PSI_MS));
                                //<cvParam accession="MS:1001189" cvRef="PSI-MS" name="modification specificity N-term"/>
                                spcv.setAccession("MS:1001189");
                                spcv.setName("modification specificity N-term");
                                if(cterm)
                                {
                                    spcv.setAccession("MS:1001190");
                                    spcv.setName("modification specificity peptide C-term");
                                }
                                sprules.getCvParam().add(spcv);
                            }

                            modParams.getSearchModification().add(searchMod);
                        }
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                sip.setModificationParams(modParams);
            }

            //Enzymes independent="false"
            Enzymes enzs = new Enzymes();
            Enzyme enz = new Enzyme();
            //enz.setId();
            enz.setName("from a CV term");

            //FragmentTolerance
            //ParentTolerance

            //Threshold
            ParamList thresholds = new ParamList();

            ThresholdCvParam thr = new ThresholdCvParam();
            CvParam param = getthisCVparam(cvPepFDR, CvPSIMS, peptideFDR);
            thr.setCv(CvPSIMS);
            thr.setAccession(param.getAccession());
            thr.setName(param.getName());
            thr.setValue(param.getValue());
            thresholds.getCvParam().add(thr);
            sip.setThreshold(thresholds);

            sips.add(sip);
        }


        /*

        1	Workflow	AcquiredDate	30-Dec-1899
        1	Workflow	AcquiredName	T140802_39
        1	Workflow	AcquiredTime	00%3A00%3A00
        1	Workflow	AMINO_ACID_SEQUENCE_DIGESTOR	Trypsin
        1	Workflow	ENTRIES_SEARCHED	4015
        1	Workflow	FASTA_FORMAT	Long+Description
        1	Workflow	InstrumentModel	SYNAPT+G2-S
        1	Workflow	InstrumentSerialNumber	UEB073K
        1	Workflow	MISSED_CLEAVAGES	2
        1	Workflow	MODIFICATIONS	name%3ACarbamidomethyl%2BC%2Cstatus%3AFIXED%2Cenriched%3AFALSE%2Capplies_to%3AC%2Cdelta_mass%3A57.0215%3Bname%3AOxidation%2BM%2Cstatus%3AVARIABLE%2Cenriched%3AFALSE%2Capplies_to%3AM%2Cdelta_mass%3A15.9949%3Bname%3APhosphoryl%2BSTY%2Cstatus%3AVARIABLE%2Cenriched%3ATRUE%2Capplies_to%3ASTY%2Cdelta_mass%3A79.9663
        1	Workflow	PROGRAM_BUILD_DATE	11%2F20%2F2012+12%3A21+PM
        1	Workflow	PROGRAM_COMMAND_LINE	-paraXMLFileName+C%3A%5CUsers%5Cstefan%5CAppData%5CLocal%5CTemp%5Cplgs8352481121462682873.params+-pep3DFilename+C%3A%5CUsers%5Cstefan%5CAppData%5CLocal%5CTemp%5Cplgs8352481121462682873.bin+-proteinFASTAFileName+D%3A%5Cdata%5Cplgs302b5%5CSequence_Databases%5Cpeptidelibraries%5CDC_mouse_peptide_library%5CT140802_33_sequences_longDescription_reverese.fas_def+-outPutDirName+C%3A%5CUsers%5Cstefan%5CAppData%5CLocal%5CTemp+-ResponseFactor+1000.0+-newWorkflowXML+-maxCPUs+31
        1	Workflow	PROGRAM_NAME	iaDBs.exe
        1	Workflow	PROGRAM_VERSION	2.135.0
        1	Workflow	RawFile	T%3A%5CT1408%5CT140802_39.raw
        1	Workflow	SampleDescription	2014-081-05+DCs+JS+Phospho*
        1	Workflow	SEARCH_DATABASE	T140802_33_DDA_IDs_mouseDC_REVERSE-1.0
        1	Workflow	SEARCH_ENGINE_TYPE	PLGS
        1	Workflow	SEARCH_TYPE	Electrospray-Shotgun
        1	Workflow	WORKFLOW_ID	_14115481740390_6654881975277439
        1	Workflow	WORKFLOW_REPLICATE_NAME
        1	Workflow	WORKFLOW_XML_FILE_PATH	D%3A%5Cdata%5Cplgs302b5%5Cprojects%5C2014-081+DC+DIA+LibSearch%5Croot%5CProj__14114839722750_6083218576473945%5C_14114840925360_7193913627949663%5C_14114840925360_7193913627949663_WorkflowResults%5C_14115481740390_6654881975277439.xml




        workflow_index	type	name	value
        1	Workflow	AcquiredDate	30-Dec-1899
        1	Workflow	AcquiredName	T140802_39
        1	Workflow	AcquiredTime	00%3A00%3A00
        1	Workflow	AMINO_ACID_SEQUENCE_DIGESTOR	Trypsin
        1	Workflow	ENTRIES_SEARCHED	4015
        1	Workflow	FASTA_FORMAT	Long+Description
        1	Workflow	InstrumentModel	SYNAPT+G2-S
        1	Workflow	InstrumentSerialNumber	UEB073K
        1	Workflow	MISSED_CLEAVAGES	2
        1	Workflow	MODIFICATIONS	name%3ACarbamidomethyl%2BC%2Cstatus%3AFIXED%2Cenriched%3AFALSE%2Capplies_to%3AC%2Cdelta_mass%3A57.0215%3Bname%3AOxidation%2BM%2Cstatus%3AVARIABLE%2Cenriched%3AFALSE%2Capplies_to%3AM%2Cdelta_mass%3A15.9949%3Bname%3APhosphoryl%2BSTY%2Cstatus%3AVARIABLE%2Cenriched%3ATRUE%2Capplies_to%3ASTY%2Cdelta_mass%3A79.9663
        1	Workflow	NUM_BY_MATCH_FOR_PEPTIDE_MINIMUM	2
        1	Workflow	NUM_BY_MATCH_FOR_PROTEIN_MINIMUM	2
        1	Workflow	NUM_PEPTIDE_FOR_PROTEIN_MINIMUM	1
        1	Workflow	PROGRAM_BUILD_DATE	11%2F20%2F2012+12%3A21+PM
        1	Workflow	PROGRAM_COMMAND_LINE	-paraXMLFileName+C%3A%5CUsers%5Cstefan%5CAppData%5CLocal%5CTemp%5Cplgs8352481121462682873.params+-pep3DFilename+C%3A%5CUsers%5Cstefan%5CAppData%5CLocal%5CTemp%5Cplgs8352481121462682873.bin+-proteinFASTAFileName+D%3A%5Cdata%5Cplgs302b5%5CSequence_Databases%5Cpeptidelibraries%5CDC_mouse_peptide_library%5CT140802_33_sequences_longDescription_reverese.fas_def+-outPutDirName+C%3A%5CUsers%5Cstefan%5CAppData%5CLocal%5CTemp+-ResponseFactor+1000.0+-newWorkflowXML+-maxCPUs+31
        1	Workflow	PROGRAM_NAME	iaDBs.exe
        1	Workflow	PROGRAM_VERSION	2.135.0
        1	Workflow	RawFile	T%3A%5CT1408%5CT140802_39.raw
        1	Workflow	SampleDescription	2014-081-05+DCs+JS+Phospho*
        1	Workflow	SEARCH_DATABASE	T140802_33_DDA_IDs_mouseDC_REVERSE-1.0
        1	Workflow	SEARCH_ENGINE_TYPE	PLGS
        1	Workflow	SEARCH_TYPE	Electrospray-Shotgun
        1	Workflow	WORKFLOW_ID	_14115481740390_6654881975277439
        1	Workflow	WORKFLOW_REPLICATE_NAME
        1	Workflow	WORKFLOW_XML_FILE_PATH	D%3A%5Cdata%5Cplgs302b5%5Cprojects%5C2014-081+DC+DIA+LibSearch%5Croot%5CProj__14114839722750_6083218576473945%5C_14114840925360_7193913627949663%5C_14114840925360_7193913627949663_WorkflowResults%5C_14115481740390_6654881975277439.xml
        1	MassSpectrum	AcquiredDate	09-Aug-14
        1	MassSpectrum	AcquiredName	T140802_39
        1	MassSpectrum	AcquiredTime	05%3A16%3A52
        1	MassSpectrum	APEX3D_BIN_INTEN_THRESHOLD	750
        1	MassSpectrum	APEX3D_ENDING_RT
        1	MassSpectrum	APEX3D_HIGH_ENERGY_THRESHOLD	25
        1	MassSpectrum	APEX3D_LOCKMASS_CHARGE_1
        1	MassSpectrum	APEX3D_LOCKMASS_CHARGE_2	785.8426
        1	MassSpectrum	APEX3D_LOCKMASS_TOLERANCE	0.25
        1	MassSpectrum	APEX3D_LOW_ENERGY_THRESHOLD	135
        1	MassSpectrum	APEX3D_MS_RESOLUTION	Automatic
        1	MassSpectrum	APEX3D_PEAK_WIDTH	Automatic
        1	MassSpectrum	APEX3D_STARTING_RT
        1	MassSpectrum	BinFractionFWHM	0.1429
        1	MassSpectrum	ChromFWHM_Min	0.2494
        1	MassSpectrum	deadTimeChannels	14
        1	MassSpectrum	InputXMLFile	T%3A%5CT1408%5CT140802_39.raw
        1	MassSpectrum	interScanTimeMin	0.0207
        1	MassSpectrum	ionDetectionThresholdCountsHE	25
        1	MassSpectrum	ionDetectionThresholdCountsLE	135
        1	MassSpectrum	IsADC	1
        1	MassSpectrum	MinHEIntenThresh
        1	MassSpectrum	MinHEMHPlus
        1	MassSpectrum	MinLEIntenThresh
        1	MassSpectrum	MinLEMHPlus	350
        1	MassSpectrum	msResolution	20008.8203
        1	MassSpectrum	PROGRAM_BUILD_DATE	9%2F25%2F2012+10%3A22+AM
        1	MassSpectrum	PROGRAM_COMMAND_LINE	null
        1	MassSpectrum	PROGRAM_NAME	Peptide3D.exe
        1	MassSpectrum	PROGRAM_VERSION	2.96.4651.16860
        1	MassSpectrum	pulsesPerSpectrum	8695
        1	MassSpectrum	pusherFrequencyHz	14493
        1	MassSpectrum	SampleDescription	2014-081-05+DCs+JS+Phospho*
        1	MassSpectrum	scanTimeSeconds	0.6
        */


        return sips;
    }

    private  ProteinDetectionProtocol getProteinDetectionProtocol(DBProject p)
    {
        /*
        <!-- A separate protocol for protein inference must be given, although there are no essential parameters. Parameters can be supplied under AnalysisParams as cvParams (preferred) or userParams. -->
        <ProteinDetectionProtocol analysisSoftware_ref="ID_software" id="PDL1">
        <AnalysisParams>
        <userParam name="Protein inference method" value="in house method"/>
        </AnalysisParams>
        <Threshold>
        <cvParam accession="MS:1001494" cvRef="PSI-MS" name="no threshold"/>
        </Threshold>
        </ProteinDetectionProtocol>
        */

        ProteinDetectionProtocol pdp = new ProteinDetectionProtocol();
        pdp.setName("PDP_1");
        pdp.setId("PDP_1");

        //AnalysisParams
        ParamList anParams = new ParamList();
        UserParam upProtDetMethod = new UserParam();
        upProtDetMethod.setName("Protein inference method");
		upProtDetMethod.setValue( "ISOQuant" );
        UserParam upResHomology = new UserParam();
        upResHomology.setType("xsd:boolean");
        upResHomology.setName("protein.resolveHomology");
        upResHomology.setValue(his_resolveHomology);
        anParams.getUserParam().add(upProtDetMethod);
        UserParam upMaxSeqCluster = new UserParam();
        upMaxSeqCluster.setType("xsd:unsignedInt");
        upMaxSeqCluster.setName("protein.maxSequencesPerEMRTCluster");
        upMaxSeqCluster.setValue(his_maxSeqCluster);
        anParams.getUserParam().add(upMaxSeqCluster);

        pdp.setAnalysisParams(anParams);
        //Thresholds
		String proteinFDR = p.mysql.getFirstValue( "history", "note", "`type` = 'parameter' AND value = 'process.quantification.maxProteinFDR' " +
                "ORDER BY time desc;");

        ThresholdUserParam thrFPR = new ThresholdUserParam();
        thrFPR.setType("xsd:float");
        thrFPR.setName("FPR");
        thrFPR.setValue(proteinFDR);

        ParamList thrList = new ParamList();
        thrList.getUserParam().add(thrFPR);

        pdp.setThreshold(thrList);

        //AnalysisSoftware
        AnalysisSoftware anSoft = getAnalysisSoftware();
        pdp.setAnalysisSoftware(anSoft);

        return pdp;
    }

    private AnalysisSoftware getAnalysisSoftware()
    {
        AnalysisSoftware as = new AnalysisSoftware();
		as.setName( "ISOQuant" );
        as.setId("isoquant");
        ContactRole crJorg = new ContactRole();
        as.setContactRole(crJorg);
        as.setCustomizations("");
        CvParam cvSoftwareName = new CvParam();
		cvSoftwareName.setName( "ISOQuant" );

        Param softname = new Param();
        as.setSoftwareName(softname);
        as.setUri("http://www.immunologie.uni-mainz.de/isoquant/");
        as.setVersion(Defaults.version());

        return as;
    }

    private DataCollection getDataCollection(DBProject p) throws IOException {
        //     DataCollection
        //         Inputs
        //         AnalysisData
        //             SpectrumIdentificationList
        //             ProteinDetectionList
        //         /AnalysisData
        //     /DataCollection

        DataCollection dc = new DataCollection();
        Inputs inp = getInputs(p);

        AnalysisData ad = getAnalysisData(p);
        dc.setInputs(inp);
        dc.setAnalysisData(ad);

        return dc;
    }

    private Inputs getInputs(DBProject p)
    {
        /*
        <Inputs>
            <!-- File converted to mzIdentML -->
            <SourceFile location="build/classes/resources/55merge_omssa.omx" id="SourceFile_1">
                <FileFormat>
                    <cvParam accession="MS:1001400" cvRef="PSI-MS" name="OMSSA xml file"/>
                </FileFormat>
            </SourceFile>

            <!-- Database searched, provide a cvParam for DatabaseName if this is a canonical database e.g. Uniprot -->
            <SearchDatabase numDatabaseSequences="22348"
            location="D:/Software/Databases/Neospora_3rndTryp/Neo_rndTryp_3times.fasta"
            id="SearchDB_1">
                <FileFormat>
                    <cvParam accession="MS:1001348" cvRef="PSI-MS" name="FASTA format"/>
                </FileFormat>
                <DatabaseName>
                    <userParam
                            name="D:/Software/Databases/Neospora_3rndTryp/Neo_rndTryp_3times.fasta"/>
                </DatabaseName>
            </SearchDatabase>

            <!-- Location of the searched spectra data. A valid mzIdentML file, should be accompanied by the searched spectra. -->
            <SpectraData location="55merge_tiny.mgf" id="SID_1">
                <FileFormat>
                    <cvParam accession="MS:1001062" cvRef="PSI-MS" name="Mascot MGF file"/>
                </FileFormat>
                <SpectrumIDFormat>
                    <cvParam accession="MS:1000774" cvRef="PSI-MS"
                    name="multiple peak list nativeID format"/>
                </SpectrumIDFormat>
            </SpectraData>
        </Inputs>
        */


        MySQL db = p.mysql;

        Inputs inps = new Inputs();

        //Inputs
            //SourceFile
            //SearchDatabase
            //SpectraData

        List<Workflow> runs = IQDBUtils.getWorkflows(p);
        int nRuns = runs.size();

        for (int i = 0; i < nRuns; i++) {
            Workflow run = runs.get(i);
            Map<String, String> workflowMetaInfo = IQDBUtils.getWorkflowMetaInfo(p, run, "Workflow");

            //SpectraData
            SpectraData spd = new SpectraData();
            spd.setId("SD_" + Integer.toString(run.index));
            //spd.setName("");
            spd.setLocation(run.acquired_name + ".mgf");

            FileFormat spdff = new FileFormat();
            spdff.setCvParam(getthisCVparam(cvMGF, CvPSIMS, ""));
            spd.setFileFormat(spdff);

            SpectrumIDFormat spidf = new SpectrumIDFormat();
            spidf.setCvParam(getthisCVparam(cvMultiplePeakFile, CvPSIMS, ""));
            spd.setSpectrumIDFormat(spidf);

            inps.getSpectraData().add(spd);

            //SourceFile
            SourceFile sfile = new SourceFile();
            sfile.setId("SF_" + Integer.toString(run.index));
            sfile.setLocation(run.acquired_name + ".xml");
            FileFormat sfff = new FileFormat();
            //sfff.setCvParam(getthisCVparam(cvPLGSWorkflowFile , CvPSIMS,""));
            UserParam up_ff = new UserParam();  // TODO: this should be a CvParam
            up_ff.setName("PLGS Workflow XML file");
            sfile.getUserParam().add(up_ff);

            inps.getSourceFile().add(sfile);

        }


        //SearchDatabase  //TODO: SearchDataBase could have some more data (CVparams and UserParams)
            /*
            <SearchDatabase location="file:///C:/inetpub/mascot/sequence/SwissProt/current/SwissProt_51.6.fasta"
            id="SDB_SwissProt" name="SwissProt" numDatabaseSequences="257964" numResidues="93947433"
            releaseDate="2011-03-01T21:32:52" version="SwissProt_51.6.fasta">
            <FileFormat>
            <cvParam accession="MS:1001348" name="FASTA format" cvRef="PSI-MS"/>
            </FileFormat>
            <DatabaseName>
            <userParam name="SwissProt_51.6.fasta"/>
            </DatabaseName>
            <cvParam accession="MS:1001073" name="database type amino acid" cvRef="PSI-MS"/>
            </SearchDatabase>
            */
        SearchDatabase sdb = new SearchDatabase();

        sdb.setId(dbId);
        sdb.setLocation("unknown");
        UserParam upDBName = new UserParam();
        upDBName.setName(dbname);
        if(dbname.length()==0)
            upDBName.setName(dbId);
        Param pDBname = new Param();
        pDBname.setParam(upDBName);
        sdb.setDatabaseName(pDBname);
        sdb.getDatabaseName().setParam(upDBName);
        sdb.setId(dbId);
        sdb.setName(dbname);
        if(dbname.length()==0)
            sdb.setName(dbId);
        sdb.setNumDatabaseSequences(numDBsequences);
        if(cfg_dbversion.length()>0)
            sdb.setVersion(cfg_dbversion);
        inps.getSearchDatabase().add(sdb);


        return inps;
    }

    private AnalysisData getAnalysisData(DBProject p)
    {

        // SpectrumIdentificationList
        // ProteinDetectionList

        AnalysisData ad = new AnalysisData();

        List<SpectrumIdentificationList> sils = getSpectrumIdentificationList(p);
        for(SpectrumIdentificationList sil: sils)
            ad.getSpectrumIdentificationList().add(sil);

        ProteinDetectionList pdl = getProteinDetectionList(p);
        ad.setProteinDetectionList(pdl);

        return ad;
    }

    private List<SpectrumIdentificationList> getSpectrumIdentificationList(DBProject p)
    {

        /*
        <SpectrumIdentificationList id="SIL_1" numSequencesSearched="71412">
            <SpectrumIdentificationResult id="SIR_1" spectrumID="query=1" spectraData_ref="SD_1">

                  <SpectrumIdentificationItem id="SII_4_6"
                          calculatedMassToCharge="1085.055738" chargeState="2"
                          experimentalMassToCharge="1084.9" peptide_ref="peptide_4_6" rank="6" passThreshold="false">

                      <PeptideEvidenceRef peptideEvidence_ref="PE_2_1_HSP7C_SAGOE_0"/>
                      <PeptideEvidenceRef peptideEvidence_ref="PE_2_1_HSP7D_DROME_0"/>

                      <cvParam accession="MS:1001171" name="mascot:score" cvRef="PSI-MS" value="5.31"/>
                      <cvParam accession="MS:1001172" name="mascot:expectation value" cvRef="PSI-MS" value="397.217200507622"/>

                  </SpectrumIdentificationItem>

                <cvParam accession="MS:1001371" name="mascot:identity threshold" cvRef="PSI-MS" value="44"/>
                <cvParam accession="MS:1001370" name="mascot:homology threshold" cvRef="PSI-MS" value="18"/>
                <cvParam accession="MS:1001030" name="number of peptide seqs compared to each spectrum" cvRef="PSI-MS" value="26981"/>
                <cvParam accession="MS:1000796" name="spectrum title" cvRef="PSI-MS" value="dp210198                       21-Jan-98 DERIVED SPECTRUM    #9"/>

            </SpectrumIdentificationResult>

        </SpectrumIdentificationList>
        */

        //WARNING: We don't include the Fragmentation in our analyses, since they are massive. We relate them to mgf files.
        MySQL db = p.mysql;
        db.executeSQLFile(getPackageResource(sql_mzid_SpectrumIdentificationItem));

        List<SpectrumIdentificationList> specIdentificationLists = new ArrayList<SpectrumIdentificationList>();


        try
        {
            List<Workflow> runs = IQDBUtils.getWorkflows(p);
            int nRuns = runs.size();

            // Each workflow (database search) is reported in a separated SpectrumIdentificationList
            for (int i = 0; i < nRuns; i++)
            {
                Workflow run = runs.get(i);
				db.executeSQL( "SELECT * FROM mzidentml_SpectrumIdentificationItem WHERE workflow_index =" + run.index + " ORDER BY query_mass_index ASC" );
                ResultSet rs = db.getStatement().getResultSet();

                SpectrumIdentificationList sil = new SpectrumIdentificationList();
                sil.setId("SIL_" + run.index);
                sil.setName(run.replicate_name);
                // sil.setNumSequencesSearched(1000);

				int lastQmIndex = -1;
				int specIdentItemCounter = 0;
				SpectrumIdentificationResult spectrumIdentificationResult = null;

				while (rs.next())
				{
					specIdentItemCounter++;
					int qmIndex = rs.getInt( "query_mass_index" );
					int leID = rs.getInt( "low_energy_id" );

					if (spectrumIdentificationResult == null)
						spectrumIdentificationResult = newSpectrumIdentificationResult( run.index, leID );

					SpectrumIdentificationItem sii = newSpectrumIdentificationItem( run.index, leID, rs, specIdentItemCounter );

					spectrumIdentificationResult.getSpectrumIdentificationItem().add( sii );

					// add result to the list when query mass index changes
					if (lastQmIndex != qmIndex && lastQmIndex > -1)
					{
						sil.getSpectrumIdentificationResult().add( spectrumIdentificationResult );
						spectrumIdentificationResult = null;
						specIdentItemCounter = 0;
					}

					lastQmIndex = qmIndex;
                }
				// do not foget to add the last one
				sil.getSpectrumIdentificationResult().add( spectrumIdentificationResult );

                specIdentificationLists.add(sil);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return specIdentificationLists;
    }

	/**
	 * @return
	 * @throws SQLException 
	 */
	private SpectrumIdentificationItem newSpectrumIdentificationItem(int runIndex, int leId, ResultSet rs, int specIdentItemCounter) throws SQLException
	{
		int id = rs.getInt( "id" );
		Double Mobility = rs.getDouble( "Mobility" );
		Double theoretical_MassToCharge = rs.getDouble( "theoretical_MassToCharge" );
		Double experimental_MassToCharge = rs.getDouble( "experimental_MassToCharge" );
		int Z = rs.getInt( "Z" );
		String peptide_ref = "PEP_" + Integer.toString( rs.getInt( "peptide_ref" ) );
		String PLGS_score = Double.toString( rs.getDouble( "PLGS_score" ) );
		String PLGS_type = rs.getString( "PLGS_type" );
		String PLGS_frag_string = rs.getString( "PLGS_frag_string" );
		String[] peptideEvidence_refs = rs.getString( "peptideEvidence_refs" ).split( "," );
		SpectrumIdentificationItem sii = new SpectrumIdentificationItem();
		String siiID = "SII_" + runIndex + "_" + leId + "_" + id;
		sii.setId( siiID );
		sii.setCalculatedMassToCharge( theoretical_MassToCharge );
		sii.setExperimentalMassToCharge( experimental_MassToCharge );
		sii.setChargeState( Z );
		Peptide pep = new Peptide();
		// Dummy sequence, we are only interested here on the the peptide_ref
		pep.setPeptideSequence( "AAK" );
		pep.setId( peptide_ref );
		sii.setPeptide( pep );
		sii.setPassThreshold( specIdentItemCounter == 1 );
		sii.setRank( specIdentItemCounter );
		for ( String pepev : peptideEvidence_refs )
		{
			PeptideEvidenceRef per = new PeptideEvidenceRef();
			// Dummy PeptideEvidence, we only need to set the id
			PeptideEvidence pe = new PeptideEvidence();
			pe.setId( "PE_" + pepev );
			per.setPeptideEvidence( pe );
			sii.getPeptideEvidenceRef().add( per );
		}
		UserParam plgs_score_param = getthisUserParam( null, "PLGS_SCORE", PLGS_score );
		UserParam plgs_type_param = getthisUserParam( null, "PLGS_TYPE", PLGS_type );
		UserParam plgs_frag_string_param = getthisUserParam( null, "PLGS_FRAG_STRING", PLGS_frag_string );
		sii.getUserParam().add( plgs_score_param );
		sii.getUserParam().add( plgs_type_param );
		sii.getUserParam().add( plgs_frag_string_param );
		return sii;
	}

	/**
	 * @return 
	 * 
	 */
	private SpectrumIdentificationResult newSpectrumIdentificationResult(int runIndex, int leId)
	{
		SpectrumIdentificationResult spectrumIdentificationResult = new SpectrumIdentificationResult();
		spectrumIdentificationResult.setId( "SIR_" + runIndex + "_" + leId );
		// TITLE=LEPeakID:1:_DRIFT_TIME_IN_BINS=48.809803
		// mgf files are indexed by LEPeakID.
		// specifications suggest.
		spectrumIdentificationResult.setSpectrumID( "index=" + Integer.toString( leId ) );
		SpectraData sd = new SpectraData();
		sd.setName( "specDataName" );
		sd.setId( "SD_" + Integer.toString( runIndex ) );
		sd.setExternalFormatDocumentation( "fasta" );
		spectrumIdentificationResult.setSpectraData( sd );
		return spectrumIdentificationResult;
	}

	private ProteinDetectionList getProteinDetectionList(DBProject p)
    {
        /*
        <ProteinDetectionList id="PDL_1">
            <ProteinAmbiguityGroup id="PAG_hit_1">
                <ProteinDetectionHypothesis id="PDH_HSP7D_MANSE_0" dBSequence_ref="DBSeq_HSP7D_MANSE" passThreshold="true">
                    <PeptideHypothesis peptideEvidence_ref="PE_1_1_HSP7D_MANSE_0">
                        <SpectrumIdentificationItemRef spectrumIdentificationItem_ref="SII_1_1"/>
                    </PeptideHypothesis>
                    <PeptideHypothesis peptideEvidence_ref="PE_3_1_HSP7D_MANSE_0">
                        <SpectrumIdentificationItemRef spectrumIdentificationItem_ref="SII_1_1"/>
                    </PeptideHypothesis>
                    <cvParam accession="MS:1001171" name="mascot:score" cvRef="PSI-MS" value="104.854382332144"/>
                    <cvParam accession="MS:1001093" name="sequence coverage" cvRef="PSI-MS" value="4"/>
                    <cvParam accession="MS:1001097" name="distinct peptide sequences" cvRef="PSI-MS" value="2"/>
                </ProteinDetectionHypothesis>

                <ProteinDetectionHypothesis id="PDH_HSP7C_ICTPU_0" dBSequence_ref="DBSeq_HSP7C_ICTPU" passThreshold="true">
                    <PeptideHypothesis peptideEvidence_ref="PE_1_1_HSP7C_ICTPU_0">
                        <SpectrumIdentificationItemRef spectrumIdentificationItem_ref="SII_1_1"/>
                    </PeptideHypothesis>
                    <cvParam accession="MS:1001171" name="mascot:score" cvRef="PSI-MS" value="62.72"/>
                    <cvParam accession="MS:1001093" name="sequence coverage" cvRef="PSI-MS" value="1"/>
                    <cvParam accession="MS:1001097" name="distinct peptide sequences" cvRef="PSI-MS" value="1"/>
                </ProteinDetectionHypothesis>

                <ProteinDetectionHypothesis id="PDH_HSP70_ONCMY_0" dBSequence_ref="DBSeq_HSP70_ONCMY" passThreshold="true">
                    <PeptideHypothesis peptideEvidence_ref="PE_1_1_HSP70_ONCMY_0">
                        <SpectrumIdentificationItemRef spectrumIdentificationItem_ref="SII_1_1"/>
                    </PeptideHypothesis>
                    <cvParam accession="MS:1001171" name="mascot:score" cvRef="PSI-MS" value="60.0528111023421"/>
                    <cvParam accession="MS:1001093" name="sequence coverage" cvRef="PSI-MS" value="1"/>
                    <cvParam accession="MS:1001097" name="distinct peptide sequences" cvRef="PSI-MS" value="1"/>
                </ProteinDetectionHypothesis>
            </ProteinAmbiguityGroup>
        </ProteinDetectionList>
        */

        MySQL db = p.mysql;
        db.executeSQLFile(getPackageResource(getSql_mzid_ProteinAmbiguityGroup));

        //We define a single ProteinDetectionList for the whole data set, regardless the different runs
        ProteinDetectionList pdl = new ProteinDetectionList();
        pdl.setId("PDL_1");
        //pdl.setName("name");

        ProteinAmbiguityGroup pag = new ProteinAmbiguityGroup();
        ArrayList<ProteinDetectionHypothesis> pdh_list = new ArrayList<ProteinDetectionHypothesis>();

        try {
            ResultSet rs = db.getStatement().getResultSet();
            String last_entry = "before_first_entry";

            while(rs.next()) {

                String entry = rs.getString("entry");
                //String sequence = rs.getString("sequence");
                int pepEvidence_ref = rs.getInt("pepEvidence_ref");
                String[] proteinHomologyInfo = rs.getString("proteinHomologyInfo").split(";");
				String sii_refs = rs.getString( "spectrumIdentificationItem_refs" );
				if (sii_refs.contains( "SII_11_551748_3106" ))
				{
					System.out.println( "here" );
				}
				String[] spectrumIdentificationItem_refs = sii_refs.split( "," );
                int num_peptides = rs.getInt("num_peptides");

                if(last_entry.equals("before_first_entry"))
                {
                    //This is the first protein group. Generate ProteinDetectionHypothesisList
                    pag = new ProteinAmbiguityGroup();

                    //We generated a table grouped by Protein groups and sequences.
                    // We need to generate as many proteinDetectionHypothesis as dbSequence_refs
                    pdh_list = new ArrayList<ProteinDetectionHypothesis>();
                    int numDbSequences = proteinHomologyInfo.length;
                    for(int i=0; i<numDbSequences;i++)
                    {
                        ProteinDetectionHypothesis pdh = new ProteinDetectionHypothesis();
                        String[] phInfo = proteinHomologyInfo[i].split("_");
                        String dbs_ref = "DBS_" + phInfo[0];
                        String prot_cov = phInfo[1];
                        String prot_score = phInfo[2];

                        pdh.setId(entry + "_" + dbs_ref);  //ProteinDetectionHypothesis primary key: entry + dbsequence
                        //pdh.setName("Name");
                        pdh.setPassThreshold(Boolean.TRUE);
                        DBSequence dbs = new DBSequence();
                        dbs.setId(dbs_ref);
                        pdh.setDBSequence(dbs);

                        CvParam cvCov = getthisCVparam(cvCoverage, CvPSIMS, prot_cov);
                        CvParam cvNumSeq = getthisCVparam(cvNumSequences, CvPSIMS, Integer.toString(num_peptides));
                        UserParam upPLGSproteinScore = getthisUserParam(null, "PLGS_PROTEIN_SCORE", prot_score);

                        pdh.getCvParam().add(cvCov);
                        pdh.getCvParam().add(cvNumSeq);
                        pdh.getUserParam().add(upPLGSproteinScore);

                        pdh_list.add(pdh);
                    }
                }

                PeptideHypothesis peph = new PeptideHypothesis();
                PeptideEvidence pepEv = new PeptideEvidence();
                pepEv.setId("PE_" + Integer.toString(pepEvidence_ref));
                peph.setPeptideEvidence(pepEv);

                for(String sii : spectrumIdentificationItem_refs)
                {
                    SpectrumIdentificationItemRef siir = new SpectrumIdentificationItemRef();
                    siir.setSpectrumIdentificationItemRef(sii);
                    peph.getSpectrumIdentificationItemRef().add(siir);
                }

                for(ProteinDetectionHypothesis pdh: pdh_list)
                {
                    pdh.getPeptideHypothesis().add(peph);
                }

				if (( !entry.equals( last_entry ) && !last_entry.equals( "before_first_entry" ) ) || rs.isLast())
                {
                    // In our case, one of the protein entries of the group is used as primary key for protein groups.
                    pag.setId("PAG_" + last_entry);

                    for (ProteinDetectionHypothesis pdh: pdh_list)
                    {
                        pag.getProteinDetectionHypothesis().add(pdh);
                    }
                    pdl.getProteinAmbiguityGroup().add(pag);

                    pag = new ProteinAmbiguityGroup();

                    //We generated a table grouped by Protein groups and sequences.
                    // We need to generate as many proteinDetectionHypothesis as dbSequence_refs
                    pdh_list = new ArrayList<ProteinDetectionHypothesis>();
                    int numDbSequences = proteinHomologyInfo.length;
                    for(int i=0; i<numDbSequences;i++)
                    {
                        ProteinDetectionHypothesis pdh = new ProteinDetectionHypothesis();
                        String[] phInfo = proteinHomologyInfo[i].split("_");
                        String dbs_ref = "DBS_" + phInfo[0];
                        String prot_cov = phInfo[1];
                        String prot_score = phInfo[2];

                        pdh.setId(entry + "_" + dbs_ref);  //ProteinDetectionHypothesis primary key: entry + dbsequence
                        //pdh.setName("Name");
                        pdh.setPassThreshold(Boolean.TRUE);
                        DBSequence dbs = new DBSequence();
                        dbs.setId(dbs_ref);
                        pdh.setDBSequence(dbs);

                        CvParam cvCov = getthisCVparam(cvCoverage, CvPSIMS, prot_cov);
                        CvParam cvNumSeq = getthisCVparam(cvNumSequences, CvPSIMS, Integer.toString(num_peptides));
                        UserParam upPLGSproteinScore = getthisUserParam(null, "PLGS_PROTEIN_SCORE", prot_score);

                        pdh.getCvParam().add(cvCov);
                        pdh.getCvParam().add(cvNumSeq);
                        pdh.getUserParam().add(upPLGSproteinScore);

                        pdh_list.add(pdh);
                    }
                }
                last_entry = entry;
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return pdl;
    }

    private List<BibliographicReference> getBibliographicReference()
    {
        List<BibliographicReference> bib = new ArrayList<BibliographicReference>();

        BibliographicReference b1 = new BibliographicReference();
		b1.setId( "ISOQuant_ref" );
		b1.setName( "ISOQuant reference" );
        b1.setAuthors("Ute Distler, Joerg Kuharev, Pedro Navarro, Yishai Levin, Hans-Joerg Schild, Stefan Tenzer");
        b1.setDoi("doi:10.1038/nmeth.2767");
        b1.setIssue("2");
        b1.setPages("167–170");
        b1.setPublication("Nature Methods");
        b1.setPublisher("Nature Publishing Group");
        b1.setTitle("Drift time-specific collision energies enable deep-coverage data-independent acquisition proteomics");
        b1.setVolume("11");
        b1.setYear(2014);

        bib.add(b1);

        return bib;
    }



	/* (non-Javadoc)
	 * @see isoquant.kernel.plugin.SingleActionPlugin#getMenuItemText()
	 */
	@Override
	public String getMenuItemText() {
		return "MzIdentML";
	}

	/* (non-Javadoc)
	 * @see isoquant.kernel.plugin.SingleActionPlugin#getMenuItemIconName()
	 */
	@Override
	public String getMenuItemIconName() {
		return "xml";
	}

    @Override public String getPluginName()
    {
        return "MzIdentML Reporter";
    }

    @Override public String getHintFileNameSuffix()
    {
		return "";
    }


}
