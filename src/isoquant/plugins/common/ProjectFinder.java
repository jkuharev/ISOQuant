/** ISOQuant_1.0, isoquant.plugins.common, 15.02.2011*/
package isoquant.plugins.common;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.ToolBarPlugin;

/**
 * <h3>{@link ProjectFinder}</h3>
 * search projects from fs and db project lists by 
 * a part of project title or regular expression
 * @author Joerg Kuharev
 * @version 15.02.2011 11:44:14
 */
public class ProjectFinder extends ToolBarPlugin
{
	private final static List<DBProject> emptyProjectList = new ArrayList<DBProject>();
	private int hitCounter = 0;
	
	private String oldSearch = "";
	
	public ProjectFinder(iMainApp app)
	{
		super(app);
	}

	@Override public String getIconName(){return "find_project";}
	@Override public String getPluginName(){return "DBProject Finder";}
	
	@Override public void run()
	{
		try{runPluginAction();} catch (Exception e){e.printStackTrace();}
	}
	
	@Override public void runPluginAction() throws Exception
	{
		hitCounter = 0;

		String s = (String) JOptionPane.showInputDialog(
			app.getGUI(), 
			"Please type partial or complete project title\n" +
			"then press ENTER or click Ok.",
			getPluginName(),
			JOptionPane.PLAIN_MESSAGE,
			this.getPluginIcon(),
			null,
			oldSearch
		);
		
		if(s==null) return;
		
		// select all
		if( s.equals("*") )
		{
			app.selectDBProjects( app.getDBProjects() );
			app.selectFSProjects( app.getFSProjects() );
			app.setMessage("all projects selected");
		}
		else
		// deselect all
		if( s.equals("") )
		{
			app.selectDBProjects( emptyProjectList );
			app.selectFSProjects( emptyProjectList );
			app.setMessage("selection cleared");
		}
		// search
		else
		{
			app.selectDBProjects( findProjects(app.getDBProjects(), s) );
			app.selectFSProjects( findProjects(app.getFSProjects(), s) );
			app.setMessage(hitCounter + " projects selected");
		}
		
		oldSearch = s;
	}

	private List<DBProject> findProjects(List<DBProject> prjs, String s)
	{
		List<DBProject> f = new ArrayList<DBProject>();
		for(DBProject p : prjs)
		{
			if (p.data.title.toLowerCase().contains( s.toLowerCase() ) || p.data.title.matches( ".*" + s + ".*" ))
			{
				f.add(p);
				hitCounter++;
			}
		}
		return f;
	}

	@Override public int getExecutionOrder()
	{
		return 0;
	}	 
}
