
-- @ filtering quantified proteins by FPR . . . 
DROP TABLE IF EXISTS finalquant;
CREATE TABLE finalquant
SELECT
	sample_index, workflow_index, entry, top3_avg_inten
FROM
	finalquant_fpr
WHERE
	fpr <= %MAX_PROTEIN_FDR%
;
ALTER TABLE finalquant ADD INDEX(sample_index);
ALTER TABLE finalquant ADD INDEX(workflow_index);
ALTER TABLE finalquant ADD INDEX(entry);
-- @ filtering quantified proteins by FPR . . . [done]