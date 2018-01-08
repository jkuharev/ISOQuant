package isoquant.plugins.db;

import java.util.List;

import javax.swing.JOptionPane;

import de.mz.jk.jsix.utilities.Bencher;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

/**
 * <h3>{@link ProjectCleaner}</h3>
 * cleaning project databases
 * @author Joerg Kuharev
 * @version 24.02.2011 12:55:34
 */
public class ProjectCleaner extends SingleActionPlugin4DB 
{
	public static boolean silentMode = true;
	
	public ProjectCleaner(iMainApp app) 
	{
		super(app);
	}

	@Override public void runPluginAction() throws Exception
	{
		List<DBProject> prjs = app.getSelectedDBProjects();
		if(prjs==null || prjs.size()<1) return;
		
		int userInput = 
			silentMode 
				? JOptionPane.YES_OPTION 
				: JOptionPane.showConfirmDialog(
				app.getGUI(), 
				"You are going to reset processing progress for " + prjs.size() + " projects!\n" +
				"Are you sure you want to proceed?",
				"reset projects?", 
				JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				this.getPluginIcon()
		);
		
		if(userInput==JOptionPane.YES_OPTION)
		{
			for(DBProject p : prjs)
			{
				runPluginAction(p);
			}
		}	
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		System.out.println( "cleaning project '" + p.data.title + "' ..." );
		
		Bencher t = new Bencher(true);
		
		List<String> allTables = p.mysql.listTables();
		String[] basTables = app.getProjectManager().listBasicProjectTables();
		
		outerLoop:
		for(String tab : allTables)
		{
			for(String bTab: basTables)
			{
				if( bTab.equalsIgnoreCase(tab) ) 
					continue outerLoop;
			}
			System.out.println(":	droping table '" + tab + "' ...");
			p.mysql.dropTable(tab);
		}
		
		System.out.println("project cleaning duration ["+t.stop().getSecString() +"]");
		
		app.updateDBProjects();
	}
	
	@Override public int getExecutionOrder()
	{
		return 2;
	}

	@Override public String getMenuItemIconName()
	{
		return "clean_db";
	}

	@Override public String getMenuItemText()
	{
		return "clean project database";
	}
	
	@Override public String getPluginName()
	{
		return "DBProject Cleaner";
	}
}
