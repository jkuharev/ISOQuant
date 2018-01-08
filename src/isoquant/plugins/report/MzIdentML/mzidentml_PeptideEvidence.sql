-- @ :	evaluating peptide evidences ...
DROP TABLE IF exists mzIdentML_PepEvidence;
CREATE TABLE mzIdentML_PepEvidence
(id INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (id), KEY (peptide_ref), KEY (dbSequence_ref), KEY(sequence), KEY(entry))
  ENGINE=MyISAM
    SELECT
      peptide_ref ,
      dbSequence_ref,
      if(length(pre)<1, "-", pre) as pre,
      if(length(post)<1, "-", post) as post,
      start,
      end,
      sequence,
      entry,
      is_decoy
    FROM
      (
        SELECT
          mzid_pep.`index` as peptide_ref ,
          pro.`index` as dbSequence_ref,
          MID( pro.sequence, `start`-1, 1) as pre,
          MID( pro.sequence, `end`+2, 1) as post,
          pep.start,
          pep.end,
          pep.sequence,
          pro.entry,
          CASE
          WHEN (pro.entry like 'REVERSE%') THEN TRUE
          ELSE FALSE
          END AS is_decoy
        FROM `peptide` as pep
          JOIN protein as pro ON pro.`index` = pep.`protein_index`
          JOIN mzidentml_dbsequence as mzid_dbseq ON mzid_dbseq.`index` = pro.`index`
          INNER JOIN best_peptides_for_quantification bp ON bp.sequence = pep.sequence
          JOIN peptides_in_proteins_before_homology_filtering as prbef ON pro.entry = prbef.entry
          JOIN mzidentml_peptide mzid_pep ON ( mzid_pep.sequence = pep.sequence AND mzid_pep.modifier = pep.modifier )
        GROUP BY pep.sequence, prbef.entry
      ) as xx;
-- @ :	retrieving peptide evidences ...
SELECT * FROM mzIdentML_PepEvidence;