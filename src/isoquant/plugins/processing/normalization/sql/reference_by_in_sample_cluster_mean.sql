
-- @ : creating normalization reference by in sample emrt cluster means ... 
DROP TABLE IF EXISTS normalization_reference_per_sample_cluster;
CREATE TABLE normalization_reference_per_sample_cluster 
SELECT 
	cluster_average_index as `cluster`, 
	sample_index as `sample`, 
	AVG(cor_inten) as refint 
FROM 
	pooled_ce AS ce 
	JOIN workflow AS w ON ce.workflow_index=w.`index` 
GROUP BY 
	cluster, sample 
;
ALTER TABLE normalization_reference_per_sample_cluster ADD INDEX(`cluster`);
ALTER TABLE normalization_reference_per_sample_cluster ADD INDEX(`sample`);
-- @ : creating normalization reference by in sample emrt cluster means ... [done]