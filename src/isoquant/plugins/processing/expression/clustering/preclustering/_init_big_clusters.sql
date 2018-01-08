-- @ :	exploring pre-cluster sizes ...

SET @criticalSize = %CRITICAL_CLUSTER_SIZE%;
SET @maxClusterID = 1;

SELECT 
	( max(cluster_average_index)+1 ) INTO @maxClusterID
FROM
	clustered_emrt
;

DROP TABLE IF EXISTS big_clusters;
CREATE TABLE big_clusters
SELECT
	`cluster_average_index`, 
	count(*) as size
FROM 
	`clustered_emrt` 
GROUP BY 
	`cluster_average_index`
HAVING
	size > @criticalSize
;
ALTER TABLE big_clusters ADD INDEX(cluster_average_index);


-- CREATE INPUT TABLE ce(id, nc, pc, mz, rt, dt)
-- 
DROP TABLE IF EXISTS ce;
CREATE TABLE ce 
SELECT 
	`index` as id, 
	@maxClusterID as nc, 
	@maxClusterID as pc, 
	mass as mz, 
	ref_rt as rt,
	Mobility as dt
FROM 
	clustered_emrt JOIN big_clusters USING(cluster_average_index)
;
-- ALTER TABLE ce ADD INDEX(id);
-- ALTER TABLE ce ADD INDEX(nc);
-- ALTER TABLE ce ADD INDEX(pc);
-- ALTER TABLE ce ADD INDEX(mz);
-- ALTER TABLE ce ADD INDEX(rt);
-- ALTER TABLE ce ADD INDEX(dt);