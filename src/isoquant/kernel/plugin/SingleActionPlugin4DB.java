package isoquant.kernel.plugin;

import isoquant.interfaces.iMainApp;
import isoquant.interfaces.iProjectManager;
import isoquant.interfaces.plugin.iProjectProcessingPlugin;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.log.LogEntry;

import java.awt.Component;
import java.util.Collections;
import java.util.List;

import de.mz.jk.jsix.utilities.Bencher;


/**
 * <h3>SingleActionPlugin4DB</h3>
 * abstract plugin implementing common functionality 
 * of plugins on the database side of ISOQuant
 * @author Joerg Kuharev
 * @version 19.01.2011 16:56:40
 */
public abstract class SingleActionPlugin4DB extends SingleActionPlugin implements iProjectProcessingPlugin
{
	public SingleActionPlugin4DB(iMainApp app) 
	{
		super(app);
	}

	@Override public void runPluginAction() throws Exception
	{
		iProjectManager prjMgr = app.getProjectManager();
		List<DBProject> selectedProjects = app.getSelectedDBProjects();
		for(DBProject p : selectedProjects)
		{
			if(p.mysql==null) p.mysql = prjMgr.getProjectDB(p);
			p.mysql.getConnection();
			Bencher t = new Bencher().start();
			try{
				runPluginAction( p );
				p.log.add(LogEntry.newEvent(this.getPluginName() + ": successfully executed", t.stop().getSecString()));
			}catch (Exception e) {
				p.log.add(LogEntry.newEvent(this.getPluginName() +": failed", t.stop().getSecString()));
				e.printStackTrace();
			}
			p.mysql.closeConnection(true);
		}
	}

	@Override public List<Component> getDBMenuComponents(){return Collections.singletonList( (Component)menu );}
	@Override public List<Component> getFSMenuComponents(){return null;}
	@Override public List<Component> getToolBarComponents(){return null;}
}
