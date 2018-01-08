package isoquant.plugins.help;

import java.awt.Frame;

import de.mz.jk.jsix.ui.HelpWindow;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.plugin.ToolBarPlugin;

/**
 * <h3>ShowHelpPlugin</h3>
 * enable ISOQuant to show a help window
 * @author Joerg Kuharev
 * @version 29.12.2010 15:53:37
 *
 */
public class HelpWindowPresenter extends ToolBarPlugin
{
	private String helpFilePath = "isoquant/plugins/help/html/help.html";

	public HelpWindowPresenter(iMainApp app)
	{
		super(app);
	}

	@Override public void runPluginAction() throws Exception
	{
		HelpWindow hlpWin = new HelpWindow((Frame) app.getGUI(), helpFilePath);
		hlpWin.setSize(app.getGUI().getSize());
		hlpWin.showHelpWindow();
	}

	@Override public int getExecutionOrder()
	{
		return 0;
	}

	@Override public String getIconName()
	{
		return "help";
	}

	@Override public String getPluginName()
	{
		return "ISOQuant Help Viewer";
	}
}
