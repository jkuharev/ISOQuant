/** ISOQuant, isoquant.plugins.plgs, Apr 19, 2018*/
package isoquant.plugins.plgs.plain;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import de.mz.jk.jsix.libs.XFiles;
import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.ui.JXButton;
import de.mz.jk.jsix.utilities.ResourceLoader;
import de.mz.jk.plgs.data.Project;
import isoquant.interfaces.iMainApp;
import isoquant.interfaces.iProjectManager;
import isoquant.interfaces.plugin.iProjectImportingPlugin;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.ToolBarPlugin;
import isoquant.plugins.configuration.ConfigurationEditor;
import isoquant.plugins.plgs.importing.design.ProjectDesignPanel;

/**
 * <h3>{@link PlainPLGSDataImporter}</h3>
 * @author jkuharev
 * @version Apr 19, 2018 3:55:44 PM
 */
public class PlainPLGSDataImporter extends ToolBarPlugin implements iProjectImportingPlugin, ActionListener
{
	private File csvFile = null;
	private File rootDir = null;

	protected String quoteChar = "\"";
	protected String colSep = ",";
	protected String decPoint = ".";

	private JDialog dlgWin = null;
	private JPanel mainPane = new JPanel( new BorderLayout() );
	private JTabbedPane tabPane = new JTabbedPane();

	private JPanel btnPane = new JPanel();
	private JButton btnOk = new JXButton( ResourceLoader.getIcon( "small_play" ), "proceed", "accept and proceed" );
	private JButton btnCancel = new JXButton( ResourceLoader.getIcon( "small_stop" ), "cancel", "cancel" );
	private JButton btnSettings = new JXButton( ResourceLoader.getIcon( "small_options" ), "config", "edit configuration" );

	/**
	 * @param app
	 */
	public PlainPLGSDataImporter(iMainApp app)
	{
		super( app );
		loadSettings();
		initDlg();
	}

	public void loadSettings()
	{
		rootDir = new File( app.getSettings().getStringValue( "setup.plgs.projectCsv.dir", iMainApp.appDir.replace( "\\\\", "/" ), false ) );
		decPoint = app.getSettings().getStringValue( "setup.projectCsv.decimalPoint", "'" + decPoint + "'", false );
		colSep = app.getSettings().getStringValue( "setup.projectCsv.columnSeparator", "'" + colSep + "'", false );
		quoteChar = app.getSettings().getStringValue( "setup.projectCsv.textQuote", "'" + quoteChar + "'", false );

		decPoint = XJava.stripQuotation( decPoint );
		colSep = XJava.stripQuotation( colSep );
		quoteChar = XJava.stripQuotation( quoteChar );
	}

	private void initDlg()
	{
		dlgWin = new JDialog( (Frame)app.getGUI(), this.getPluginName(), true );
		dlgWin.setAlwaysOnTop( true );
		dlgWin.setModal( true );
		dlgWin.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
		btnPane.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ) );
		btnPane.setLayout( new BorderLayout() );
		JPanel pnlSettings = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		JPanel pnlOkCancel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
		pnlSettings.add( btnSettings );
		pnlOkCancel.add( btnCancel );
		pnlOkCancel.add( btnOk );
		btnPane.add( pnlSettings, BorderLayout.WEST );
		btnPane.add( pnlOkCancel, BorderLayout.CENTER );
		tabPane.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ) );
		mainPane.add( tabPane, BorderLayout.CENTER );
		mainPane.add( btnPane, BorderLayout.SOUTH );
		btnOk.addActionListener( this );
		btnCancel.addActionListener( this );
		btnSettings.addActionListener( this );
		dlgWin.setContentPane( mainPane );
		dlgWin.setSize( app.getGUI().getWidth(), app.getGUI().getHeight() );
		dlgWin.setLocationRelativeTo( app.getGUI() );
	}

	@Override public void runPluginAction() throws Exception
	{
		loadSettings();
		iProjectManager pm = app.getProjectManager();

		if (pm == null)
		{
			app.showErrorMessage( "please connect to the database before trying to import projects!" );
			return;
		}

		if (!chooseProjectCsvFile())
		{
			System.out.println( "file selection aborted." );
			return;
		}

		System.out.println( "selected file: " + csvFile );
		
		PlainPLGSDataProjectCreator prjMaker = new PlainPLGSDataProjectCreator( csvFile );
		Project plainProject = prjMaker.getProject();
		DBProject p = new DBProject( plainProject );
		p.data.db = pm.getNextSchemaNameForPrefix( pm.suggestSchemaNamePrefix( p.data.id, "" ) );

		ProjectDesignPanel pdp = new ProjectDesignPanel( p, false );
		JPanel pnl = new JPanel();
		pnl.setLayout( new BorderLayout() );
		pnl.add( pdp, BorderLayout.CENTER );
		pnl.setBorder( BorderFactory.createEtchedBorder() );
		pnl.add(
				new JLabel( "<html>use drag and drop and right mouse button to redefine the project <b>'" + p.data.title + "'</b><br></html>" ),
				BorderLayout.NORTH );
		tabPane.addTab( p.data.title, pnl );

		dlgWin.setVisible( true );
	}

	/**
	 * @return
	 */
	private boolean chooseProjectCsvFile()
	{
		try
		{
			File file = XFiles.chooseFile( getFileChooserTitle(), false, new File( "project.csv" ), rootDir, getFileExtensions(), app.getGUI() );
			if (file == null) throw new Exception( "file selection aborted." );
			if (!file.exists()) throw new Exception( "selected file does not exist. (" + file + ")" );
			File dir = file.getParentFile();

			if (!dir.getAbsolutePath().equals( rootDir.getAbsolutePath() ))
				app.getSettings().setValue( "setup.plgs.projectCsv.dir", dir.getAbsolutePath().replace( "\\\\", "/" ) );
			
			csvFile = file;
			return true;
		}
		catch (Exception e)
		{
			System.out.println( e.getMessage() );
			e.printStackTrace();
		}
		return false;
	}

	@Override public void actionPerformed(ActionEvent e)
	{
		Object src = e.getSource();
		if (src.equals( btnOk ))
		{
			System.out.println( "evaluating designed projects ..." );
			// evaluateSelection();
		}
		else if (src.equals( btnCancel ))
		{
			System.out.println( "aborting project designing ..." );
			cancelSelection();
		}
		else if (src.equals( btnSettings ))
		{
			System.out.println( "changing settings ..." );
			new ConfigurationEditor( app ).editModal( dlgWin );
		}
		else
		{
			super.actionPerformed( e );
		}
	}

	public String[] getFileExtensions()
	{
		return new String[] { "csv; Comma Separated Values File" };
	}

	/**
	 * @return
	 */
	private String getFileChooserTitle()
	{
		return "select project.csv file";
	}

	@Override public int getExecutionOrder()
	{
		return 0;
	}

	@Override public String getIconName()
	{
		return "open_project_csv";
	}

	@Override public void importProjects(List<DBProject> projects)
	{}

	@Override public List<DBProject> getImportedProjects()
	{
		return null;
	}

	private void cancelSelection()
	{
		dlgWin.setVisible( false );
		System.out.println( "project design cancelled by user interaction." );
		// selectedProjects = new ArrayList<DBProject>();
		// thread.start();
	}
}
