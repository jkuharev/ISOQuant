package isoquant.plugins.report.MzIdentML;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by napedro on 05/08/14.
 */
public class PeptideModsReference
{
	/*
	<Modification location="19" residues="K" monoisotopicMassDelta="127.063324">
	<cvParam accession="UNIMOD:29" name="SMA" cvRef="UNIMOD"/>
	</Modification>
	*/
	public static final PeptideModification undefinedModification =
			new PeptideModification( "undefined", 0.0, "undefined modification", "undefined", "", "UNIMOD:00" );

	public static final PeptideModification[] defaultModifications = new PeptideModification[]
	{
			new PeptideModification( "Dehydration", -18.010565, "Dehydration", "Phospho+PL", "H(-2) O(-1)", "UNIMOD:23" ),
			new PeptideModification( "Pyrrolidone", -17.026549, "Pyro-glu from Q", "Pyro-glu", "H(-3) N(-1)", "UNIMOD:28" ),
			new PeptideModification( "Amidation", -0.984016, "Amidation", "Amide", "H N O(-1)", "UNIMOD:2" ),
			new PeptideModification( "Deamidation", 0.984016, "Deamidation", "Deamidation", "H(-1) N(-1) O", "UNIMOD:7" ),
			new PeptideModification( "O18", 4.008491, "O18 label at both C-terminal oxygens", "double_O18", "O(-2) 18O(2)", "UNIMOD:193" ),
			new PeptideModification( "13C", 6.020129, "13C(6) Silac label", "13C6", "C(-6) 13C(6)", "UNIMOD:188" ),
			new PeptideModification( "13C", 10.008269, "13C(6) 15N(4) Silac label", "13C6-15N4", "C(-6) 13C(6) N(-4) 15N(4)", "UNIMOD:267" ),
			new PeptideModification( "Methyl", 14.01565, "Methylation", "Methyl", "H(2) C", "UNIMOD:34" ),
			new PeptideModification( "Hydroxyl", 15.994915, "Oxidation or Hydroxylation", "Hydroxylation", "O", "UNIMOD:35" ),
			new PeptideModification( "Oxidation", 15.994915, "Oxidation or Hydroxylation", "Hydroxylation", "O", "UNIMOD:35" ),
			new PeptideModification( "Formyl", 27.994915, "Formylation", "Formyl", "C O", "UNIMOD:122" ),
			new PeptideModification( "Acetyl", 42.010565, "Acetylation", "Acetyl", "H(2) C(2) O", "UNIMOD:1" ),
			new PeptideModification( "Carbamyl", 43.005814, "Carbamylation", "Carbamyl", "H C N O", "UNIMOD:5" ),
			new PeptideModification( "Gamma-carboxyglutamic", 43.989829, "Carboxylation", "carboxyl", "C O(2)", "UNIMOD:299" ),
			new PeptideModification( "Carbamidomethyl", 57.021464, "Iodoacetamide derivative", "Carbamidomethyl", "H(3) C(2) N O", "UNIMOD:4" ),
			new PeptideModification( "Carboxymethyl", 58.005479, "Iodoacetic acid derivative", "Carboxymethyl", "H(2) C(2) O(2)", "UNIMOD:6" ),
			new PeptideModification( "Propionamide", 71.037114, "Acrylamide adduct", "Propionamide", "H(5) C(3) N O", "UNIMOD:24" ),
			new PeptideModification( "Phosphoryl", 79.966331, "Phosphorylation", "Phospho", "H O(3) P", "UNIMOD:21" ),
			new PeptideModification( "NIPCAM", 99.068414, "N-isopropylcarboxamidomethyl", "NIPCAM", "H(9) C(5) N O", "UNIMOD:17" ),
			new PeptideModification( "S-pyridylethyl", 105.057849, "S-pyridylethylation", "S-pyridylethyl", "H(7) C(7) N", "UNIMOD:31" ),
			new PeptideModification( "SMA", 127.063329, "N-Succinimidyl-2-morpholine acetate", "SMA", "H(9) C(6) N O(2)", "UNIMOD:29" ),
			new PeptideModification( "Isobaric", 144.099599, "Accurate mass for 115", "iTRAQ115", "H(12) C(6) 13C N 15N 18O", "UNIMOD:533" ),
			new PeptideModification( "Isobaric", 144.102063, "Representative mass and accurate mass for 116 &amp; 117", "iTRAQ", "H(12) C(4) 13C(3) N 15N O", "UNIMOD:214" ),
			new PeptideModification( "Isobaric", 144.105918, "Accurate mass for 114", "iTRAQ114", "H(12) C(5) 13C(2) N(2) 18O", "UNIMOD:532" ),
			new PeptideModification( "Glycation", 162.052824, "Hexose", "Hex", "Hex", "UNIMOD:41" ),
			new PeptideModification( "C-Mannosyl", 162.052824, "Hexose", "Hex", "Hex", "UNIMOD:41" ),
			new PeptideModification( "Lipoyl", 188.032956, "Lipoyl", "Lipoyl", "H(12) C(8) O S(2)", "UNIMOD:42" ),
			new PeptideModification( "O-GlcNac", 203.079373, "N-Acetylhexosamine", "HexNAc", "HexNAc", "UNIMOD:43" ),
			new PeptideModification( "Farnesyl", 204.187801, "Farnesylation", "Farnesylation", "H(24) C(15)", "UNIMOD:44" ),
			new PeptideModification( "Myristoyl", 210.198366, "Myristoylation", "Myristoylation", "H(26) C(14) O", "UNIMOD:45" ),
			new PeptideModification( "Biotin", 226.077598, "Biotinylation", "Biotin", "H(14) C(10) N(2) O(2) S", "UNIMOD:3" ),
			new PeptideModification( "12C", 227.126991, "Applied Biosystems cleavable ICAT(TM) light", "ICAT_light", "H(17) C(10) N(3) O(3)", "UNIMOD:105" ),
			new PeptideModification( "Pyridoxal", 229.014009, "Pyridoxal phosphate", "Pyridoxal-phos", "H(8) C(8) N O(5) P", "UNIMOD:46" ),
			new PeptideModification( "13C", 236.157185, "Applied Biosystems cleavable ICAT(TM) heavy", "ICAT_heavy", "H(17) C 13C(9) N(3) O(3)", "UNIMOD:106" ),
			new PeptideModification( "Geranyl-geranyl", 272.250401, "Geranyl-geranyl", "Geranyl-geranyl", "H(32) C(20)", "UNIMOD:48" ),
			new PeptideModification( "Palmitoyl", 238.229666, "Palmitoylation", "Palmitoylation", "H(30) C(16) O", "UNIMOD:47" ),
			new PeptideModification( "Phosphopantetheine", 340.085794, "Phosphopantetheine", "p-pantetheine", "H(21) C(11) N(2) O(6) P S", "UNIMOD:49" ),
			new PeptideModification( "1H", 442.224991, "Applied Biosystems original ICAT(TM) d0", "AB_old_ICATd0", "H(34) C(20) N(4) O(5) S", "UNIMOD:13" ),
			new PeptideModification( "2H", 450.275205, "Applied Biosystems original ICAT(TM) d8", "AB_old_ICATd8", "H(26) 2H(8) C(20) N(4) O(5) S", "UNIMOD:12" ),
			new PeptideModification( "Flavin-adenine", 783.141486, "Flavin adenine dinucleotide", "FAD", "H(31) C(27) N(9) O(15) P(2)", "UNIMOD:50" )
	};

