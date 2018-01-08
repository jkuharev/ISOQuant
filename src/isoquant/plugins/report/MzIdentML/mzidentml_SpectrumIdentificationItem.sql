-- SpectrumIdentificationItem
-- @ :	relating mass spectra to peptides ...

SET @protonMass = 1.0072765;
OPTIMIZE TABLE mass_spectrum, query_mass, peptide, best_peptides_for_quantification, mzidentml_pepevidence;

DROP TABLE IF EXISTS mzidentml_SpectrumIdentificationItem;
CREATE TABLE mzidentml_SpectrumIdentificationItem
(id INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (id), KEY (workflow_index), KEY (low_energy_index), KEY(Mobility), KEY(theoretical_MassToCharge))
  ENGINE=MyISAM
    SELECT
      pep.query_mass_index,
      ms.workflow_index,
      ms.low_energy_index,
      qm.low_energy_id,
      ms.Mobility,
      (pep.mass + ms.Z * @protonMass) / ms.Z as theoretical_MassToCharge,
      (ms.Mass + ms.Z * @protonMass) / ms.Z as experimental_MassToCharge,
      ms.Z,
      pepev.peptide_ref,
      pep.score as PLGS_score,
      pep.`type`as PLGS_type,
      pep.frag_string as PLGS_frag_string,
      GROUP_CONCAT( DISTINCT pepev.id ) as peptideEvidence_refs
--    , "" as siiID
    FROM mass_spectrum ms
      JOIN query_mass qm USING (low_energy_index)
      JOIN peptide pep ON qm.`index` = pep.query_mass_index
--      INNER JOIN best_peptides_for_quantification bp ON bp.sequence = pep.sequence
      JOIN mzidentml_pepevidence pepev ON pepev.sequence = pep.sequence
    GROUP BY ms.low_energy_index, pep.sequence, pep.modifier
    ORDER BY ms.low_energy_index, pep.sequence, pep.modifier;
    
-- UPDATE `mzidentml_SpectrumIdentificationItem` SET siiID = CONCAT_WS("_", workflow_index, low_energy_index, id);