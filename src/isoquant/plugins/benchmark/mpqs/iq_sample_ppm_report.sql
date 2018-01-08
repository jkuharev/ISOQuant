-- ppm per sample
DROP TABLE IF EXISTS protein_sample_ppm;
CREATE TEMPORARY TABLE protein_sample_ppm
SELECT 
	entry, sample_index, ROUND(AVG(ppm),2) as ppm
FROM
	finalquant_extended
GROUP BY
	entry, sample_index
;
ALTER TABLE protein_sample_ppm ADD INDEX(entry);
ALTER TABLE protein_sample_ppm ADD INDEX(sample_index);


