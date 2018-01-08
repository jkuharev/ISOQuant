package isoquant.plugins.db;

import java.util.List;

import javax.swing.JOptionPane;

import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

/**
 * <h3>{@link ProjectRemover}</h3>
 * removing project databases
 * @author Joerg Kuharev
 * @version 24.02.2011 12:55:34
 */
public class ProjectRemover extends SingleActionPlugin4DB 
{
	public ProjectRemover(iMainApp app) 
	{
		super(app);
	}

	@Override public void runPluginAction() throws Exception
	{
		List<DBProject> prjs = app.getSelectedDBProjects();
		if(prjs==null || prjs.size()<1) return;
		
		int userInput = JOptionPane.showConfirmDialog(
				app.getGUI(), 
				"You are going to delete " + prjs.size() + " projects from database!\n" +
				"Are you sure you want to proceed?", 
				"remove projects?", 
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
		System.out.println( "removing project '" + p.data.title + "' from database ..." );
		app.getProjectManager().removeProject(p);
		app.updateDBProjects();
	}
	
	@Override public int getExecutionOrder()
	{
		return 0;
	}

	@Override public String getMenuItemIconName()
	{
		return "trash";
	}

	@Override public String getMenuItemText()
	{
		return "remove from database";
	}
	
	@Override public String getPluginName()
	{
		return "DBProject Remover";
	}
}
