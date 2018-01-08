package isoquant.kernel.plugin.gui;

import isoquant.interfaces.plugin.iPlugin;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class PluginListCellRenderer extends JLabel implements ListCellRenderer 
{
	private static final long serialVersionUID = 20110220L;
	private static final Color HIGHLIGHT_COLOR = new Color(0, 0, 128);
	
	public PluginListCellRenderer() 
	{
		setOpaque(true);
		setIconTextGap( 12 );
	}
	
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) 
	{
		iPlugin plugin = (iPlugin) value;
		
		setText( plugin.getPluginName() );
		setIcon( plugin.getPluginIcon() );
		
		if (isSelected) 
		{
			setBackground(HIGHLIGHT_COLOR);
			setForeground( Color.white );
		} else 
		{
			setBackground( Color.white );
			setForeground( Color.black );
		}
		return this;
	}
}
