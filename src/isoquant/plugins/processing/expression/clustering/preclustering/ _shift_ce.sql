-- @ :		shifting resolved big cluster identities ...

SET @maxClusterID = 1;

SELECT 
	( max(cluster_average_index)+1 ) INTO @maxClusterID
FROM
	clustered_emrt
;

UPDATE ce SET nc = nc + @maxClusterID;
