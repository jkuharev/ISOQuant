-- @ :	collecting peptides in proteins statistics . . .

-- @ :		counting unique peptides . . . 
DROP TABLE IF EXISTS upc;
CREATE TEMPORARY TABLE upc
SELECT entry, count(distinct sequence) AS ucnt
FROM
	peptides_in_proteins_before_homology_filtering as bhf
	JOIN peptides_in_proteins as pip USING(entry, sequence)
WHERE
	bhf.src_proteins=1
GROUP BY entry
;
ALTER TABLE upc ADD PRIMARY KEY(entry);

-- @ :		counting razor peptides . . . 
DROP TABLE IF EXISTS rpc;
CREATE TEMPORARY TABLE rpc
SELECT entry, count(distinct sequence) AS rcnt
FROM peptides_in_proteins
WHERE src_proteins=1
GROUP BY entry
;
ALTER TABLE rpc ADD PRIMARY KEY(entry);

-- @ :		counting shared peptides . . . 
DROP TABLE IF EXISTS spc;
CREATE TEMPORARY TABLE spc
SELECT entry, count(distinct sequence) AS scnt
FROM peptides_in_proteins
WHERE src_proteins>1
GROUP BY entry
;
ALTER TABLE spc ADD PRIMARY KEY(entry);

-- @ :		storing statistics . . .
DROP TABLE IF EXISTS peptides_in_proteins_stats;
CREATE TABLE peptides_in_proteins_stats
SELECT
	DISTINCT entry,
	0 as unique_peptides,
	0 as razor_peptides,
	0 as shared_peptides
FROM 
	peptides_in_proteins
;
ALTER TABLE peptides_in_proteins_stats ADD PRIMARY KEY(entry);

-- commit unique peptides
UPDATE peptides_in_proteins_stats JOIN upc USING(entry) 
SET unique_peptides = ucnt;

-- commit razor peptides
UPDATE peptides_in_proteins_stats JOIN rpc USING(entry)
SET razor_peptides = rcnt;

-- correct razor peptides by subtracting uniques
UPDATE peptides_in_proteins_stats JOIN rpc USING(entry) JOIN upc USING(entry)
SET razor_peptides = (rcnt - ucnt);

-- commit shared peptides
UPDATE peptides_in_proteins_stats JOIN spc USING(entry)
SET shared_peptides = scnt;

DROP TABLE IF EXISTS upc;
DROP TABLE IF EXISTS rpc;
DROP TABLE IF EXISTS spc;

-- @ :	collecting peptides in proteins statistics . . . [done]