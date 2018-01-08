SET SESSION group_concat_max_len = 1000000;

-- @ collecting information for protein quantification report:

OPTIMIZE TABLE emrt4quant; 

-- @ :	collecting proteins for detailed peptide quantification ...
-- collect and count entries for each quantified peptide
DROP TABLE IF EXISTS report_entry_concat;
CREATE TEMPORARY TABLE report_entry_concat
	SELECT 
		`sequence`, `modifier`, `type`,
		GROUP_CONCAT(DISTINCT entry SEPARATOR ';') AS entries,
		count(DISTINCT entry) as n
	FROM 
		`emrt4quant`
	GROUP BY 
		`sequence`, `modifier`, `type`
;
ALTER TABLE report_entry_concat ADD INDEX(`sequence`);
ALTER TABLE report_entry_concat ADD INDEX(`modifier`);
ALTER TABLE report_entry_concat ADD INDEX(`type`);

-- collect original protein count for each peptide
DROP TABLE IF EXISTS report_ppu;
CREATE TEMPORARY TABLE report_ppu
SELECT 
	DISTINCT `sequence`, `src_proteins` as n 
FROM 
	`peptides_in_proteins_before_homology_filtering`
;
ALTER TABLE report_ppu ADD INDEX(`sequence`);

-- @ :	grouping protein/peptide combinations by cluster ...
-- list peptides per protein and cluster
DROP TABLE IF EXISTS report_pep_prot_cluster;
CREATE TEMPORARY TABLE report_pep_prot_cluster
SELECT
	entry, `sequence`, `modifier`, `type`, `cluster_average_index`, 
	ROUND(AVG(`ref_rt`), 2) as ave_ref_rt, 
	ROUND(AVG(`mass`),4) as ave_mass, 
	entries, 
	report_entry_concat.n as razor_count, 
	report_ppu.n as abs_count
FROM 
	emrt4quant as e4q 
	JOIN report_entry_concat USING(`sequence`, `modifier`, `type`)
	LEFT JOIN report_ppu USING(sequence)
GROUP BY
	entry, `sequence`, `modifier`, `type`, cluster_average_index
;
ALTER TABLE report_pep_prot_cluster ADD INDEX(`sequence`);
ALTER TABLE report_pep_prot_cluster ADD INDEX(`modifier`);
ALTER TABLE report_pep_prot_cluster ADD INDEX(`type`);
ALTER TABLE report_pep_prot_cluster ADD INDEX(`cluster_average_index`);
ALTER TABLE report_pep_prot_cluster ADD INDEX(`entry`);

-- @ :	summing intensities for protein/peptide/cluster combinations by runs ...
-- sum intensities for each peptide+protein+cluster+run permutation
DROP TABLE IF EXISTS report_pep_in_runs;
CREATE TEMPORARY TABLE report_pep_in_runs
SELECT
	`entry`, `sequence`, `modifier`, `type`, `cluster_average_index`,
	`sample_index`, `workflow_index`, 
	FLOOR(SUM(`cor_inten`)) as src_inten, 
	FLOOR(SUM(`dist_inten`)) as dist_inten
FROM 
	emrt4quant as e4q JOIN report_pep_prot_cluster 
	USING(`entry`, `sequence`, `modifier`, `type`, `cluster_average_index`)
GROUP BY 
	`entry`, `sequence`, `modifier`, `type`, 
	`cluster_average_index`, `workflow_index`
;
ALTER TABLE report_pep_in_runs ADD INDEX(`sequence`);
ALTER TABLE report_pep_in_runs ADD INDEX(`modifier`);
ALTER TABLE report_pep_in_runs ADD INDEX(`type`);
ALTER TABLE report_pep_in_runs ADD INDEX(`cluster_average_index`);
ALTER TABLE report_pep_in_runs ADD INDEX(`entry`);
ALTER TABLE report_pep_in_runs ADD INDEX(`sample_index`);

