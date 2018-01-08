-- @ :	removing shared peptides . . . 
DELETE 
	pip
FROM 
	peptides_in_proteins as pip	
	JOIN
	peptides_in_proteins_before_homology_filtering as ori
	USING(sequence)
WHERE
	ori.src_proteins > 1
;
OPTIMIZE TABLE peptides_in_proteins;
-- @ :	removing shared peptides . . . [done]