package isoquant.plugins.plgs;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import de.mz.jk.jsix.libs.XFiles;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.jsix.utilities.Settings;
import de.mz.jk.plgs.reader.ProjectReader;
import isoquant.app.ISOQuant;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.ToolBarPlugin;

/**
 * <h3>PLGSProjectExplorer</h3>
 * explores a PLGS root folder and finds contained projects
 * @author Joerg Kuharev
 * @version 28.12.2010 16:13:48
 */
public class PLGSProjectExplorer extends ToolBarPlugin 
{
	private File iniRoot = null;
	private File rootDir = null;
	private File currentDirectory = new File(ISOQuant.appDir); 

	private boolean OPTION_FSList_showCountExpressionAnalyses=true;
	private boolean OPTION_FSList_showProjectFolderSize=false;
	private boolean OPTION_autoLoadFS=false;
	
	public PLGSProjectExplorer(iMainApp app)
	{
		super(app);
	
		loadSettings( app.getSettings() );
		
		if(OPTION_autoLoadFS)
		{
			System.out.println("loading last selected PLGS project root ...");
			runThread();
		}
	} 
	
	@Override public void loadSettings(Settings cfg)
	{
		iniRoot = new File( app.getSettings().getStringValue("setup.plgs.root.dir", currentDirectory.getAbsolutePath(), false) );
		OPTION_autoLoadFS = app.getSettings().getBooleanValue("setup.plgs.root.autoLoad", OPTION_autoLoadFS, false);
		OPTION_FSList_showCountExpressionAnalyses = app.getSettings().getBooleanValue("setup.plgs.root.showEACount", OPTION_FSList_showCountExpressionAnalyses, false);
		OPTION_FSList_showProjectFolderSize = app.getSettings().getBooleanValue("setup.plgs.root.showFSSize", OPTION_FSList_showProjectFolderSize, false);
		if(rootDir==null) rootDir = iniRoot;
	}
	
	@Override public String getIconName(){return "open_plgs";}
	@Override public String getPluginName(){return "PLGS DBProject Explorer";}
	
	@Override public void runPluginAction() 
	{
		app.getProcessProgressListener().startProgress("searching PLGS projects");
		System.out.println("searching PLGS projects from \n\t" + rootDir.getAbsolutePath());
		int numOfPrj = 0;
		Bencher t = new Bencher().start();
		
		try 
		{
			List<File> prjFiles = ProjectReader.getProjectFileList( rootDir );
			
			/* 
			 * work around for detecting project folder selected instead of root folder
			 * e.g. PLGS/root/Proj_xxxx/ instead of PLGS/root/
			 */
			if( prjFiles.size()==1 && new File(rootDir.getAbsoluteFile() + File.separator + "DBProject.xml").exists() )
			{
					rootDir = rootDir.getParentFile();
					prjFiles = ProjectReader.getProjectFileList( rootDir );
					System.out.println( "Wrong root folder selection detected!" );
					System.out.println( "correcting root folder to:\n\t" + rootDir.getAbsolutePath() );
			}
			
			/* 
			 * work around for detecting PLGS parent folder selected instead of root folder
			 * e.g. PLGS/ instead of PLGS/root/
			 */
			if( prjFiles.size()==0 && new File(rootDir.getAbsoluteFile() + File.separator + "root").exists() )
			{
					rootDir = new File(rootDir.getAbsoluteFile() + File.separator + "root");
					prjFiles = ProjectReader.getProjectFileList( rootDir );
					System.out.println( "Wrong root folder selection detected!" );
					System.out.println( "correcting root folder to:\n\t" + rootDir.getAbsolutePath() );
			}
			
			if( prjFiles.size()>0 )
			{
				System.out.println("collecting information about " + prjFiles.size() + " projects");
				List<DBProject> prj = new ArrayList<DBProject>();
				
				for(File f : prjFiles)
				{
					System.out.print(".");
					DBProject p = new DBProject( ProjectReader.getProject( f, false ) );
					p.data.titleSuffix =
						( (OPTION_FSList_showCountExpressionAnalyses) ? 
									" [" + p.data.expressionAnalysisIDs.size() + " EA]" : "" ) +
						( (OPTION_FSList_showProjectFolderSize) ? 
											" [" + XFiles.getFileSize( new File( p.data.getProjectDirectoryPath() ), 'g', 2 ) + "GB]" : "" );
					prj.add(p);
				}
				
				System.out.println();
				app.setFSProjects(prj);			
				numOfPrj = prj.size();
				app.getSettings().setValue("setup.plgs.root.dir", rootDir.getAbsolutePath());
			}
			else
			{
				app.setFSProjects( new ArrayList<DBProject>() );
				JOptionPane.showMessageDialog(
						app.getGUI(), "Please select a folder that contains some PLGS projects!", 
						"ERROR: invalid root directory selected", JOptionPane.ERROR_MESSAGE
				);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		System.out.println(numOfPrj + " PLGS projects found. [" + t.stop().getSec() + "s]");
		app.getProcessProgressListener().endProgress(numOfPrj + " PLGS projects found");
	}

	@Override public void actionPerformed(ActionEvent e) 
	{
		try{
			chooseRootDirectory();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.out.println("resetting root directory to '" + currentDirectory +"'");
			rootDir = currentDirectory;
			chooseRootDirectory();
		}
	}

	private void chooseRootDirectory() 
	{
		File userSelection = chooseFolder( 
			"Please select PLGS root folder", 
			rootDir !=null ? rootDir : currentDirectory , 
			app.getGUI() 
		);
		
    	if( userSelection!=null && userSelection.canRead() )
    	{
    		rootDir = userSelection.getAbsoluteFile();
    		System.out.println("PLGS root folder selected:\n\t" + userSelection.getAbsolutePath());
    		new Thread(this).start();
    	}
    	else
    	{
    		System.out.println("PLGS root folder selection cancelled.");
    	}
	}

	@Override public int getExecutionOrder()
	{
		return 0;
	}
	
	private static File chooseFolder(String dialogTitle, File hintDir, Component parent) 
	{
		JFileChooser fc = new JFileChooser();			
		fc.setDialogTitle( dialogTitle );
		fc.resetChoosableFileFilters();
		fc.setAcceptAllFileFilterUsed( false );
		fc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
		
		if(hintDir!=null)
		{
			fc.setCurrentDirectory( hintDir.getParentFile() );
			fc.setSelectedFile( hintDir );
		}
		
    	int state = fc.showOpenDialog( parent );
    	File file = fc.getSelectedFile();
    	
    	return ( state!=JFileChooser.CANCEL_OPTION && file!=null ) ? file.getAbsoluteFile() : null;   	
	}
}
