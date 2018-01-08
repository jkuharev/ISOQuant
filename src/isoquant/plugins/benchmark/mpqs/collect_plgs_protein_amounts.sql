DROP TABLE IF EXISTS ngs;
CREATE TEMPORARY TABLE ngs
SELECT 
	sample_index,
	workflow_index,
	sum(`aq_ngrams`) as sum_aq_ngrams
FROM 
	protein JOIN workflow ON protein.workflow_index=workflow.`index`
WHERE
	aq_ngrams > 0
GROUP BY 
	workflow_index
;
ALTER TABLE ngs ADD PRIMARY KEY(workflow_index);

-- ppm per run
DROP TABLE IF EXISTS protein_plgs_amounts;
CREATE TEMPORARY TABLE protein_plgs_amounts
SELECT
	entry,
	sample_index,
	workflow_index,
	ROUND( aq_fmoles, 4 ) as fmol,
	ROUND( aq_ngrams, 4 ) as ngram,
	ROUND( aq_ngrams / sum_aq_ngrams * 1000000, 4) as ppm
FROM
	protein as p JOIN ngs USING(workflow_index)
;
ALTER TABLE protein_plgs_amounts ADD INDEX(entry);
ALTER TABLE protein_plgs_amounts ADD INDEX(sample_index);
ALTER TABLE protein_plgs_amounts ADD INDEX(workflow_index);

-- ppm per sample
DROP TABLE IF EXISTS protein_plgs_sample_amounts;
CREATE TEMPORARY TABLE protein_plgs_sample_amounts
SELECT 
	entry,
	sample_index,
	ROUND(AVG(fmol),4) as fmol,
	ROUND(AVG(ngram),4) as ngram,
	ROUND(AVG(ppm),4) as ppm
FROM
	protein_plgs_amounts
GROUP BY
	entry, sample_index
;
ALTER TABLE protein_plgs_sample_amounts ADD INDEX(entry);
ALTER TABLE protein_plgs_sample_amounts ADD INDEX(sample_index);

-- concat ppm by workflow
--SELECT
--	entry,
--	GROUP_CONCAT(workflow_index, ':', ppm ORDER BY workflow_index ASC SEPARATOR ';') as ppm
--FROM
--	protein_plgs_amounts
--GROUP BY
--	entry
--ORDER BY 
--	entry ASC

-- concat ppm by sample
--SELECT
--	entry,
--	GROUP_CONCAT(sample_index, ':', ppm ORDER BY sample_index ASC SEPARATOR ';') as ppm
--FROM
--	protein_plgs_sample_amounts
--GROUP BY
--	entry
--ORDER BY 
--	entry ASC

