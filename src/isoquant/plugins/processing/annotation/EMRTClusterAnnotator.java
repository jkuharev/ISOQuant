package isoquant.plugins.processing.annotation;

import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.jsix.utilities.Settings;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.log.LogEntry;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

/**
 * <h3>{@link EMRTClusterAnnotator}</h3>
 * assign peptides to emrt clusters
 * @author Joerg Kuharev
 * @version 20.01.2011 12:59:09
 */
public class EMRTClusterAnnotator extends SingleActionPlugin4DB
{
	private AnnotationFilter af = null;
	private AnnotationMode annotationMode = AnnotationMode.all;

	public EMRTClusterAnnotator(iMainApp app)
	{
		super(app);
		af = new AnnotationFilter();
		af.loadSettings(app.getSettings());
	}

	@Override public void loadSettings(Settings cfg)
	{
		String m = cfg.getStringValue( "process.annotation.useSharedPeptides", annotationMode.toString(), false );
		annotationMode = AnnotationMode.fromString(m);
		if (!m.equals( annotationMode.toString() ))
		{
			cfg.setValue("process.annotation.useSharedPeptides", annotationMode.toString());
		}
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		MySQL db = p.mysql;
		Bencher t = new Bencher().start();
		progressListener.setStatus( "annotating emrt for '" + p.data.title + "'" );
		af.loadSettings(app.getSettings());
		af.checkReplicationRates(db);
		af.logParameters(p.log);
		p.log.add(LogEntry.newParameter("process.annotation.useSharedPeptides", annotationMode.toString()));

		// apply peptide identification filter
		db.executeSQLFile( getPackageResource( "01_peptide_identification_filter.sql" ), af );

		// relate filtered peptides to proteins
		db.executeSQLFile( getPackageResource( "02_peptides_in_proteins.sql" ), af );

		// annotate EMRT clusters
		db.executeSQLFile(getPackageResource("03_best_peptides.sql"), af);

		// filter peptides by FPR
		db.executeSQLFile(getPackageResource("04_peptide_fpr.sql"), af);

		// do homology/isoform filtering
		applyProteinHomologyFilter( db );

		// calculate protein sequence coverage
		db.executeSQLFile( getPackageResource( "08_protein_sequence_coverage.sql" ), af );

		// make peptides in proteins statistics
		db.executeSQLFile( getPackageResource( "09_peptides_in_proteins_stats.sql" ), af );

		progressListener.setStatus("emrt annotation done");
		System.out.println("cluster annotation duration: " + t.stop().getSecString());
		p.log.add(LogEntry.newEvent("emrt cluster annotated", "cluster annotation duration=" + t.getSecString()));
	}

	/**
	 * @param db
	 */
	private void applyProteinHomologyFilter(MySQL db)
	{
		// pre homology filtering action
		if (annotationMode.equals( AnnotationMode.unique ))
		{
			db.executeSQLFile( getPackageResource( "05_use_unique_peptides_only.sql" ), af );
		}

		// apply or fake homology filtering
		if (af.useHomologyFilter && !annotationMode.equals( AnnotationMode.unique ))
		{
			db.executeSQLFile( getPackageResource( "06a_01_homology_filtering.sql" ), af );
			db.executeSQLFile( getPackageResource( "06a_02_homology_rooting.sql" ), af );
		}
		else
		{
			db.executeSQLFile( getPackageResource( "06b_fake_homology.sql" ) );
		}

		// post homology filtering action
		if (annotationMode.equals( AnnotationMode.razor ))
		{
			db.executeSQLFile( getPackageResource( "07_use_razor_and_unique_peptides_only.sql" ), af );
		}
	}

	@Override public String getMenuItemIconName()
	{
		return "annotation";
	}

	@Override public String getMenuItemText()
	{
		return "annotate emrt clusters";
	}

	@Override public String getPluginName()
	{
		return "EMRT Cluster by Peptide Annotation Finder";
	}

	@Override public int getExecutionOrder()
	{
		return 20;
	}
}
