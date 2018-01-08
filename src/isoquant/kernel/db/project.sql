DROP TABLE IF EXISTS `project`;
DROP TABLE IF EXISTS `expression_analysis`;
DROP TABLE IF EXISTS `cluster_average`;
DROP TABLE IF EXISTS `group`;
DROP TABLE IF EXISTS `sample`;
DROP TABLE IF EXISTS `workflow`;
DROP TABLE IF EXISTS `low_energy`;
DROP TABLE IF EXISTS `query_mass`;
DROP TABLE IF EXISTS `protein`;
DROP TABLE IF EXISTS `peptide`;
DROP TABLE IF EXISTS `clustered_emrt`;
DROP TABLE IF EXISTS `mass_spectrum`;
DROP TABLE IF EXISTS `history`;

CREATE TABLE IF NOT EXISTS `project`(
	`index` 			INTEGER NOT NULL AUTO_INCREMENT,
	`id` 				VARCHAR(255),
	`title` 			VARCHAR(2048),
	`root` 				VARCHAR(2048),
	`db` 				VARCHAR(255),
	PRIMARY KEY(`index`),
	INDEX(`id`)
) ENGINE=MyISAM DEFAULT CHARACTER SET=latin1;


CREATE TABLE IF NOT EXISTS `expression_analysis`(
	`index` 			INTEGER NOT NULL AUTO_INCREMENT,
	`project_index` 		INTEGER NOT NULL,
	`id` 				VARCHAR(255),
	`name` 				VARCHAR(2048),
	`description` 			VARCHAR(2048),
	PRIMARY KEY(`index`),
	INDEX(`project_index`),
	INDEX(`id`)
) ENGINE=MyISAM DEFAULT CHARACTER SET=latin1;

CREATE TABLE IF NOT EXISTS `cluster_average`(
	`index` 			INTEGER NOT NULL AUTO_INCREMENT,
	`expression_analysis_index` 	INTEGER,
	`cluster_id` 			INTEGER,
	`ave_mhp` 			DOUBLE,
	`std_mhp` 			DOUBLE,
	`ave_rt` 			DOUBLE,
	`std_rt` 			DOUBLE,
	`ave_inten` 			DOUBLE,
	`std_inten` 			DOUBLE,
	`ave_ref_rt` 			DOUBLE,
	`std_ref_rt` 			DOUBLE,
	`ave_charge` 			DOUBLE,
	`std_charge` 			DOUBLE,
	`total_rep_rate` 		TINYINT(4),
	PRIMARY KEY(`index`),
	INDEX(`expression_analysis_index`),
	INDEX(`cluster_id`),
	INDEX(`ave_inten`)
) ENGINE=MyISAM DEFAULT CHARACTER SET=latin1;

CREATE TABLE IF NOT EXISTS `group`(
	`index` 			INTEGER NOT NULL AUTO_INCREMENT,
	`expression_analysis_index` 	INTEGER,
	`id`				VARCHAR(255),
	`name` 				VARCHAR(2048),
	PRIMARY KEY(`index`),
	INDEX(`expression_analysis_index`),
	INDEX(`id`)
) ENGINE=MyISAM DEFAULT CHARACTER SET=latin1;

CREATE TABLE IF NOT EXISTS `sample`(
	`index` 			INTEGER NOT NULL AUTO_INCREMENT,
	`group_index` 			INTEGER,
	`id` 				VARCHAR(255),
	`name` 				VARCHAR(2048),
	PRIMARY KEY(`index`),
	INDEX(`group_index`),
	INDEX(`id`)
) ENGINE=MyISAM DEFAULT CHARACTER SET=latin1;

CREATE TABLE IF NOT EXISTS `workflow`(
	`index` 			INTEGER NOT NULL AUTO_INCREMENT,
	`sample_index` 			INTEGER,
	`id` 				VARCHAR(255),
	`title` 			VARCHAR(2048),
	`sample_tracking_id` 		VARCHAR(255),
	`sample_description` 		VARCHAR(2048),
	`input_file` 			VARCHAR(2048),
	`acquired_name` 		VARCHAR(2048),
	`abs_quan_response_factor` 	DOUBLE,
	`replicate_name` 		VARCHAR(2048),
	PRIMARY KEY(`index`),
	INDEX(`sample_index`),
	INDEX(`id`)
) ENGINE=MyISAM DEFAULT CHARACTER SET=latin1;

