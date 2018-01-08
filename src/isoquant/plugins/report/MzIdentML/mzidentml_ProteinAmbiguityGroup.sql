-- @ :	collecting protein inference information ...
DROP TABLE IF EXISTS mzidentml_proteinHomology;
CREATE TABLE mzidentml_proteinHomology
(id INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (id), KEY (entryLeft), KEY (entryRemoved), KEY(entryRemoved_dbSequenceRef))
  ENGINE=MyISAM
    SELECT ph.*, pi.score as entryRemoved_score, pi.coverage as entryRemoved_coverage, mzdbs.index as entryRemoved_dbSequenceRef
    FROM protein_homology ph
      JOIN mzidentml_dbsequence mzdbs ON ph.entryRemoved = mzdbs.entry
      JOIN protein_info pi ON pi.entry = ph.entryRemoved
;

OPTIMIZE TABLE
finalquant,
peptides_in_proteins_after_homology_filtering,
mzidentml_proteinHomology,
mzidentml_pepevidence,
peptides_in_proteins_stats,
mzidentml_spectrumidentificationitem
;

SET @@session.group_concat_max_len=1000000;
-- @ :	exploring protein ambiguity cases ...
DROP TABLE IF EXISTS mzidentml_ProteinAmbiguityGroup;
CREATE TABLE mzidentml_ProteinAmbiguityGroup
(ID INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (id), KEY (entry), KEY (sequence) )
  ENGINE=MyISAM
    SELECT
      fq.entry,
      pepah.sequence,
      mzpepev.`id` AS pepEvidence_ref,
      GROUP_CONCAT( DISTINCT CONCAT(entryRemoved_dbSequenceRef, "_", entryRemoved_coverage, "_", entryRemoved_score) SEPARATOR ';') AS proteinHomologyInfo,
-- GROUP_CONCAT( entryRemoved_dbSequenceRef SEPARATOR ',') AS dbSequence_refs,
-- GROUP_CONCAT( entryRemoved_coverage SEPARATOR ',') AS protein_coverages,
-- GROUP_CONCAT( entryRemoved_score SEPARATOR ',') AS protein_scores,
      GROUP_CONCAT(DISTINCT CONCAT("SII_", mz_sii.workflow_index, "_", mz_sii.low_energy_id, "_",  mz_sii.id) SEPARATOR ',') AS spectrumIdentificationItem_refs,
      pepst.unique_peptides,
      pepst.razor_peptides,
      pepst.shared_peptides,
      (pepst.unique_peptides + pepst.razor_peptides + pepst.shared_peptides) as num_peptides
    FROM finalquant fq
      JOIN peptides_in_proteins_after_homology_filtering pepah USING (entry)
      JOIN mzidentml_proteinHomology ph ON fq.entry=ph.entryLeft
      JOIN mzidentml_pepevidence mzpepev ON mzpepev.sequence = pepah.sequence
      JOIN peptides_in_proteins_stats pepst ON fq.entry = pepst.entry
      JOIN mzidentml_spectrumidentificationitem mz_sii ON mz_sii.peptide_ref = mzpepev.peptide_ref
    GROUP BY fq.entry, pepah.sequence
    ORDER BY fq.entry, pepah.sequence
;
-- @ :	retrieving data ...
SELECT * FROM mzidentml_ProteinAmbiguityGroup
;
