package isoquant.kernel.plugin.gui;

import isoquant.kernel.plugin.Plugin;

import java.util.List;

import javax.swing.JList;

public class PluginList extends JList 
{
	private static final long serialVersionUID = 20110220L;
	
	public PluginList()
	{
		setCellRenderer(new PluginListCellRenderer());
	}
	
	public PluginList(Plugin[] plugins)
	{
		this();
		setListData( plugins );
	}
	
	public PluginList(List<Plugin> plugins)
	{
		this();
		setListData( plugins.toArray() );
	}
}
