
-- @ : creating normalization reference by in group emrt cluster means ...
DROP TABLE IF EXISTS normalization_reference_per_group_cluster;
CREATE TABLE normalization_reference_per_group_cluster 
SELECT 
	cluster_average_index as `cluster`, 
	group_index as `group`,
	AVG(cor_inten) as refint 
FROM 
	pooled_ce AS ce 
	JOIN workflow AS w ON ce.workflow_index=w.`index` 
	JOIN sample AS s ON w.sample_index=s.`index`
GROUP BY 
	`cluster`, `group` 
;
ALTER TABLE normalization_reference_per_group_cluster ADD INDEX(`cluster`);
ALTER TABLE normalization_reference_per_group_cluster ADD INDEX(`group`);
-- @ : creating normalization reference by in group emrt cluster means ... [done]