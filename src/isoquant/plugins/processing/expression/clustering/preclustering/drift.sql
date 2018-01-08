-- @ :		splitting clusters by ion mobility (drift time) domain ...

-- backup previous clustering results
DROP TABLE IF EXISTS tce;
RENAME TABLE ce TO tce;
UPDATE tce SET pc=nc;

-- max drift time distance definition
SET @dDRIFT := 1.0 * %MAX_DELTA_DRIFT%;

-- START MZ SEPARATION
SET @preV := 1.0;
SET @newC := 0;
SET @preC := 0;

DROP TABLE IF EXISTS ce;
CREATE TABLE ce
SELECT 
	id,
	@newC := @newC + if( ((dt-@preV)<@dDRIFT) AND (@preC=pc), 0, 1 ) as nc,
	@preC := pc as pc,
	mz,
	rt,
	@preV := dt as dt
FROM 
	tce
ORDER BY 
	tce.nc ASC, 
	tce.dt ASC
;
-- END MZ SEPARATION


-- @ :		splitting clusters by ion mobility (drift time) domain ... [done]