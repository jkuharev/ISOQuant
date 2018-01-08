-- @ : mapping all peptides to features  . . .

-- map EMRT to peptides ...
DROP TABLE IF EXISTS emrt_annotated_by_plgs;
CREATE TEMPORARY TABLE emrt_annotated_by_plgs
SELECT 
	ce.`index` as emrt_index,
	pep.`index` as peptide_index
FROM 
	peptide as pep
	JOIN query_mass as qm ON qm.`index`=pep.`query_mass_index`
	JOIN clustered_emrt as ce USING(low_energy_index)
;
ALTER TABLE emrt_annotated_by_plgs ADD INDEX (`emrt_index`);
ALTER TABLE emrt_annotated_by_plgs ADD INDEX (`peptide_index`);

-- @ : flagging false positive features  . . .

-- find false positive EMRTs
DROP TABLE IF EXISTS emrt_fp_by_plgs;
CREATE TEMPORARY TABLE emrt_fp_by_plgs
SELECT 
	emrt_index, fp	
FROM 
	emrt_annotated_by_plgs 
	JOIN peptide ON emrt_index=`index`
	JOIN peptide_fp USING(sequence, modifier)
;
ALTER TABLE emrt_fp_by_plgs ADD INDEX (`emrt_index`);

-- @ : calculating feature FDR based on original PLGS identifications . . .

-- store result
INSERT INTO feature_fdr_stats (description, all_count, fp_count, fdr) 
SELECT 
	"feature FDR based on original plgs identification" as description,
	count( emrt_index ) AS all_count, 
	sum( fp ) AS fp_count, 
	sum( fp ) / count( emrt_index ) AS fdr
FROM `emrt_fp_by_plgs`;
