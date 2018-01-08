-- @ :		splitting clusters by mass domain ...

-- backup previous clustering results
DROP TABLE IF EXISTS tce;
RENAME TABLE ce TO tce;
UPDATE tce SET pc=nc;

-- mass distance definition
SET @dPPM := 1.0 * %MAX_DELTA_MASS_PPM%;

-- START MZ SEPARATION
SET @preV := 1.0;
SET @newC := 0;
SET @preC := 0;

DROP TABLE IF EXISTS ce;
CREATE TABLE ce
SELECT 
	id,
	@newC := @newC + if( (ABS(mz-@preV)/@preV < @dPPM) AND (@preC=pc), 0, 1 ) as nc,
	@preC := pc as pc,
	@preV := mz as mz,
	rt,
	dt
FROM
	tce
ORDER BY
	tce.nc ASC,
	tce.mz ASC
;
-- END MZ SEPARATION

-- @ :		splitting clusters by mass domain ... [done]