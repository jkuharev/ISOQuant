/** ISOQuant, isoquant.plugins.importing, 04.08.2011 */
package isoquant.plugins.importing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import de.mz.jk.jsix.libs.XFiles;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.utilities.Bencher;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.plugin.ToolBarPlugin;

/**
 * <h3>{@link ProjectFromFileImporter}</h3>
 * @author Joerg Kuharev
 * @version 04.08.2011 16:03:26
 */
public class ProjectFromFileImporter extends ToolBarPlugin
{
	private File outDir = new File(iMainApp.appDir);

	public ProjectFromFileImporter(iMainApp app)
	{
		super(app);
	}

	public void loadSettings()
	{
		outDir = new File(app.getSettings().getStringValue("setup.report.dir", iMainApp.appDir.replace("\\\\", "/"), false));
	}

	@Override public String getIconName()
	{
		return "import";
	}

	@Override public int getExecutionOrder()
	{
		return 256;
	}

	@Override public String getPluginName()
	{
		return "ISOQuant DBProject from File Importer";
	}

	@Override public void runPluginAction() throws Exception
	{
		if (app.getManagementDB() == null)
		{
			app.showErrorMessage("please connect to a database then try again.");
			return;
		}
		MySQL db = app.getManagementDB().clone();
		db.getConnection();
		loadSettings();
		File file = XFiles.chooseFile("choose an ISOQuant project database file", false, null, outDir, "iqdb", app.getGUI());
		if (file == null) return;
		Bencher t = new Bencher(true);
		System.out.println("importing ISOQuant DBProject from file '" + file.getAbsolutePath() + "' ...");
		System.out.println("please be patient, importing process may take a while and depends on your database performance ...");
		BufferedReader in = new BufferedReader(new FileReader(file));
		String line = "";
		String sql = "";
		while ((line = in.readLine()) != null)
		{
			// ignore comments
			if (line.startsWith("--"))
			{
				System.out.println("\t" + line.substring(3));
			}
			else
			{
				sql += "\n" + line;
				if (line.trim().endsWith(";"))
				{
					db.executeSQL(sql);
					sql = "";
				}
			}
		}
		in.close();
		System.out.println("project restoration completed [" + t.stop().getSecString() + "]");
		app.updateDBProjects();
	}
}
