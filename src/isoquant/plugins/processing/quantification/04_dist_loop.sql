-- distribution loop

set @topXdegree = %TOPX_DEGREE%;

-- alle Proteinzahlen>1 werden durchlaufen
-- und die Intensitäten auf Mehrfachvorkommen verteilt.

-- start loop für alle werte aus redist_src_prots n >1 n=2
set @N := 2;
-- die nächste kleinste Proteinzahl aus der Liste holen
SELECT @N := min( `src_proteins` ) FROM `redist_src_prots`;

-- die kleinste Proteinzahl aus der Liste entfernen
DELETE FROM `redist_src_prots` WHERE `src_proteins`=@N;

-- Für alle Peptide die zu weniger als N Proteinen passen
-- Berechnung vom Mittelwert der dist_inten innerhalb eines Samples
-- zu jeder eindeutigen entry+sequence+modifier-Kombination
DROP TABLE IF EXISTS redist_upep1;
CREATE TEMPORARY TABLE redist_upep1
SELECT 
	avg(`dist_inten`) as `avg_inten`, `sequence`, 
	`modifier`, `sample_index`, `entry`, `src_proteins`
FROM 
	emrt4quant
WHERE
	`src_proteins` < @N
GROUP BY
	`sample_index`, `entry`, `sequence`, `modifier`
ORDER BY
	`sample_index` ASC, `entry` ASC, `avg_inten` DESC
;
ALTER TABLE redist_upep1 ADD INDEX(`sample_index`);
ALTER TABLE redist_upep1 ADD INDEX(`entry`);
ALTER TABLE redist_upep1 ADD INDEX(`sequence`);
ALTER TABLE redist_upep1 ADD INDEX(`modifier`);

-- Ranking der Mittelwert-Intensitäten und Begrenzung auf die Top 3
set @num := 0 , @old_entry :='';
DROP TABLE IF EXISTS redist_top3inten;
CREATE TEMPORARY TABLE redist_top3inten
SELECT 
	sample_index, entry, avg_inten
FROM
	(
		SELECT 
			sample_index, entry, avg_inten,
			@num := if(@old_entry = entry, @num +1, 1) as row_number,
			@old_entry := entry as dummy
		FROM 
			redist_upep1
		ORDER BY
			avg_inten DESC
	) cee
WHERE
	row_number <= @topXdegree
;

-- Mittelwert der Top3-Intensitäten zu jeder Entry innerhalb eines Samples
DROP TABLE IF EXISTS redist_upa;
CREATE TEMPORARY TABLE redist_upa
SELECT
	`sample_index`, `entry`, avg(`avg_inten`) as redist_top3inten
FROM 
	redist_top3inten
GROUP BY
	`entry`, `sample_index`
;
ALTER TABLE redist_upa ADD INDEX(`entry`);

-- zu jeder Peptid->Protein-Zuordnung die Top3-Intensität hinzufügen
DROP TABLE IF EXISTS redist_bu;
CREATE TEMPORARY TABLE redist_bu
SELECT	
	p.sequence, p.entry, u.sample_index, u.redist_top3inten
FROM	
	peptides_in_proteins as p JOIN redist_upa as u USING(entry)
;
ALTER TABLE redist_bu ADD INDEX(`sequence`);
ALTER TABLE redist_bu ADD INDEX(`sample_index`);
	
-- zu jeder Peptidsequenz innerhalb eines Samples wird die Summe der Intensitäten berechnet
-- VORSICHT: sum_of_top3_inten kann NULL enthalten!
-- sum(null,null,1) = 1
-- sum(null, null) = null
-- redist_st3: Sum of top3
DROP TABLE IF EXISTS redist_st3;
CREATE TEMPORARY TABLE redist_st3
SELECT 
	sequence, sample_index, sum(redist_top3inten) as sum_of_top3_inten
FROM 
	redist_bu
GROUP BY 
	sequence, sample_index
;
ALTER TABLE redist_st3 ADD INDEX(sequence);
ALTER TABLE redist_st3 ADD INDEX(sample_index);

-- Berechnung des Intensitätsaufteilungsfaktors
-- redist_factor = redist_top3inten / sum_of_redist_top3inten
-- aber: wenn redist_top3inten = NULL und sum_of_redist_top3inten != NULL dann redist_factor = 0
DROP TABLE IF EXISTS redist_bust;
CREATE TEMPORARY TABLE redist_bust
SELECT
	sequence, entry, sample_index, redist_top3inten, sum_of_top3_inten,
	IF( 
		(sum_of_top3_inten is not NULL) AND (redist_top3inten is NULL)
		, 0
		, redist_top3inten / sum_of_top3_inten
	) as redist_factor
FROM
	redist_bu LEFT JOIN redist_st3 USING(`sequence`, `sample_index`)
;
ALTER TABLE redist_bust ADD INDEX(`sequence`);
ALTER TABLE redist_bust ADD INDEX(`sample_index`);
ALTER TABLE redist_bust ADD INDEX(`entry`);

-- Summe der Verteilungsfaktoren per Peptide per Sample
-- erwartet: 1.0
DROP TABLE IF EXISTS redist_factor_sum;
CREATE TABLE redist_factor_sum
SELECT 
	`sequence`, `sample_index`, sum(`redist_factor`) as redist_factor_sum
FROM
	`redist_bust`
GROUP BY
	`sample_index`, `sequence`
;
ALTER TABLE redist_factor_sum ADD INDEX(`sequence`);
ALTER TABLE redist_factor_sum ADD INDEX(`sample_index`);

-- START update for n
-- join emrt4quant with redist_bust
-- Berechnung der "neuen" dist_inten anhand des redist_factor und der rt_cor_inten
UPDATE 
	emrt4quant as q
	LEFT JOIN redist_bust as b USING( sequence, entry, sample_index )
	LEFT JOIN redist_factor_sum as c USING( sequence, sample_index )
SET 
	q.`dist_inten`=
		IF( `redist_factor` is NULL
			, IF(redist_factor_sum is NULL, `cor_inten` / @N, 0)
			, q.`cor_inten` * b.`redist_factor`
		)
WHERE 
	q.`src_proteins`=@N;
-- END	