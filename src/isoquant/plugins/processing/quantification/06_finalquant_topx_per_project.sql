-- 06

-- @ calculating topX protein intensities ...
-- @ :	using the same peptides for a protein in all runs.

set @topXdegree = %TOPX_DEGREE%;
set @minPepCount = %MIN_PEPTIDE_COUNT%;


-- @ :	reordering peptides ...
-- for each protein-peptide the max inten from all runs
DROP TABLE IF EXISTS toppep;
CREATE TEMPORARY TABLE toppep
SELECT 
	`entry`, 
	`sequence`, 
	`modifier`, 
	max(`dist_inten`) as max_dist_inten
FROM 
	`emrt4quant`
GROUP BY 
	`entry`, 
	`sequence`, 
	`modifier`
ORDER BY 
	entry, 
	max_dist_inten DESC
;

-- @ :	limiting number of peptides per protein ...
DROP TABLE IF EXISTS topxpep;
CREATE TEMPORARY TABLE topxpep
	SELECT 
		*
	FROM
		(
			SELECT 
				tp.`entry`, 
				tp.`sequence`, 
				tp.`modifier`, 
				tp.`max_dist_inten`,
				@num := if(@entry = tp.entry, @num +1, 1) as row_number,
				@entry := tp.entry as dummy
			FROM 
				toppep as tp,
				(SELECT @num:=0, @entry:='') as vx
		) as txp
	WHERE 
		row_number <= @topXdegree
;
ALTER TABLE topxpep ADD INDEX(entry);
ALTER TABLE topxpep ADD INDEX(`sequence`);
ALTER TABLE topxpep ADD INDEX(`modifier`);

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




-- @ :	calculating topX quantities ...
DROP TABLE IF EXISTS finalquant;
CREATE TABLE finalquant
SELECT
	`sample_index`, 
	`workflow_index`, 
	`entry`, 
	avg(`dist_inten`) as top3_avg_inten
FROM 
	`emrt4quant` as eq JOIN topxpep as tp USING(`entry`, `sequence`, `modifier`)
--	quantification_topxfinalquant
GROUP BY
	`sample_index`, 
	`workflow_index`, 
	`entry`
;
ALTER TABLE finalquant ADD INDEX(sample_index);
ALTER TABLE finalquant ADD INDEX(workflow_index);
ALTER TABLE finalquant ADD INDEX(entry);