	private List<PeptideModification> peptideModifications = new ArrayList<PeptideModification>();

	private static final PeptideModification[] vuMods = new PeptideModification[]
	{
		// short name (data), monoisotopicMassDelta, unimodDescription, unimodCodeName, composition, unimodId
		// new PeptideModification( "name", 0.0, "desc", "codeName", "formula", "UNIMOD:" ),
		new PeptideModification( "QtopyroE", -17.026549, "Pyro-glu from Q", "Pyro-glu", "H(-3) N(-1)", "UNIMOD:28" ),
		new PeptideModification( "Galactosyl", 178.047738, "Gluconoylation", "Galactosyl", "O Hex", "UNIMOD:907" ),
		new PeptideModification( "Galactosyl hydroxy", 178.047738, "Gluconoylation", "Galactosyl", "O Hex", "UNIMOD:907" ),
		new PeptideModification( "Allysine", -1.031634, "Lysine oxidation to aminoadipic semialdehyde", "Lysaminoadipicsealde", "H(-3) N(-1) O", "UNIMOD:352" ),
		new PeptideModification( "GlucosylGalactosyl", 340.100562, "glucosylgalactosyl hydroxylysine", "glucosylgalactosyl", "O Hex(2)", "UNIMOD:393" ),
		new PeptideModification( "Glucosylhydroxy", 148.037173, "glycosyl-L-hydroxyproline", "glycosyl", "H(-2) C(-1) Hex", "UNIMOD:408" ),
		new PeptideModification( "Hydroxyallysine", 14.963280, "alpha-amino adipic acid", "aminoadipic", "H(-3) N(-1) O(2)", "UNIMOD:381" )
	};

	public PeptideModsReference()
	{
		Collections.addAll( peptideModifications, defaultModifications );
		Collections.addAll( peptideModifications, vuMods );
		peptideModifications.add( undefinedModification );
	}

	/**
	 * add some new modifications
	 * @param newMods
	 */
	public void addModifications(Collection<PeptideModification> newMods)
	{
		peptideModifications.addAll( newMods );
	}

	/**
	 * @return the peptideModifications
	 */
	public List<PeptideModification> getPeptideModifications()
	{
		return peptideModifications;
	}

	public PeptideModification getPeptideMod_byUnimodId(String UnimodId) throws Exception
	{
		for (PeptideModification pm : peptideModifications) 
		{
			if (pm.getUnimod_Id().equals(UnimodId))	return(pm);
		}
		throw new Exception("undefined peptide modification! (unimod_id="+UnimodId+ ")");
	}

	public PeptideModification getPeptideModByPLGScodeAndDeltaMass(String plgs_code, double deltaMass)
	 {
		List<PeptideModification> mod = getPeptideModsByPLGScode( plgs_code );
		PeptideModification mod_final = null;
	
		// if there is more than one modification available for this plgs_code,
		// report the closest in delta_mass.
		for ( PeptideModification pm : mod )
		{
			if (mod_final == null)
				mod_final = pm;
			else
			if (Math.abs( pm.getMonoisotopicMassDelta() - deltaMass ) < Math.abs( mod_final.getMonoisotopicMassDelta() - deltaMass ))
				mod_final = pm;
		 }
		 return mod_final;
	 }

	public List<PeptideModification> getPeptideModsByPLGScode(String plgs_code)
	{
		List<PeptideModification> mod = new ArrayList<PeptideModification>();
		for ( PeptideModification pm : peptideModifications )
		{
			if (pm.getPlgs_first_word_name().toLowerCase().equals( plgs_code.toLowerCase() ))
			{
				mod.add( pm );
			}
		}
		return mod;
	}
}
