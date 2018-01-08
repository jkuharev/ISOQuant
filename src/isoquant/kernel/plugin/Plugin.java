package isoquant.kernel.plugin;

import isoquant.interfaces.iMainApp;
import isoquant.interfaces.plugin.iPlugin;

import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import de.mz.jk.jsix.ui.iProcessProgressListener;
import de.mz.jk.jsix.utilities.ResourceLoader;
import de.mz.jk.jsix.utilities.Settings;

/**
 * <h3>Plugin</h3>
 * abstract isoquant plugin with common functionality
 * @author Joerg Kuharev
 * @version 30.12.2010 10:51:25
 *
 */
public abstract class Plugin implements iPlugin, Runnable
{
	/** reference to the main application */
	protected iMainApp app = null;
	/** reference to a process progress listener */
	protected iProcessProgressListener progressListener = null;
	private Thread thread = null;

	/** enforce subclasses to set main application */
	public Plugin(iMainApp app)
	{
		setMainApp(app);
	}

	/** full name of this class is the default plugin name **/
	@Override public String getPluginName()
	{
		return this.getClass().getName();
	}

	@Override public void setMainApp(iMainApp app)
	{
		this.app = app;
		setProgressListener(app.getProcessProgressListener());
	}

	/** an implementation should use this method for loading user defined paremeters */
	public void loadSettings(Settings cfg)
	{}

	@Override public iMainApp getMainApp()
	{
		return app;
	}

	@Override public void setProgressListener(iProcessProgressListener pl)
	{
		progressListener = pl;
	}

	/** wrap runPlugin with start/end progress and exception handling */
	@Override public void run()
	{
		loadSettings(app.getSettings());
		if (progressListener != null) progressListener.startProgress();
		try
		{
			runPluginAction();
		}
		catch (Exception e)
		{
			System.out.println(this.getPluginName() + " caused an error:");
			e.printStackTrace();
		}
		if (progressListener != null) progressListener.endProgress();
	}

	/**
	 * build path for using in ClassLoader 
	 * from given file name and the package of current plugin.<br> 
	 * @param fileName
	 * @return
	 */
	public String getPackageResource(String fileName)
	{
		return this.getClass().getPackage().getName().replaceAll("\\.", "/") + "/" + fileName;
	}

	/**
	 * create a menu item with given label and icon 
	 * and this plugin is the assigned action listener 
	 * @param label
	 * @param icon
	 * @return new JMenuItem
	 */
	public JMenuItem createMenuItem(String label, String icon)
	{
		JMenuItem i = new JMenuItem();
		i.setText(label);
		i.setIcon(ResourceLoader.getIcon(icon));
		i.addActionListener((ActionListener) this);
		return i;
	}

	@Override public Thread getThread()
	{
		return this.thread;
	}

	@Override public Thread runThread()
	{
		thread = new Thread(this);
		thread.start();
		return getThread();
	}

	@Override public Thread runAfterThread(Thread precursorThread)
	{
		if (precursorThread != null) try
		{
			precursorThread.join();
		}
		catch (InterruptedException e)
		{}
		return runThread();
	}

	@Override public int compareTo(iPlugin o)
	{
		return o.getExecutionOrder() - this.getExecutionOrder();
	}

	@Override public void shutdown(boolean waitForEnd)
	{
		try
		{
			if (thread != null && thread.isAlive())
			{
				if (waitForEnd)
					thread.join();
				else thread.interrupt();
			}
			else ;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * create an instance of {@link Plugin} from an app and plugin's className
	 * @param app main app to be passed to the constructor of a plugin
	 * @param className plugin's class name
	 * @return {@link Plugin} object
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked") public static Plugin revive(iMainApp app, String className) throws Exception
	{
		Class c4p = null;
		try
		{
			c4p = Class.forName(className);
			Plugin plg = (Plugin) c4p.getConstructor(iMainApp.class).newInstance(app);
			plg.loadSettings(app.getSettings());
			return plg;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw (e);
		}
	}

	@Override public String toString()
	{
		return this.getPluginName();
	}
}
