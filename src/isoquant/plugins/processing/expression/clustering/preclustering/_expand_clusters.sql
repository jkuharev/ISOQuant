-- @ :		clustering consensus peaks ... [done]

-- @ :		merging in-sample clusters ...

-- update preclustered_peaks by results from ce
ALTER TABLE ce ADD INDEX(id);

UPDATE 
	preclustered_peaks as pp JOIN ce ON pp.pc=ce.id
SET 
	pp.nc=ce.nc
;

-- move results back to ce
DROP TABLE ce;
RENAME TABLE preclustered_peaks TO ce;

-- @ :		merging in-sample clusters ... [done]