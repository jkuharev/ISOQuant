-- @ :	summing run intensities ...
-- summarize emrt intensities per run
DROP TABLE IF EXISTS ins;
CREATE TEMPORARY TABLE ins
	SELECT 
		`workflow_index`, 
		sum(`cor_inten`) as sum_of_inten
	FROM 
		`clustered_emrt` 
	GROUP BY 
		workflow_index;

-- @ :	determining average run intensity sum ...
-- get average sum of intensities
SELECT @avg_inten := 0;
SELECT AVG(`sum_of_inten`) INTO @avg_inten FROM `ins`;

-- @ :	calculating linear intensity correction factors ...
-- calculate correction factors per run
DROP TABLE IF EXISTS eqf;
CREATE TEMPORARY TABLE eqf
	SELECT 
		workflow_index, 
		(`sum_of_inten` / @avg_inten) as factor 
	FROM `ins`;
ALTER TABLE eqf ADD INDEX(workflow_index);

-- @ :	equalizing intensities ...
-- equalize emrt intensities
UPDATE 
	clustered_emrt as ce LEFT JOIN eqf USING(`workflow_index`)
SET
	ce.cor_inten = ce.cor_inten / eqf.factor;