-- @ :	merging cluster information ...
SELECT
-- emrt
	cluster_average_index as `cluster`,
	emrt.avg_mass, 
	emrt.avg_rt, 
	emrt.avg_ref_rt,
	emrt.avg_drift_time,
	emrt.intensities,
-- peptide
	pept.workflow_index,
	pept.sequence,
	pept.modifier,
	pept.type,
	pept.start, 
	pept.end, 
	pept.frag_string, 
	pept.products, 
	pept.score as peptide_annotated_max_score, 
	pept.stat_max_score as peptide_overall_max_score,
	pep_fpr.fpr as peptide_fdr_level,
	pept.annotation_rate as peptide_annotated_replication_rate,
	pept.stat_count_workflows as peptide_overall_replication_rate, 	
-- peak
	peak.Mass as signal_mass,
	peak.Intensity as signal_intensity, 
	peak.AverageCharge as signal_charge, 
	peak.Z as signal_z,
	peak.FWHM as signal_fwhm,
	peak.RT as signal_rt,
	peak.LiftOffRT as signal_liftoffrt, 
	peak.InfUpRT as signal_infuprt,
	peak.InfDownRT as signal_infdown_rt, 
	peak.TouchDownRT as signal_touchdownrt,
-- proteins before homology filtering
	preh.entries as pre_homology_entries,
	preh.accessions as pre_homology_accessions,
-- proteins after homology filtering
	posth.entries as post_homology_entries,
	posth.accessions as post_homology_accessions,
-- protein
	prot.entry, 
	prot.accession, 
	prot.description,
	prot.mw, 
	prot.`pi`,
	prot.score as protein_score,
	prot.coverage,
	prot.stat_count_workflows as protein_replication_rate,
	prot.stat_count_peptides as protein_assigned_peptides_rate,
	prot.stat_count_unique_peptides as protein_unique_paptides_rate
FROM
	cluster_info_runs as emrt
	LEFT JOIN cluster_info_peptide as pept USING(cluster_average_index)
	LEFT JOIN cluster_info_peak as peak USING(cluster_average_index)
	LEFT JOIN cluster_info_proteins_before_homology_filtering as preh USING(cluster_average_index)
	LEFT JOIN cluster_info_proteins_after_homology_filtering as posth  USING(cluster_average_index)
	LEFT JOIN cluster_info_proteins as prot USING(cluster_average_index)
	LEFT JOIN pep_fpr USING(sequence, modifier)
ORDER BY 
	`cluster` ASC
;

-- @ :	retrieving data ...
