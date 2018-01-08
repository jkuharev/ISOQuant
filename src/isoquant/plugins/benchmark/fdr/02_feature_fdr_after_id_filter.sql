-- @ : mapping identification filter passing peptides to features  . . .

-- find false positive EMRTs
DROP TABLE IF EXISTS emrt_fp_after_identification_filter;
CREATE TEMPORARY TABLE emrt_fp_after_identification_filter
SELECT 
	emrt_index, fp
FROM 
	emrts_annotated_by_filtered_peptides 
	JOIN peptide ON emrt_index=`index`
	JOIN peptide_fp USING(sequence, modifier)
;
ALTER TABLE emrt_fp_after_identification_filter ADD INDEX (`emrt_index`);

-- @ : calculating feature FDR based on peptide identification filter . . .

INSERT INTO feature_fdr_stats (description, all_count, fp_count, fdr)
SELECT 
	"feature FDR after identification filter" as description,
	count( emrt_index ) AS all_count, 
	sum( fp ) AS fp_count, 
	sum( fp ) / count( emrt_index ) AS fdr
FROM emrt_fp_after_identification_filter;
