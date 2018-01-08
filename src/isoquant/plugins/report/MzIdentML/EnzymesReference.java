package isoquant.plugins.report.MzIdentML;

//import uk.ac.ebi.pride.jmztab.model.CVParam;
import uk.ac.ebi.jmzidml.model.mzidml.CvParam;
import uk.ac.ebi.jmzidml.model.mzidml.Enzyme;
import uk.ac.ebi.jmzidml.model.mzidml.ParamList;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by napedro on 27/10/14.
 */
public enum EnzymesReference {

    /*
    <Enzyme id="ENZ_0" cTermGain="OH" nTermGain="H" missedCleavages="2" semiSpecific="0">
    <SiteRegexp><![CDATA[(?<=[KR])(?!P)]]></SiteRegexp>
    <EnzymeName>
    <cvParam accession="MS:1001251" name="Trypsin" cvRef="PSI-MS" />
    </EnzymeName>
    </Enzyme>
    */

    enz_2_iodobenzoate("2-iodobenzoate","MS:1001918","(?<=W)"),
    enz_Arg_C("Arg-C","MS:1001303","(?<=R)(?!P)"),
    enz_Asp_N("Asp-N","MS:1001304","(?=[HD])"),
    enz_Asp_N_ambic("Asp-N_ambic","MS:1001305","(?=[DE])"),
    enz_CNBr("CNBr","MS:1001307","(?<=M)"),
    enz_Chymotrypsin("Chymotrypsin","MS:1001306","(?<=[FYWL])(?!P)"),
    enz_Formic_acid("Formic_acid","MS:1001308","((?<=D))|((?=D))"),
    enz_Lys_C("Lys-C","MS:1001309","(?<=K)(?!P)"),
    enz_Lys_C_P("Lys-C/P","MS:1001310","(?<=K)"),
    enz_No_Enzyme("NoEnzyme","MS:1001091",""),
    enz_Pepsin_A("PepsinA","MS:1001311","(?<=[FL])"),
    enz_Tryp_Chymo("TrypChymo","MS:1001312","(?<=[FYWLKR])(?!P)"),
    enz_Trypsin("Trypsin", "MS:1001251","(?<=[KR])(?!P)"),
    enz_Trypsin_P("Trypsin/P","MS:1001313","(?<=[KR])"),
    enz_V8_DE("V8-DE","MS:1001314","(?<=[BDEZ])(?!P)"),
    enz_V8_E("V8-E","MS:1001315","(?<=[EZ])(?!P)"),
    enz_glutamyl_endopeptidase("glutamyl endopeptidase","MS:1001917","(?<=[^E]E)"),
    enz_Leukocyte_elastase("leukocyte elastase","MS:1001915","(?<=[ALIV])(?"),
    enz_no_cleavage("no cleavage","MS:1001955",""),
    enz_proline_endopeptidase("proline endopeptidase","MS:1001916","(?<=[HKR]P)(?"),
    enz_unspecific_cleavage("unspecific cleavage","MS:1001956","")
    // MS_MULTIPLE_PEAK_FORMAT("MS","MS:1000774","multiple peak list nativeID format","")   //Not sure about children!
    ;

    private final String name;
    private final String accession;
    private final String cleavageRule;

    private EnzymesReference(String name, String accession, String cleavageRule) {
        this.name = name;
        this.accession = accession;
        this.cleavageRule = cleavageRule;
    }

    public String getName() { return name; }
    public String getAccession() { return accession; }
    public String getCleavageRule() { return cleavageRule; }

    public static Enzyme getEnzymeByName_MzIdentML(String enz_name)
    {

        Enzyme outEnzyme = new Enzyme();
        CvParam enzCVparam = new CvParam();

        EnzymesReference[] enzymes = EnzymesReference.values();
        Collection<String> results = new ArrayList<String>();
        CvTermReference[] cvTerms = CvTermReference.values();
        for (EnzymesReference enz : enzymes)
        {
            if(enz.name.equalsIgnoreCase(enz_name))
            {
                enzCVparam.setAccession(enz.accession);
                enzCVparam.setName(enz.name);
                enzCVparam.setCv(PSIStandardUtils.getthisCv(CvReference.PSI_MS));
                outEnzyme.setName(enz.name);
                if (enz.cleavageRule.length() > 0)
                    outEnzyme.setSiteRegexp(enz.cleavageRule);
                //outEnzyme.setEnzymeName(ParamList);
                ParamList prm = new ParamList();
                prm.getCvParam().add(enzCVparam);
                outEnzyme.setEnzymeName(prm);
                break;
            }
        }

    return outEnzyme;
    }

}
