-- @ : filtering peptides for quantification ...
DROP TABLE IF EXISTS best_peptides_for_quantification;
CREATE TABLE best_peptides_for_quantification LIKE best_peptides_for_annotation;
INSERT INTO best_peptides_for_quantification
SELECT 
	cluster_average_index, 
	`sequence`,
	`modifier`,
	`type`,
	seq_per_cluster,
	max_cluster_score
FROM 
	best_peptides_for_annotation
WHERE
	`max_cluster_score` 	>= %PEPTIDE_MIN_MAX_SCORE_PER_CLUSTER%
	AND
	`type` IN (%PEPTIDE_TYPE_FILTER_ADDITIONAL_TYPES%)
;

OPTIMIZE TABLE best_peptides_for_quantification;
-- @ : filtering peptides for quantification ... [done]