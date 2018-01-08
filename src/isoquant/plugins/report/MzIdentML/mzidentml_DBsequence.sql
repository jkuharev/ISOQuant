-- DBSequence
-- @ :	pooling protein database sequences ...
DROP TABLE IF EXISTS mzIdentML_DBsequence;
CREATE TABLE mzIdentML_DBsequence
(KEY (`index`), KEY(entry))
  ENGINE=MyISAM
    SELECT
      min(pr.index) as `index`,
      pr.entry,
      pr.accession,
      pr.description,
      pr.sequence,
      length(pr.sequence) as seqlength
    FROM protein pr
-- JOIN finalquant fq USING (entry)
    GROUP BY pr.entry
    ORDER BY pr.entry;
-- @ :	retrieving protein database sequences ...
SELECT * FROM mzIdentML_DBsequence;