-- distribution loop

set @topXdegree = %TOPX_DEGREE%;

-- alle Proteinzahlen>1 werden durchlaufen
-- und die Intensitäten auf Mehrfachvorkommen verteilt.

-- start loop für alle werte aus redist_src_prots n >1 n=2
set @N := 2;
-- die nächste kleinste Proteinzahl aus der Liste holen
SELECT @N := min( src_proteins ) FROM redist_src_prots;

-- die kleinste Proteinzahl aus der Liste entfernen
DELETE FROM redist_src_prots WHERE src_proteins=@N;

-- Für alle Peptide die zu weniger als N Proteinen passen
-- Mittelwert von dist_inten per Sample
-- zu jeder Protein-Peptid-Kombination
DROP TABLE IF EXISTS redist_pip_per_sample;
CREATE  TABLE redist_pip_per_sample
SELECT 
	avg(dist_inten) as avg_inten, 
	sequence, modifier, 
	sample_index, entry, src_proteins
FROM 
	emrt4quant 
WHERE
	src_proteins < @N
GROUP BY
	sample_index, entry, sequence, modifier
ORDER BY
	sample_index ASC, entry ASC, avg_inten DESC
;
ALTER TABLE redist_pip_per_sample ADD INDEX(sample_index);
ALTER TABLE redist_pip_per_sample ADD INDEX(entry);
ALTER TABLE redist_pip_per_sample ADD INDEX(sequence);
ALTER TABLE redist_pip_per_sample ADD INDEX(modifier);

-- Ranking der Mittelwert-Intensitäten und Begrenzung auf die Top X
DROP TABLE IF EXISTS redist_protein_per_run_top3;
CREATE  TABLE redist_protein_per_run_top3
SELECT 
	sample_index, entry, avg_inten
FROM
	(SELECT 
			sample_index, 
			entry, 
			avg_inten,
			@num := if(@e = entry, @num +1, 1) as row_number,
			@e := entry as dummy
		FROM 		redist_pip_per_sample, (SELECT @num := 0, @e :='') as xxx
		ORDER BY	avg_inten DESC
	) as ce
WHERE
	row_number <= @topXdegree
;

-- TopX für Protein per Sample
DROP TABLE IF EXISTS redist_protein_per_sample_top3;
CREATE  TABLE redist_protein_per_sample_top3
SELECT 
	sample_index, entry, avg(avg_inten) as top3_inten
FROM 
	redist_protein_per_run_top3
GROUP BY
	entry, sample_index
;
ALTER TABLE redist_protein_per_sample_top3 ADD INDEX(entry);

-- zu jedem Peptid->Protein-Paar
-- Top3-Intensität des jeweiligen Proteins hinzufügen
DROP TABLE IF EXISTS redist_pip_per_sample_top3;
CREATE  TABLE redist_pip_per_sample_top3
SELECT	
	sequence, entry, sample_index, top3_inten
FROM	
	peptides_in_proteins as p JOIN redist_protein_per_sample_top3 as u USING(entry)
;
ALTER TABLE redist_pip_per_sample_top3 ADD INDEX(sequence);
ALTER TABLE redist_pip_per_sample_top3 ADD INDEX(sample_index);
	
-- VORSICHT: sum_of_top3 kann NULL enthalten!
-- sum(null, 1) = 1
-- sum(null, null) = null
-- redist_peptide_per_sample_sum_of_top3: Sum of top3
-- Für jedes Peptid Summe aller Top3 seiner Proteine
DROP TABLE IF EXISTS redist_peptide_per_sample_sum_of_top3;
CREATE  TABLE redist_peptide_per_sample_sum_of_top3
SELECT 
	sequence, sample_index, sum(top3_inten) as sum_of_top3
FROM 
	redist_pip_per_sample_top3
GROUP BY 
	sequence, sample_index
;
ALTER TABLE redist_peptide_per_sample_sum_of_top3 ADD INDEX(sequence);
ALTER TABLE redist_peptide_per_sample_sum_of_top3 ADD INDEX(sample_index);

-- Faktor zur Aufteilung der Peptidintensität 
-- als Anteil der Top3
-- inten_factor = top3_inten / sum_of_top3
-- aber: wenn top3_inten = NULL und sum_of_top3 != NULL dann inten_factor = 0
DROP TABLE IF EXISTS redist_pip_per_sample_dist_factor;
CREATE  TABLE redist_pip_per_sample_dist_factor
SELECT
	sequence, entry, sample_index, top3_inten, sum_of_top3,
	IF( sum_of_top3 IS NOT NULL AND top3_inten IS NULL
		, 0
		, top3_inten / sum_of_top3
	) as inten_factor
FROM
	redist_pip_per_sample_top3 
	LEFT JOIN redist_peptide_per_sample_sum_of_top3 
	USING(sequence, sample_index)
;
ALTER TABLE redist_pip_per_sample_dist_factor ADD INDEX(sequence);
ALTER TABLE redist_pip_per_sample_dist_factor ADD INDEX(sample_index);
ALTER TABLE redist_pip_per_sample_dist_factor ADD INDEX(entry);

-- update emrt4quant mit 
-- dist_inten = cor_inten * inten_factor
UPDATE 
	emrt4quant 
	LEFT JOIN redist_pip_per_sample_dist_factor USING( sequence, entry, sample_index )
SET 
	dist_inten = IF( inten_factor is NULL, cor_inten / @N, cor_inten * inten_factor )
WHERE 
	q.src_proteins=@N;