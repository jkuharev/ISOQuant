

-- @ :		building proteins lookup table ...
DROP TABLE IF EXISTS tmp_prots;
CREATE TABLE tmp_prots
SELECT 
	@i:=(@i+1) as id, 
	entry, 
	sequence
FROM 
	(SELECT entry, sequence FROM protein GROUP BY entry) x1,
	(SELECT @i:=0) x2
;
ALTER TABLE tmp_prots ADD PRIMARY KEY ( id );
ALTER TABLE tmp_prots ADD INDEX ( entry );


-- @ :		building peptide lookup table ...
DROP TABLE IF EXISTS tmp_pepts;
CREATE TABLE tmp_pepts
SELECT 
	@i:=(@i+1) as id, 
	sequence
FROM 
	(SELECT sequence FROM peptide GROUP BY sequence) x1,
	(SELECT @i:=0) x2
;
ALTER TABLE tmp_pepts ADD PRIMARY KEY ( id );
ALTER TABLE tmp_pepts ADD INDEX ( sequence(100) );