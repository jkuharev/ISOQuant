-- @ calculating protein coverage . . .

-- @ :	collecting absolute peptide positions . . .
-- protein sequences
DROP TABLE IF EXISTS p4c;
CREATE TABLE `p4c` (  
	sequence VARCHAR(255), entry VARCHAR(255),
	start INT, stop INT, len INT
);
INSERT INTO p4c
SELECT
	pep.sequence,
        entry,
        @s:=LOCATE( pep.sequence, pro.sequence ) as start,
        (@s + length(pep.sequence)) as stop,
        length(pro.sequence) as len
FROM 
	(SELECT DISTINCT entry, sequence FROM protein) as pro 
	JOIN peptides_in_proteins as pep USING(entry),
	(SELECT @s:=0) xxx
;

DROP TABLE IF EXISTS eid;
CREATE TABLE eid 
SELECT 
	entry, @eid:=@eid+1 as eid
FROM
	(SELECT DISTINCT entry FROM p4c ORDER BY entry) x1,
        (SELECT @eid:=0) x2
;
ALTER TABLE eid ADD INDEX(entry);
ALTER TABLE eid ADD INDEX(eid);

-- @ :	determining peptide overlaps . . .
-- partial lengths and positions of peptides in proteins
DROP TABLE IF EXISTS p4cr;
CREATE TABLE p4cr
-- (	part_start INT, part_stop INT, part_len INT, prot_len INT,
-- 	entry VARCHAR(255), sequence VARCHAR(255) 
-- );
-- INSERT INTO p4cr
SELECT
	@s := if( (eid<>@id) OR (start > @e), start, @e ) as part_start,
	@e := if( (eid<>@id) OR (stop > @e), stop, @e ) as part_stop,
	(@e - @s) as part_len,
	len as prot_len,
	(@id := eid) as eid,
	entry,
	sequence
FROM
	p4c JOIN eid USING(entry), (SELECT @id:='x', @s:=0, @e:=0) x
ORDER BY
	entry ASC,
	start ASC,
	stop ASC
;
ALTER TABLE p4cr ADD INDEX(entry);

-- @ :	pooling coverage values . . .
-- store calculated coverage
DROP TABLE IF EXISTS protein_coverage;
CREATE TABLE protein_coverage (entry VARCHAR(255), coverage DOUBLE) ENGINE=MYISAM;
INSERT INTO protein_coverage
SELECT 
	entry, ROUND( 100.0 * sum(part_len) / prot_len, 2)  as coverage
FROM
	p4cr
GROUP BY
	entry
;
ALTER TABLE p4c ADD INDEX(entry);

DROP TABLE IF EXISTS p4c;
DROP TABLE IF EXISTS p4cr;
DROP TABLE IF EXISTS eid;

-- @ calculating protein coverage . . . [done]