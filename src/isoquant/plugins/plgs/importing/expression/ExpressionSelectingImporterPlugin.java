package isoquant.plugins.plgs.importing.expression;

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
import isoquant.interfaces.iProjectManager;
import isoquant.interfaces.plugin.iProjectImportingPlugin;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4FS;
import isoquant.plugins.configuration.ConfigurationEditor;
import isoquant.plugins.plgs.importing.importer.PLGSDataImporter;

/**
 * <h3>EAOrientedPLGSProjectImporter</h3>
 * let user choose which expression analyses should be imported
 * @author Joerg Kuharev
 * @version 06.01.2011 12:09:18
 *
 */
public abstract class ExpressionSelectingImporterPlugin extends SingleActionPlugin4FS implements iProjectImportingPlugin
{
	private JDialog dlgWin = null;	
	private JPanel mainPane = new JPanel(new BorderLayout());
	private JTabbedPane tabPane = new JTabbedPane();

	private JPanel btnPane = new JPanel();
	private JButton btnOk = new JXButton(ResourceLoader.getIcon("small_play"), "proceed", "accept and proceed");
	private JButton btnCancel = new JXButton(ResourceLoader.getIcon("small_stop"), "abort", "abort");
	private JButton btnSettings = new JXButton(ResourceLoader.getIcon("small_options"), "config", "edit configuration");
	
	private List<DBProject> originalProjects = null;
	private List<DBProject> selectedProjects = null;
	
	private List<ExpressionAnalysisTableCreator> eaTabs = null;
	
	private Thread thread = null;
	
	public ExpressionSelectingImporterPlugin(iMainApp app)
	{
		super(app);
		initDlg();
	}
	
	@Override public String getMenuItemIconName(){return "import";}
	
	private void initDlg()
	{
		dlgWin = new JDialog((Frame) app.getGUI(), "Expression Analysis Chooser", true);
		
		dlgWin.setAlwaysOnTop(true);
		dlgWin.setModal(true);
		dlgWin.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
//		btnPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
//		btnPane.setLayout(new FlowLayout(FlowLayout.CENTER));
//		
//		btnPane.add(btnOk);
//		btnPane.add(btnCancel);

		btnPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		btnPane.setLayout( new BorderLayout() );
		
		JPanel pnlSettings = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel pnlOkCancel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			
		pnlSettings.add(btnSettings);
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
		
		dlgWin.setContentPane(mainPane);
		dlgWin.setSize(640, 400);
		
		dlgWin.setLocationRelativeTo( app.getGUI() );
	}
	
	private void runSelection()
	{
		iProjectManager prjMgr = app.getProjectManager();
		if(prjMgr==null)
		{
			app.showErrorMessage("please connect to a database before trying to import projects!");
			return;
		}
		
		selectedProjects = new ArrayList<DBProject>();
		eaTabs = new ArrayList<ExpressionAnalysisTableCreator>();
		
		while(tabPane.getTabCount()>0) tabPane.remove(0);
		
		for(int i=0; i<originalProjects.size(); i++)
		{
			DBProject p = originalProjects.get(i);
			ExpressionAnalysisTableCreator et = new ExpressionAnalysisTableCreator(p, prjMgr);
			
			JPanel pnl = new JPanel();
				pnl.setLayout(new BorderLayout());
				pnl.add(et.getTableComponent(), BorderLayout.CENTER);

				pnl.setBorder(BorderFactory.createEtchedBorder());
				pnl.add(
					new JLabel( "<html>Expression analyses for project <br>'" + p.data.title + "'</html>" ),
					BorderLayout.NORTH
				);
				
			tabPane.addTab( p.data.title, pnl );
			eaTabs.add(et);
		}
		
		dlgWin.setVisible(true);
	}
	
	private void cancelSelection()
	{
		dlgWin.setVisible(false);
		System.out.println("expression analysis selection cancelled by user interaction.");
		selectedProjects = new ArrayList<DBProject>();
		thread.start();
	}

	private void evaluateSelection()
	{
		System.out.println("processing ...");
		for(ExpressionAnalysisTableCreator eaTab : eaTabs)
		{
			selectedProjects.addAll( eaTab.getSelectedExpressionAnalysesAsProjects() );
		}
		dlgWin.setVisible(false);
		thread.start();
	}
	
	@Override public void runPluginAction() throws Exception
	{
		for(DBProject p : selectedProjects)
		{
			runPluginAction( p );
		}
	}

	@Override public void runPluginAction(DBProject p) throws Exception 
	{
		PLGSDataImporter dataImporter = new PLGSDataImporter();
		
		// create database for project
		app.getProjectManager().addProject( p );
		
		dataImporter.importProject( 
			p, 
			shouldReadExpressionAnalysisFromFileSystem(), 
			shouldImportExpressionAnalysisResults() 
		);	
		
		// update list of projects
		app.updateDBProjects();
	}
	
	protected abstract boolean shouldImportExpressionAnalysisResults();
	protected abstract boolean shouldReadExpressionAnalysisFromFileSystem();

	@Override public void actionPerformed(ActionEvent e)
	{
		Object src = e.getSource();
		if(src.equals(menu))
		{
			System.out.println("running expression analysis selection ...");
			importProjects( app.getSelectedFSProjects() );
		}
		else
		if(src.equals(btnOk))
		{
			System.out.println("evaluating expression analysis selection ...");
			evaluateSelection();
		}
		else
		if(src.equals(btnCancel))
		{
			System.out.println("aborting expression analysis selection ...");
			cancelSelection();
		}
		else
		if(src.equals(btnSettings))
		{
			System.out.println("changing settings ...");
			new ConfigurationEditor(app).editModal(dlgWin);
		}
	}

	@Override public List<DBProject> getImportedProjects()
	{
		try{thread.join();}catch(Exception e){e.printStackTrace();}
		return selectedProjects;
	}
	
	@Override public void importProjects(List<DBProject> projects)
	{
		originalProjects = projects;
		thread = new Thread(this);
		runSelection();
	}

	@Override public int getExecutionOrder()
	{
		return 1;
	}
}
