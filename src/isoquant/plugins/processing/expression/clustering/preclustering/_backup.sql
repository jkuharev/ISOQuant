
-- @ creating emrt table backup ...

-- BACKUP PRECLUSTERING RESULTS
DROP TABLE IF EXISTS preclustered_emrt;
CREATE TABLE preclustered_emrt SELECT `index`, `cluster_average_index` FROM clustered_emrt;
ALTER TABLE preclustered_emrt ADD PRIMARY KEY(`index`);
ALTER TABLE preclustered_emrt ADD INDEX(`cluster_average_index`);

-- @ :	collecting cluster information ...

-- info about preclusters
DROP TABLE IF EXISTS preclustered_emrt_info;
CREATE TABLE preclustered_emrt_info
SELECT 
	`cluster_average_index`,
        count(*) as size,
        AVG(`mass`) as avg_mass,
        AVG(`ref_rt`) as avg_rt,
        AVG(`inten`) as avg_inten,
        MIN(`inten`) as min_inten,
        MAX(`inten`) as max_inten
FROM `clustered_emrt` 
GROUP BY `cluster_average_index`
-- HAVING size>400
ORDER BY size DESC
;

-- @ :	calculating histogram data ...

-- create precluster-size histogram
DROP TABLE IF EXISTS preclustered_emrt_sizes;
CREATE TABLE preclustered_emrt_sizes
SELECT 
	`cluster_average_index`,
	floor( exp( floor( log( count(`index`) )*10 ) /10 ) ) as size
FROM 
	`preclustered_emrt` 
GROUP BY
	`cluster_average_index`
;
ALTER TABLE preclustered_emrt_sizes ADD INDEX(`cluster_average_index`);
ALTER TABLE preclustered_emrt_sizes ADD INDEX(`size`);

DROP TABLE IF EXISTS preclustered_emrt_histogram;
CREATE TABLE preclustered_emrt_histogram
SELECT 
	size,
	count(`cluster_average_index`) as frequency
FROM
	preclustered_emrt_sizes
GROUP BY size
ORDER BY size DESC
;

-- @ creating emrt table backup ... [done]