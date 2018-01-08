
-- @ :		storing preclustering results ...

-- @ :			optimizing EMRT indices ...
-- improve JOIN performance
ALTER TABLE ce ADD PRIMARY KEY(id);
-- ALTER TABLE ce ADD INDEX(nc);
OPTIMIZE TABLE clustered_emrt;
OPTIMIZE TABLE ce;

-- @ :			copying cluster identities ...
-- copy results of mass-rt-mass-rt-clustering into clustered_emrt table
UPDATE 
	ce JOIN clustered_emrt as tar ON ce.id=tar.`index`
SET 
	tar.cluster_average_index=ce.nc
;

-- @ :			optimizing EMRT indices ...
OPTIMIZE TABLE clustered_emrt;

-- @ :			backing up preclustering results ...
DROP TABLE IF EXISTS preclustered_peaks;
RENAME TABLE ce TO preclustered_peaks;
-- DROP TABLE ce;

-- @ :	preclustering ... [done]
