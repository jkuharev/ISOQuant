-- @ : pooling split peaks ...
DROP TABLE IF EXISTS pooled_ce;
CREATE TABLE pooled_ce
SELECT 
	`index`,
	`workflow_index`,
	`cluster_average_index`,
	sum( `cor_inten` ) AS cor_inten
FROM 
	`clustered_emrt`
GROUP BY 
	`workflow_index` , `cluster_average_index`
;

-- @ : indexing pooled peaks ...
ALTER TABLE pooled_ce ADD INDEX(cluster_average_index);
ALTER TABLE pooled_ce ADD INDEX(workflow_index);

-- @ : pooling split peaks [done]