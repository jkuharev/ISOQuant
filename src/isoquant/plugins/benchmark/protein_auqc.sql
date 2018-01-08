
-- make sample average intensities for proteins
DROP TABLE IF EXISTS finalquant_sample_average;
CREATE TABLE finalquant_sample_average
SELECT
	`sample_index`,
	entry,
	AVG(`top3_avg_inten`) as inten
FROM 
	`finalquant`
GROUP BY 
	`sample_index`, entry
;
ALTER TABLE finalquant_sample_average ADD INDEX(entry);
ALTER TABLE finalquant_sample_average ADD INDEX(sample_index);

-- calculate absolute log-ratios
DROP TABLE IF EXISTS finalquant_abs_logratios;
CREATE TABLE finalquant_abs_logratios
SELECT
	workflow_index,
	ABS(LOG2(top3_avg_inten / inten)) as abs_logratio
FROM
	finalquant as f JOIN finalquant_sample_average as fsa
	USING(sample_index, entry)
ORDER BY
	abs_logratio ASC
;
ALTER TABLE finalquant_abs_logratios ADD INDEX(workflow_index);
