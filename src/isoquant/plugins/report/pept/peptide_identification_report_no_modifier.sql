-- @ :	collecting peptide identification information ...
SELECT
	sequence,
	score,
	fpr,
	src_proteins,
	GROUP_CONCAT( entry ) AS entries
FROM
	pep_fpr
	JOIN peptides_in_proteins_before_homology_filtering USING( sequence )
	JOIN best_peptides USING( sequence )
GROUP BY 
	sequence
ORDER BY
	fpr ASC, score DESC
;
-- @ :	retrieving data ...