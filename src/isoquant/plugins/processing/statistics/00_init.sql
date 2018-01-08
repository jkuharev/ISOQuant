-- @ building peptide to protein relations ...

OPTIMIZE TABLE peptide;
OPTIMIZE TABLE protein;

-- @ :	using all proteins ...
-- unfiltered peptides to proteins relation
DROP TABLE IF EXISTS pep2pro;
CREATE TEMPORARY TABLE pep2pro
SELECT 
	pe.`index` as pep_index, w.sample_index, pe.`workflow_index`, 
	pe.`sequence`, pe.`modifier`, pe.type, pe.`score` as pep_score, 
	pr.`entry`, pr.`score` as prot_score
FROM 
	`peptide` as pe 
	INNER JOIN protein as pr ON pe.`protein_index`=pr.`index`
	LEFT JOIN workflow as w ON w.`index`=pe.`workflow_index`
;
ALTER TABLE pep2pro ADD INDEX (`pep_index`);
ALTER TABLE pep2pro ADD INDEX (`sample_index`);
ALTER TABLE pep2pro ADD INDEX (`workflow_index`);
ALTER TABLE pep2pro ADD INDEX (`sequence`);
ALTER TABLE pep2pro ADD INDEX (`modifier`);
ALTER TABLE pep2pro ADD INDEX (`entry`);
