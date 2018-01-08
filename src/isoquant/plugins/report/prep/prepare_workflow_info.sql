-- @ :	collecting workflow statistics ...

-- @ :		refreshing indexes ...
OPTIMIZE TABLE workflow;
OPTIMIZE TABLE protein;
OPTIMIZE TABLE peptide;

-- @ :		summing acquired nanograms ...
DROP TABLE IF EXISTS tmp_sng;
CREATE TABLE tmp_sng
SELECT `workflow_index`, ROUND(sum(`aq_ngrams`), 2) as sum_acquired_ngrams 
FROM `protein` WHERE aq_ngrams > 0 GROUP BY `workflow_index`;
ALTER TABLE tmp_sng ADD PRIMARY KEY(workflow_index);

-- @ :		counting identified proteins ...
DROP TABLE IF EXISTS tmp_id_prot;
CREATE TABLE tmp_id_prot
SELECT workflow_index, COUNT(DISTINCT entry) as identified_proteins 
FROM `protein` GROUP BY `workflow_index`;
ALTER TABLE tmp_id_prot ADD PRIMARY KEY(workflow_index);

-- @ :		counting quantified proteins ...
DROP TABLE IF EXISTS tmp_qt_prot;
CREATE TABLE tmp_qt_prot
SELECT workflow_index, COUNT(DISTINCT entry) as quantified_proteins 
FROM `protein` WHERE `aq_fmoles`>0 GROUP BY workflow_index; 
ALTER TABLE tmp_qt_prot ADD PRIMARY KEY(workflow_index);

-- @ :		counting all identified peptides ...
DROP TABLE IF EXISTS tmp_id_pep_all;
CREATE TABLE tmp_id_pep_all
SELECT workflow_index, COUNT(DISTINCT sequence, modifier) as cnt 
FROM `peptide` GROUP BY `workflow_index`; 
ALTER TABLE tmp_id_pep_all ADD PRIMARY KEY(workflow_index);

-- @ :		counting identified peptides by type ...
DROP TABLE IF EXISTS tmp_id_pep_by_type;
CREATE TABLE tmp_id_pep_by_type
SELECT 
	workflow_index, 
	COUNT(DISTINCT sequence, modifier) as cnt,
	`type`
FROM `peptide` GROUP BY `workflow_index`, `type`;
ALTER TABLE tmp_id_pep_by_type ADD PRIMARY KEY(workflow_index, `type`);

-- @ :		combining workflow statistics ...
DROP TABLE IF EXISTS workflow_report;
CREATE TABLE workflow_report
SELECT
	`index` as workflow_index, `replicate_name`,
	`title`, `sample_description`,
	`input_file`, `acquired_name`,
	ROUND(`abs_quan_response_factor`) as abs_quan_response_factor,
	sum_acquired_ngrams,
	identified_proteins,
	quantified_proteins,
	tmp_id_pep_all.cnt as `all identified peptides`,
	id_pep_frag_1.cnt as `PEP_FRAG_1 peptides`,
	id_pep_frag_2.cnt as `PEP_FRAG_2 peptides`
FROM 
	`workflow` 
	LEFT JOIN tmp_id_prot ON workflow.index=`workflow_index`
	LEFT JOIN tmp_sng USING(`workflow_index`)
	LEFT JOIN tmp_qt_prot USING(`workflow_index`)
	LEFT JOIN tmp_id_pep_all USING(`workflow_index`)
	LEFT JOIN tmp_id_pep_by_type as id_pep_frag_1 USING(`workflow_index`)
	LEFT JOIN tmp_id_pep_by_type as id_pep_frag_2 USING(`workflow_index`)
WHERE
	id_pep_frag_1.`type` = "PEP_FRAG_1"
AND
	id_pep_frag_2.`type` = "PEP_FRAG_2"
ORDER BY 
	`workflow_index`;
ALTER TABLE workflow_report ADD PRIMARY KEY(workflow_index);

-- @ :		cleaning temporary data ...
DROP TABLE IF EXISTS tmp_sng;
DROP TABLE IF EXISTS tmp_id_prot;
DROP TABLE IF EXISTS tmp_qt_prot;
DROP TABLE IF EXISTS tmp_id_pep_all;

-- @ :	collecting workflow statistics [done]