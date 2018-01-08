/** ISOQuant, isoquant.plugins.processing.annotation, 15.11.2013*/
package isoquant.plugins.processing.annotation;

import isoquant.app.Defaults;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.utilities.ResourceLoader;

/**
 * <h3>{@link TestHomology}</h3>
 * @author kuharev
 * @version 15.11.2013 13:33:44
 */
public class TestHomology
{
	/**
	 * 
	 */
	public TestHomology()
	{}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		MySQL db = new MySQL( "127.0.0.1:3307", "Proj__13836055683500_48613684007494473_100_2", "root", "", true );
		AnnotationFilter af = new AnnotationFilter();
		af.loadSettings( Defaults.config );
		af.checkReplicationRates( db );

		if (false)
		{
			// apply peptide identification filter
			db.executeSQLFile( getPackageResource( "01_peptide_identification_filter.sql" ), af );
			// relate filtered peptides to proteins
			db.executeSQLFile( getPackageResource( "02_peptides_in_proteins.sql" ), af );
			// annotate EMRT clusters
			db.executeSQLFile( getPackageResource( "03_best_peptides.sql" ), af );
			// filter peptides by FPR
			db.executeSQLFile( getPackageResource( "04_peptide_fpr.sql" ), af );
		}

		// do homology/isoform filtering
		if (true) db.executeSQLFile( getPackageResource( "06a_01_homology_filtering.sql" ), af );

// db.executeSQLFile( getPackageResource( "06a_02_homology_rooting.sql" ), af );

// // calculate protein sequence coverage
// db.executeSQLFile( getPackageResource( "08_protein_sequence_coverage.sql" ), af );
//
// // make peptides in proteins statistics
// db.executeSQLFile( getPackageResource( "09_peptides_in_proteins_stats.sql" ), af );

		db.closeConnection();
	}

	/**
	 * @param fileName
	 * @return
	 */
	private static String getPackageResource(String fileName)
	{
		return ResourceLoader.getParentPath( TestHomology.class ) + fileName;
	}
}
