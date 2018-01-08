-- @ :	filtering proten homology . . .

-- executing this file will replace contents 
-- of table peptides_in_proteins
-- by contents after homology filtering
-- the filtering is done by unlisting homolgue proteins 
-- while leaving the best ones listed


-- INPUT: peptides_in_proteins(sequence, entry, src_proteins)

-- make working copy of table peptides_in_proteins
DROP TABLE IF EXISTS peptides_in_proteins_temp;
CREATE TABLE peptides_in_proteins_temp LIKE peptides_in_proteins;
INSERT INTO peptides_in_proteins_temp SELECT * FROM peptides_in_proteins;
OPTIMIZE TABLE peptides_in_proteins_temp;

-- @ :		storing proteins identified by unique peptides ...
-- proteins with at least one unique peptide
DROP TABLE IF EXISTS unique_prots;
CREATE TABLE unique_prots
SELECT 
	DISTINCT entry
FROM
	peptides_in_proteins as pip
WHERE
	src_proteins = 1
;
ALTER TABLE unique_prots ADD INDEX(entry);

-- new peptides_in_proteins table containing only
-- proteins having at least 1 unique peptide
DROP TABLE IF EXISTS peptides_in_unique_prots;
CREATE TABLE peptides_in_unique_prots
SELECT 
	sequence, entry
FROM 
	peptides_in_proteins JOIN unique_prots USING(entry)
;
ALTER TABLE peptides_in_unique_prots ADD INDEX(sequence);
ALTER TABLE peptides_in_unique_prots ADD INDEX(entry);

-- unique_peptides_in_proteins contains pep.seq, prot.entry 
-- for proteins with at least one unique peptide

-- we will trace protein depletion
DROP TABLE IF EXISTS protein_homology_filter_trace;
CREATE TABLE protein_homology_filter_trace(
	entryLeft VARCHAR(255),
	entryRemoved VARCHAR(255),
	loopNumber INT
);

-- write unique proteins into trace table
INSERT INTO protein_homology_filter_trace
SELECT 
	entry as entryLeft,
	entry as entryRemoved,
	0 as loopNumber
FROM
	unique_prots
;

-- deplete unique proteins from peptides_in_proteins_temp
DELETE
	peptides_in_proteins_temp 
FROM
	peptides_in_proteins_temp JOIN unique_prots USING(`entry`)
;

-- trace proteins which are subsets of unique proteins
INSERT INTO protein_homology_filter_trace
SELECT 
	pup.entry AS entryLeft, 
	pip.entry AS entryRemoved,
	0 AS loopNumber 
FROM
	peptides_in_proteins_temp AS pip 
	JOIN peptides_in_unique_prots AS pup USING(`sequence`)
GROUP BY 
	entryLeft,
	entryRemoved
;

-- *****      ***** *********	<- 1x unique peptide
-- *****  ***					<- no uniques
--        *** *****				<- no uniques

-- remove all peptides matching to protein having at least one unique peptide
DELETE peptides_in_proteins_temp
FROM peptides_in_proteins_temp JOIN peptides_in_unique_prots USING(`sequence`);

-- get max protein.score for all entries
DROP TABLE IF EXISTS max_score_for_entry;
CREATE TEMPORARY TABLE max_score_for_entry
SELECT 
	entry, 
	max(score) as max_score,
	length(sequence) as seq_length
FROM protein GROUP BY entry;
ALTER TABLE max_score_for_entry ADD PRIMARY KEY(`entry`);

-- add score to peptides_in_proteins_temp
ALTER TABLE peptides_in_proteins_temp ADD COLUMN max_score DOUBLE;
ALTER TABLE peptides_in_proteins_temp ADD COLUMN seq_length INTEGER;
UPDATE
	peptides_in_proteins_temp as ap
	JOIN max_score_for_entry as mse USING(entry) 
SET 
	ap.max_score = mse.max_score,
	ap.seq_length = mse.seq_length
;

-- peptides_in_proteins_temp contains only shared peptides

