-- execution order: 03 after normalization

-- @ preparing peptide intensity redistribution ...

-- @ :	pooling cluster annotation and peptide details ...

-- pool peaks per run and annotate by best peptide per cluster
-- sample_index und das beste Peptid zu jedem Clustered_EMRT
DROP TABLE IF EXISTS redist_best_emrt2;
CREATE TEMPORARY TABLE redist_best_emrt2
SELECT 
	ce.`index`,
	ce.`workflow_index`, 
	ce.`expression_analysis_index`, 
	ce.`cluster_average_index`, 
	ce.`cluster_id`, 
	AVG(ce.`mass`) as mass,
	AVG(ce.`sd_mhp`) as sd_mhp, 
	FLOOR(SUM(ce.`inten`)) as inten, 
	ce.`spec_index`, 
	AVG(ce.`charge`) as charge, 
	AVG(ce.`rt`) as rt,
	AVG(ce.`sd_rt`) as sd_rt, 
	AVG(ce.`ref_rt`) as ref_rt, 
	ce.`precursor_type`, 
	ce.`low_energy_index`, 
	FLOOR( SUM(ce.`cor_inten`) ) as cor_inten,
	best.`sequence`, 
	best.`modifier`, 
	best.`type`, 
	`sample_index`
FROM 
	best_peptides_for_quantification as best 
	JOIN clustered_emrt as ce USING(`cluster_average_index`)
	JOIN workflow as w ON ce.`workflow_index`=w.`index`
GROUP BY 
	`workflow_index`, `cluster_average_index`
;
ALTER TABLE redist_best_emrt2 ADD INDEX(`sequence`);

-- @ :	linking proteins to annotated clusters ...

-- linking emrt-peptide to peptide-protein
-- one row per emrt-peptide-protein entity
-- d.h. wenn ein Peptid zu mehreren Proteinen passt, steht's mehrfach drin
DROP TABLE IF EXISTS emrt4quant;
CREATE TABLE emrt4quant
SELECT 
	redist_best_emrt2.*, 
	entry,
	src_proteins,
	000000000000.000000 as `dist_inten`
--	(cor_inten / src_proteins) as `dist_inten`
FROM 
	redist_best_emrt2 LEFT JOIN `peptides_in_proteins` as p USING(`sequence`)
;
ALTER TABLE emrt4quant ADD INDEX(`cor_inten`);
ALTER TABLE emrt4quant ADD INDEX(`src_proteins`);
ALTER TABLE emrt4quant ADD INDEX(`sequence`); 
ALTER TABLE emrt4quant ADD INDEX(`modifier`);
ALTER TABLE emrt4quant ADD INDEX(`type`);
ALTER TABLE emrt4quant ADD INDEX(`entry`);
ALTER TABLE emrt4quant ADD INDEX(`cluster_average_index`);
