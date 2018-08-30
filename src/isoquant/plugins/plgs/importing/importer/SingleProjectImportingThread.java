/** ISOQuant, isoquant.plugins.plgs, Aug 21, 2018*/
package isoquant.plugins.plgs.importing.importer;

import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;

/**
 * <h3>{@link SingleProjectImportingThread}</h3>
 * @author jkuharev
 * @version Aug 21, 2018 2:46:04 PM
 */
public class SingleProjectImportingThread extends Thread
{
	private DBProject project = null;
	private iMainApp application = null;

	public SingleProjectImportingThread(iMainApp app, DBProject p)
	{
		this.application = app;
		this.project = p;
	}

	@Override public void run()
	{
		if (project == null || project.data.expressionAnalyses.size() < 1)
		{
			System.err.println( "nothing to import." );
				return;
			}
		application.getProcessProgressListener().startProgress();
			System.out.println( "importing project: " + project.data.title );
		try
		{
			PLGSDataImporter pim = new PLGSDataImporter();
			// create database for project
			application.getProjectManager().addProject( project );
			// import project data
			pim.importProject( project, false, false );
			// update list of projects
			application.updateDBProjects();
		}
		catch (Exception e)
		{
			System.err.println( "failed to import project: " + project.data.title );
				e.printStackTrace();
			}
		application.getProcessProgressListener().endProgress();
	}
}
