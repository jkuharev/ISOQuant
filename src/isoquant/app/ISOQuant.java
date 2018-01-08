package isoquant.app;

import java.awt.Component;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JOptionPane;

import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.ui.iProcessProgressListener;
import de.mz.jk.jsix.utilities.ResourceLoader;
import de.mz.jk.jsix.utilities.Settings;
import de.mz.jk.jsix.utilities.XLookAndFeelChanger;
import isoquant.gui.SplitListGUI;
import isoquant.interfaces.iGUI;
import isoquant.interfaces.iMainApp;
import isoquant.interfaces.iProjectManager;
import isoquant.interfaces.plugin.iPluginManager;
import isoquant.interfaces.plugin.iProjectProcessingPlugin;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.db.ProjectManager;
import isoquant.kernel.plugin.Plugin;
import isoquant.kernel.plugin.management.PluginManager;

/**
 * ISOQuant main application
 * 
 * Do not forget to use command line options: <b>-Xms64m -Xmx1024m</b>
 * for increasing usable memory size. Use higher Xmx values for complex datasets like 4096<br>
 * e.g.<br>
 * <b>java -Xms64m -Xmx1024m {@link isoquant.app.ISOQuant}</b>
 * 
 * @author Joerg Kuharev
 * @version 22.12.2010 15:43:03
 */
public class ISOQuant implements iMainApp
{
	public static final long serialVersionUID = 201505271550L;
	private Settings config = null;

