-- @ :	collecting peptide identification information ...
SELECT
	sequence,
	modifier,
	score,
	fpr,
	src_proteins,
	GROUP_CONCAT( DISTINCT entry ) AS entries
FROM
	pep_fpr
	JOIN peptides_in_proteins_before_homology_filtering USING( sequence )
	JOIN best_peptides USING(sequence, modifier)
GROUP BY 
	sequence, 
	modifier
ORDER BY
	fpr ASC, score DESC
;
-- @ :	retrieving data ...