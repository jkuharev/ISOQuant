package isoquant.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.*;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.ui.JListJPopupMenuBinder;
import de.mz.jk.jsix.ui.JTextAreaOutputStream;
import de.mz.jk.jsix.ui.iProcessProgressListener;
import de.mz.jk.jsix.utilities.ResourceLoader;
import isoquant.app.Defaults;
import isoquant.interfaces.iGUI;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;

/**
 * 
 * <h3>SplitListGUI</h3>
 * 
 * @author Joerg Kuharev
 * @version 22.12.2010 15:46:11
 *
 */
public class SplitListGUI extends JFrame 
	implements WindowListener, ComponentListener, ActionListener, iGUI, iProcessProgressListener
{
	public static final long serialVersionUID = 201009251302L;
	
	private int defaultWindowWidth=800;
	private int defaultWindowHeight=480;
	
	private String logFileName = "isoquant.log";
	
	public static void main(String[] args) 
	{
		SplitListGUI gui = new SplitListGUI();
		gui.setVisible(true);
	}

	/**
	 * backup standard output and standard error devices
	 */
	private final PrintStream stdOut = System.out;
	private final PrintStream stdErr = System.err;
	
	private boolean captureStdOut = true;
	private boolean captureStdErr = true;
	
//	private Font defaultFont = new Font( "Helvetica", Font.PLAIN, 12 );
//	private Font font = defaultFont;
	
	private boolean isLocked = false;
	
	/**
	 * GUI components
	 */
	private JPanel mainContentPane = new JPanel();
	private JToolBar toolBar = new JToolBar();
	private JTabbedPane tabPane = new JTabbedPane();
	
	private JPanel statusBarPanel = new JPanel();
	private JSplitPane listsSplitPanel = new JSplitPane();
	private JPanel leftPanel = new JPanel();
	private JPanel rightPanel = new JPanel();
	
	private JTextArea logTextArea = new JTextArea();
	private JScrollPane msgPane = new JScrollPane( logTextArea );	
	private JList listFS = new JList();
	private JList listDB = new JList();
	private JLabel statusBarLeftLabel = new JLabel(" ");
	private JProgressBar progressBar = new JProgressBar();
	
	private JPopupMenu menuFS = new JPopupMenu("apply to projects in FS");
	private JPopupMenu menuDB = new JPopupMenu("apply to projects in DB");
	
	private JButton btnSwitchGUIMode = new JButton(ResourceLoader.getIcon("change_view"));
	private JButton btnExit = new JButton(ResourceLoader.getIcon("exit"));
	
	private JPanel toolBarLeftPane = new JPanel(new FlowLayout(FlowLayout.LEADING));
	private JPanel toolBarRightPane = new JPanel(new FlowLayout(FlowLayout.TRAILING));
	
	private boolean tabListsViewMode = true;
	
	private ImageIcon iconISOQuant = ResourceLoader.getIcon("isoquant");
	
	private List<DBProject> dbProjects = new ArrayList<DBProject>();
	private List<DBProject> fsProjects = new ArrayList<DBProject>();
	private iMainApp app = null;
	
	public SplitListGUI()
	{
		this(null, false);
	}
		
	public SplitListGUI(iMainApp app)
	{
		this(app, app.getSettings().getBooleanValue("setup.ui.captureConsoleMessages", true, false) );
	}
	
	public SplitListGUI(iMainApp app, boolean captureConsoleMessages)
	{
		super( "ISOQuant " + Defaults.version() );
		this.app = app;
		captureStdOut = captureConsoleMessages;
		captureStdErr = captureConsoleMessages;
		initGUI();
	}
	
	private void initGUI() 
	{
		// this.setSize( 800, 480 );
		if(app!=null) loadGUISettings();
		
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener( this );
		this.addComponentListener( this );
		//this.setIconImage(iconISOQuant.getImage());
		
		this.setLayout( new BorderLayout() );
//		this.add(jxLayer, BorderLayout.CENTER);
		this.add( mainContentPane, BorderLayout.CENTER );
		this.add( statusBarPanel, BorderLayout.SOUTH );
		
		mainContentPane.setLayout( new BorderLayout() );
		toolBar.setLayout( new BorderLayout() );
		
		toolBar.add( toolBarLeftPane, BorderLayout.LINE_START );
		toolBar.add( toolBarRightPane, BorderLayout.LINE_END );
		
		statusBarPanel.setBorder( BorderFactory.createLoweredBevelBorder() );
		statusBarPanel.setLayout( new BorderLayout() );
		statusBarPanel.add( statusBarLeftLabel, BorderLayout.WEST );
		statusBarPanel.add( progressBar, BorderLayout.EAST );

		leftPanel.setLayout( new BorderLayout() );
		leftPanel.setBorder( BorderFactory.createTitledBorder("projects in PLGS root folder") );
		leftPanel.add( new JScrollPane(listFS), BorderLayout.CENTER );
		
		rightPanel.setLayout( new BorderLayout() );
		rightPanel.setBorder( BorderFactory.createTitledBorder("projects in MySQL database") );
		rightPanel.add( new JScrollPane(listDB), BorderLayout.CENTER );
		
		mainContentPane.add( toolBar, BorderLayout.NORTH );
		mainContentPane.add( tabPane, BorderLayout.CENTER );

		// tabPane.addTab("PLGS", null, leftPanel);
		// tabPane.addTab("DB", null, rightPanel);

		tabPane.addTab( "messages", msgPane );
		
		// btnSwitchGUIMode.addActionListener(this);
		
		btnExit.setToolTipText("stop all actions and exit");
		btnExit.addActionListener( this );
		toolBarRightPane.add( btnExit );
		
		new JListJPopupMenuBinder( listDB, menuDB, true );
		new JListJPopupMenuBinder( listFS, menuFS, true );
		
		switchView();
		
		logTextArea.setFont( new Font( "Helvetica", Font.PLAIN, 12 ) );
		logTextArea.setForeground( new Color(200, 255, 200) );
		logTextArea.setBackground( new Color(0, 80, 0) );
		
		if (captureStdErr || captureStdOut)
		{
			boolean logPerSession = app.getSettings().getBooleanValue( "setup.log.perSession", true, false ); 
			String logDirName = app.getSettings().getStringValue( "setup.log.dir", "log", false );
			File logDir = new File( logDirName );
			if (!logDir.isDirectory()) logDir.mkdirs();
			if ( !logDir.isDirectory() ) 
			{
				System.out.println( "log directory path '" + logDirName + "' is not accessible." );
				logDir = new File( "." );
			}
			File logFile = new File( logDir, logPerSession ? XJava.timeStamp() + ".log" : "isoquant.log" );
			logFileName = logFile.getAbsolutePath();
			JTextAreaOutputStream taos = new JTextAreaOutputStream( logTextArea, captureStdOut, captureStdErr, logFile );
		}
		
		app.getSettings().getBooleanValue( "setup.ui.promptForExit", false, false );
		
//		String fontName = app.getSettings().getValue("setup.ui.font.name", defaultFont.getName(), !Defaults.DEBUG);
//		int fontSize = app.getSettings().getValue("setup.ui.font.size", defaultFont.getSize(), !Defaults.DEBUG);
//		font = new Font(fontName, Font.PLAIN, fontSize);
//		listDB.setFont(font);
//		listFS.setFont(font);
	}
	
	private void switchView()
	{
		if(tabListsViewMode)
		{
			listsSplitPanel.setLeftComponent(leftPanel);
			listsSplitPanel.setRightComponent(rightPanel);
			listsSplitPanel.setDividerLocation(this.getWidth()/2);
			tabPane.insertTab("projects", null, listsSplitPanel, "projects", 0);
		}
		else
		{
			tabPane.remove(listsSplitPanel);			
			tabPane.insertTab("PLGS", null, leftPanel, "projects from PLGS", 0);
			tabPane.insertTab("DB", null, rightPanel, "projects from database", 1);
		}
		
		tabPane.setSelectedIndex(0);
		tabPane.repaint();
		
		tabListsViewMode = !tabListsViewMode;
	}
	
// --------------------------------------------------------------------------------
/* WindowListener */
	@Override public void windowActivated(WindowEvent e){}
	@Override public void windowClosed(WindowEvent e){}
	@Override public void windowDeactivated(WindowEvent e){}
	@Override public void windowDeiconified(WindowEvent e){}
	@Override public void windowIconified(WindowEvent e){}
	@Override public void windowClosing(WindowEvent e){ shutdown(); }
	@Override public void windowOpened(WindowEvent e){listsSplitPanel.setDividerLocation(0.5);}
// --------------------------------------------------------------------------------
/* ComponentListener */
	@Override public void componentHidden(ComponentEvent e){}
	@Override public void componentMoved(ComponentEvent e){}
	@Override public void componentShown(ComponentEvent e){}
	@Override public void componentResized(ComponentEvent e){listsSplitPanel.setDividerLocation(0.5);}
// --------------------------------------------------------------------------------

/* ActionListener */
	@Override public void actionPerformed(ActionEvent e)
	{
		if( e.getSource().equals(btnSwitchGUIMode) )
		{
			switchView();
		}
		else
		if( e.getSource().equals(btnExit) )
		{
			shutdown();
		}
	}

	/* iGUI */
	@Override public void setDBProjects(List<DBProject> prjs)
	{
		Collections.sort( prjs );
		dbProjects = prjs;
		listDB.setListData( prjs.toArray() );
	}
	
	@Override public void setFSProjects(List<DBProject> data)
	{
		Collections.sort(data);
		fsProjects = data;
		listFS.setListData(data.toArray());
	}
	
	@Override public List<DBProject> getDBProjects(){return dbProjects;}
	@Override public List<DBProject> getFSProjects(){return fsProjects;}	
	
	@Override public List<DBProject> getSelectedDBProjects()
	{
		int[] selIndeces = listDB.getSelectedIndices();
		List<DBProject> prjList = new ArrayList<DBProject>();
		// get selected Projects
		for(int i : selIndeces)
		{
			prjList.add( (DBProject)( listDB.getModel().getElementAt(i) ) );
		}	
		return prjList;
	}
	
	@Override public List<DBProject> getSelectedFSProjects()
	{
		int[] selIndeces = listFS.getSelectedIndices();
		List<DBProject> prjList = new ArrayList<DBProject>();
		// get selected Projects
		for(int i : selIndeces)
		{
			prjList.add( (DBProject)( listFS.getModel().getElementAt(i) ) );
		}	
		return prjList;
	}
	
	@Override public void selectDBProjects(List<DBProject> projects){selectProjects(listDB, projects);}
	@Override public void selectFSProjects(List<DBProject> projects){selectProjects(listFS, projects);}
	
	private void selectProjects(JList list, List<DBProject> prjs)
	{
		int n = list.getModel().getSize();
		List<Integer> s = new ArrayList<Integer>();
		for(int i=0; i<n; i++)
		{
			DBProject pi = (DBProject)(list.getModel().getElementAt(i));
			for(DBProject p : prjs)
			{
				if (pi.data.title.equals( p.data.title ))
				{
					s.add(i);
					break;
				}					
			}
		}
		int[] si = new int[s.size()];
		for(int i=0; i<si.length; i++) si[i] = s.get(i);
		list.setSelectedIndices(si);
	}
	
	@Override public void updateDBProjects(){}
	@Override public void updateFSProjects(){}
	
	@Override public void addDBMenuComponent(Component c)
	{
		if(c==null) 
			menuDB.addSeparator();
		else
			menuDB.add(c);
		
		menuDB.updateUI();
	}
	
	@Override public void addFSMenuComponent(Component c)
	{
		if(c==null) 
			menuFS.addSeparator();
		else
			menuFS.add(c);
		
		menuFS.updateUI();
	}
	
	@Override public void addToolBarComponent(Component c)
	{
		toolBarLeftPane.add(c);
		toolBarLeftPane.updateUI();
	}	
	
	@Override public void setProgressMaxValue(int maxValue){progressBar.setMaximum(maxValue);}
	@Override public void setProgressValue(int value){progressBar.setValue(value);}
	@Override public void setStatusMessage(String text){statusBarLeftLabel.setText(text);}
	@Override public void setLocked(boolean locked)
	{
		isLocked = locked;	
		listDB.setEnabled(!locked);
		listFS.setEnabled(!locked);
		
		for(Component c : menuDB.getComponents()) c.setEnabled(!locked);
		for(Component c : menuFS.getComponents()) c.setEnabled(!locked);
		for(Component c : toolBarLeftPane.getComponents()) c.setEnabled(!locked);
		// for(Component c : toolBarRightPane.getComponents()) c.setEnabled(!locked);
	}

	@Override public Component getGUIComponent() {return this;}
	
	Component oldPane = null;
	@Override public void startProgress()
	{
		setLocked(true);
		oldPane = tabPane.getSelectedComponent();
		if(captureStdOut) tabPane.setSelectedComponent(msgPane);
	}
	
	@Override public void endProgress()
	{
		setLocked(false);
		if(captureStdOut) tabPane.setSelectedIndex(0);
		setStatus("");
	}
	
	@Override public void startProgress(String message) 
	{
		setStatus(message);
		startProgress();
	}
	@Override public void endProgress(String Message) 
	{
		endProgress();
		setStatus(Message);
	}
	
	@Override public synchronized void setMessage(String message)
	{
		System.out.println( message );
	}

	@Override public synchronized void setStatus(String msg)
	{
		setStatusMessage( msg );
	}

	@Override public iProcessProgressListener getProcessProgressListener(){	return this; }
	
	private int shutdownCounter = 0;
	
	private void shutdown()
	{
		shutdownCounter++;
		
		if(shutdownCounter > 1) 
		{
			System.out.println("user requested to kill the application ...");
			System.exit(0);
		}
		
		if( app.getSettings().getBooleanValue("setup.ui.promptForExit", true, false) )
		{
			int userInput = JOptionPane.showConfirmDialog(
				app.getGUI(),
				"are you sure you want to quit?", 
				"quit", 
				JOptionPane.YES_NO_OPTION, 
				JOptionPane.WARNING_MESSAGE, 
				ResourceLoader.getIcon("isoquant")
			);
			if( userInput == JOptionPane.NO_OPTION) {
				shutdownCounter=0;
				return;
			}
		}
		
		System.out.println();
		System.out.println("You have requested a clean application shutdown!");
		System.out.println("The application will quit after safely ending current process!");
		System.out.println();
		System.out.println("You can immediately stop (kill) the application ");
		System.out.println("by clicking the SHUTDOWN button again.");
		
		saveGUISettings();
		new Thread()
		{
			public void run()
			{
				app.shutdown(true);
				System.exit( 0 );
			}
		}.start();
	}

	private void saveGUISettings()
	{
		app.getSettings().setValue( "setup.ui.size.width", this.getWidth() + "");
		app.getSettings().setValue( "setup.ui.size.height", this.getHeight() + "");
		app.getSettings().setValue( "setup.ui.location.left", this.getLocation().x + "");
		app.getSettings().setValue( "setup.ui.location.top", this.getLocation().y + "");
	}
	
	private void loadGUISettings()
	{
		int sW = Toolkit.getDefaultToolkit().getScreenSize().width;
		int sH = Toolkit.getDefaultToolkit().getScreenSize().height;
		
		int w = app.getSettings().getIntValue( "setup.ui.size.width", defaultWindowWidth, false);
		int h = app.getSettings().getIntValue( "setup.ui.size.height", defaultWindowHeight, false);
		
		if(w>=sW || w<480) w = sW / 2;
		if(h>=sH || h<320) h = sH / 2;
		
		this.setSize(w, h);
		
		int x = app.getSettings().getIntValue( "setup.ui.location.left", (sW-w)/2, false);
		int y = app.getSettings().getIntValue( "setup.ui.location.top", (sH-h)/2, false);
		
		if((x+w)>sW) x = sW - w;
		if((y+h)>sH) x = sH - h;

		if(x<0) x = 0;
		if(y<0) y = 0;
		
		this.setLocation(x, y);
	}
}