	/* 
	 * remember, the only valid measurement of code quality:	[WTFs/minute]
	 */
	/**
	 * do not forget java vm command line options:
	 * -Xms64m -Xmx4096m
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
		new ISOQuant();
	}
	private iGUI gui = null;
	private iPluginManager pluginManager = null;
	private iProjectManager projectManager = null;
	private boolean firstRunDetected = false;

	public ISOQuant()
	{
		config = Defaults.config;
		String laf = config.getStringValue("setup.ui.lookAndFeel", "NATIVE", true);
		XLookAndFeelChanger.changeLookAndFeelByName(laf, false);
		initApp(new SplitListGUI(this));
	}

	public ISOQuant(iGUI gui)
	{
		initApp(gui);
	}

	private void initApp(iGUI gui)
	{
		this.gui = gui;
		this.gui.getGUIComponent().setVisible(true);
		this.setPluginManager(new PluginManager(this));
		initPlugins(pluginManager.getPlugins());
	}

	public void initPlugins(List<Plugin> pluginList)
	{
		if (pluginManager == null)
		{
			setPluginManager(new PluginManager(this, null));
			for (Plugin p : pluginList)
			{
				getPluginManager().addPlugin(p);
			}
		}
		JMenu fsProcessingSubMenu = new JMenu("manual processing");
		JMenu dbProcessingSubMenu = new JMenu("manual processing");
		fsProcessingSubMenu.setIcon(ResourceLoader.getIcon("manual_processing"));
		dbProcessingSubMenu.setIcon(ResourceLoader.getIcon("manual_processing"));
		boolean showDBSubmenu = false;
		boolean showFSSubmenu = false;
		for (Plugin p : pluginList)
		{
			// in case of fire
			if (p == null) continue;
			try
			{
				if (p.getToolBarComponents() != null)
					for (Component c : p.getToolBarComponents())
					{
						gui.addToolBarComponent(c);
					}
			}
			catch (Exception e)
			{
				System.err.println("'" + p.getPluginName() + "': initialization of tool bar control elements failed!");
				e.printStackTrace();
			}
			try
			{
				if (p.getDBMenuComponents() != null)
					for (Component c : p.getDBMenuComponents())
					{
						if (p instanceof iProjectProcessingPlugin && p.getExecutionOrder() > 0)
						{
							showDBSubmenu = true;
							dbProcessingSubMenu.add(c);
						}
						else
						{
							gui.addDBMenuComponent(c);
						}
					}
			}
			catch (Exception e)
			{
				System.err.println("'" + p.getPluginName() + "': initialization of db side control elements failed!");
				e.printStackTrace();
			}
			try
			{
				if (p.getFSMenuComponents() != null)
					for (Component c : p.getFSMenuComponents())
					{
						if (p instanceof iProjectProcessingPlugin && p.getExecutionOrder() > 0)
						{
							showFSSubmenu = true;
							fsProcessingSubMenu.add(c);
						}
						else
						{
							gui.addFSMenuComponent(c);
						}
					}
			}
			catch (Exception e)
			{
				System.err.println("'" + p.getPluginName() + "': initialization of fs side control elements failed!");
				e.printStackTrace();
			}
		}
		if (Defaults.DEBUG || Defaults.TEST)
		{
			if (showDBSubmenu)
			{
				gui.addDBMenuComponent(dbProcessingSubMenu);
			}
			if (showFSSubmenu)
			{
				gui.addFSMenuComponent(fsProcessingSubMenu);
			}
		}
	}

	@Override public void setPluginManager(iPluginManager pluginManager)
	{
		this.pluginManager = pluginManager;
	}

	@Override public iPluginManager getPluginManager()
	{
		return pluginManager;
	}

	@Override public void setDBProjects(List<DBProject> projects)
	{
		gui.setDBProjects(projects);
	}

	@Override public void setFSProjects(List<DBProject> projects)
	{
		gui.setFSProjects(projects);
	}

	@Override public List<DBProject> getDBProjects()
	{
		return gui.getDBProjects();
	}

	@Override public List<DBProject> getFSProjects()
	{
		return gui.getFSProjects();
	}

	@Override public List<DBProject> getSelectedDBProjects()
	{
		return gui.getSelectedDBProjects();
	}

	@Override public List<DBProject> getSelectedFSProjects()
	{
		return gui.getSelectedFSProjects();
	}

	@Override public void selectDBProjects(List<DBProject> projects)
	{
		gui.selectDBProjects(projects);
	}

	@Override public void selectFSProjects(List<DBProject> projects)
	{
		gui.selectFSProjects(projects);
	}

	@Override public void updateFSProjects()
	{}

	@Override public void updateDBProjects()
	{
		getProjectManager().onDBExternallyChangedAction();
		List<DBProject> prjs = projectManager.getProjects();
		System.out.println(" loading " + prjs.size() + " projects from database  ...");
		gui.setDBProjects(prjs);
	}

	@Override public Component getGUI()
	{
		return (gui != null) ? gui.getGUIComponent() : null;
	}

	@Override public MySQL getManagementDB()
	{
		return (projectManager != null) ? projectManager.getManagementDB() : null;
	}

	@Override public void setManagementDB(MySQL db)
	{
		if (projectManager == null)
		{
			projectManager = new ProjectManager(db);
		}
		else if (!projectManager.getManagementDB().equals(db))
		{
			projectManager.setManagementDB(db);
		}
	}

	@Override public Settings getSettings()
	{
		return config;
	}

	@Override public iProjectManager getProjectManager()
	{
		return this.projectManager;
	}

	@Override public iProcessProgressListener getProcessProgressListener()
	{
		return gui.getProcessProgressListener();
	}

	@Override public void setMessage(String text)
	{
		gui.setStatusMessage(text);
	}

	@Override public void showErrorMessage(String msg)
	{
		JOptionPane.showMessageDialog(getGUI(), msg, "error", JOptionPane.ERROR_MESSAGE);
	}

	@Override public void shutdown(boolean waitForEnd)
	{
		List<Plugin> ps = getPluginManager().getPlugins();
		for (Plugin p : ps)
		{
			try
			{
				if (p != null) p.shutdown(waitForEnd);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		try
		{
			getManagementDB().closeConnection();
		}
		catch (Exception e)
		{}
		System.exit(0);
	}
}
