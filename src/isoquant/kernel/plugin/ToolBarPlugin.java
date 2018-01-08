package isoquant.kernel.plugin;

import isoquant.interfaces.iMainApp;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;

import de.mz.jk.jsix.utilities.ResourceLoader;

/**
 * <h3>SingleActionPlugin</h3>
 * definition of an abstract plugin having 
 * a single button placed on the tool bar 
 * and automatically bound to the execution 
 * of a new thread that executes 
 * the implementation of runPluginAction() in a subclass  
 * @author Joerg Kuharev
 * @version 19.01.2011 16:53:09
 */
public abstract class ToolBarPlugin extends Plugin implements ActionListener
{
	protected JButton button = new JButton();
	
	public ToolBarPlugin(iMainApp app) 
	{
		super(app);
		button.setIcon( getPluginIcon() );
		button.addActionListener(this);
	}
	
	/** icon name is used to determine the icon for this plugin.
	 *  The icon name can be either a file name of a png picture from resources/icons folder
	 *  or a full or relative file path to an image file to be the icon
	 * */
	public abstract String getIconName();

	@Override public Icon getPluginIcon(){return ResourceLoader.getIcon( getIconName() );}
	@Override public List<Component> getDBMenuComponents(){return null;}
	@Override public List<Component> getFSMenuComponents(){return null;}
	@Override public List<Component> getToolBarComponents(){return Collections.singletonList((Component)button);}
	@Override public void actionPerformed(ActionEvent e){if(e.getSource().equals(button)){runThread();}}
}
