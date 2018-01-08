-- @ : mapping peptides to reannotated features  . . .
DROP TABLE IF EXISTS emrt_fp_after_reannotation;
CREATE TEMPORARY TABLE emrt_fp_after_reannotation
SELECT 
	ce.`index` as emrt_index, fp
FROM
	best_peptides_for_annotation
	JOIN clustered_emrt as ce USING(cluster_average_index)
	JOIN peptide_fp USING(sequence, modifier)
;
ALTER TABLE emrt_fp_after_reannotation ADD INDEX (`emrt_index`);

-- @ : calculating feature FDR based on EMRT cluster annotation . . .

INSERT INTO feature_fdr_stats (description, all_count, fp_count, fdr)
SELECT 
	"feature FDR after cluster annotation" as description,
	count( emrt_index ) AS all_count, 
	sum( fp ) AS fp_count, 
	sum( fp ) / count( emrt_index ) AS fdr
FROM emrt_fp_after_reannotation;