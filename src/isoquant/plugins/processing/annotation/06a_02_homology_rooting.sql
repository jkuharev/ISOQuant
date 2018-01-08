-- some times, homologue protein chains are sequentially resolved
-- like A removed by B, then B removed by C, then C removed by D.
-- At the end of this chain is the result that A, B, C are removed by D
-- however the filter trace contains only steps of every single iteration
-- like pairs A-B, B-C, C-D
-- rerooting procedure maps every removed protein to it's
-- top level representative protein (the only reported protein of a group).

-- @ :	rerooting homologe proteins . . .

DROP PROCEDURE IF EXISTS rerootHomology;

CREATE PROCEDURE rerootHomology()
BEGIN
	DECLARE doLoop BOOLEAN; -- x
	SET doLoop = TRUE; -- x
	
	-- get all protein pairs (excluding self-related)
	DROP TABLE IF EXISTS hr; -- x
	CREATE TABLE hr
	SELECT	entryLeft as pid, entryRemoved as id FROM protein_homology_filter_trace
	WHERE 	entryLeft != entryRemoved; -- x
	ALTER TABLE hr ADD INDEX(id); -- x
	ALTER TABLE hr ADD INDEX(pid); -- x
	
	-- store roots
	DROP TABLE IF EXISTS hrt; -- x
	CREATE TABLE hrt
	SELECT	r.pid, r.id FROM hr AS r LEFT JOIN hr AS p ON p.id=r.pid
	WHERE	p.id IS NULL; -- x
	ALTER TABLE hrt ADD INDEX(id); -- x
	ALTER TABLE hrt ADD INDEX(pid); -- x
	
	-- delete roots from original
	DELETE hr FROM hr JOIN hrt USING(pid, id); -- x
	OPTIMIZE TABLE hr; -- x
	
	-- count children
	SELECT if(count(*)>0, TRUE, FALSE) INTO doLoop FROM hrt JOIN hr ON hrt.id=hr.pid; -- x
	-- SELECT if(count(*)>0, TRUE, FALSE) INTO doLoop FROM hr; -- x
	
	-- do loop until no more children left
	WHILE doLoop DO
		-- store children of roots
		-- and update child.pid by root.pid
		-- converting children to roots
		DROP TABLE IF EXISTS hrc; -- x
		CREATE TABLE hrc
		SELECT hrt.pid, hr.id FROM hrt JOIN hr ON hrt.id=hr.pid
		GROUP BY hrt.pid, hr.id; -- x
	
		-- remove children of roots from original
		DELETE hr FROM hrt JOIN hr ON hrt.id=hr.pid; -- x
		-- OPTIMIZE TABLE hr; -- x
	
		-- copy children to roots
		INSERT INTO hrt SELECT * FROM hrc; -- x
		-- OPTIMIZE TABLE hrt; -- x
	
		-- count children
		SELECT if(count(*)>0, TRUE, FALSE) INTO doLoop FROM hrt JOIN hr ON hrt.id=hr.pid; -- x
		-- SELECT if(count(*)>0, TRUE, FALSE) INTO doLoop FROM hr; -- x
	-- end loop	
	END WHILE; -- x
	
	-- include self related proteins
	DROP TABLE IF EXISTS protein_homology; -- x
	CREATE TABLE protein_homology
	SELECT entryLeft, entryRemoved FROM protein_homology_filter_trace
	WHERE entryLeft = entryRemoved; -- x
	
	INSERT INTO protein_homology
	SELECT pid as entryLeft, id as entryRemoved FROM hrt; -- x
	
	-- remove entries appearing both on the right AND on the left
	DELETE a
	FROM protein_homology as a JOIN protein_homology as b ON a.entryRemoved=b.entryLeft
	WHERE a.entryLeft!=a.entryRemoved; -- x
	
	ALTER TABLE protein_homology ADD INDEX(entryLeft); -- x
	ALTER TABLE protein_homology ADD INDEX(entryRemoved); -- x
	
	DROP TABLE IF EXISTS hr; -- x
	DROP TABLE IF EXISTS hrt; -- x
	DROP TABLE IF EXISTS hrc; -- x
END;

CALL rerootHomology;
-- @ :	rerooting homologe proteins . . . [done]