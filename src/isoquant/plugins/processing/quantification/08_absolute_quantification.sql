-- @ calculating absolute protein amounts ...

set @STD_FMOL = %STANDARD_FMOL%;
set @STD_ENTRY = '%STANDARD_ENTRY%';

-- @ .	calculating raw amounts ...
DROP TABLE IF EXISTS finalquant_raw_amounts;
CREATE TEMPORARY TABLE finalquant_raw_amounts
SELECT
	sample_index, workflow_index, entry, 
	top3_avg_inten,
	(top3_avg_inten * mw / 1000000) as rawAmount
FROM
	finalquant JOIN protein_info USING(entry)
;
ALTER TABLE finalquant_raw_amounts ADD INDEX(`sample_index`);
ALTER TABLE finalquant_raw_amounts ADD INDEX(`workflow_index`);
ALTER TABLE finalquant_raw_amounts ADD INDEX(`entry`);

-- @ .	summing raw amounts per run ...
DROP TABLE IF EXISTS finalquant_run_amounts;
CREATE TEMPORARY TABLE finalquant_run_amounts
SELECT
	workflow_index, sum( rawAmount ) as sum_amount
FROM 
	finalquant_raw_amounts
GROUP BY 
	workflow_index
;
ALTER TABLE finalquant_run_amounts ADD PRIMARY KEY(workflow_index);

-- @ .	summing standard raw amounts ...
DROP TABLE IF EXISTS finalquant_run_standard;
CREATE TEMPORARY TABLE finalquant_run_standard
SELECT
	workflow_index, top3_avg_inten, rawAmount
FROM 
	finalquant_raw_amounts
WHERE
	entry=@STD_ENTRY
GROUP BY 
	workflow_index
;
ALTER TABLE finalquant_run_standard ADD PRIMARY KEY(workflow_index);

-- @ .	standardizing raw amounts ...
DROP TABLE IF EXISTS finalquant_extended;
CREATE TABLE finalquant_extended
SELECT
	sample_index, workflow_index, entry, fq.top3_avg_inten, fq.rawAmount,
	(fq.top3_avg_inten / sum_amount * 1000) as fmolug,
	(fq.rawAmount / sum_amount) * 1000000 as ppm,
	(fq.top3_avg_inten * @STD_FMOL / fqs.top3_avg_inten) as absqfmol,
	(fq.rawAmount * @STD_FMOL / fqs.top3_avg_inten) as absqng
FROM
	finalquant_raw_amounts as fq 
	LEFT JOIN finalquant_run_standard as fqs USING(workflow_index)
	LEFT JOIN finalquant_run_amounts as fqr USING(workflow_index)
;
ALTER TABLE finalquant_extended ADD INDEX(`sample_index`);
ALTER TABLE finalquant_extended ADD INDEX(`workflow_index`);
ALTER TABLE finalquant_extended ADD INDEX(`entry`);

-- @ calculating absolute protein amounts ... [done]