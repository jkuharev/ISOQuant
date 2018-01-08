/** ISOQuant, isoquant.plugins.report, 07.07.2011*/
package isoquant.kernel.plugin;

import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;

import java.io.File;

import de.mz.jk.jsix.libs.XFiles;

/**
 * <h3>{@link SingleActionPlugin4DBExportToFolder}</h3>
 * @author Joerg Kuharev
 * @version 07.07.2011 14:13:36
 */
public abstract class SingleActionPlugin4DBExportToFolder extends SingleActionPlugin4DB
{
	private File outDir=null;
	
	public SingleActionPlugin4DBExportToFolder(iMainApp app)
	{
		super(app);
		outDir = new File( app.getSettings().getStringValue("setup.report.dir", iMainApp.appDir.replace("\\\\", "/"), false) );
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		outDir = new File( app.getSettings().getStringValue("setup.report.dir", iMainApp.appDir.replace("\\\\", "/"), false) );
		
		System.out.println( "starting folder selection in " + outDir );
		File dir = XFiles.chooseFolder( getFolderSelectionDialogTitle(), outDir, app.getGUI() );
		if(dir==null) 
		{
			app.showErrorMessage("selection of target folder cancelled");
			return;
		}
		outDir = dir;
		app.getSettings().setValue("setup.report.dir", outDir.getAbsolutePath().replace("\\\\", "/") );
		
		runExportAction(p, outDir);
	}
	
	public abstract String getFolderSelectionDialogTitle();
	public abstract void runExportAction(DBProject prj, File tarDir);
}
