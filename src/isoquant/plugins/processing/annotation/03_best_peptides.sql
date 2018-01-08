-- @ :	determining best peptides for EMRT cluster annotation ...

-- --------------------------------------------------------------------
-- update indices
OPTIMIZE TABLE  
	`cluster_average`,`clustered_emrt`,`expression_analysis`,  
	`group`,`low_energy`,`peptide`,`project`,`protein`,
	`query_mass`,`sample`,`workflow`
;
-- --------------------------------------------------------------------

-- @ :		pooling peptide identifications per cluster ...
-- --------------------------------------------------------------------
-- group cluster-to-peptide-identities
DROP TABLE IF EXISTS cluster_annotation_pooled_by_peptide;
CREATE TEMPORARY TABLE cluster_annotation_pooled_by_peptide
SELECT 
	`cluster_average_index`,
	`sequence`, 
	`modifier`,
	`type`,
	MAX(`score`) AS max_cluster_score,
	SUM(`score`) AS sum_of_score,
	COUNT(DISTINCT pep.workflow_index) AS rep_count
FROM 
	emrts_annotated_by_filtered_peptides
	JOIN peptide AS pep ON peptide_index=pep.`index`
	JOIN clustered_emrt AS ce ON emrt_index=ce.`index`
GROUP BY 
	`cluster_average_index`, `sequence`, `modifier`, `type`
ORDER BY 
	`cluster_average_index`, sum_of_score DESC
;
ALTER TABLE cluster_annotation_pooled_by_peptide ADD INDEX (`cluster_average_index`);
ALTER TABLE cluster_annotation_pooled_by_peptide ADD INDEX (`sum_of_score`);
-- --------------------------------------------------------------------

-- @ :		filtering by maximum sequences per cluster ...

-- --------------------------------------------------------------------
-- find maximum sum of score for each cluster 
-- and apply sequence count filter
DROP TABLE IF EXISTS cluster_annotation_stats;
CREATE TEMPORARY TABLE cluster_annotation_stats
SELECT 
	cluster_average_index,
	MAX( sum_of_score ) AS sum_of_score, 
	COUNT(DISTINCT `sequence`) AS seq_per_cluster
FROM 
	cluster_annotation_pooled_by_peptide
GROUP BY 
	cluster_average_index
HAVING
	seq_per_cluster <= %MAX_ALLOWED_SEQUENCES_PER_EMRT_CLUSTER%
;
ALTER TABLE cluster_annotation_stats ADD INDEX (cluster_average_index);
ALTER TABLE cluster_annotation_stats ADD INDEX (sum_of_score);
-- --------------------------------------------------------------------

-- @ :		resolving conflicting annotations ...
-- --------------------------------------------------------------------
-- Liste der besten Peptide
-- @TODO: dokumentieren welche peptide selektiert werden, wie viele Konkurrenten
-- @TODO: eventuell type mit einbeziehen
DROP TABLE IF EXISTS best_peptides_for_annotation;
CREATE TABLE best_peptides_for_annotation
SELECT 
	cluster_average_index, 
	`sequence`,
	`modifier`,
	`type`,
	seq_per_cluster,
	max_cluster_score
FROM 
	cluster_annotation_pooled_by_peptide
	JOIN cluster_annotation_stats USING(cluster_average_index, sum_of_score)
ORDER BY
	cluster_average_index
;
-- amino acids leucine and isoleucine can not be distinguished by mass spec
-- peptides having I/L in the sequence are identified with the same score
-- so we get mutliple peptide sequences mapping to the same emrt clusters
-- removed:
-- ALTER TABLE best_peptides_for_annotation ADD PRIMARY KEY(cluster_average_index);
-- replacement:
ALTER TABLE best_peptides_for_annotation ADD INDEX (cluster_average_index);
ALTER TABLE best_peptides_for_annotation ADD INDEX (`sequence`);
ALTER TABLE best_peptides_for_annotation ADD INDEX (`modifier`);
ALTER TABLE best_peptides_for_annotation ADD INDEX (`type`);

-- @ :	determining best peptides for EMRT cluster annotation ... [done]
