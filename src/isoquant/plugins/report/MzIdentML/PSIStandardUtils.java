package isoquant.plugins.report.MzIdentML;

import uk.ac.ebi.jmzidml.model.mzidml.Cv;
import uk.ac.ebi.jmzidml.model.mzidml.CvParam;
import uk.ac.ebi.jmzidml.model.mzidml.Modification;
import uk.ac.ebi.jmzidml.model.mzidml.UserParam;
import uk.ac.ebi.jmzidml.model.mzidml.params.ModificationCvParam;

import java.util.ArrayList;
import java.util.List;

import isoquant.kernel.db.DBProject;

/**
 * Created by napedro on 24/10/14.
 */
public class PSIStandardUtils {

    public static CvParam getthisCVparam(CvTermReference cvtermref, Cv termCv, String value)
    {
        CvParam myCvparam = new CvParam();

        myCvparam.setAccession(cvtermref.getAccession());
        myCvparam.setName(cvtermref.getName());
        if (value.length() > 0)
            myCvparam.setValue(value);
        myCvparam.setCv(termCv);

        return myCvparam;
    }

    public static CvParam getthisCVparam(CvTermReference cvtermref, Cv termCv, CvTermReference unitCvTerm, String value)
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

    public static UserParam getthisUserParam(String type, String name, String value)
    {
        UserParam up = new UserParam();

        up.setType(type);
        up.setName(name);
        up.setValue(value);

        return up;
    }

    public static void writeInTempTable(DBProject p, String key, String value)
    {
        p.mysql.executeSQL("INSERT INTO mzidentml_temporalInfo (InfoName, InfoValue) VALUES ( '" +
                key + "', '" + value +
                "')");
    }
    public static String readFromTempTable(DBProject p, String key)
    {
        return p.mysql.getFirstValue("SELECT InfoValue FROM mzidentml_temporalInfo WHERE InfoName = '" + key + "'", 1);
    }

    public static Cv getthisCv(CvReference cvref)
    {
        Cv myCv = new Cv();
        myCv.setId(cvref.getId());
        myCv.setFullName(cvref.getFullName());
        myCv.setVersion(cvref.getVersion());
        myCv.setUri(cvref.getUri());

        return myCv;
    }

    public static Modification getthisModification(String plgs_code, Integer location) throws Exception {
        /*
        <Modification location="1" residues="M" monoisotopicMassDelta="15.994919">
        <cvParam accession="UNIMOD:35" name="Oxidation" cvRef="UNIMOD"/>
        <cvParam accession="MS:1001524" name="fragment neutral loss" cvRef="PSI-MS" value="0"
        unitAccession="UO:0000221" unitName="dalton" unitCvRef="UO"/>
        */

        Cv CvUnimod = PSIStandardUtils.getthisCv(CvReference.UNIMOD);
        //Cv CvUO = PSIStandardUtils.getthisCv(CvReference.UNIT_ONTOLOGY);

        Modification mod = new Modification();
        PeptideModsReference pmReference = new PeptideModsReference();

        List<PeptideModification> pmods = pmReference.getPeptideModsByPLGScode(plgs_code);
        if(pmods.isEmpty()) {
            throw new Exception("Unknown peptide modification found!");
        }
        PeptideModification pmod = pmods.get(0);
        //System.out.println(plgs_code);
        ModificationCvParam modCv = new ModificationCvParam();
        modCv.setAccession(pmod.getUnimod_Id());
        modCv.setName(pmod.getUnimod_code_name());
        modCv.setCv(CvUnimod);
        mod.setMonoisotopicMassDelta(pmod.getMonoisotopicMassDelta());
        mod.setLocation(location);

        mod.getCvParam().add(modCv);

        return mod;
    }



    public static List<Modification> parseModificationsFromPLGS(String modifier) //throws Exception
    {

        Cv CvUnimod = PSIStandardUtils.getthisCv(CvReference.UNIMOD);

        //modifier
        // 'Carbamidomethyl+C(21), Carbamidomethyl+C(24)'
        List<Modification> modList = new ArrayList<Modification>();

        String[] mods = modifier.split(",");

        for (String m: mods)
        {
            String[] modpos = m.split("\\+");
            String plgscode = modpos[0].replaceAll("\\s+","");
            String[] p = modpos[1].split("[\\(||\\)]");
            Integer pos = Integer.parseInt(p[1]);

            Modification mod = new Modification();
            PeptideModsReference pmReference = new PeptideModsReference();

            List<PeptideModification> pmods = pmReference.getPeptideModsByPLGScode(plgscode);
            if(pmods.isEmpty()) {
                return null;
            }
            PeptideModification pmod = pmods.get(0);
            //System.out.println(plgs_code);
            ModificationCvParam modCv = new ModificationCvParam();
            modCv.setAccession(pmod.getUnimod_Id());
            modCv.setName(pmod.getUnimod_code_name());
            modCv.setCv(CvUnimod);
            mod.setMonoisotopicMassDelta(pmod.getMonoisotopicMassDelta());
            mod.setLocation(pos);

            mod.getCvParam().add(modCv);


            modList.add(mod);
        }
        return modList;
    }



}
