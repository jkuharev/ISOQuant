-- @ collecting information for peptide report ...

-- @ :	pooling clusters ...
-- sum intensities of emrts of the same run in a cluster
-- (but only for annotated clusters)
DROP TABLE IF EXISTS run_pooled_ce;
CREATE TEMPORARY TABLE run_pooled_ce
SELECT
	cluster_average_index,
	FLOOR(sum(cor_inten)) as cor_inten,
	workflow_index
FROM
	best_peptides_for_quantification INNER JOIN clustered_emrt as ce 
	USING(cluster_average_index)
GROUP BY 
	cluster_average_index, workflow_index
;
ALTER TABLE run_pooled_ce ADD PRIMARY KEY(cluster_average_index, workflow_index);

-- @ :	calculating sample averages ...
DROP TABLE IF EXISTS sample_pooled_ce;
CREATE TEMPORARY TABLE sample_pooled_ce
SELECT
	cluster_average_index,
	FLOOR(AVG(cor_inten)) as cor_inten,
	sample_index
FROM
	run_pooled_ce JOIN workflow ON workflow_index=`index`
GROUP BY 
	cluster_average_index, sample_index
;
ALTER TABLE sample_pooled_ce ADD PRIMARY KEY(cluster_average_index, sample_index);

-- @ :	calculating cluster averages ...
-- best peptides cluster averages
DROP TABLE IF EXISTS cluster_info_avg;
CREATE TEMPORARY TABLE cluster_info_avg
SELECT
	cluster_average_index, 
	ROUND(AVG(mass), 4) as avg_mass, 
	ROUND(AVG(rt), 2) as avg_rt, 
	ROUND(AVG(ref_rt), 2) as avg_ref_rt
FROM
	best_peptides_for_quantification as bp
	JOIN clustered_emrt as ce USING(cluster_average_index)
GROUP BY
	cluster_average_index
;
ALTER TABLE cluster_info_avg ADD PRIMARY KEY(cluster_average_index);

-- collect cluster info for best peptides
DROP TABLE IF EXISTS cluster_info_runs;
CREATE TABLE cluster_info_runs
SELECT
	cluster_average_index, 
	avg_mass, 
	avg_rt, 
	avg_ref_rt,
	GROUP_CONCAT(`workflow_index`, ':', `cor_inten` ORDER BY `workflow_index` ASC SEPARATOR ';') as intensities
FROM
	cluster_info_avg JOIN run_pooled_ce USING(cluster_average_index)
GROUP BY
	cluster_average_index
;
ALTER TABLE cluster_info_runs ADD PRIMARY KEY(cluster_average_index);


-- collect cluster info for best peptides
DROP TABLE IF EXISTS cluster_info_samples;
CREATE TABLE cluster_info_samples
SELECT
	cluster_average_index, 
	avg_mass, 
	avg_rt, 
	avg_ref_rt,
	GROUP_CONCAT(`sample_index`, ':', `cor_inten` ORDER BY `sample_index` ASC SEPARATOR ';') as intensities
FROM
	cluster_info_avg JOIN sample_pooled_ce USING(cluster_average_index)
GROUP BY
	cluster_average_index
;
ALTER TABLE cluster_info_samples ADD PRIMARY KEY(cluster_average_index);


-- @ :	finding high score peptides for clusters ...
-- all peptides per cluster
DROP TABLE IF EXISTS peptides_for_cluster;
CREATE TEMPORARY TABLE peptides_for_cluster
SELECT
	pep.`index`, cluster_average_index, score
FROM
	peptide as pep 
	JOIN `query_mass` as qm ON qm.`index`=pep.`query_mass_index`
	JOIN clustered_emrt as ce USING(low_energy_index)
	JOIN best_peptides_for_quantification as bp USING(cluster_average_index, sequence, modifier)
;
ALTER TABLE peptides_for_cluster ADD INDEX(`index`);
ALTER TABLE peptides_for_cluster ADD INDEX(`cluster_average_index`);
ALTER TABLE peptides_for_cluster ADD INDEX(`score`);

-- determine max score in a cluster
DROP TABLE IF EXISTS peptides_for_cluster_max;
CREATE TEMPORARY TABLE peptides_for_cluster_max
SELECT 
	cluster_average_index, max(score) as score 
FROM 
	peptides_for_cluster 
GROUP BY 
	cluster_average_index;
ALTER TABLE peptides_for_cluster_max ADD INDEX(`cluster_average_index`);
ALTER TABLE peptides_for_cluster_max ADD INDEX(`score`);

-- high-score peptide for cluster
DROP TABLE IF EXISTS high_score_peptide_for_cluster;
CREATE TEMPORARY TABLE high_score_peptide_for_cluster
SELECT
	cluster_average_index, `index`
FROM
	peptides_for_cluster_max 
	JOIN peptides_for_cluster USING(cluster_average_index, score)
GROUP BY
	cluster_average_index
;
ALTER TABLE high_score_peptide_for_cluster ADD INDEX(`index`);
ALTER TABLE high_score_peptide_for_cluster ADD INDEX(`cluster_average_index`);

-- info for high score cluster - peptide
-- counting its sequence's annotation rate
DROP TABLE IF EXISTS cluster_info_peptide;
CREATE TABLE cluster_info_peptide
SELECT
	cluster_average_index, 
	hp.workflow_index,
	hp.sequence,
	hp.modifier, 
	hp.type,
	hp.score, 
	hp.start, 
	hp.end, 
	hp.frag_string, 
	hp.products, 
	hp.stat_count_workflows, 
	hp.stat_max_score,
	count(DISTINCT pep.workflow_index) as annotation_rate
