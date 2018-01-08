
-- @ :		joining all peaks in a single cluster ...
UPDATE clustered_emrt SET cluster_average_index=1;

-- @ :		optimizing EMRT indices ...
OPTIMIZE TABLE clustered_emrt;


-- @ :	preclustering ... [done]
