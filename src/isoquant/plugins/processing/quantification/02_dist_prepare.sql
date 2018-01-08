-- @ :	listing peptide in proteins mappings ...

-- Auflisten der möglichen Proteinzahlen pro Peptid --
DROP TABLE IF EXISTS `redist_src_prots`;
CREATE TEMPORARY TABLE `redist_src_prots`
SELECT DISTINCT `src_proteins` FROM emrt4quant ORDER BY `src_proteins` ASC;

-- @ :	assigning full intensity to unique peptides ...

-- IF cor_inten=null THEN cor_inten=inten
UPDATE emrt4quant SET `cor_inten`=`inten` WHERE `cor_inten` IS NULL;

-- alle eindeutig zugeordneten Peptide bekommen komplette Intensität
UPDATE emrt4quant SET `dist_inten`=`cor_inten` WHERE `src_proteins`=1;

-- die Proteinzahl 1 entfernen
DELETE FROM `redist_src_prots` WHERE `src_proteins`=1;