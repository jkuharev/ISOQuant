/** ISOQuant, isoquant.plugins.plgs.importing.design, 12.04.2011 */
package isoquant.plugins.plgs.importing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import de.mz.jk.jsix.ui.JXButton;
import de.mz.jk.jsix.utilities.ResourceLoader;
import isoquant.interfaces.iMainApp;
import isoquant.interfaces.plugin.iProjectImportingPlugin;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4FS;
import isoquant.plugins.configuration.ConfigurationEditor;
import isoquant.plugins.plgs.importing.design.ProjectDesignPanel;
import isoquant.plugins.plgs.importing.importer.PLGSDataImporter;

/**
 * <h3>{@link ProjectImporterByUserDesignMulti}</h3>
 * @author Joerg Kuharev
 * @version 12.04.2011 16:17:16
 */
public class ProjectImporterByUserDesignMulti extends SingleActionPlugin4FS implements iProjectImportingPlugin
{
	private JDialog dlgWin = null;
	private JPanel mainPane = new JPanel(new BorderLayout());
	private JTabbedPane tabPane = new JTabbedPane();
	private JPanel btnPane = new JPanel();
	private JButton btnOk = new JXButton(ResourceLoader.getIcon("small_play"), "proceed", "accept and proceed");
	private JButton btnCancel = new JXButton(ResourceLoader.getIcon("small_stop"), "cancel", "cancel");
	private JButton btnSettings = new JXButton(ResourceLoader.getIcon("small_options"), "config", "edit configuration");
	private JButton btnMulti = new JXButton(ResourceLoader.getIcon("small_clone"), "split", "create an additional tab from this input project");
	private List<DBProject> originalProjects = null;
	private List<DBProject> selectedProjects = null;
	private Thread thread = null;
	private List<ProjectDesignPanel> designPanelTabs = null;

	public ProjectImporterByUserDesignMulti(iMainApp app)
	{
		super(app);
		initDlg();
	}

	@Override public String getMenuItemIconName()
	{
		return "design_project";
	}

	@Override public String getMenuItemText()
	{
		return "design analysis and import data";
	}

	@Override public int getExecutionOrder()
	{
		return 1;
	}

	@Override public String getPluginName()
	{
		return "ISOQuant DBProject Importer by User Design";
	}

	private void initDlg()
	{
		dlgWin = new JDialog((Frame) app.getGUI(), this.getPluginName(), true);
		dlgWin.setAlwaysOnTop(true);
		dlgWin.setModal(true);
		dlgWin.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		btnPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		btnPane.setLayout(new BorderLayout());
		JPanel pnlSettings = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel pnlOkCancel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pnlSettings.add(btnSettings);
		pnlSettings.add(btnMulti);
		pnlOkCancel.add(btnCancel);
		pnlOkCancel.add(btnOk);
		btnPane.add(pnlSettings, BorderLayout.WEST);
		btnPane.add(pnlOkCancel, BorderLayout.CENTER);
		tabPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		mainPane.add(tabPane, BorderLayout.CENTER);
		mainPane.add(btnPane, BorderLayout.SOUTH);
		btnOk.addActionListener(this);
		btnCancel.addActionListener(this);
		btnSettings.addActionListener(this);
		btnMulti.addActionListener(this);
		dlgWin.setContentPane(mainPane);
		dlgWin.setSize(app.getGUI().getWidth(), app.getGUI().getHeight());
		dlgWin.setLocationRelativeTo(app.getGUI());
	}

	private void runSelection()
	{
		if (app.getProjectManager() == null)
		{
			app.showErrorMessage("please connect to the database before trying to import projects!");
			return;
		}
		app.getProcessProgressListener().startProgress("reading project details");
		selectedProjects = new ArrayList<DBProject>();
		designPanelTabs = new ArrayList<ProjectDesignPanel>();
		// remove old tabs
		while (tabPane.getTabCount() > 0)
		{
			tabPane.remove(0);
		}
		// fill GUI with tabs for each project
		for (int i = 0; i < originalProjects.size(); i++)
		{
			showInTab(originalProjects.get(i));
		}
		dlgWin.setVisible(true);
		// app.getProcessProgressListener().endProgress();
	}

	/**
	 * add GUI Tab for designing this project
	 */
	private void showInTab(DBProject p)
	{
		try
		{
			System.out.println( "reading details for project '" + p.data.title + "' from file system ..." );
			ProjectDesignPanel pdp = new ProjectDesignPanel(p);
			JPanel pnl = new JPanel();
			pnl.setLayout(new BorderLayout());
			pnl.add(pdp, BorderLayout.CENTER);
			pnl.setBorder(BorderFactory.createEtchedBorder());
			pnl.add(
					new JLabel( "<html>use drag and drop and right mouse button to redefine the project <b>'" + p.data.title + "'</b><br></html>" ),
					BorderLayout.NORTH
					);
			tabPane.addTab( p.data.title, pnl );
			designPanelTabs.add(pdp);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void cancelSelection()
	{
		dlgWin.setVisible(false);
		System.out.println("project design cancelled by user interaction.");
		selectedProjects = new ArrayList<DBProject>();
		thread.start();
	}

	private void evaluateSelection()
	{
		for (ProjectDesignPanel tab : designPanelTabs)
		{
			selectedProjects.add(tab.getDesignedProject());
		}
		dlgWin.setVisible(false);
		thread.start();
		System.out.println();
	}

	@Override public void runPluginAction() throws Exception
	{
		for (DBProject p : selectedProjects)
		{
			runPluginAction(p);
		}
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		if (p == null || p.data.expressionAnalyses.size() < 1) return;
		System.out.println( "importing project: " + p.data.title );
		PLGSDataImporter pim = new PLGSDataImporter();
		// create database for project
		app.getProjectManager().addProject(p);
		// import project data
		pim.importProject(p, false, false);
		// update list of projects
		app.updateDBProjects();
	}

	@Override public void actionPerformed(ActionEvent e)
	{
		Object src = e.getSource();
		if (src.equals(menu))
		{
			System.out.println("running project designer ...");
			importProjects(app.getSelectedFSProjects());
		}
		else if (src.equals(btnOk))
		{
			System.out.println("evaluating designed projects ...");
			evaluateSelection();
		}
		else if (src.equals(btnCancel))
		{
			System.out.println("aborting project designing ...");
			cancelSelection();
		}
		else if (src.equals(btnSettings))
		{
			System.out.println("changing settings ...");
			new ConfigurationEditor(app).editModal(dlgWin);
		}
		else if (src.equals(btnMulti))
		{
			// add an additional tab for the same selected project
			DBProject prj2clone = originalProjects.get(tabPane.getSelectedIndex());
			originalProjects.add(prj2clone);
			showInTab(prj2clone);
		}
	}

	@Override public List<DBProject> getImportedProjects()
	{
		try
		{
			thread.join();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return selectedProjects;
	}

	// init project list by user selected projects
	@Override public void importProjects(List<DBProject> projects)
	{
		originalProjects = projects;
		thread = new Thread(this);
		runSelection();
	}
}
