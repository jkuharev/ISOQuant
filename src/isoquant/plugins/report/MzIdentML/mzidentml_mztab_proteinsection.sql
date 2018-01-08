-- @ :	generating protein quantitation values ...
DROP TABLE IF EXISTS mzidentml_mztab_protein_averages;
CREATE TABLE mzidentml_mztab_protein_averages
(ID INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (id), KEY (entry), KEY (sample_index))
    SELECT
      fq.entry, fq.sample_index,
      AVG(fq.top3_avg_inten) as avg_top3_avg_inten, AVG(fq.rawAmount) as avg_rawAmount, AVG(fq.fmolug) as avg_fmolug, AVG(fq.ppm) as avg_ppm,
      STDDEV(fq.top3_avg_inten) as stdev_top3_avg_inten, STDDEV(fq.rawAmount) as stdev_rawAmount, STDDEV(fq.fmolug) as stdev_fmolug, STDDEV(fq.ppm) as stdev_ppm,
      STDDEV(fq.top3_avg_inten)/SQRT(COUNT(*)) as sem_top3_avg_inten, STDDEV(fq.rawAmount)/SQRT(COUNT(*)) as sem_rawAmount, STDDEV(fq.fmolug)/SQRT(COUNT(*)) as sem_fmolug, STDDEV(fq.ppm)/SQRT(COUNT(*)) as sem_ppm
    FROM finalquant_extended fq
    GROUP BY fq.entry, fq.sample_index;

DROP TABLE IF EXISTS mzidentml_mztab_protein_section;
CREATE TABLE mzidentml_mztab_protein_section
(ID INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (id), KEY (entry), KEY (sample_index), KEY(workflow_index))
    SELECT
      fq.entry, fq.sample_index, fq.workflow_index, fq.top3_avg_inten, fq.rawAmount, fq.fmolug, fq.ppm,
      mzid_pr.unique_peptides, mzid_pr.razor_peptides, mzid_pr.shared_peptides, mzid_pr.num_peptides,
      pr_i.accession, pr_i.description, pr_i.coverage, pr_i.score as best_score,
      pr.score,
      pravg.avg_top3_avg_inten, pravg.avg_rawAmount, pravg.avg_fmolug, pravg.avg_ppm,
      pravg.stdev_top3_avg_inten, pravg.stdev_rawAmount, pravg.stdev_fmolug, pravg.stdev_ppm,
      pravg.sem_top3_avg_inten, pravg.sem_rawAmount, pravg.sem_fmolug, pravg.sem_ppm
    FROM finalquant_extended fq
      JOIN mzidentml_proteinambiguitygroup mzid_pr
      USING (entry)
      JOIN protein_info pr_i
      USING (entry)
      LEFT JOIN protein pr
      USING (entry, workflow_index)
      LEFT JOIN mzidentml_mztab_protein_averages pravg
      USING (entry, sample_index)
    ORDER BY fq.entry, fq.sample_index, fq.workflow_index;
