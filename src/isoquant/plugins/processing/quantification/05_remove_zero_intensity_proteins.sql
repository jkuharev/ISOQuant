-- @ removing non-quantifiable proteins ...
DROP TABLE IF EXISTS tmp_noquant;
CREATE TEMPORARY TABLE tmp_noquant
SELECT entry, max(dist_inten) as max_dist_inten
FROM emrt4quant GROUP BY entry HAVING max_dist_inten = 0;
ALTER TABLE tmp_noquant ADD PRIMARY KEY (entry);
DELETE emrt4quant FROM emrt4quant JOIN tmp_noquant USING(entry);