-- @ :	pooling peptide sequences ...
DROP TABLE IF EXISTS mzIdentML_Peptide;
CREATE TABLE mzIdentML_Peptide
(UNIQUE(`index`))
  ENGINE=MyISAM
    SELECT
      min(pep.index) as `index`,
      bp.sequence,
      bp.modifier
    FROM best_peptides_for_quantification bp
      JOIN peptide pep USING (sequence, modifier)
    GROUP BY bp.sequence, bp.modifier;
-- @ :	retrieving peptide sequences ...
SELECT * FROM mzIdentML_Peptide;