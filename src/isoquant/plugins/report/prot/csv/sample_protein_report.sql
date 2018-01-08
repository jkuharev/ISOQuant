-- @ calculating samplewise protein quantification ...


-- @ :	calculating average protein abundances ...
DROP TABLE IF EXISTS sample_quant;
CREATE TEMPORARY TABLE sample_quant
SELECT 
	entry, 
	sample_index as sample, 
	ROUND(AVG(`top3_avg_inten`)) as inten
FROM 
	`finalquant`
GROUP BY 
	entry, `sample_index`
ORDER BY
	entry ASC, sample ASC
;

-- @ :	flipping sample abundances to a pivot table ...
DROP TABLE IF EXISTS sample_report;
CREATE TABLE sample_report
SELECT DISTINCT entry FROM sample_quant ORDER BY entry;
ALTER TABLE sample_report ADD PRIMARY KEY(entry);

DROP PROCEDURE IF EXISTS fillPivot;
CREATE PROCEDURE fillPivot()
BEGIN
	DECLARE maxSampleIndex INT; -- x 
	DECLARE sampleIndex INT; -- x 
	DECLARE sqlString VARCHAR(1024); -- x 
	DECLARE sampleColName VARCHAR(255); -- x 
	
	SET maxSampleIndex = 0; -- x 
	SET sampleIndex = 1; -- x 
	
	SELECT max(sample) INTO maxSampleIndex FROM sample_quant; -- x 
	
	WHILE (sampleIndex <= maxSampleIndex) DO 
		SELECT name INTO sampleColName FROM sample WHERE `index`=sampleIndex; -- x 
	
		SET @sqlString = CONCAT('ALTER TABLE sample_report ADD COLUMN `', sampleColName, '` DOUBLE DEFAULT 1.0 '); -- x
		PREPARE stmt FROM @sqlString; -- x 
		EXECUTE stmt; -- x 
		DEALLOCATE PREPARE stmt; -- x 
		
		SET @sqlString = CONCAT('UPDATE sample_report JOIN sample_quant USING(entry) SET `',sampleColName,'` = inten WHERE sample=',sampleIndex); -- x 
		PREPARE stmt FROM @sqlString; -- x 
		EXECUTE stmt; -- x 
		DEALLOCATE PREPARE stmt; -- x 

		SET sampleIndex = sampleIndex + 1; -- x 
	END WHILE; -- x 
END;

call fillPivot();
DROP PROCEDURE IF EXISTS fillPivot;
-- @ calculating samplewise protein quantification ... [done]