DROP PROCEDURE IF EXISTS protFilterLoop;
-- DELIMITER //
CREATE PROCEDURE protFilterLoop()
BEGIN
	DECLARE doLoop BOOLEAN; -- x
	DECLARE hiProt VARCHAR(255); -- x
	DECLARE theCnt INT; -- x
	
	SET doLoop = TRUE; -- x
	SET hiProt = ''; -- x
	SET theCnt = 0; -- x
	
	-- check if table peptides_in_proteins_temp is empty? 
	SELECT if(count(*)>0, TRUE, FALSE) INTO doLoop FROM peptides_in_proteins_temp; -- x
	
	-- do loop until table peptides_in_proteins_temp has no entries
	WHILE doLoop DO

		-- count the loop
		SET theCnt = theCnt + 1; -- x

		-- count peptides in proteins having no unique peptides		
		DROP TABLE IF EXISTS nu; -- x
		CREATE TEMPORARY TABLE nu 
		SELECT count(DISTINCT `sequence`) as peps, entry, max_score, seq_length 
		FROM `peptides_in_proteins_temp` GROUP BY entry; -- x
		ALTER TABLE nu ADD INDEX(entry); -- x
		
		-- find protein having most peptides
		-- in case of multiple choice take one with the highest protein.score
		-- TODO extract score finding for entries before LOOP
		SELECT nu.entry INTO hiProt FROM nu GROUP BY entry 
		ORDER BY peps DESC, seq_length ASC, max_score DESC, entry ASC LIMIT 1;  -- x
		
		-- copy peptides of hiProt into unique_peptides_in_proteins
		INSERT INTO peptides_in_unique_prots 
		SELECT sequence, entry FROM peptides_in_proteins WHERE entry=hiProt; -- x
		
		-- create a list of peptide sequences for hiProt
		DROP TABLE IF EXISTS peptides_to_deplete; -- x
		CREATE TEMPORARY TABLE peptides_to_deplete
		SELECT DISTINCT sequence FROM peptides_in_proteins_temp WHERE entry=hiProt; -- x
		ALTER TABLE peptides_to_deplete ADD INDEX(`sequence`); -- x

		-- make trace
		INSERT INTO protein_homology_filter_trace
		SELECT hiProt as entryLeft, entry as entryRemoved, theCnt as loopNumber
		FROM peptides_in_proteins_temp
		JOIN peptides_to_deplete USING(`sequence`) GROUP BY entry; -- x

		-- delete all proteins from peptides_in_proteins_temp having same peptides as hiProt
		DELETE peptides_in_proteins_temp FROM peptides_in_proteins_temp
		JOIN peptides_to_deplete USING(`sequence`); -- x

		-- check if table peptides_in_proteins_temp is empty?
		SELECT if(count(*)>0, TRUE, FALSE) INTO doLoop FROM peptides_in_proteins_temp; -- x

	END WHILE; -- x
END;
-- END //
-- DELIMITER ;

-- @ :		finding homologue proteins (may take a while) . . .

CALL protFilterLoop();
DROP PROCEDURE IF EXISTS protFilterLoop;

OPTIMIZE TABLE peptides_in_unique_prots;

-- count peptide sharing grades after homology filter
DROP TABLE IF EXISTS peptide_in_proteins_sharing_grades;
CREATE TEMPORARY TABLE peptide_in_proteins_sharing_grades
SELECT sequence, count(entry) as src_proteins
FROM peptides_in_unique_prots GROUP BY sequence;
ALTER TABLE peptide_in_proteins_sharing_grades ADD INDEX(sequence);

DROP TABLE IF EXISTS peptides_in_proteins;
CREATE TABLE peptides_in_proteins
	SELECT 
		npp.*, src_proteins
	FROM
		peptides_in_unique_prots as npp
		JOIN peptide_in_proteins_sharing_grades USING(sequence)
;
ALTER TABLE peptides_in_proteins ADD INDEX(`sequence`);
ALTER TABLE peptides_in_proteins ADD INDEX(`entry`);

DROP TABLE IF EXISTS peptides_in_proteins_after_homology_filtering;
CREATE TABLE peptides_in_proteins_after_homology_filtering LIKE peptides_in_proteins;
INSERT INTO peptides_in_proteins_after_homology_filtering SELECT * FROM peptides_in_proteins;
OPTIMIZE TABLE peptides_in_proteins_after_homology_filtering;

-- @ :	filtering protein homology/isoforms . . . [done]
