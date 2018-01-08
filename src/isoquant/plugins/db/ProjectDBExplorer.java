package isoquant.plugins.db;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.Icon;

import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.mysql.MySQLConnectionDialog;
import de.mz.jk.jsix.utilities.ResourceLoader;
import de.mz.jk.jsix.utilities.Settings;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.plugin.ToolBarPlugin;

/**
 * <h3>ProjectDBExplorer</h3>
 * load isoquant database
 * @author Joerg Kuharev
 * @version 29.12.2010 13:33:58
 */
public class ProjectDBExplorer extends ToolBarPlugin 
{
	public static MySQL defaultDB = new MySQL("localhost", "mass_projects", "root", "");
	private MySQL db = defaultDB;
	
	private boolean OPTION_autoLoadDB = false;
	
	public ProjectDBExplorer(iMainApp app) 
	{
		super(app);
		
		loadSettings(app.getSettings());
		
		if(OPTION_autoLoadDB)
        {
        	System.out.println("loading last selected mysql database ...");
    		new Thread(this).start();
        }
	}

	@Override public void loadSettings(Settings cfg)
	{
		db.setHost( app.getSettings().getStringValue("setup.db.host", defaultDB.getHost(), false) );
		db.setUser( app.getSettings().getStringValue("setup.db.user", defaultDB.getUser(), false) );
		db.setPass( app.getSettings().getStringValue("setup.db.pass", defaultDB.getPass(), false) );
		OPTION_autoLoadDB = app.getSettings().getBooleanValue("setup.db.autoLoad", OPTION_autoLoadDB, false);
	}
	
	@Override public String getIconName(){return "database";}
	@Override public Icon getPluginIcon(){return ResourceLoader.getIcon("database");}
	@Override public String getPluginName(){return "ISOQuant Database Explorer";}
	
	@Override public void runPluginAction()
	{
		loadDB( db );
	}
	
	public void loadDB(MySQL db)
	{
		try
		{
			System.out.println( db.toString() );
			
			app.setManagementDB(db);
			this.db = db;
			
			app.updateDBProjects();
			
			app.getSettings().setValue( "setup.db.host", db.getHost() );
			app.getSettings().setValue( "setup.db.user", db.getUser() );
			app.getSettings().setValue( "setup.db.pass", db.getPass() );
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Error while trying to connect database!");
			app.showErrorMessage(
				"Database connection was not established!\n" +
				"Please correct the connection data and try again."
			);
		}
	}

	@Override public void actionPerformed(ActionEvent e)
	{
		MySQL _db = MySQLConnectionDialog.inputConnectionData(
			(Frame)app.getGUI(), 
			db.getHost(), 
			db.getUser(), 
			db.getPass()
		);
		
		if(_db!=null) 
			loadDB( _db );
		else
			app.setMessage("db connection dialog aborted.");
			
	}

	@Override public int getExecutionOrder()
	{
		return 0;
	}
}
