/** ISOQuant, isoquant.plugins.batch, 25.07.2013 */
package isoquant.plugins.batch;

import java.awt.Frame;

import javax.swing.Icon;

import de.mz.jk.jsix.ui.ListChooserDialog;
import isoquant.kernel.plugin.Plugin;
import isoquant.kernel.plugin.queue.PluginQueue;

/**
 * <h3>{@link ManualSelectionQueue}</h3>
 * @author kuharev
 * @version 25.07.2013 13:43:44
 */
public class ManualSelectionQueue extends PluginQueue
{
	private Frame parentGUI = null;

	public ManualSelectionQueue(String name, Icon icon, String[] pluginClasses, Frame parentGUI)
	{
		super(name, icon, pluginClasses);
		this.parentGUI = parentGUI;
	}

	@Override public Plugin[] getPlugins()
	{
		Plugin[] allPlugins = super.getPlugins();
		int[] si = ListChooserDialog.chooseItemsFrom(parentGUI, allPlugins, "select plugins to run", "plugin selection");
		Plugin[] selPlugins = new Plugin[si.length];
		for (int i = 0; i < si.length; i++)
			selPlugins[i] = allPlugins[si[i]];
		return selPlugins;
	}
}