-- @ :	summing intensities for protein/peptide/cluster combinations by samples ...
DROP TABLE IF EXISTS report_pep_in_samples;
CREATE TEMPORARY TABLE report_pep_in_samples
SELECT 
	`sequence`, `modifier`, `type`, `entry`, `cluster_average_index`, `sample_index`, 
	FLOOR(AVG(`src_inten`)) as ave_src_inten,
	FLOOR(AVG(`dist_inten`)) as ave_dist_inten
FROM 
	`report_pep_in_runs`
GROUP BY 
	`sequence`, `modifier`, `type`, `entry`, `cluster_average_index`, `sample_index`
;
ALTER TABLE report_pep_in_samples ADD INDEX(`sequence`);
ALTER TABLE report_pep_in_samples ADD INDEX(`modifier`);
ALTER TABLE report_pep_in_samples ADD INDEX(`type`);
ALTER TABLE report_pep_in_samples ADD INDEX(`cluster_average_index`);
ALTER TABLE report_pep_in_samples ADD INDEX(`entry`);
ALTER TABLE report_pep_in_samples ADD INDEX(`sample_index`);

-- @ :	building sample based intensity pivots ...
-- inten per sample 
DROP TABLE IF EXISTS report_sample_concat;
CREATE TEMPORARY TABLE report_sample_concat
SELECT 
	`sequence`, `modifier`, `type`, `entry`, `cluster_average_index`,
	GROUP_CONCAT(`sample_index`, ':', `ave_src_inten` SEPARATOR ';') as sample_src_inten,
	GROUP_CONCAT(`sample_index`, ':', `ave_dist_inten` SEPARATOR ';') as sample_dist_inten
FROM 
	`report_pep_in_samples`
GROUP BY
	`sequence`, `modifier`, `type`, `entry`, `cluster_average_index`
;
ALTER TABLE report_sample_concat ADD INDEX(`sequence`);
ALTER TABLE report_sample_concat ADD INDEX(`modifier`);
ALTER TABLE report_sample_concat ADD INDEX(`type`);
ALTER TABLE report_sample_concat ADD INDEX(`entry`);
ALTER TABLE report_sample_concat ADD INDEX(`cluster_average_index`);

-- @ :	building run based intensity pivots ...
-- inten per run
DROP TABLE IF EXISTS report_run_concat;
CREATE TEMPORARY TABLE report_run_concat
SELECT 
	`sequence`, `modifier`, `type`, `entry`, `cluster_average_index`,
	GROUP_CONCAT(`workflow_index`, ':', `src_inten` SEPARATOR ';') as run_src_inten,
	GROUP_CONCAT(`workflow_index`, ':', `dist_inten` SEPARATOR ';') as run_dist_inten
FROM 
	`report_pep_in_runs`
GROUP BY
	`sequence`, `modifier`, `type`, `entry`, `cluster_average_index`
ORDER BY entry;
ALTER TABLE report_run_concat ADD INDEX(`sequence`);
ALTER TABLE report_run_concat ADD INDEX(`modifier`);
ALTER TABLE report_run_concat ADD INDEX(`type`);
ALTER TABLE report_run_concat ADD INDEX(`entry`);
ALTER TABLE report_run_concat ADD INDEX(`cluster_average_index`);

-- @ :	building merging protein report tables ...
-- collect results
DROP TABLE IF EXISTS report_peptide_quantification;
CREATE TABLE report_peptide_quantification
SELECT 
	*
FROM 
	report_pep_prot_cluster
	JOIN report_sample_concat	USING (`sequence`, `modifier`, `type`, `entry`, `cluster_average_index`)
	JOIN report_run_concat 		USING (`sequence`, `modifier`, `type`, `entry`, `cluster_average_index`)
ORDER BY entry
;
ALTER TABLE report_peptide_quantification ADD INDEX(`entry`);
ALTER TABLE report_peptide_quantification ADD INDEX(`cluster_average_index`);
ALTER TABLE report_peptide_quantification ADD INDEX(`sequence`);
ALTER TABLE report_peptide_quantification ADD INDEX(`modifier`); 
ALTER TABLE report_peptide_quantification ADD INDEX(`type`);

-- @ collecting information for protein quantification report: [done]