
-- @ : creating normalization reference by emrt cluster means ...
DROP TABLE IF EXISTS normalization_reference;

CREATE TABLE normalization_reference 
SELECT 
	cluster_average_index as `cluster`,
	cor_inten as `refint` 
FROM pooled_ce 
WHERE
	workflow_index=%RUN_INDEX%
;

ALTER TABLE normalization_reference ADD INDEX(`cluster`);
-- @ : creating normalization reference by emrt cluster means ... [done]