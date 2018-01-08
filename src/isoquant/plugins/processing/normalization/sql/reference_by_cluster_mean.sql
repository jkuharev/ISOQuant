
-- @ : creating normalization reference by emrt cluster means ...
DROP TABLE IF EXISTS normalization_reference;
CREATE TABLE normalization_reference
SELECT 
	cluster_average_index as `cluster`,
	AVG(cor_inten) as `refint`,
	COUNT(`index`) as size
FROM
	pooled_ce 
GROUP BY `cluster`
HAVING size > 1
;
ALTER TABLE normalization_reference ADD INDEX(`cluster`);
-- @ : creating normalization reference by emrt cluster means ... [done]