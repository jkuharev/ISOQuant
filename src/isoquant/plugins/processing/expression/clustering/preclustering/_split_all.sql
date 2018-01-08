
-- @ :		splitting all peaks into separate clusters ...
UPDATE clustered_emrt SET cluster_average_index=`index`;

-- @ :		optimizing EMRT indices ...
OPTIMIZE TABLE clustered_emrt;


-- @ :	preclustering ... [done]
