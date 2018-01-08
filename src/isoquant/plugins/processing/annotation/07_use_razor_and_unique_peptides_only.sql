-- after homology filtering
-- @ :	removing shared peptides . . . 
DELETE FROM peptides_in_proteins WHERE src_proteins > 1;
OPTIMIZE TABLE peptides_in_proteins;
-- @ :	removing shared peptides . . . [done]