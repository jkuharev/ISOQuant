package isoquant.interfaces;

import isoquant.interfaces.plugin.iPluginManager;

import java.awt.Component;

import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.ui.iProcessProgressListener;
import de.mz.jk.jsix.utilities.Settings;

public interface iMainApp extends iDualProjectListManager
{
	public final String appDir = System.getProperty("user.dir");

	// public final Settings config = new Settings("isoquant.ini",
// "ISOQuant configuration");
	public void setMessage(String text);

	public void setManagementDB(MySQL db);

	public MySQL getManagementDB();

	public iProjectManager getProjectManager();

	public void setPluginManager(iPluginManager pluginManager);

	public iPluginManager getPluginManager();

	public Component getGUI();

	public iProcessProgressListener getProcessProgressListener();

	public Settings getSettings();

	public void showErrorMessage(String msg);

	public void shutdown(boolean waitForEnd);
}
