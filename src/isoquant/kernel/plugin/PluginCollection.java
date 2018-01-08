/** ISOQuant, isoquant.kernel.plugin, 29.08.2012*/
package isoquant.kernel.plugin;

import isoquant.interfaces.iMainApp;
import isoquant.kernel.plugin.queue.PluginQueue;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

import javax.swing.*;

import de.mz.jk.jsix.utilities.ResourceLoader;

/**
 * <h3>{@link PluginCollection}</h3>
 * @author kuharev
 * @version 29.08.2012 11:29:08
 */
public abstract class PluginCollection extends Plugin
{
	private Plugin[] plugins = null;
	
	private JMenu fsMenu = new JMenu();
	private JMenu dbMenu = new JMenu();
	private JPopupMenu tbMenu = new JPopupMenu();
	
	private boolean hasFsMenu = false, hasDbMenu = false, hasTbMenu = false; 
	
	public PluginCollection(iMainApp app)
	{
		super(app);
		initPlugins();
	}
		
	private void initPlugins()
	{
		fsMenu.setText( getMenuItemText() );
		fsMenu.setIcon( getPluginIcon() );
		dbMenu.setText( getMenuItemText() );
		dbMenu.setIcon( getPluginIcon() );
		
		String[] cns = getPluginClassNames();
		if(cns==null) cns = new String[]{};
		
		PluginQueue q = new PluginQueue( "", cns );
		q.revivePlugins( getMainApp() );
		plugins = q.getPlugins();
		
		for(Plugin p : plugins)
		{
			List<Component> cs = p.getDBMenuComponents();
			if(cs!=null && cs.size()>0)
			{
				hasDbMenu = true;
				for(Component c : cs) 
				{ 
					dbMenu.add( c==null ? new JSeparator(SwingConstants.VERTICAL) : c); 
				} 
			}
			
			cs = p.getFSMenuComponents();
			if(cs!=null && cs.size()>0)
			{
				hasFsMenu = true;
				for(Component c : cs) 
				{ 
					fsMenu.add( c==null ? new JSeparator(SwingConstants.VERTICAL) : c); 
				} 
			}
			
			cs = p.getToolBarComponents();
			if(cs!=null && cs.size()>0)
			{
				hasTbMenu = true;
				for(Component c : cs) 
				{ 
					tbMenu.add( c==null ? new JSeparator(SwingConstants.VERTICAL) : c); 
				} 
			}
		}		
	}

	/** returned text will appear on the menu item of this plugin */
	public abstract String getMenuItemText();
	
	/** icon name is used to determine the icon for this plugin.
	 *  The icon name can be either a file name of a png picture from resources/icons folder
	 *  or a full or relative file path to an image file to be the icon
	*/
	public abstract String getMenuItemIconName();
	
	/** implementing class should list names of plugin classes */ 
	public abstract String[] getPluginClassNames();

	@Override public List<Component> getFSMenuComponents()
	{
		return hasFsMenu ? Collections.singletonList( (Component)fsMenu ) : null;
	}

	@Override public List<Component> getDBMenuComponents()
	{
		return hasDbMenu ? Collections.singletonList( (Component)dbMenu ) : null;
	}

	@Override public List<Component> getToolBarComponents()
	{
		if(!hasTbMenu) return null;
		final JButton btn = new JButton( getPluginIcon() );
		
		btn.setToolTipText( getMenuItemText() );
		btn.addActionListener( 
			new ActionListener()
			{
				@Override public void actionPerformed(ActionEvent e)
				{ 
					if( e.getSource().equals(btn) ) tbMenu.setVisible(true); 
				}
			} 
		);
		return Collections.singletonList((Component) btn ); 
	}

	@Override public Icon getPluginIcon(){ return ResourceLoader.getIcon( getMenuItemIconName() ); }
	@Override public int getExecutionOrder(){ return 0; }
	@Override public void runPluginAction() throws Exception { /* do nothing */ }		
}
