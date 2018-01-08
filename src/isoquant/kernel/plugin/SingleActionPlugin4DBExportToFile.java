/** ISOQuant, isoquant.kernel.plugin, 18.12.2012 */
package isoquant.kernel.plugin;

import java.io.File;

import de.mz.jk.jsix.libs.XFiles;
import isoquant.app.Defaults;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;

/**
 * abstract plugin base for exporting data into a file
 * 
 * <h3>{@link SingleActionPlugin4DBExportToFile}</h3>
 * @author kuharev
 * @version 18.12.2012 13:43:29
 */
public abstract class SingleActionPlugin4DBExportToFile extends SingleActionPlugin4DB
{
	protected File outDir = null;

	public SingleActionPlugin4DBExportToFile(iMainApp app)
	{
		super(app);
		outDir = new File( app.getSettings().getStringValue( "setup.report.dir", iMainApp.appDir.replace( "\\\\", "/" ), false ) );
	}
	protected boolean silentMode = false;
	protected int numberOfSelectedProjects = 0;

	@Override public void runPluginAction() throws Exception
	{
		numberOfSelectedProjects = app.getSelectedDBProjects().size();
		silentMode =
				numberOfSelectedProjects < 2
						? false
						: app.getSettings().getBooleanValue("setup.report.silentMode", false, !Defaults.DEBUG);
		if (silentMode)
		{
			File dir = XFiles.chooseFolder("choose folder for batch output of '" + getPluginName() + "'", outDir, app.getGUI());
			if (dir == null) return;
			if (!dir.exists() || !dir.isDirectory()) dir = dir.getParentFile();
			outDir = dir;
			app.getSettings().setValue("setup.report.dir", outDir.getAbsolutePath().replace("\\\\", "/"));
		}
		super.runPluginAction();
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		if (!silentMode)
			outDir = new File(
					app.getSettings().getStringValue(
							"setup.report.dir",
							iMainApp.appDir.replace("\\\\", "/"),
							false
							)
					);
		try
		{
			File hintFile = new File(outDir, getHintFileName(p));
			File file = silentMode
					? hintFile
					: XFiles.chooseFile(getFileChooserTitle(), true, hintFile, outDir, getFileExtensions(), app.getGUI());
			if (file == null) return;
			if (file.exists() && (silentMode || !XFiles.overwriteFileDialog(app.getGUI(), getPluginIcon()))) return;
			File dir = file.getParentFile();
			if (!dir.getAbsolutePath().equals(outDir.getAbsolutePath()))
				app.getSettings().setValue("setup.report.dir", dir.getAbsolutePath().replace("\\\\", "/"));
			runExportAction(p, file);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/** 
	 * default hint file is build from project title 
	 * by replacing all non word characters with underscore
	 * followed by user defined suffix
	 * and appending first available file extension
	 * @param p the project object
	 * @return file name
	 */
	public String getHintFileName(DBProject p)
	{
		return ( p.data.title + "_" + p.data.info ).replace( "\\W", "_" ) +
				getHintFileNameSuffix() + "." + getFileExtensions()[0].replaceAll(";.*", "");
	}

	/** user defined suffix to be used in hint file name */
	public String getHintFileNameSuffix()
	{
		return "";
	}

	public abstract String getFileChooserTitle();

	public abstract String[] getFileExtensions();

	public abstract void runExportAction(DBProject prj, File tarFile) throws Exception;

	@Override public int getExecutionOrder()
	{
		return 0;
	}
}
