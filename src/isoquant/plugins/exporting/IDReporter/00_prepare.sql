-- @ :	filtering PLGS peptide identities

DROP TABLE IF EXISTS peptide_plgs_filtered;
CREATE TABLE peptide_plgs_filtered
	SELECT sequence, count(DISTINCT workflow_index) as r
	FROM peptide
	WHERE
		`type` IN (%PEPTIDE_TYPE_FILTER_ADDITIONAL_TYPES%)
		AND
		length(`sequence`) >= %PEPTIDE_SEQUENCE_LENGTH_MIN_VALUE% 
	GROUP BY sequence
;
ALTER TABLE peptide_plgs_filtered ADD INDEX(`sequence`);
ALTER TABLE peptide_plgs_filtered ADD INDEX(`r`);
