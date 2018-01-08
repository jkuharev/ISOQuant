-- @ : cleaning tables . . .

DROP TABLE IF EXISTS feature_fdr_stats;
CREATE TABLE feature_fdr_stats (
	timepoint	TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	description	VARCHAR(255),
	all_count	INT,
	fp_count	INT,
	fdr		DOUBLE
);

-- @ : flagging false positive proteins . . .

-- what proteins are fp
DROP TABLE IF EXISTS protein_fp;
CREATE TABLE protein_fp
SELECT
	DISTINCT entry,
	IF( entry LIKE '%REVERSE%' 
		 OR entry LIKE '%RANDOM%'
		 OR entry LIKE '%DECOY%', 1, 0 ) as fp
FROM
	protein
;
ALTER TABLE protein_fp ADD PRIMARY KEY(entry);

-- @ : flagging false positive peptides . . .

-- check if a peptide (as sequence-modifier tuple) maps to a decoy protein
DROP TABLE IF EXISTS peptide_fp;
CREATE TABLE peptide_fp
SELECT 
	pep.sequence,
	pep.modifier,
	MAX(fp) as fp
FROM
	peptide as pep 
	JOIN protein as pro ON pep.`protein_index`=pro.`index`
	JOIN protein_fp USING(entry)
GROUP BY 
	pep.sequence, pep.modifier
;
ALTER TABLE peptide_fp ADD INDEX(sequence);
ALTER TABLE peptide_fp ADD INDEX(modifier);

