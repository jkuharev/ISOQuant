
-- before running this script make sure
-- clusters in clustered_emrt contain only emrts of runs belonging to the same sample!!!
-- by e.g. running clustering in IN_SAMPLE mode

-- @ building sample consensus peak lists ...

-- @ :	determining sample sizes ...
DROP TABLE IF EXISTS sample_sizes;
CREATE TABLE sample_sizes
SELECT 
	`index` as workflow_index, 
	sample_index, 
	sample_size, 
	(sample_size/2) as min_cluster_size
FROM
	workflow as w JOIN 
	(
		SELECT sample_index, count(`index`) as sample_size 
		FROM workflow GROUP BY sample_index
	) as x 
	USING(sample_index)
;
ALTER TABLE sample_sizes ADD PRIMARY KEY(workflow_index);
ALTER TABLE sample_sizes ADD INDEX(sample_index);
ALTER TABLE sample_sizes ADD INDEX(sample_size);
ALTER TABLE sample_sizes ADD INDEX(min_cluster_size);


-- @ :	filtering clusters ...
DROP TABLE IF EXISTS cluster_sizes;
CREATE TABLE cluster_sizes
SELECT 
	cluster_average_index, COUNT(DISTINCT workflow_index) as cluster_size, min_cluster_size, sample_index
FROM 
	clustered_emrt LEFT JOIN sample_sizes USING(workflow_index)
GROUP BY 
	cluster_average_index
HAVING
	cluster_size >= min_cluster_size
;
ALTER TABLE cluster_sizes ADD PRIMARY KEY(cluster_average_index);


-- @ :	generating sample consensus runs ...
DROP TABLE IF EXISTS sample_emrt;
CREATE TABLE sample_emrt
SELECT
	cluster_average_index as `index`, 
	sample_index,
	ROUND(AVG(ref_rt),2) as rt, 
	ROUND(AVG(mass),4) as mass, 
	MAX(inten) as inten
FROM
	cluster_sizes LEFT JOIN clustered_emrt USING(cluster_average_index)
GROUP BY 
	cluster_average_index
;
ALTER TABLE sample_emrt ADD PRIMARY KEY(`index`);
ALTER TABLE sample_emrt ADD INDEX(`sample_index`);
ALTER TABLE sample_emrt ADD INDEX(`rt`);
ALTER TABLE sample_emrt ADD INDEX(`inten`);

-- @ :	removing temorary data ...
DROP TABLE IF EXISTS cluster_sizes;
DROP TABLE IF EXISTS sample_sizes;

-- @ building sample consensus peak lists ... [done]