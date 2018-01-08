-- @ :	preclustering ...
-- @ :	creating initial all-peaks-cluster

-- CREATE INPUT TABLE ce(id, nc, pc, mz, rt, dt)
-- 
DROP TABLE IF EXISTS ce;
CREATE TABLE ce 
SELECT 
	`index` as id, 
	1 as nc, 
	1 as pc, 
	mass as mz, 
	ref_rt as rt,
	Mobility as dt
FROM 
	clustered_emrt
;
-- ALTER TABLE ce ADD INDEX(id);
-- ALTER TABLE ce ADD INDEX(nc);
-- ALTER TABLE ce ADD INDEX(pc);
-- ALTER TABLE ce ADD INDEX(mz);
-- ALTER TABLE ce ADD INDEX(rt);
-- ALTER TABLE ce ADD INDEX(dt);