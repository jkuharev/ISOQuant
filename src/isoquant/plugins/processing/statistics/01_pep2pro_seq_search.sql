
-- @ :		building protein lookup table ...
-- list of proteins
DROP TABLE IF EXISTS temp_prots;
CREATE TEMPORARY TABLE temp_prots SELECT `sequence`, entry FROM protein GROUP BY entry;
ALTER TABLE temp_prots ADD INDEX (sequence(100));
OPTIMIZE TABLE temp_prots;

-- @ :		building peptide lookup table ...
-- list of peptides
DROP TABLE IF EXISTS temp_peps;
CREATE TEMPORARY TABLE temp_peps SELECT DISTINCT `sequence` FROM peptide;
ALTER TABLE temp_peps ADD INDEX (sequence(100));
OPTIMIZE TABLE temp_peps;

-- @ :		searching peptide sequences in proteins (may take a while) ...
-- all existing combinations of peptides and proteins
-- WARNING: this query is very slow on MAC!!!
DROP TABLE IF EXISTS pep2proBySeq;
CREATE TEMPORARY TABLE pep2proBySeq
SELECT temp_peps.sequence AS pepseq, entry
FROM temp_peps JOIN temp_prots 
ON LOCATE(temp_peps.`sequence`, temp_prots.`sequence`) > 0;

ALTER TABLE pep2proBySeq ADD INDEX(`pepseq`);
ALTER TABLE pep2proBySeq ADD INDEX(`entry`);
OPTIMIZE TABLE pep2proBySeq;
-- @ building peptide to protein relations ... [done]