DROP PROCEDURE IF EXISTS findComplexProteins;
CREATE PROCEDURE findComplexProteins()
BEGIN 
	DECLARE nOld INT; -- x 
	DECLARE nNew INT; -- x 
	
	SET nOld = 0; -- x 
	SET nNew = 1; -- x 

	-- get proteins having no unique peptides at all
	DROP TABLE IF EXISTS tmp_proteins; -- x 
	CREATE TABLE tmp_proteins
	SELECT 
		entry
	FROM 
		`peptides_in_proteins_before_homology_filtering`
	GROUP BY 
		entry
	HAVING 
		MIN(`src_proteins`) > 1
	; -- x
	ALTER TABLE tmp_proteins ADD INDEX(entry); -- x

	WHILE (nNew > nOld)
	DO
		SET nOld = nNew; -- x 
		-- find peptides for previously selected entries
		DROP TABLE IF EXISTS tmp_peptides; -- x 
		CREATE TABLE tmp_peptides
		SELECT 
			DISTINCT sequence 
		FROM 
			`peptides_in_proteins_before_homology_filtering` JOIN tmp_proteins USING(entry); -- x 
		ALTER TABLE tmp_peptides ADD INDEX(sequence); -- x

		-- recollect entries by selected pepetides
		DROP TABLE IF EXISTS tmp_proteins; -- x
		CREATE TABLE tmp_proteins
			SELECT 
			DISTINCT entry
		FROM 
			`peptides_in_proteins_before_homology_filtering` JOIN tmp_peptides USING(sequence); -- x 
		ALTER TABLE tmp_proteins ADD INDEX(entry); -- x
		
		-- get current number of entries in tmp_proteins
		SELECT count(entry) INTO nNew FROM tmp_proteins; -- x 

	END WHILE; -- x 

	-- backup resulting entries
	DROP TABLE IF EXISTS proteins_in_reducible_networks; -- x 
	CREATE TABLE proteins_in_reducible_networks
	SELECT entry FROM tmp_proteins; -- x 
	ALTER TABLE proteins_in_reducible_networks ADD INDEX(entry); -- x
	
	DROP TABLE IF EXISTS tmp_proteins; -- x
	DROP TABLE IF EXISTS tmp_peptides; -- x
END ;

CALL findComplexProteins;