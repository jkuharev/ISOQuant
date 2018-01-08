-- @ :	removing temporary data ...
DROP TABLE IF EXISTS 
	mzIdentML_DBsequence,
	mzIdentML_PepEvidence,
	mzIdentML_Peptide,
	mzidentml_ProteinAmbiguityGroup,
	mzidentml_SpectrumIdentificationItem,
	mzidentml_proteinHomology,
  mzidentml_mztab_protein_averages,
  mzidentml_mztab_protein_section,
  mzidentml_mztab_peptide,
  mzidentml_temporalInfo
;