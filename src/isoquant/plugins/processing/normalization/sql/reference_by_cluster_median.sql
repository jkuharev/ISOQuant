
-- @ : creating normalization reference by emrt cluster median ...
DROP TABLE IF EXISTS normalization_reference;

CREATE TABLE normalization_reference
SELECT
	cluster_average_index as `cluster`,
	(
		substring_index(
			substring_index( 
				group_concat(cor_inten order by cor_inten),
				',',
				ceiling(peakCount(*)/2) 
			),
			',',
			-1) + 
		substring_index( 
			substring_index( 
				group_concat(cor_inten order by cor_inten),
				',',
				-ceiling(peakCount(*)/2) 
			),
			',',
			1
		)
	) / 2 as `refint`,
	peakCount(`index`) as size
FROM 
	pooled_ce 
GROUP BY 
	`cluster`
HAVING 
	size>1
;

ALTER TABLE normalization_reference ADD INDEX(`cluster`);
-- @ : creating normalization reference by emrt cluster median ... [done]