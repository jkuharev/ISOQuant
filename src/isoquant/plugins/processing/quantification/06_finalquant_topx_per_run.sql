-- 06

-- @ calculating topX protein intensities ...
-- @ :	using different peptides for a protein in different runs.
set @topXdegree = %TOPX_DEGREE%;
set @minPepCount = %MIN_PEPTIDE_COUNT%;

-- @ :	reordering peptides ...
DROP TABLE IF EXISTS emrt4quant_reordered;
CREATE TEMPORARY TABLE emrt4quant_reordered
	SELECT 
		* 
	FROM 
		`emrt4quant`
	ORDER BY 
		`workflow_index` ASC, 
		`entry` ASC, 
		`dist_inten` DESC
;

-- @ :	limiting number of peptides per protein ...
DROP TABLE IF EXISTS topxpep;
CREATE TEMPORARY TABLE topxpep
	SELECT 
		cee.* 
	FROM
		(
			SELECT 
				ce.`workflow_index`, 
				ce.`sample_index`, 
				ce.`entry`, 
				ce.`sequence`, 
				ce.`dist_inten`,
				@num := if(@entry = ce.entry, @num +1, 1) as row_number,
				@entry := ce.entry as dummy
			FROM 
				emrt4quant_reordered as ce,
				(SELECT @num:=0, @entry:='') as vx
		) cee
	WHERE 
		row_number <= @topXdegree
;
ALTER TABLE topxpep ADD INDEX(entry);
ALTER TABLE topxpep ADD INDEX(sample_index);
ALTER TABLE topxpep ADD INDEX(workflow_index);

-- @ :	filtering proteins by mininum peptide count ...
DROP TABLE IF EXISTS topxpep_blacklist;
CREATE TEMPORARY TABLE topxpep_blacklist
	SELECT 
		entry 
	FROM 
		`topxpep`
	GROUP BY 
		`entry`
	HAVING 
		count(distinct sequence) < @minPepCount
;
ALTER TABLE topxpep_blacklist ADD INDEX(entry);

DELETE q FROM `topxpep` as q JOIN topxpep_blacklist USING(entry);




-- @ :	calculating topX quantities ...DROP TABLE IF EXISTS finalquant;
CREATE TABLE finalquant
	SELECT
		`sample_index`, 
		`workflow_index`, 
		`entry`, 
		avg(`dist_inten`) as top3_avg_inten
	FROM 
		topxpep
	GROUP BY
		`sample_index`, 
		`workflow_index`, 
		`entry`
;
ALTER TABLE finalquant ADD INDEX(sample_index);
ALTER TABLE finalquant ADD INDEX(workflow_index);
ALTER TABLE finalquant ADD INDEX(entry);