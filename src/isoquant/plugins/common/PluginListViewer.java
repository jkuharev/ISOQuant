package isoquant.plugins.common;

import javax.swing.*;

import isoquant.interfaces.iMainApp;
import isoquant.kernel.plugin.ToolBarPlugin;
import isoquant.kernel.plugin.gui.PluginList;

public class PluginListViewer extends ToolBarPlugin 
{
	public PluginListViewer(iMainApp app){super(app);}

	@Override public String getPluginName(){return "Plugin Liste Viewer";}
	@Override public String getIconName(){return "plugins";}
	
	@Override public void runPluginAction() throws Exception 
	{
		JList list = new PluginList( app.getPluginManager().getPlugins() );
		
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("following plugins are loaded:"));
		panel.add(new JScrollPane(list));
		
		JOptionPane.showMessageDialog(
			app.getGUI(), 
			panel, 
			getPluginName(),
			JOptionPane.INFORMATION_MESSAGE,
			getPluginIcon()
		);
	}

	@Override public int getExecutionOrder()
	{
		return 0;
	}
}
