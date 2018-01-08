-- @ :	preclustering ...
-- @ :	creating initial clusters by separating samples ...

-- CREATE INPUT TABLE ce(id, nc, pc, mz, rt, dt)
DROP TABLE IF EXISTS ce;
CREATE TABLE ce 
SELECT 
	ce.`index` as id,
	sample_index as nc,
	sample_index as pc,
	mass as mz,
	ref_rt as rt,
	Mobility as dt
FROM 
	clustered_emrt as ce JOIN workflow as w ON ce.`workflow_index`=w.`index`
;
-- ALTER TABLE ce ADD INDEX(id);
-- ALTER TABLE ce ADD INDEX(nc);
-- ALTER TABLE ce ADD INDEX(pc);
-- ALTER TABLE ce ADD INDEX(mz);
-- ALTER TABLE ce ADD INDEX(rt);
-- ALTER TABLE ce ADD INDEX(dt);