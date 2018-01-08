-- @ calculating protein statistics ...

-- @ :	calculating overall protein statistics ...

-- @ :		counting all peptides ...

DROP TABLE IF EXISTS tmp_stat;
CREATE TEMPORARY TABLE tmp_stat
SELECT `entry`,
	count(DISTINCT sample_index) as samples,
	count(DISTINCT workflow_index) as workflows,
	count(DISTINCT CONCAT(sequence, modifier)) as peptides
FROM 
	`pep2pro` 
GROUP BY 
	entry
;
ALTER TABLE tmp_stat ADD PRIMARY KEY(entry);

-- `stat_count_samples`
-- `stat_count_workflows`
-- `stat_count_peptides`
UPDATE 
	protein as pro LEFT JOIN tmp_stat as stat USING(entry)
SET 
	pro.stat_count_samples=stat.samples,
	pro.stat_count_workflows=stat.workflows,
	pro.stat_count_peptides=stat.peptides
;
OPTIMIZE TABLE protein;



-- @ :		counting unique peptides ...

DROP TABLE IF EXISTS tmp_stat;
CREATE TEMPORARY TABLE tmp_stat
SELECT 
	`entry`, stat_count_proteins,
	count(DISTINCT CONCAT(pp.sequence, pp.modifier)) as peptides
FROM 
	`pep2pro` as pp INNER JOIN peptide as pep ON pp.pep_index=pep.`index`
GROUP BY 
	entry 
HAVING 
	pep.`stat_count_proteins`=1
;
ALTER TABLE tmp_stat ADD PRIMARY KEY(entry);

-- `stat_count_unique_peptides`
UPDATE 
	protein as pro LEFT JOIN tmp_stat as stat USING(entry)
SET 
	pro.stat_count_unique_peptides=stat.peptides
;
OPTIMIZE TABLE protein;
UPDATE protein SET stat_count_unique_peptides=0 WHERE stat_count_unique_peptides IS NULL;
OPTIMIZE TABLE protein;






-- @ :	calculating sample-wise protein statistics ...

-- @ :		counting all peptides ...

DROP TABLE IF EXISTS tmp_stat;
CREATE TEMPORARY TABLE tmp_stat
SELECT 
	entry, sample_index,
	count(DISTINCT CONCAT(sequence, modifier)) as peptides
FROM 
	`pep2pro` 
GROUP BY 
	entry, sample_index
;
ALTER TABLE tmp_stat ADD PRIMARY KEY(entry, sample_index);


-- `stat_count_peptides_per_sample`
UPDATE 
	protein as pro 
	LEFT JOIN workflow as w ON pro.workflow_index=w.`index`
	LEFT JOIN tmp_stat as stat USING (entry, sample_index)
--	ON stat.entry=pro.entry AND stat.sample_index=w.sample_index
SET 
	pro.stat_count_peptides_per_sample=stat.peptides
;
OPTIMIZE TABLE protein;

-- @ :		counting unique peptides ...

DROP TABLE IF EXISTS tmp_stat;
CREATE TEMPORARY TABLE tmp_stat
SELECT 
	`entry`, stat_count_proteins, sample_index,
	count(DISTINCT CONCAT(pp.sequence, pp.modifier)) as peptides
FROM 
	`pep2pro` as pp INNER JOIN peptide as pep ON pp.pep_index=pep.`index`
GROUP BY 
	entry, sample_index
HAVING 
	pep.`stat_count_proteins`=1
;
ALTER TABLE tmp_stat ADD PRIMARY KEY(entry, sample_index);

-- `stat_count_unique_peptides_per_sample`
UPDATE 
	protein as pro 
	LEFT JOIN workflow as w ON pro.workflow_index=w.`index`
	LEFT JOIN tmp_stat as stat USING (entry, sample_index)
SET 
	pro.stat_count_unique_peptides_per_sample=stat.peptides
;
OPTIMIZE TABLE protein;

UPDATE protein SET stat_count_unique_peptides_per_sample=0 WHERE stat_count_unique_peptides_per_sample IS NULL;
OPTIMIZE TABLE protein;

-- @ :	calculating workflow-wise protein statistics ...
-- @ :		counting all peptides ...

DROP TABLE IF EXISTS tmp_stat;
CREATE TEMPORARY TABLE tmp_stat
SELECT 
	entry, workflow_index,
	count(DISTINCT CONCAT(sequence, modifier)) as peptides
FROM 
	`pep2pro` 
GROUP BY 
	entry, workflow_index
;
ALTER TABLE tmp_stat ADD PRIMARY KEY(entry, workflow_index);

-- `stat_count_peptides_per_workflow`
UPDATE 
	protein as pro 
	LEFT JOIN tmp_stat as stat USING (entry, workflow_index)
SET 
	pro.stat_count_peptides_per_workflow=stat.peptides
;
OPTIMIZE TABLE protein;

-- @ :		counting unique peptides ...

DROP TABLE IF EXISTS tmp_stat;
CREATE TEMPORARY TABLE tmp_stat
SELECT 
	entry, stat_count_proteins, pp.workflow_index,
	count(DISTINCT CONCAT(pp.sequence, pp.modifier)) as peptides
FROM 
	`pep2pro` as pp INNER JOIN peptide as pep ON pp.pep_index=pep.`index`
GROUP BY 
	entry, pp.workflow_index
HAVING 
	pep.`stat_count_proteins`=1
;
ALTER TABLE tmp_stat ADD PRIMARY KEY(entry, workflow_index);

-- `stat_count_unique_peptides_per_workflow`
UPDATE 
	protein as pro 
	LEFT JOIN tmp_stat as stat USING (entry, workflow_index)
SET 
	pro.stat_count_unique_peptides_per_workflow=stat.peptides
;
OPTIMIZE TABLE protein;

UPDATE 
	protein 
SET 
	stat_count_unique_peptides_per_workflow=0 
WHERE 
	stat_count_unique_peptides_per_workflow IS NULL;

OPTIMIZE TABLE protein;

-- @ :	collecting maximum values for proteins ...
-- max values for proteins
DROP TABLE IF EXISTS protein_info;
CREATE TABLE protein_info
SELECT 
	entry, accession, description, mw, `pi`,
	max(score) as score,
	max(coverage) as coverage,
	max(stat_count_workflows) as stat_count_workflows,
	max(stat_count_peptides) as stat_count_peptides,
	max(stat_count_unique_peptides) as stat_count_unique_peptides
FROM `protein`
GROUP BY 
	entry
ORDER BY 
	score DESC
;
ALTER TABLE protein_info ADD PRIMARY KEY(entry);

-- @ calculating protein statistics ... [done]