/** ISOQuant_1.0, isoquant.kernel.plugin, 03.03.2011*/
package isoquant.kernel.plugin;

import isoquant.interfaces.iMainApp;
import isoquant.interfaces.iProjectManager;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.log.LogEntry;
import de.mz.jk.jsix.utilities.Bencher;

/**
 * <h3>{@link HeadlessPlugin4DB}</h3>
 * @author Joerg Kuharev
 * @version 03.03.2011 09:58:40
 */
public abstract class HeadlessPlugin4DB extends HeadlessPlugin
{
	public HeadlessPlugin4DB(iMainApp app)
	{
		super(app);
	}

	@Override public void runPluginAction() throws Exception
	{
		iProjectManager prjMgr = app.getProjectManager();
		for(DBProject p : app.getSelectedDBProjects())
		{
			if(p.mysql==null) p.mysql = prjMgr.getProjectDB(p);
			p.mysql.getConnection();
			Bencher t = new Bencher().start();
			try{
				runPluginAction( p );
				p.log.add(LogEntry.newEvent(this.getPluginName() + ": successfully executed", t.stop().getSecString()));
			}catch (Exception e) {
				p.log.add(LogEntry.newEvent(this.getPluginName() +": failed", t.stop().getSecString()));
			}
			p.mysql.closeConnection();
		}
	}
}
