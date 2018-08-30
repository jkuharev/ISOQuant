/** ISOQuant, isoquant.plugins.plgs.importing.importer, Aug 30, 2018*/
package isoquant.plugins.plgs.importing.importer;

import java.util.List;

import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;

/**
 * <h3>{@link MultiProjectImportingThread}</h3>
 * @author jkuharev
 * @version Aug 30, 2018 2:42:53 PM
 */
public class MultiProjectImportingThread extends Thread
{
	public MultiProjectImportingThread(iMainApp app, List<DBProject> projects)
	{
		this.application = app;
		this.projects = projects;
	}
	private List<DBProject> projects = null;
	private iMainApp application = null;

	@Override public void run()
	{
		for ( DBProject p : projects )
		{
			SingleProjectImportingThread t = new SingleProjectImportingThread( application, p );
			t.run();
		}
	}
}