CREATE TABLE IF NOT EXISTS `workflow_metadata`(
	`workflow_index`	INTEGER NOT NULL,
	`type`				VARCHAR(255),
	`name`				VARCHAR(255),
	`value`				VARCHAR(2048),
	PRIMARY KEY( `workflow_index`, `type`, `name`)
) ENGINE=MyISAM DEFAULT CHARACTER SET=latin1;

CREATE TABLE IF NOT EXISTS `low_energy`(
	`index` 			INTEGER NOT NULL AUTO_INCREMENT,
	`workflow_index` 		INTEGER,
	`id` 				INTEGER,
	`charge` 			DOUBLE,
	`mass` 				DOUBLE,
	`retention_time` 		DOUBLE,
	`retention_time_rounded` 	DOUBLE,
	PRIMARY KEY(`index`),
	INDEX(`workflow_index`),
	INDEX(`id`),
	INDEX(`mass`),
	INDEX(`retention_time`),
	INDEX(`retention_time_rounded`)
) ENGINE=MyISAM DEFAULT CHARACTER SET=latin1;

CREATE TABLE IF NOT EXISTS `query_mass`(
	`index` 			INTEGER NOT NULL AUTO_INCREMENT,
	`workflow_index` 		INTEGER,
	`low_energy_id` 		INTEGER NOT NULL,  
	`id` 				INTEGER,
	`intensity` 			DOUBLE,
	`low_energy_index` 		INTEGER NULL,
	PRIMARY KEY(`index`),
	INDEX(`workflow_index`),
	INDEX(`low_energy_index`),
	INDEX(`id`),
	INDEX(`low_energy_id`),
	INDEX(`intensity`)
) ENGINE=MyISAM DEFAULT CHARACTER SET=latin1;

CREATE TABLE IF NOT EXISTS `protein`(
	`index` 			INTEGER NOT NULL AUTO_INCREMENT,
	`workflow_index` 		INTEGER,
	`id` 				INTEGER NOT NULL,
	`auto_qc` 			INTEGER,
	`curated` 			INTEGER,
	`coverage` 			DOUBLE,
	`score` 			DOUBLE,
	`rms_mass_error_prec` 		DOUBLE,
	`rms_mass_error_frag` 		DOUBLE,
	`rms_rt_error_frag` 		INTEGER,
	`entry` 			VARCHAR(100),
	`accession` 			VARCHAR(100),
	`description` 			VARCHAR(512),
	`mw` 				DOUBLE,
	`aq_fmoles` 			DOUBLE,
	`aq_ngrams` 			DOUBLE,
	`pi` 				DOUBLE,
	`sequence` 			TEXT,
	`peptides` 			INTEGER,
	`products` 			INTEGER,
-- for statistics, added on 2010-05-11
	`stat_count_samples` 			SMALLINT DEFAULT 0,
	`stat_count_workflows` 			SMALLINT DEFAULT 0,
	`stat_count_peptides`	 		SMALLINT DEFAULT 0,
	`stat_count_unique_peptides`	 		SMALLINT DEFAULT 0,	
	`stat_count_peptides_per_sample` 		SMALLINT DEFAULT 0,
	`stat_count_unique_peptides_per_sample` 	SMALLINT DEFAULT 0,
	`stat_count_peptides_per_workflow` 		SMALLINT DEFAULT 0,	
	`stat_count_unique_peptides_per_workflow` 	SMALLINT DEFAULT 0,
-- end statistics
	PRIMARY KEY(`index`),
	INDEX(`workflow_index`),
	INDEX(`id`),
	INDEX(`score`),
	INDEX(`entry`(50)),
	INDEX(`accession`(50)),
	INDEX(`stat_count_samples`),
	INDEX(`stat_count_workflows`),
	INDEX(`stat_count_peptides`),
	INDEX(`stat_count_unique_peptides`),
	INDEX(`stat_count_peptides_per_sample`),
	INDEX(`stat_count_unique_peptides_per_sample`),
	INDEX(`stat_count_peptides_per_workflow`),
	INDEX(`stat_count_unique_peptides_per_workflow`)
) ENGINE=MyISAM DEFAULT CHARACTER SET=latin1;

