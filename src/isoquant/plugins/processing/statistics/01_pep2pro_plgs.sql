-- @ :		acquiring peptides in proteins (by PLGS assignment) ...
-- we trust in PLGS protein assignment
DROP TABLE IF EXISTS pep2proBySeq;
CREATE TEMPORARY TABLE pep2proBySeq
SELECT pep.sequence AS pepseq, entry 
FROM peptide as pep JOIN protein as pro ON pep.protein_index=pro.`index`
GROUP BY pep.sequence, entry
;

ALTER TABLE pep2proBySeq ADD INDEX(`pepseq`);
ALTER TABLE pep2proBySeq ADD INDEX(`entry`);
OPTIMIZE TABLE pep2proBySeq;
-- @ building peptide to protein relations ... [done]