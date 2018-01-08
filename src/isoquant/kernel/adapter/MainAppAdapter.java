/** ISOQuant_1.0, isoquant.kernel.adapter, 11.02.2011 */
package isoquant.kernel.adapter;

import isoquant.interfaces.iMainApp;
import isoquant.interfaces.iProjectManager;
import isoquant.interfaces.plugin.iPluginManager;
import isoquant.kernel.db.DBProject;

import java.awt.Component;
import java.util.List;

import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.ui.iProcessProgressListener;
import de.mz.jk.jsix.utilities.Settings;

/**
 * <h3>{@link MainAppAdapter}</h3>
 * empty iMainApp implementation,
 * all functions do nothing and return null
 * @author Joerg Kuharev
 * @version 11.02.2011 09:11:45
 */
public class MainAppAdapter implements iMainApp
{
	@Override public Component getGUI()
	{
		return null;
	}

	@Override public MySQL getManagementDB()
	{
		return null;
	}

	@Override public iProcessProgressListener getProcessProgressListener()
	{
		return null;
	}

	@Override public iProjectManager getProjectManager()
	{
		return null;
	}

	@Override public Settings getSettings()
	{
		return null;
	}

	@Override public void setManagementDB(MySQL db)
	{}

	@Override public void setMessage(String text)
	{}

	@Override public List<DBProject> getDBProjects()
	{
		return null;
	}

	@Override public List<DBProject> getFSProjects()
	{
		return null;
	}

	@Override public List<DBProject> getSelectedDBProjects()
	{
		return null;
	}

	@Override public List<DBProject> getSelectedFSProjects()
	{
		return null;
	}

	@Override public void selectDBProjects(List<DBProject> projects)
	{}

	@Override public void selectFSProjects(List<DBProject> projects)
	{}

	@Override public void setDBProjects(List<DBProject> projects)
	{}

	@Override public void setFSProjects(List<DBProject> projects)
	{}

	@Override public void updateDBProjects()
	{}

	@Override public void updateFSProjects()
	{}

	@Override public void showErrorMessage(String msg)
	{}

	@Override public void setPluginManager(iPluginManager pluginManager)
	{}

	@Override public iPluginManager getPluginManager()
	{
		return null;
	}

	@Override public void shutdown(boolean waitForEnd)
	{}
}
