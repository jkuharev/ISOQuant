-- -----------------------------------------------------
-- This file contains table definitions for
-- Input and Output operations of
-- the Retention Time Warping (Alignment)
-- on peak lists basis
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Table `rtw_in`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `rtw_in` ;
CREATE  TABLE IF NOT EXISTS `rtw_in` 
(
	`peak_id` INT,
	`peak_list_id` INT,
	`time` FLOAT,
	`type` TINYINT,
	`mass` DOUBLE,
	`drift` FLOAT,
	PRIMARY KEY (`peak_id`, `type`),
	INDEX (`peak_list_id`),
	INDEX (`time`)
);

-- -----------------------------------------------------
-- Table `rtw_out`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `rtw_out` ;
CREATE  TABLE IF NOT EXISTS `rtw_out` 
(
	`peak_list_id` INT,
	`old_time` FLOAT,
	`new_time` FLOAT,
	PRIMARY KEY (`peak_list_id`),
	INDEX (`old_time`)
);
