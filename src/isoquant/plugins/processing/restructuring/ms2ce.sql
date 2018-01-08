-- @ creating clustered emrt data from mass spectrum signals ...

SET @zeroDrift = '%ZERO_DRIFT_TIME%';
SET @gasMass = '%MASS_OF_DRIFT_GAS%';
SET @minIntensity = %MIN_INTENSITY%;
SET @minMass = %MIN_MASS%;

CREATE TABLE IF NOT EXISTS `cluster_average`(
	`index` 					INTEGER NOT NULL AUTO_INCREMENT,
	`expression_analysis_index` 			INTEGER,
	`cluster_id` 					INTEGER,
	`ave_mhp` 					DOUBLE,
	`std_mhp` 					DOUBLE,
	`ave_rt` 					DOUBLE,
	`std_rt` 					DOUBLE,
	`ave_inten` 					DOUBLE,
	`std_inten` 					DOUBLE,
	`ave_ref_rt` 					DOUBLE,
	`std_ref_rt` 					DOUBLE,
	`ave_charge` 					DOUBLE,
	`std_charge` 					DOUBLE,
	`total_rep_rate` 				TINYINT(4),
	PRIMARY KEY(`index`),
	INDEX(`expression_analysis_index`),
	INDEX(`cluster_id`),
	INDEX(`ave_inten`)
) ENGINE=MyISAM DEFAULT CHARACTER SET=latin1;

CREATE TABLE IF NOT EXISTS `clustered_emrt`(
	`index` 			INTEGER NOT NULL AUTO_INCREMENT,
	`workflow_index` 		INTEGER,
	`expression_analysis_index` 	INTEGER,
	`cluster_average_index` 	INTEGER,
	`cluster_id` 			INTEGER,
	`mass` 				DOUBLE,
	`sd_mhp` 			DOUBLE,
	`inten` 			DOUBLE,
	`spec_index` 			INTEGER,
	`charge` 			DOUBLE,
	`rt` 				DOUBLE,
	`sd_rt` 			DOUBLE,
	`ref_rt` 			DOUBLE,
	`precursor_type` 		TINYINT(4),
	`low_energy_index` 		INTEGER,
	`cor_inten`			DOUBLE,
	`Mobility`			DOUBLE DEFAULT 00.0000,
	PRIMARY KEY(`index`),
	INDEX(`expression_analysis_index`),
	INDEX(`workflow_index`),
	INDEX(`cluster_average_index`),
	INDEX(`low_energy_index`),	
	INDEX(`cluster_id`),
	INDEX(`mass`),
	INDEX(`inten`),
	INDEX(`rt`),
	INDEX(`Mobility`)
) 
ENGINE=MyISAM DEFAULT CHARACTER SET=latin1;

TRUNCATE cluster_average;
TRUNCATE clustered_emrt;

INSERT INTO 
	clustered_emrt
SELECT 
	`index`,
	`workflow_index`,
	1 as `expression_analysis_index`,
	`index` as `cluster_average_index`,
	`index` as `cluster_id`,
	`Mass` as `mass`,
	`MassSD` as `sd_mhp`,
	`Intensity` as `inten`,
	`index` as `spec_index`,
	`Z` as `charge`,
	ROUND(`RT`, 2) as `rt`,
	`RTSD` as `sd_rt`,
	`RT` as `ref_rt`,
	0 as `precursor_type`,
	`low_energy_index`,
	`Intensity` as `cor_inten`,
--	(`Mobility`-@zeroDrift)*Z*SQRT(Mass*@gasMass/(Mass+@gasMass))/10 as mobility
	Mobility
FROM 
	`mass_spectrum`
WHERE
	Intensity >= @minIntensity 
	AND 
	Mass >= @minMass
;

OPTIMIZE TABLE clustered_emrt;
-- @ creating clustered emrt data from mass spectrum signals ... [done]