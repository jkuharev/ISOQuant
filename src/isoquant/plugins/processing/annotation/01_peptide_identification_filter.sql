-- @ annotating emrt clusters ...
-- execution order: 01 after normalization

-- @ :	filtering peptides by peptide filter criteria
DROP TABLE IF EXISTS peptides_passing_filter;
CREATE TABLE peptides_passing_filter
SELECT	`index`
FROM	peptide
WHERE
	`stat_count_workflows` >= %PEPTIDE_STAT_COUNT_WORKFLOWS_MIN_VALUE%
	AND `stat_max_score` >= %PEPTIDE_MIN_STAT_MAX_SCORE%
	AND `score` >= %PEPTIDE_MIN_SCORE%
	AND `type` IN (%PEPTIDE_TYPE_FILTER_ADDITIONAL_TYPES%)
	AND length(`sequence`) >= %PEPTIDE_SEQUENCE_LENGTH_MIN_VALUE%
;
ALTER TABLE peptides_passing_filter ADD INDEX (`index`);

-- @ :	mapping peaks to filtered peptides ...
DROP TABLE IF EXISTS emrts_annotated_by_filtered_peptides;
CREATE TABLE emrts_annotated_by_filtered_peptides
SELECT 
	ce.`index` as emrt_index,
	pep.`index` as peptide_index
FROM 
	peptide as pep
	JOIN peptides_passing_filter USING(`index`)
	JOIN `query_mass` as qm ON qm.`index`=pep.`query_mass_index`
	JOIN clustered_emrt as ce USING(low_energy_index)
;
ALTER TABLE emrts_annotated_by_filtered_peptides ADD INDEX (`emrt_index`);
ALTER TABLE emrts_annotated_by_filtered_peptides ADD INDEX (`peptide_index`);
