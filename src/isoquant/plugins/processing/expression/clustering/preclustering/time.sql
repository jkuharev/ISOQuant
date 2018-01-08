-- @ :		splitting clusters by retention time domain ...

-- backup previous clustering results
DROP TABLE IF EXISTS tce;
RENAME TABLE ce TO tce;
UPDATE tce SET pc=nc;

-- rt distance definition
SET @dRT := 1.0 * %MAX_DELTA_TIME%;

-- START RT SEPARATION
SET @preV := 1.0;
SET @newC := 0;
SET @preC := 0;

DROP TABLE IF EXISTS ce;
CREATE TABLE ce
SELECT 
	id,
	@newC := @newC + if( ((rt-@preV)<@dRT) AND (@preC=pc), 0, 1 ) as nc,
	@preC := pc as pc,
	mz,
	@preV := rt as rt,
	dt
FROM 
	tce
ORDER BY 
	tce.nc ASC,
	tce.rt ASC
;
-- END RT SEPARATION


-- @ :		splitting clusters by retention time domain ... [done]
