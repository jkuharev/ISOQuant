-- @ creating xkeys table ...
DROP TABLE IF EXISTS xkey;
CREATE TABLE xkey
(
	id			VARCHAR(10),
	accession	VARCHAR(16),
	entry		VARCHAR(16),
	description	VARCHAR(256),
	PRIMARY KEY(id),
	INDEX(accession),
	INDEX(entry)
);