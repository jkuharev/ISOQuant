-- @ :	filtering protein homology . . .

-- restore backup
-- DELETE FROM peptides_in_proteins WHERE 1;
-- INSERT INTO peptides_in_proteins SELECT * FROM cluster_info_proteins_before_homology_filtering;
-- OPTIMIZE TABLE peptides_in_proteins;

-- trace of protein depletion
DROP TABLE IF EXISTS protein_homology_filter_trace;
CREATE TABLE protein_homology_filter_trace(
	entryLeft VARCHAR(255),
	entryRemoved VARCHAR(255),
	loopNumber INT
);

-- TODO remove sequence and work only with entries !!!
-- surviving proteins and their peptides
DROP TABLE IF EXISTS protein_homology_filter_result;
CREATE TABLE protein_homology_filter_result(
	entry		VARCHAR(255),
	sequence	VARCHAR(255),
	PRIMARY KEY (entry, sequence)
);

-- make working copy of table peptides_in_proteins
DROP TABLE IF EXISTS pip;
CREATE TABLE pip LIKE peptides_in_proteins;
INSERT INTO pip SELECT * FROM peptides_in_proteins;
-- add score and length columns
ALTER TABLE pip ADD COLUMN max_score DOUBLE;
ALTER TABLE pip ADD COLUMN seq_length INTEGER;
OPTIMIZE TABLE pip;
-- get max protein.score for all entries
DROP TABLE IF EXISTS max_score_for_entry;
CREATE TEMPORARY TABLE max_score_for_entry
	SELECT entry, max(score) as max_score, length(sequence) as seq_length 
	FROM protein GROUP BY entry
;
ALTER TABLE max_score_for_entry ADD PRIMARY KEY(`entry`);
UPDATE pip JOIN max_score_for_entry as mse USING(entry) 
	SET pip.max_score = mse.max_score, pip.seq_length = mse.seq_length
;
-- recount peptide sharing grades
UPDATE pip JOIN (SELECT sequence, count(entry) as cnt FROM pip GROUP BY sequence) xx USING(sequence) 
	SET src_proteins = cnt
;
OPTIMIZE TABLE pip;

-- @ :		storing proteins identified by unique peptides ...
-- proteins with at least one unique peptide
DROP TABLE IF EXISTS unique_prots;
CREATE TABLE unique_prots 
	SELECT DISTINCT entry FROM pip WHERE src_proteins = 1
;
ALTER TABLE unique_prots ADD INDEX(entry);

-- include unique proteins into results
INSERT INTO protein_homology_filter_result
	SELECT entry, sequence FROM pip JOIN unique_prots USING(entry)
;

-- write unique proteins into trace table
INSERT INTO protein_homology_filter_trace
	SELECT entry as entryLeft, entry as entryRemoved, 0 as loopNumber FROM unique_prots
;

-- deplete unique proteins from pip
DELETE pip FROM pip JOIN unique_prots USING(entry);

-- trace depleted proteins
INSERT INTO protein_homology_filter_trace
	SELECT
		res.entry AS entryLeft, pip.entry AS entryRemoved, 0 AS loopNumber 
	FROM 
		pip JOIN protein_homology_filter_result AS res USING(`sequence`)
	GROUP BY
		entryLeft, entryRemoved
;

-- deplete peptides matching to unique proteins
DELETE pip FROM pip JOIN protein_homology_filter_result USING(`sequence`);

-- pip contains now only shared peptides

