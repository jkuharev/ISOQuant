package isoquant.kernel.plugin;

import isoquant.interfaces.iMainApp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import de.mz.jk.jsix.utilities.ResourceLoader;

/**
 * <h3>SingleActionPlugin</h3>
 * definition of an abstract plugin having 
 * a single menu item which is automatically bound 
 * to the execution of new thread that executes 
 * the implementation of runPluginAction() in a subclass  
 * @author Joerg Kuharev
 * @version 19.01.2011 16:53:09
 */
public abstract class SingleActionPlugin extends Plugin implements ActionListener
{
	protected JMenuItem menu = new JMenuItem();
	
	public SingleActionPlugin(iMainApp app) 
	{
		super(app);

		menu.setIcon( getPluginIcon() );
		menu.setText( getMenuItemText() );
		menu.addActionListener(this);
	}
	
	/** returned text will appear on the menu item of this plugin */
	public abstract String getMenuItemText();
	
	/** icon name is used to determine the icon for this plugin.
	 *  The icon name can be either a file name of a png picture from resources/icons folder
	 *  or a full or relative file path to an image file to be the icon
	 * */
	public abstract String getMenuItemIconName();

	@Override public Icon getPluginIcon(){ return ResourceLoader.getIcon( getMenuItemIconName() ); }
	@Override public void actionPerformed(ActionEvent e){ if( e.getSource().equals(menu) ) runThread(); }

	@Override public int getExecutionOrder(){return 256;}
}
