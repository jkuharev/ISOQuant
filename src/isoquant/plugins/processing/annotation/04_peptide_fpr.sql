
-- FDR filtering
-- @ filtering peptides by FDR . . .

-- @ :	determining false positives . . .
-- find out if peptides are false positive
DROP TABLE IF EXISTS pep_fp;
CREATE TABLE pep_fp
SELECT
	sequence,
	modifier,
	MAX( IF( entry LIKE '%REVERSE%' OR entry LIKE '%RANDOM%', 1, 0 ) ) as fp,
	MAX(curated) as curated,
	MAX(score) as score
FROM
	peptides_in_proteins 
	JOIN best_peptides_for_annotation USING(sequence)
	JOIN peptide USING(sequence, modifier)
GROUP BY sequence, modifier
;

-- @ :	calculating FDR . . .
-- claculate peptide FDR
DROP TABLE IF EXISTS pep_fpr;
CREATE TABLE pep_fpr
SELECT
	sequence,
	modifier,
	curated,
	score,
	@id := (@id + 1) as id,
	@fps := (@fps + fp) as fps,
	ROUND( @fdr := if(@fdr > @fps/@id, @fdr, @fps/@id), 4) as fpr
FROM
	pep_fp, (SELECT @id:=0, @fps:=0, @fdr:=0) x
ORDER BY
	curated DESC, score DESC
;
ALTER TABLE pep_fpr ADD INDEX(sequence(100));
ALTER TABLE pep_fpr ADD INDEX(fpr);

-- @ :	depleting peptides not passing FDR filter . . .
-- delete peptides by FDR limit

DELETE best_peptides_for_annotation 
FROM 
	best_peptides_for_annotation JOIN pep_fpr USING(sequence, modifier)
WHERE
	fpr > %MAX_PEPTIDE_FDR%
;
OPTIMIZE TABLE best_peptides_for_annotation;

-- synchronize contents of best_peptides and peptides_in_proteins
DELETE
	pip
FROM 
	peptides_in_proteins as pip
	LEFT JOIN best_peptides_for_annotation as best USING(sequence)
WHERE
	best.sequence is NULL
;
OPTIMIZE TABLE peptides_in_proteins;

DROP TABLE IF EXISTS peptide_in_proteins_sharing_grades;
CREATE TEMPORARY TABLE peptide_in_proteins_sharing_grades
	SELECT
		sequence, count(entry) as protein_count
	FROM
		peptides_in_proteins
	GROUP BY
		sequence
;
ALTER TABLE peptide_in_proteins_sharing_grades ADD INDEX(sequence);

-- store resulting sharing grades
UPDATE
	peptides_in_proteins JOIN peptide_in_proteins_sharing_grades USING(sequence)
SET 
	src_proteins = protein_count
;
OPTIMIZE TABLE peptides_in_proteins;

-- @ filtering peptides by FDR . . . [done]