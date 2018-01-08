-- @ creating clustering benchmark metrics ...

set @jetzt=now();

CREATE TABLE IF NOT EXISTS clustering_benchmark
(
	`series` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	`name` VARCHAR(255),
	`line` DOUBLE DEFAULT 1,
	`value` DOUBLE DEFAULT 0
);


-- @ :	count all clusters ...
-- alle cluster zählen
INSERT INTO clustering_benchmark (`series`, `name`, `value`)
SELECT 
	@jetzt,
	'number of all clusters',
	COUNT(DISTINCT `cluster_average_index`) 
FROM `clustered_emrt` WHERE 1
;

-- @ :	count annotated clusters ...
-- annotierte Cluster zählen
INSERT INTO clustering_benchmark (`series`, `name`, `value`)
SELECT 
	@jetzt,
	'annotated clusters',
	count(distinct ce.`cluster_average_index`)
FROM 
	`clustered_emrt` as ce 
	JOIN `query_mass` as qm USING(`low_energy_index`) 
	JOIN `peptide` as p ON qm.`index`=p.`query_mass_index`
;

-- @ :	create cluster size histogram ...
-- Cluster-Größen Histogramm
INSERT INTO clustering_benchmark (`series`, `name`, `line`, `value`)
SELECT 
	@jetzt,
	'cluster sizes',
	cs,
	count(cluster_average_index) as csc
FROM
	(
		SELECT 
			`cluster_average_index`, count(distinct `index`) as cs
		FROM 
			`clustered_emrt`
		GROUP BY 
			`cluster_average_index`
	) as xx
GROUP BY cs
ORDER BY cs ASC;

-- @ :	count clusters having distinct annotation  ...
-- eindeutig annotierte Cluster zählen
INSERT INTO clustering_benchmark (`series`, `name`, `value`)
SELECT 
	@jetzt,
	'distinct annotated clusters',
	count(`cluster_average_index`)
FROM
	(
		SELECT
			ce.`cluster_average_index`, count(distinct p.sequence) as spc
		FROM 
			`clustered_emrt` as ce
			JOIN `query_mass` as qm USING(`low_energy_index`)
			JOIN `peptide` as p ON qm.`index`=p.`query_mass_index`
		GROUP BY 
			`cluster_average_index`
	) as xx
WHERE spc=1;

-- @ :	count clusters having ambiguous annotation  ...
-- mehrdeutig annotierte Cluster zählen
-- d.h. verschiedene Peptid-Identitäten pro Cluster
INSERT INTO clustering_benchmark (`series`, `name`, `value`)
SELECT 
	@jetzt,
	'ambiguous annotated clusters',
	count(`cluster_average_index`)
FROM
	(
		SELECT
			ce.`cluster_average_index`, count(distinct p.sequence) as spc
		FROM `clustered_emrt` as ce
		JOIN `query_mass` as qm USING(`low_energy_index`)
		JOIN `peptide` as p ON qm.`index`=p.`query_mass_index`
		GROUP BY `cluster_average_index`
	) as xx
WHERE spc>1;

-- @ :	create peptides per cluster histogram ...
-- Cluster-Annotations-Größen Histogramm
-- d.h. Peptide-Identitätäten pro Cluster
INSERT INTO clustering_benchmark (`series`, `name`, `line`, `value`)
SELECT 
	@jetzt,
	'peptides per cluster',
	spc,
	count(cluster_average_index)
FROM
	(
		SELECT
			ce.`cluster_average_index`, count(distinct p.sequence) as spc
		FROM `clustered_emrt` as ce
		JOIN `query_mass` as qm USING(`low_energy_index`)
		JOIN `peptide` as p ON qm.`index`=p.`query_mass_index`
		GROUP BY `cluster_average_index`
	) as xx
GROUP BY spc
ORDER BY spc ASC;

-- @ :	count peptides ...
-- unterschiedliche Peptide zählen
INSERT INTO clustering_benchmark (`series`, `name`, `value`)
SELECT 
	@jetzt,
	'number of peptide identities',
	count(DISTINCT CONCAT(sequence, type, modifier)) 
FROM 
	`peptide`;
	
-- @ :	count peptide sequences ...
INSERT INTO clustering_benchmark (`series`, `name`, `value`)
SELECT 
	@jetzt,
	'number of peptide sequences',
	count(DISTINCT sequence)
FROM 
	`peptide`;

-- @ :	create clusters per peptide histogram ...
-- Clusters-per-Peptide Histogramm
INSERT INTO clustering_benchmark (`series`, `name`, `line`, `value`)
SELECT 
	@jetzt,
	'clusters per peptide',
	cc,
	count(pepid) as cpid
FROM
(
	SELECT
		pepid, count(cluster_average_index) as cc
	FROM
	(
		SELECT ce.`cluster_average_index`, CONCAT(p.sequence, p.type, p.modifier) as pepid
		FROM `clustered_emrt` AS ce
		JOIN `query_mass` AS qm
		USING ( `low_energy_index` )
		JOIN `peptide` AS p ON qm.`index` = p.`query_mass_index`
		GROUP BY `cluster_average_index`, pepid
	) as xx
	GROUP BY pepid
) as xy
GROUP BY cc;

-- @ :	create clusters per peptide sequence histogram ...
-- Clusters-per-Peptide-Sequence Histogramm
INSERT INTO clustering_benchmark (`series`, `name`, `line`, `value`)
SELECT 
	@jetzt,
	'clusters per peptide sequence',
	cc,
	count(pepid) as cpid
FROM
(
	SELECT
		pepid, count(cluster_average_index) as cc
	FROM
	(
		SELECT ce.`cluster_average_index`, p.sequence as pepid
		FROM `clustered_emrt` AS ce
		JOIN `query_mass` AS qm
		USING ( `low_energy_index` )
		JOIN `peptide` AS p ON qm.`index` = p.`query_mass_index`
		GROUP BY `cluster_average_index`, pepid
	) as xx
	GROUP BY pepid
) as xy
GROUP BY cc;

-- @ :	count peptides having unambiguous cluster assignment ...
-- eindeutig zugeordnete Peptide zählen
INSERT INTO clustering_benchmark (`series`, `name`, `value`)
SELECT 
	@jetzt,
	'number of peptides used for distinct annotation',
    count(pepid)
FROM
(
    SELECT
            pepid, count(cluster_average_index) as cc
    FROM
    (
        SELECT ce.`cluster_average_index`, CONCAT(p.sequence, p.type, p.modifier) as pepid
        FROM `clustered_emrt` AS ce
        JOIN `query_mass` AS qm
        USING ( `low_energy_index` )
        JOIN `peptide` AS p ON qm.`index` = p.`query_mass_index`
        GROUP BY `cluster_average_index`, pepid
    ) as xx
    GROUP BY pepid
) as xy
WHERE cc=1;

-- @ :	count peptides having ambiguous cluster assignment ...
-- auseinandergerissene Peptide zählen
INSERT INTO clustering_benchmark (`series`, `name`, `value`)
SELECT 
	@jetzt,
	'number of peptides for ambiguous annotation',
    count(pepid)
FROM
(
    SELECT
            pepid, count(cluster_average_index) as cc
    FROM
    (
        SELECT ce.`cluster_average_index`, CONCAT(p.sequence, p.type, p.modifier) as pepid
        FROM `clustered_emrt` AS ce
        JOIN `query_mass` AS qm
        USING ( `low_energy_index` )
        JOIN `peptide` AS p ON qm.`index` = p.`query_mass_index`
        GROUP BY `cluster_average_index`, pepid
    ) as xx
    GROUP BY pepid
) as xy
WHERE cc>1;