DROP PROCEDURE IF EXISTS protFilterLoop;
-- DELIMITER //
CREATE PROCEDURE protFilterLoop()
BEGIN
	DECLARE doLoop BOOLEAN; -- x
	DECLARE hiProt VARCHAR(255); -- x
	DECLARE theCnt INT; -- x
	DECLARE minProts INT; -- x
	
	SET doLoop = TRUE; -- x
	SET hiProt = ''; -- x
	SET theCnt = 0; -- x
	SET minProts = 0; -- x
		
	-- check if table pip is empty? 
	SELECT if(count(*)>0, TRUE, FALSE) INTO doLoop FROM pip; -- x
	
	-- DROP TABLE IF EXISTS debug_trace; -- x
	-- CREATE TABLE debug_trace ( l INT, k VARCHAR(255), v VARCHAR(255) ); -- x
	
	-- do loop until table pip has no entries
	WHILE doLoop DO

		-- count the loop
		SET theCnt = theCnt + 1; -- x

		-- recount proteins for every peptide
		DROP TABLE IF EXISTS protein_homology_recount_proteins; -- x
		CREATE TEMPORARY TABLE protein_homology_recount_proteins
		SELECT sequence, count(entry) as nProts 
		FROM pip GROUP BY sequence; -- x
		ALTER TABLE protein_homology_recount_proteins ADD PRIMARY KEY(sequence); -- x

		-- recount peptides for every protein
		DROP TABLE IF EXISTS protein_homology_recount_peptides; -- x
		CREATE TEMPORARY TABLE protein_homology_recount_peptides 
		SELECT entry, count(DISTINCT `sequence`) as nPeps
		FROM pip GROUP BY entry; -- x
		ALTER TABLE protein_homology_recount_peptides ADD PRIMARY KEY(entry); -- x
		
		-- get the lowest possible number of proteins
		SELECT min(nProts) INTO minProts FROM protein_homology_recount_proteins; -- x
		
		-- davon das protein auswählen mit der höchsten anzahl der peptide.
		
		-- find protein having most peptides
		-- in case of multiple choice take one with the highest protein.score
		-- TODO extract score finding for entries before LOOP
		SELECT entry INTO hiProt 
		FROM 
			pip
			JOIN protein_homology_recount_proteins USING(sequence)
			JOIN protein_homology_recount_peptides USING(entry)
		WHERE
			-- take sequences mapping to the lowest number of proteins
			nProts = minProts
		GROUP BY
			entry
		ORDER BY
			-- prefer proteins having
			-- the highest number of peptides
			nPeps DESC,
			-- the shortest sequence
			seq_length ASC,
			-- the highest identification score
			max_score DESC,
			-- and in alphabetic order
			entry ASC 
		LIMIT 1
		;  -- x
						
		-- DEBUG tracing
		-- INSERT INTO debug_trace SELECT theCnt as l, "minProts" as k, minProts as v; -- x
		
		-- DEBUG tracing
		-- INSERT INTO debug_trace SELECT theCnt as l, "hiProt" as k, hiProt as v; -- x
		
		-- DEBUG tracing
		-- INSERT INTO debug_trace
		-- SELECT theCnt as l, "peptides in hiProt" as k, count(distinct sequence) as v 
		-- FROM peptides_in_proteins WHERE entry = hiProt
		-- ; -- x
		
		-- copy peptides of hiProt into unique_peptides_in_proteins
		INSERT INTO protein_homology_filter_result 
		SELECT entry, sequence FROM peptides_in_proteins WHERE entry = hiProt; -- x
		
		-- create a list of peptide sequences for hiProt
		DROP TABLE IF EXISTS peptides_to_deplete; -- x
		CREATE TEMPORARY TABLE peptides_to_deplete
		SELECT DISTINCT sequence FROM pip WHERE entry = hiProt; -- x
		ALTER TABLE peptides_to_deplete ADD INDEX(`sequence`); -- x

		-- DEBUG tracing
		-- INSERT INTO debug_trace
		-- SELECT theCnt as l, "peptides to deplete" as k, count(*) as v FROM peptides_to_deplete
		-- ; -- x

		-- make trace
		INSERT INTO protein_homology_filter_trace
		SELECT hiProt as entryLeft, entry as entryRemoved, theCnt as loopNumber
		FROM pip JOIN peptides_to_deplete USING(`sequence`) GROUP BY entry; -- x

		-- delete all proteins from pip having same peptides as hiProt
		DELETE pip FROM pip JOIN peptides_to_deplete USING(`sequence`); -- x

		-- check if table pip is empty?
		SELECT if(count(*)>0, TRUE, FALSE) INTO doLoop FROM pip; -- x

	END WHILE; -- x
END;
-- END //
-- DELIMITER ;


-- @ :		finding homologue proteins (may take a while) . . .
CALL protFilterLoop();
DROP PROCEDURE IF EXISTS protFilterLoop;

OPTIMIZE TABLE protein_homology_filter_result;


-- count peptide sharing grades after homology filter
DROP TABLE IF EXISTS peptide_in_proteins_sharing_grades;
CREATE TEMPORARY TABLE peptide_in_proteins_sharing_grades
SELECT sequence, count(entry) as src_proteins
FROM protein_homology_filter_result GROUP BY sequence;
ALTER TABLE peptide_in_proteins_sharing_grades ADD INDEX(sequence);

DROP TABLE IF EXISTS peptides_in_proteins;
CREATE TABLE peptides_in_proteins
	SELECT 
		npp.*, src_proteins
	FROM
		protein_homology_filter_result as npp
		JOIN peptide_in_proteins_sharing_grades USING(sequence)
;
ALTER TABLE peptides_in_proteins ADD INDEX(`sequence`);
ALTER TABLE peptides_in_proteins ADD INDEX(`entry`);

DROP TABLE IF EXISTS peptides_in_proteins_after_homology_filtering;
CREATE TABLE peptides_in_proteins_after_homology_filtering LIKE peptides_in_proteins;
INSERT INTO peptides_in_proteins_after_homology_filtering SELECT * FROM peptides_in_proteins;
OPTIMIZE TABLE peptides_in_proteins_after_homology_filtering;

-- @ :	filtering protein homology/isoforms . . . [done]
