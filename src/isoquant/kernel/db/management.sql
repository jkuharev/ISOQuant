CREATE TABLE IF NOT EXISTS `project`
(
	`index` INTEGER NOT NULL AUTO_INCREMENT,
	`time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`id` VARCHAR(255),
	`title` VARCHAR(255),
	`root` VARCHAR(255),
	`db` VARCHAR(255),
	`state` VARCHAR(50),
	PRIMARY KEY(`index`)
)
ENGINE=MyISAM DEFAULT CHARACTER SET=utf8;

-- Copyright (c) 2009 www.cryer.co.uk
-- Script is free to use provided this copyright header is included.
DROP PROCEDURE IF EXISTS AddColumnUnlessExists;

CREATE PROCEDURE AddColumnUnlessExists(
	IN dbName tinytext, 
	IN tableName tinytext, 
	IN fieldName tinytext, 
	IN fieldDef text)
BEGIN
	IF NOT EXISTS (
		SELECT * FROM information_schema.COLUMNS
		WHERE column_name=fieldName and table_name=tableName and table_schema=dbName
	)
	THEN
		SET @ddl=CONCAT('ALTER TABLE ',dbName,'.',tableName,' ADD COLUMN ',fieldName,' ',fieldDef); -- x
		prepare stmt from @ddl; -- x
		execute stmt; -- x
	END IF; -- x
END;

-- ADD COLUMN note!!!
CALL AddColumnUnlessExists('mass_projects', 'project', 'note', 'VARCHAR(255) DEFAULT ""');
