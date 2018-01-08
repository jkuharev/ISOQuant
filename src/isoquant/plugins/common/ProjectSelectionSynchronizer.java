/** ISOQuant_1.0, isoquant.plugins.common, 15.02.2011*/
package isoquant.plugins.common;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import de.mz.jk.jsix.utilities.ResourceLoader;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.plugin.Plugin;

/**
 * <h3>{@link ProjectSelectionSynchronizer}</h3>
 * @author Joerg Kuharev
 * @version 15.02.2011 11:44:14
 */
public class ProjectSelectionSynchronizer extends Plugin implements ActionListener
{	
	private JMenuItem fsMenuItem = createMenuItem("find in database", "sync_lists");
	private JMenuItem dbMenuItem = createMenuItem("find in file system", "sync_lists");
	
	public ProjectSelectionSynchronizer(iMainApp app)
	{
		super(app);
	}

	@Override public List<Component> getDBMenuComponents(){return Collections.singletonList((Component)dbMenuItem);}
	@Override public List<Component> getFSMenuComponents(){return Collections.singletonList((Component)fsMenuItem);}
	@Override public List<Component> getToolBarComponents(){return null;}
	
	@Override public Icon getPluginIcon(){return ResourceLoader.getIcon("sync_lists");}
	@Override public String getPluginName(){return "DBProject Selection Synchronizer";}
	
	@Override public void runPluginAction() throws Exception{}
	
	@Override public void actionPerformed(ActionEvent e)
	{
		if(e.getSource().equals(fsMenuItem))
			app.selectDBProjects( app.getSelectedFSProjects() );
		else
		if(e.getSource().equals(dbMenuItem))
			app.selectFSProjects( app.getSelectedDBProjects() );
	}

	@Override public int getExecutionOrder()
	{
		return 0;
	}
}
