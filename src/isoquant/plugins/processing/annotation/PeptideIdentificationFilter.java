/** ISOQuant, isoquant.plugins.processing.annotation, 09.08.2013 */
package isoquant.plugins.processing.annotation;

import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.utilities.Bencher;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

/**
 * <h3>{@link PeptideIdentificationFilter}</h3>
 * @author kuharev
 * @version 09.08.2013 12:40:52
 */
public class PeptideIdentificationFilter extends SingleActionPlugin4DB
{
	private AnnotationFilter af = null;

	public PeptideIdentificationFilter(iMainApp app)
	{
		super(app);
		af = new AnnotationFilter();
		af.loadSettings(app.getSettings());
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		MySQL db = p.mysql;
		Bencher t = new Bencher().start();
		progressListener.setStatus( "annotating emrt for '" + p.data.title + "'" );
		af.loadSettings(app.getSettings());
		af.checkReplicationRates(db);
		af.logParameters(p.log);
		db.executeSQLFile( getPackageResource( "01_peptide_identification_filter.sql" ), af );
		System.out.println("peptide identification filter applied [" + t.stop().getSecString() + "]");
	}

	@Override public String getMenuItemText()
	{
		return "Peptide Identification Filter";
	}

	@Override public String getMenuItemIconName()
	{
		return "filter";
	}
}
