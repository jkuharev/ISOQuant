SELECT
	`mass_spectrum`.`Mobility`	as	`mass_spectrum.Mobility`	,
	`mass_spectrum`.`MassSD`	as	`mass_spectrum.MassSD`	,
	`mass_spectrum`.`RT`	as	`mass_spectrum.RT`	,
	`mass_spectrum`.`RTSD`	as	`mass_spectrum.RTSD`	,
	`mass_spectrum`.`FWHM`	as	`mass_spectrum.FWHM`	,
	`query_mass`.`intensity`	as	`query_mass.intensity`	,
	`low_energy`.`charge`	as	`low_energy.charge`	,
	`peptide`.`mass`	as	`peptide.mass`	,
	`peptide`.`sequence`	as	`peptide.sequence`	,
	`peptide`.`type`	as	`peptide.type`	,
	`peptide`.`modifier`	as	`peptide.modifier`	,
	`peptide`.`rms_mass_error_prod`	as	`peptide.rms_mass_error_prod`	,
	`peptide`.`rms_rt_error_prod`	as	`peptide.rms_rt_error_prod`	,
	`peptide`.`mass_error`	as	`peptide.mass_error`	,
	`peptide`.`mass_error_ppm`	as	`peptide.mass_error_ppm`	,
	`peptide`.`score`	as	`peptide.score`	,
	`protein`.`entry`	as	`protein.entry`	,
	`protein`.`accession`	as	`protein.accession`	,
	`protein`.`description`	as	`protein.description`	,
	`protein`.`coverage`	as	`protein.coverage`	,
	`protein`.`score`	as	`protein.score`	
FROM 
	`peptide`
        LEFT JOIN `protein` ON `peptide`.`protein_index`=`protein`.`index`
        LEFT JOIN `query_mass` ON `peptide`.`query_mass_index`=`query_mass`.`index`
        LEFT JOIN `low_energy` ON `query_mass`.`low_energy_index`=`low_energy`.`index`
        LEFT JOIN `mass_spectrum` ON `low_energy`.`index`=`mass_spectrum`.`low_energy_index`
WHERE
	`peptide`.`workflow_index`=1
ORDER BY
	`protein.entry`, `peptide.sequence`