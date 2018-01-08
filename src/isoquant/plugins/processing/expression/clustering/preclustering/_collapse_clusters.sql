-- @ :		collapsing in-sample clusters to consensus peaks ...

-- backup preclustered_peaks
DROP TABLE IF EXISTS preclustered_peaks;
RENAME TABLE ce TO preclustered_peaks;
UPDATE preclustered_peaks SET pc=nc;
ALTER TABLE preclustered_peaks ADD INDEX(id);
ALTER TABLE preclustered_peaks ADD INDEX(nc);
ALTER TABLE preclustered_peaks ADD INDEX(pc);

-- merge clusters
CREATE TABLE ce
SELECT
	nc as id,
	1 as nc,
	1 as pc,
	AVG(mz) as mz,
	AVG(rt) as rt,
	AVG(dt) as dt
FROM
	preclustered_peaks
GROUP BY
	nc
;

-- @ :		clustering consensus peaks ...