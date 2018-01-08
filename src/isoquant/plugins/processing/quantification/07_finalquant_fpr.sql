-- @ calculating false positive statistics for quantified proteins . . .

-- @ :	flagging false positive proteins . . .
DROP TABLE IF EXISTS prot_fp;
CREATE TEMPORARY TABLE prot_fp
SELECT 
	entry,
        MAX( IF( entry LIKE '%REVERSE%' OR entry LIKE '%RANDOM%', 1, 0 ) ) as fp,
        score
FROM 
	finalquant JOIN protein_info USING(entry)
GROUP BY
	entry
;

-- @ :	calculating FDR . . .
DROP TABLE IF EXISTS prot_fpr;
CREATE TEMPORARY TABLE prot_fpr
SELECT
	entry,
	score,
	@id := (@id + 1) as id,
	@fps := (@fps + fp) as fps,
	@fpr := if(@fpr > @fps/@id, @fpr, @fps/@id) as fpr
FROM
	prot_fp, (SELECT @id:=0, @fps:=0, @fpr:=0) x
ORDER BY
	score DESC
;
ALTER TABLE prot_fpr ADD INDEX(entry);

-- extend original finalquant table by adding fpr column
DROP TABLE IF EXISTS finalquant_fpr;
CREATE TABLE finalquant_fpr
SELECT
	finalquant.*,
	ROUND(fpr, 4) as fpr
FROM
	finalquant JOIN prot_fpr USING(entry)
;
ALTER TABLE finalquant_fpr ADD INDEX(sample_index);
ALTER TABLE finalquant_fpr ADD INDEX(workflow_index);
ALTER TABLE finalquant_fpr ADD INDEX(entry);
ALTER TABLE finalquant_fpr ADD INDEX(fpr);

-- @ calculating false positive statistics for quantified proteins . . . [done]