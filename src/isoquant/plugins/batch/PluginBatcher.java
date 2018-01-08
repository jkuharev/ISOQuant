package isoquant.plugins.batch;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.ui.iProcessProgressListener;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.jsix.utilities.Settings;
import isoquant.interfaces.iMainApp;
import isoquant.interfaces.iProjectManager;
import isoquant.interfaces.plugin.iPluginManager;
import isoquant.interfaces.plugin.iProjectImportingPlugin;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.log.LogEntry;
import isoquant.kernel.plugin.Plugin;
import isoquant.kernel.plugin.queue.PluginQueue;

/**
 * <h3>{@link PluginBatcher}</h3>
 * @author Joerg Kuharev
 * @version 02.02.2011 09:58:33
 */
public abstract class PluginBatcher extends Plugin implements iMainApp, ActionListener
{
	protected JMenu menu = new JMenu();
	protected List<PluginQueue> queues = new ArrayList<PluginQueue>();
	protected List<JMenuItem> queueMenuItems = new ArrayList<JMenuItem>();
	protected PluginQueue currentQueue = null;
	protected DBProject currentProject = null;

	public PluginBatcher(iMainApp app)
	{
		super(app);
		menu.setText(getMenuText());
		menu.setIcon(getPluginIcon());
		initQueues();
	}

	protected abstract String getMenuText();

	private void initQueues()
	{
		queues = getQueues();
		for (PluginQueue pq : queues)
		{
			JMenuItem mi = new JMenuItem(pq.getName());
			mi.setIcon(pq.getIcon());
			mi.addActionListener(this);
			queueMenuItems.add(mi);
			menu.add(mi);
		}
	}

	protected abstract List<PluginQueue> getQueues();

	@Override public List<Component> getToolBarComponents()
	{
		return null;
	}

	@Override public void actionPerformed(ActionEvent e)
	{
		Object src = e.getSource();
		for (int i = 0; i < queueMenuItems.size(); i++)
		{
			JMenuItem qmi = queueMenuItems.get(i);
			if (src.equals(qmi))
			{
				currentQueue = queues.get(i);
				this.runThread();
				break;
			}
		}
	}

	public void runBatch(PluginQueue queue, List<DBProject> prjs)
	{
		Bencher qt = new Bencher().start();
		progressListener.startProgress("running queue: " + queue.getName());
		// reject zero selection
		if (prjs == null || prjs.size() == 0) return;
		// init plugins
		queue.revivePlugins(this);
		Plugin[] plugins = queue.getPlugins();
		System.out.println("\tqueue '" + queue.getName() + "' will run following plugins:");
		for (Plugin p : plugins)
		{
			System.out.println("\t\t" + p.getPluginName());
		}
		System.out.println();
		//
		// reject empty queue
		if (plugins.length < 1) return;
		// preselect all projects
		List<DBProject> selPrjs = prjs;
		// import projects if first plugin is an importer
		if (plugins[0] instanceof iProjectImportingPlugin)
		{
			// initiate selection
			iProjectImportingPlugin ip = (iProjectImportingPlugin) plugins[0];
			ip.importProjects(app.getSelectedFSProjects());
			// wait until projects are imported and get them
			selPrjs = ip.getImportedProjects();
			// reject if no projects to process selected
			if (selPrjs == null || selPrjs.size() == 0) return;
		}
		// run DB Batch for the List of imported Projects
		for (DBProject prj : selPrjs)
		{
			Bencher pt = new Bencher().start();
			System.out.print("optimizing schema: " + prj + " ... ");
			prj.mysql.optimizeAllTables();
			System.out.println("[" + pt.stop().getSecString() + "]");
			prj.log.add(LogEntry.newParameter("isoquant.pluginQueue.name", queue.getName()));
			Thread precursorThread = null;
			currentProject = prj;
			for (int i = (plugins[0] instanceof iProjectImportingPlugin) ? 1 : 0; i < plugins.length; i++)
			{
				try
				{
					// start after precursor termination and redefine precursor
					precursorThread = plugins[i].runAfterThread(precursorThread);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			// wait for ending this queue/project-tuple
			try
			{
				precursorThread.join();
			}
			catch (Exception e)
			{}
			System.out.println("queued project '" + prj + "' processing duration: " + pt.stop().getSecString());
		}
		System.out.println("cumulated queue execution duration: " + qt.stop().getSecString());
		progressListener.endProgress("queue finished: " + queue.getName());
	}

/* --------------- iMainApp --------------- */
	@Override public List<DBProject> getDBProjects()
	{
		return app.getDBProjects();
	}

	@Override public List<DBProject> getFSProjects()
	{
		return app.getFSProjects();
	}

	@Override public Component getGUI()
	{
		return app.getGUI();
	}

	@Override public MySQL getManagementDB()
	{
		return app.getManagementDB();
	}

	@Override public iProcessProgressListener getProcessProgressListener()
	{
		return app.getProcessProgressListener();
	}

	@Override public iProjectManager getProjectManager()
	{
		return app.getProjectManager();
	}

	@Override public List<DBProject> getSelectedDBProjects()
	{
		return Collections.singletonList(currentProject);
	}

	@Override public List<DBProject> getSelectedFSProjects()
	{
		return Collections.singletonList(currentProject);
	}

	@Override public Settings getSettings()
	{
		return app.getSettings();
	}

	@Override public void setDBProjects(List<DBProject> projects)
	{}

	@Override public void setFSProjects(List<DBProject> projects)
	{}

	@Override public void setManagementDB(MySQL db)
	{}

	@Override public void setMessage(String text)
	{
		app.setMessage(text);
	}

	@Override public void updateDBProjects()
	{
		app.updateDBProjects();
	}

	@Override public void updateFSProjects()
	{}

	@Override public void selectDBProjects(List<DBProject> projects)
	{}

	@Override public void selectFSProjects(List<DBProject> projects)
	{}

	@Override public void showErrorMessage(String msg)
	{
		app.showErrorMessage(msg);
	}

	@Override public void setPluginManager(iPluginManager pluginManager)
	{}

	@Override public iPluginManager getPluginManager()
	{
		return null;
	}

	@Override public int getExecutionOrder()
	{
		return 0;
	}
}
