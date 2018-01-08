
-- @ :	relating peptides to proteins ...
-- collect existing unique relations of peptides mapped to proteins
DROP TABLE IF EXISTS peptides_in_proteins;
CREATE TABLE peptides_in_proteins
	SELECT 
		pep.sequence 					as sequence,
		pro.entry						as entry,
		MAX(pep.stat_count_proteins)	as src_proteins
	FROM
	-- filtered peptides only!
		peptides_passing_filter 
		JOIN peptide as pep USING(`index`)
		JOIN protein as pro ON pep.`protein_index`=pro.`index`
	-- remove redundand peptide-protein relations
	GROUP BY 
		pep.sequence, pro.entry
;
ALTER TABLE peptides_in_proteins ADD INDEX( sequence(255) );
ALTER TABLE peptides_in_proteins ADD INDEX( entry );

-- @ :		backing up ...

-- backup table peptides_in_proteins
DROP TABLE IF EXISTS peptides_in_proteins_before_homology_filtering;
CREATE TABLE peptides_in_proteins_before_homology_filtering LIKE peptides_in_proteins;
INSERT INTO peptides_in_proteins_before_homology_filtering
	SELECT * FROM peptides_in_proteins;
OPTIMIZE TABLE peptides_in_proteins_before_homology_filtering;

-- @ :		recount proteins for peptides ...

-- count peptide sharing grades after identification filter
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
