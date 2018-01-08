DROP TABLE IF EXISTS peptides_in_proteins;
CREATE TABLE peptides_in_proteins LIKE peptides_in_proteins_before_homology_filtering;
INSERT INTO peptides_in_proteins SELECT * FROM peptides_in_proteins_before_homology_filtering;

DELETE pip FROM 
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