/** ISOQuant, isoquant.plugins.configuration, 10.07.2013*/
package isoquant.plugins.configuration;

import javax.swing.JOptionPane;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.utilities.Settings;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

/**
 * <h3>{@link AttachConfigParams}</h3>
 * @author kuharev
 * @version 10.07.2013 09:29:52
 */
public class AttachConfigParams extends SingleActionPlugin4DB
{
	public AttachConfigParams(iMainApp app)
	{
		super(app);
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		MySQL db = p.mysql;
		Settings iqCfg = app.getSettings();
		if (db.tableExists("config_storage"))
		{
			int ok = JOptionPane.showConfirmDialog(app.getGUI(),
					"The project you have selected already contains attached configuration.\nAre you sure you want to overwrite it by current settings?",
					"overwrite attached configuration?",
					JOptionPane.WARNING_MESSAGE,
					JOptionPane.YES_NO_OPTION
					);
			if (ok != JOptionPane.YES_OPTION) return;
		}
		// init storage table
		db.executeSQLFile(getPackageResource("init_config_storage.sql"));
		System.out.println("writing configuration parameters to project db ...");
		String[][] kv = iqCfg.toKVArray();
		app.getProcessProgressListener().setProgressMaxValue(kv.length);
		for (int i = 0; i < kv.length; i++)
		{
			db.executeSQL("INSERT INTO config_storage (k,v) VALUES ('" + kv[i][0] + "','" + XJava.encURL(kv[i][1]) + "')");
			app.getProcessProgressListener().setProgressValue(kv.length - i);
		}
		app.getProcessProgressListener().setProgressValue(0);
	}

	@Override public String getMenuItemText()
	{
		return "attach config to project db";
	}

	@Override public String getMenuItemIconName()
	{
		return "config_to_db";
	}
}
