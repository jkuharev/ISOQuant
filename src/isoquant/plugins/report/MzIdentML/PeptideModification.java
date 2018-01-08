/** ISOQuant, isoquant.plugins.report.MzIdentML, Jun 12, 2015*/
package isoquant.plugins.report.MzIdentML;

/**
 * <h3>{@link PeptideModification}</h3>
 * @author kuharev
 * @version Jun 12, 2015 11:08:06 AM
 */
public class PeptideModification
{
	private String plgs_first_word_name;
	private Double monoisotopicMassDelta;
	private String unimod_description;
	private String unimod_code_name;
	private String composition;
	private String unimod_Id;

	public String getPlgs_first_word_name()
	{
		return plgs_first_word_name;
	}

	public Double getMonoisotopicMassDelta()
	{
		return monoisotopicMassDelta;
	}

	public String getUnimod_description()
	{
		return unimod_description;
	}

	public String getUnimod_code_name()
	{
		return unimod_code_name;
	}

	public String getComposition()
	{
		return composition;
	}

	public String getUnimod_Id()
	{
		return unimod_Id;
	}

	public PeptideModification(String firstWordOfPLGSName, Double monoisotopicMassDelta, String unimodDescription,
		String unimodCodeName, String composition, String unimodId)
	{
		this.plgs_first_word_name = firstWordOfPLGSName;
		this.monoisotopicMassDelta = monoisotopicMassDelta;
		this.unimod_description = unimodDescription;
		this.unimod_code_name = unimodCodeName;
		this.composition = composition;
		this.unimod_Id = unimodId;
	}
}
