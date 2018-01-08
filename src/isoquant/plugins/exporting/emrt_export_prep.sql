
-- @ :	pooling splitted peaks ...
DROP TABLE IF EXISTS clustered_emrt_pooled;
CREATE TABLE clustered_emrt_pooled 
SELECT 
	`index`,
	`workflow_index`,
	`cluster_average_index`,
	ROUND( AVG(mass), 4) as mass,
	ROUND( AVG(Mobility), 2) as mobility,
	ROUND( AVG(rt), 2) as rt,
	ROUND( AVG(ref_rt), 3) as ref_rt,
	sum( `inten` ) as inten,
	FLOOR( sum( `cor_inten` ) ) as cor_inten
FROM 
	`clustered_emrt`
GROUP BY 
	`workflow_index`, `cluster_average_index`
;
ALTER TABLE clustered_emrt_pooled ADD INDEX( cluster_average_index );
ALTER TABLE clustered_emrt_pooled ADD INDEX( workflow_index );

-- @ :	pooling splitted peaks [done]

--SELECT
--	cluster_average_index,
--	count(DISTINCT workflow_index) as replication_rate,
--	GROUP_CONCAT(
--		`workflow_index`, ':', `mass`, ':', `rt`, ':', ref_rt, ':', inten, ':', cor_inten
--		ORDER BY `workflow_index` ASC SEPARATOR ';'
--	) as data
--FROM
--	clustered_emrt_pooled
--GROUP BY
--	cluster_average_index
