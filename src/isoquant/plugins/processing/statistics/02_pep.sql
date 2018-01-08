-- @ calculating peptide statistics ...

-- @ :	calculating overall peptide statistics ...
-- total statistics (per project):

-- @ :		counting proteins ...

-- `stat_count_proteins`
UPDATE
	`peptide` as pep 
	JOIN (
		SELECT
			pepseq, count(entry) as prots
		FROM 
			pep2proBySeq
		GROUP BY 
			pepseq
	) as pip 
	ON pep.sequence=pip.pepseq
SET
	pep.stat_count_proteins=pip.prots
;
OPTIMIZE TABLE peptide;

-- @ :		cumulating replication rates ...

-- `stat_count_samples`
-- `stat_count_workflows`
-- `stat_max_score`
DROP TABLE IF EXISTS ps;
CREATE TEMPORARY TABLE ps
SELECT 
	`sequence`, `modifier`,
	count(DISTINCT sample_index) as in_samples,
	count(DISTINCT workflow_index) as in_workflows,
	max(pep_score) as max_score
FROM 
	pep2pro
GROUP BY 
	`sequence`, `modifier`
;
ALTER TABLE ps ADD INDEX(sequence);
ALTER TABLE ps ADD INDEX(modifier);
OPTIMIZE TABLE ps;

-- @ :		storing replication rates ...

UPDATE 
	peptide as pep INNER JOIN 
	(SELECT 
		pp.pep_index, ps.*
	FROM
		pep2pro as pp LEFT JOIN ps 
		ON pp.sequence=ps.sequence AND pp.modifier=ps.modifier
	) as stat
	ON pep.`index`=stat.pep_index
SET
	pep.stat_count_samples=stat.in_samples,
	pep.stat_count_workflows=stat.in_workflows,
	pep.stat_max_score=stat.max_score
;
OPTIMIZE TABLE peptide;

-- @ :	calculating per sample peptide statistics ...

-- @ :		cumulating replication rates ...

-- `stat_count_workflows_per_sample`
-- `stat_count_proteins_per_sample`
-- `stat_max_score_per_sample`
DROP TABLE IF EXISTS ps;
CREATE TEMPORARY TABLE ps
SELECT 
	`sample_index`,`sequence`, `modifier`,
	count(DISTINCT workflow_index) as in_workflows,
	count(DISTINCT entry) as in_proteins,
	max(pep_score) as max_score
FROM 
	pep2pro 
GROUP BY 
	`sequence`, `modifier`, `sample_index`
ORDER BY 
	in_proteins DESC
;
ALTER TABLE ps ADD INDEX(sample_index);
ALTER TABLE ps ADD INDEX(sequence);
ALTER TABLE ps ADD INDEX(modifier);
OPTIMIZE TABLE ps;

-- @ :		storing replication rates ...

UPDATE 
	peptide as pep INNER JOIN 
	(SELECT 
		pp.pep_index, ps.*
	FROM
		pep2pro as pp LEFT JOIN ps ON 
			pp.sample_index=ps.sample_index AND 
			pp.sequence=ps.sequence AND 
			pp.modifier=ps.modifier
	) as stat
	ON pep.`index`=stat.pep_index
SET
	pep.stat_count_workflows_per_sample=stat.in_workflows,
	pep.stat_count_proteins_per_sample=stat.in_proteins,
	pep.stat_max_score_per_sample=stat.max_score
;
OPTIMIZE TABLE peptide;
	
-- @ calculating peptide statistics ... [done]