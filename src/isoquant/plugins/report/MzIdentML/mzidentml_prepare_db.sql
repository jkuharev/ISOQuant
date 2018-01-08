-- @ :	creating temporary data ...
DROP TABLE IF EXISTS mzidentml_temporalInfo;
CREATE TABLE IF NOT EXISTS mzidentml_temporalInfo (
  `id` INT NOT NULL AUTO_INCREMENT,
  `InfoName` VARCHAR(45) NULL ,
  `InfoValue` VARCHAR(255) NULL ,
  PRIMARY KEY (`id`))
  ENGINE = MyISAM
  DEFAULT CHARACTER SET = utf8
  COLLATE = utf8_bin;

SET @protonMass = 1.0072765;
DROP TABLE IF EXISTS mzidentml_mztab_peptide;
CREATE TABLE mzidentml_mztab_peptide
(id INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (id), KEY (sequence), KEY (modifier))
  ENGINE=MyISAM
  SELECT emq.sequence, emq.modifier, emq.entry, pep.stat_max_score, emq.charge,
    group_concat(DISTINCT concat(emq.workflow_index, ":",  emq.cor_inten) ORDER BY emq.workflow_index SEPARATOR ',') AS wf_intensities,
    group_concat(DISTINCT concat(emq.workflow_index, ":", emq.low_energy_index) ORDER BY emq.workflow_index SEPARATOR ',') AS wf_lowEnergy,
--    group_concat(DISTINCT concat(emq.workflow_index, ":", pep.score) ORDER BY emq.workflow_index SEPARATOR ',') AS wf_scores,
    (pep.mass + emq.charge * @protonMass) / emq.charge as theoretical_MassToCharge,
    emq.rt * 60 AS rt_seconds,
    ms.LiftOffRT * 60 AS rt_begin_seconds,
    ms.TouchDownRT * 60 AS rt_end_seconds
  FROM emrt4quant emq
    JOIN peptide pep USING (sequence, modifier)
    JOIN mass_spectrum ms ON ms.workflow_index = emq.workflow_index AND ms.low_energy_index = emq.low_energy_index
  WHERE emq.src_proteins >= 1
  GROUP BY emq.sequence, emq.modifier
  ORDER BY emq.sequence, emq.modifier;



