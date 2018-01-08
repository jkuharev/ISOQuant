-- using large numbers of neighbor points in DBSCAN
-- could result in noise (one point) clusters of identical features
-- e.g. it is the case for multiple searches of the same raw data.
-- solution: simply merge them!

-- @ :	merging clusters by identical features . . . 
UPDATE
	clustered_emrt as ce JOIN 
	(
		SELECT 
			mass, rt, Mobility, min(cluster_average_index) as cluster_index
		FROM
			clustered_emrt
		GROUP BY
			mass, rt, Mobility
		HAVING 
			count(*) > 1
	) as identical_features
	USING
	(
		mass, rt, Mobility
	)
SET 
	ce.cluster_average_index = cluster_index
WHERE
	cluster_average_index != cluster_index
;
-- @ :	merging clusters by identical features . . . [done!] 