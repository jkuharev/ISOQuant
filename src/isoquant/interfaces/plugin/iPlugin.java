package isoquant.interfaces.plugin;

import isoquant.interfaces.iMainApp;

import java.awt.Component;
import java.util.List;

import javax.swing.Icon;

import de.mz.jk.jsix.ui.iProcessProgressListener;

/**
 * <h3>iPlugin</h3>
 * interface for isoquant plugins
 * @author Joerg Kuharev
 * @version 30.12.2010 10:45:58
 */
public interface iPlugin extends Comparable<iPlugin>
{
	/** name of this plugin
	 * @return the name */
	public String getPluginName();

	/** main icon representing this plugin
	 * @return the icon */
	public Icon getPluginIcon();

	/** define components for beeing added to the FS side plugin menu
	 * @return list of components */
	public List<Component> getFSMenuComponents();

	/** define components for beeing added to the db side context menu
	 * @return list of components */
	public List<Component> getDBMenuComponents();

	/** define components for beeing added to the tool bar
	 * @return list of components */
	public List<Component> getToolBarComponents();

	/** running main plugin action */
	public void runPluginAction() throws Exception;

	/** assign the main application
	 * @param app */
	public void setMainApp(iMainApp app);

	/** @return reference to the main application */
	public iMainApp getMainApp();

	/** manually assign the progress listener 
	 * @param pl */
	public void setProgressListener(iProcessProgressListener pl);

	/**
	 * The order of execution, 
	 * each plugin that has an ability to be run in a 
	 * PluginQueue has a positive execution order
	 * starting by 1 for project importing plugins. 
	 * <b>EXECUTION RULES:</b><br>
	 * Plugins A and B have a=A.getExecutionOrder() and b=B.getExecutionOrder().<br>
	 * We want to run A and B subsequently (e.g. in a PluginQueue).<br>
	 * The execution order is determined by comparing a and b:<br>
	 * <table border="1">
	 *  <tr><th>case</th><th>possible execution order</th></tr>
	 * 	<tr><td>a &lt; b</td><td>A then B</td></tr>
	 * 	<tr><td>a &gt; b</td><td>B then A</td></tr>
	 * 	<tr><td>a = b</td><td>A or B</td></tr>
	 * </table>
	 * <hr>
	 * @return the defined execution order
	 */
	public int getExecutionOrder();

	/**
	 * 
	 * @return thread in which the plugin's action is beeng/was executed or null if no thread defined */
	public Thread getThread();

	/**
	 * start execution of plugin's action in a thread
	 * @return the thread
	 */
	public Thread runThread();

	/**
	 * run execution in a thread after ending the precursor thread
	 * @param precursorThread
	 * @return thread in which this plugin's action is beeing executed
	 */
	public Thread runAfterThread(Thread precursorThread);

	/**
	 * stop executing all actions and close opened files and database connections
	 * @param waitForEnd if true wait until all actions have stopped
	 */
	public void shutdown(boolean waitForEnd);
}