CREATE TABLE IF NOT EXISTS `peptide`(
	`index` 			INTEGER NOT NULL AUTO_INCREMENT,
	`workflow_index` 		INTEGER,
	`protein_index` 		INTEGER,
	`query_mass_index` 		INTEGER,
	`protein_id` 			INTEGER NOT NULL,
	`id` 				INTEGER NOT NULL,
	`mass` 				DOUBLE,
	`sequence` 			VARCHAR(255),
	`type` 				VARCHAR(50),
	`modifier` 			VARCHAR(255),
	`start` 			INTEGER,
	`end` 				INTEGER,
	`coverage` 			DOUBLE,
-- 2011-05-17: frag string with length of more than 256 found, increasing size to 512
	`frag_string` 			VARCHAR(512),
	`rms_mass_error_prod` 		DOUBLE,
	`rms_rt_error_prod` 		DOUBLE,
	`auto_qc` 			INTEGER,
	`curated` 			INTEGER,
	`mass_error` 			DOUBLE,
	`mass_error_ppm` 		DOUBLE,
	`score` 			DOUBLE,
	`products` 			SMALLINT,
-- for statistics, added on 2010-05-11
	`stat_count_samples` 		SMALLINT DEFAULT 0,
	`stat_count_workflows` 		SMALLINT DEFAULT 0,
	`stat_count_proteins`	 	SMALLINT DEFAULT 0,
	`stat_max_score`	 		DOUBLE DEFAULT 0.0,
	`stat_count_workflows_per_sample` 	SMALLINT DEFAULT 0,
	`stat_count_proteins_per_sample` 	SMALLINT DEFAULT 0,
	`stat_max_score_per_sample` 	DOUBLE DEFAULT 0.0,
-- end statistics
	PRIMARY KEY(`index`),
	INDEX(`workflow_index`),
	INDEX(`protein_index`),
	INDEX(`query_mass_index`),
	INDEX(`protein_id`),
	INDEX(`id`),
	INDEX(`mass`),
	INDEX(`sequence`(100)),
	INDEX(`type`(50)),
	INDEX(`modifier`(50)),
	INDEX(`stat_count_samples`),
	INDEX(`stat_count_workflows`),
	INDEX(`stat_count_proteins`),
	INDEX(`stat_max_score`),
	INDEX(`stat_count_workflows_per_sample`),
	INDEX(`stat_count_proteins_per_sample`),
	INDEX(`stat_max_score_per_sample`)
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
-- for general intensity corrections
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

CREATE TABLE IF NOT EXISTS `mass_spectrum`(
	`index` 		INTEGER NOT NULL AUTO_INCREMENT,
	`workflow_index` 	INTEGER,
	`Mass` 			DOUBLE,
	`Intensity` 		DOUBLE,
	`Mobility`		DOUBLE,
	`MassSD` 		DOUBLE,
	`IntensitySD` 		DOUBLE,
	`le_id` 		INTEGER,
	`AverageCharge` 	DOUBLE,
	`Z` 			TINYINT,
	`RT` 			DOUBLE,
	`RTSD` 			DOUBLE,
	`FWHM` 			DOUBLE,
	`ClusterLiftOffRT` 	DOUBLE,
	`LiftOffRT` 		DOUBLE,
	`InfUpRT` 		DOUBLE,
	`InfDownRT` 		DOUBLE,
	`TouchDownRT` 		DOUBLE,
	`ClusterTouchDownRT` 	DOUBLE,
	`low_energy_index` 	INTEGER,
	`fraction`		INTEGER DEFAULT 0,
	PRIMARY KEY(`index`),
	INDEX(`workflow_index`),
	INDEX(`le_id`),
	INDEX(`Mass`),
	INDEX(`Intensity`),
	INDEX(`RT`),
	INDEX(`low_energy_index`),
	INDEX(`fraction`)
)
ENGINE=MyISAM DEFAULT CHARACTER SET=latin1;

CREATE TABLE  IF NOT EXISTS `history` (
	`id` INT NOT NULL AUTO_INCREMENT,
	`time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`type` ENUM('event', 'parameter', 'message') DEFAULT 'message',
	`value` TEXT NOT NULL,
	`note` TEXT NOT NULL,
	PRIMARY KEY (`id`)
)
ENGINE=MyISAM DEFAULT CHARACTER SET=latin1;
-- end of SQL