FROM
	high_score_peptide_for_cluster as hpc
	JOIN peptide as hp USING(`index`)
	JOIN peptides_for_cluster as pfc USING(cluster_average_index)
	JOIN peptide as pep ON pfc.`index`=pep.`index` AND hp.sequence=pep.sequence
GROUP BY 
	cluster_average_index
;
ALTER TABLE cluster_info_peptide ADD PRIMARY KEY(`cluster_average_index`);
ALTER TABLE cluster_info_peptide ADD INDEX(`sequence`);

-- @ :	collecting mass spectrum signal information for high score peptides ...
-- mass spectrum peak data for high score peptide
DROP TABLE IF EXISTS cluster_info_peak;
CREATE TABLE cluster_info_peak
SELECT
	cluster_average_index,
	ms.Mass,
	ms.Mobility,
	ms.Intensity, 
	AverageCharge,
	Z,
	FWHM,
	RT, LiftOffRT, InfUpRT, InfDownRT, TouchDownRT
FROM
	high_score_peptide_for_cluster JOIN peptide as pep USING(`index`)
	JOIN query_mass as qm ON query_mass_index=qm.`index`
	JOIN low_energy as le ON qm.low_energy_index=le.`index`
	JOIN mass_spectrum as ms ON le.workflow_index=ms.workflow_index AND le.id=ms.LE_ID
;
ALTER TABLE cluster_info_peak ADD PRIMARY KEY(`cluster_average_index`);


-- @ :	finding proteins for high score peptides before homology filtering ...
-- proteins for high score peptides before homology filtering
DROP TABLE IF EXISTS cluster_info_proteins_before_homology_filtering;
CREATE TABLE cluster_info_proteins_before_homology_filtering
SELECT
	cluster_average_index, 
	GROUP_CONCAT(DISTINCT entry) as entries,
	GROUP_CONCAT(DISTINCT accession) as accessions
FROM
	cluster_info_peptide 
	JOIN peptides_in_proteins_before_homology_filtering USING(sequence)
	JOIN protein USING(entry)
GROUP BY
	cluster_average_index
;
ALTER TABLE cluster_info_proteins_before_homology_filtering ADD PRIMARY KEY(`cluster_average_index`);

-- @ :	finding proteins for high score peptides after homology filtering ...	
-- proteins for high score peptides after homology filtering
DROP TABLE IF EXISTS cluster_info_proteins_after_homology_filtering;
CREATE TABLE cluster_info_proteins_after_homology_filtering
SELECT
	cluster_average_index, 
	GROUP_CONCAT(DISTINCT entry) as entries,
	GROUP_CONCAT(DISTINCT accession) as accessions
FROM
	cluster_info_peptide
	JOIN peptides_in_proteins USING(sequence)
	JOIN protein USING(entry)
GROUP BY
	cluster_average_index
;
ALTER TABLE cluster_info_proteins_after_homology_filtering ADD PRIMARY KEY(`cluster_average_index`);
	
-- @ :	reassigning high score peptides to proteins ...
-- count peptides in proteins
DROP TABLE IF EXISTS peptides_in_proteins_peptide_count;
CREATE TEMPORARY TABLE peptides_in_proteins_peptide_count
	SELECT 
		entry, count(sequence) as peptide_count, score
	FROM 
		peptides_in_proteins
		JOIN protein_info USING(entry) 
	GROUP BY 
		entry
;
ALTER TABLE peptides_in_proteins_peptide_count ADD PRIMARY KEY(entry);
ALTER TABLE peptides_in_proteins_peptide_count ADD INDEX(peptide_count);
ALTER TABLE peptides_in_proteins_peptide_count ADD INDEX(score);

-- define max peptide count of proteins containing this peptide
DROP TABLE IF EXISTS peptides_in_proteins_max_peptide_count;
CREATE TEMPORARY TABLE peptides_in_proteins_max_peptide_count
	SELECT 
		sequence, max(peptide_count) as peptide_count, max(score) as score
	FROM 
		peptides_in_proteins
		JOIN peptides_in_proteins_peptide_count USING(entry) 
	GROUP BY sequence
;
ALTER TABLE peptides_in_proteins_max_peptide_count ADD PRIMARY KEY(sequence);
ALTER TABLE peptides_in_proteins_max_peptide_count ADD INDEX(peptide_count);
ALTER TABLE peptides_in_proteins_max_peptide_count ADD INDEX(score);

-- reassign proteins to peptides
DROP TABLE IF EXISTS peptides_in_proteins_reassigned;
CREATE TEMPORARY TABLE peptides_in_proteins_reassigned
	SELECT
		sequence, entry
	FROM 
		peptides_in_proteins as pip 
		JOIN peptides_in_proteins_peptide_count USING(entry)
		JOIN peptides_in_proteins_max_peptide_count USING(sequence, peptide_count, score)
	GROUP BY
		sequence
;
ALTER TABLE peptides_in_proteins_reassigned ADD INDEX(sequence);
ALTER TABLE peptides_in_proteins_reassigned ADD INDEX(entry);

-- @ :	collecting detailed protein information for clusters ...
-- proteins for high score peptides
DROP TABLE IF EXISTS cluster_info_proteins;
CREATE TABLE cluster_info_proteins
SELECT
	cluster_average_index, entry, accession, 
	pm.description, pm.mw, pm.`pi`, pm.score, pm.coverage, 
	pm.stat_count_workflows, pm.stat_count_peptides, pm.stat_count_unique_peptides
FROM
	cluster_info_peptide 
	JOIN peptides_in_proteins_reassigned USING(sequence)
	JOIN protein_info as pm USING(entry)
;
ALTER TABLE cluster_info_proteins ADD PRIMARY KEY(`cluster_average_index`);

-- @ collecting information for peptide report ... [done]