-- @ initializing configuration storage ...
DROP TABLE IF EXISTS config_storage;
CREATE TABLE config_storage
(
	k	VARCHAR(512) NOT NULL,
	v	VARCHAR(1024),
	PRIMARY KEY(k)
);
-- @ initializing configuration storage ... [done]