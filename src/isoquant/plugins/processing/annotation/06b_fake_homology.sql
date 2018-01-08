-- @ :	skipping protein homology filtering . . . 
-- fake homology
DROP TABLE IF EXISTS protein_homology;
CREATE TABLE protein_homology
SELECT entry as entryLeft, entry as entryRemoved FROM peptides_in_proteins
GROUP BY entry;

ALTER TABLE protein_homology ADD INDEX(entryLeft);
ALTER TABLE protein_homology ADD INDEX(entryRemoved);
-- @ :	skipping protein homology filtering . . . [done]