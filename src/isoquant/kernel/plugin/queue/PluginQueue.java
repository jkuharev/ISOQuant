/** ISOQuant_1.0, isoquant.plugins.batch.queue, 18.02.2011*/
package isoquant.kernel.plugin.queue;

import isoquant.interfaces.iMainApp;
import isoquant.kernel.plugin.Plugin;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import de.mz.jk.jsix.utilities.ResourceLoader;

/**
 * <h3>{@link PluginQueue}</h3>
 * @author Joerg Kuharev
 * @version 18.02.2011 10:13:58
 */
public class PluginQueue implements Serializable
{
//	public static void main(String[] args) throws Exception
//	{
//		File allPluginsFile = new File("all_plugins.obj");
//
//		String[] all = new String[]
//       	{
//       		ProjectFinder.class.getName(),
//       		ProjectSelectionSynchronizer.class.getName(),
//       		HelpWindowPresenter.class.getName(),
//       		PLGSProjectExplorer.class.getName(),
//       		ProjectDBExplorer.class.getName(),
//       		
//       		ProjectImporterByExpressionAnalysisAndBlusterResultsImporter.class.getName(),
//       		ProjectImporterByExpressionAnalysis.class.getName(),
//       		ProjectImporterByUserDesign.class.getName(),
//       		EMRTTableFromMassSpectrumCreator.class.getName(),
//       		
//       		ImportAndProcessBatcher.class.getName(),
//       		EMRTTableLinker.class.getName(),
//       		ProteinStatisticsMaker.class.getName(),
//       		EMRTClusterAnnotator.class.getName(),
//       		IntensityNormalizer.class.getName(),
//       		TopXQuantifier.class.getName(),
//       		ExpressionAnalysisPlugin.class.getName(),
//       		RetentionTimeAlignmentPlugin.class.getName(),
//       		EMRTClusteringPlugin.class.getName(),		
//       		HTMLOnePageReporter.class.getName(),
//       		HTMLMultiPageReporter.class.getName(),
//       		ProjectRemover.class.getName()
//       	};
//		
//		PluginQueue q = new PluginQueue("available\nplugins", all);
//		
//		//q.serialize(allPluginsFile);
//		// q = PluginQueue.unserialize(new MainAppAdapter(), allPluginsFile);
//		
//		PluginQueue.saveToFlatFile(q, "all_plugins.txt");
//		
//		q = PluginQueue.loadFromFlatFile(new MainAppAdapter(), "all_plugins.txt");
//		
//		System.out.println(q.name);
//		Plugin[] ps = q.getPlugins();
//		for(Plugin plugin : ps)
//		{
//			System.out.println( "\t" + plugin.getPluginName());
//		}
//	}
	
	@Override public String toString()
	{
		String res = "";
		
		if(plugins!=null)
		{
			for(int i=0; i<plugins.length; i++)
			{
				res += ((i>0) ? ", " : "") + plugins[i].getPluginName();
			}
		}
		else
		{
			for(int i=0; i<pluginClasses.length; i++)
			{
				res += ((i>0) ? ", " : "") + pluginClasses[i];
			}
		}
		
		return res;
	}
	
	private static final long serialVersionUID = 20110218L;
	private String name="unnamed";
	
	private String[] pluginClasses = new String[]{};
	private transient Plugin[] plugins = null;
	private Icon icon = ResourceLoader.getIcon("queue_small");

	private boolean forFS = true;
	
	public PluginQueue(){}
	
	public PluginQueue(String name)
	{
		setName(name);
	}
	
	public PluginQueue(String name, String[] pluginClasses)
	{
		setName(name);
		setPluginClasses(pluginClasses);
	}

	public PluginQueue(String name, Icon icon, String[] pluginClasses)
	{
		setName(name);
		setPluginClasses(pluginClasses);
		setIcon(icon);
	}		
	
	public PluginQueue(String name, Plugin[] plugins)
	{
		setName(name);
		setPlugins(plugins);
	}
	
	public PluginQueue(String name, List<Plugin> plugins)
	{
		setName(name);
		setPlugins(plugins);
	}
	
	/**
	 * define Icon for this queue
	 * @param icon
	 */
	public void setIcon(Icon icon){this.icon=icon;}
	
	/**
	 * @return the icon for this queue
	 */
	public Icon getIcon(){return icon;}
	
	/**
	 * get name of queue 
	 * @return
	 */
	public String getName(){return name;}
	
	/**
	 * name the queue
	 * @param name
	 */
	public void setName(String name){this.name=name;}
	
	public String[] getPluginClasses(){return pluginClasses;}
	
	/**
	 * set plugin class names
	 * @param pluginClasses
	 */
	public void setPluginClasses(String[] pluginClasses){this.pluginClasses = pluginClasses;}
	
	/**
	 * set plugin class names
	 * @param pluginClasses
	 */
	public void setPluginClasses(List<String> pluginClasses)
	{
		this.pluginClasses = new String[pluginClasses.size()];
		for(int i = 0; i<pluginClasses.size(); i++)	
			this.pluginClasses[i] = pluginClasses.get(i);
	}
	
	/**
	 * get plugins 
	 * @return plugins
	 */
	public Plugin[] getPlugins(){return plugins;}
	
	/**
	 * set plugins
	 * @param plugins
	 */
	public void setPlugins(Plugin[] plugins)
	{
		this.plugins=plugins;
		this.pluginClasses = new String[plugins.length];
		for (int i=0; i<plugins.length; i++)
		{
			pluginClasses[i] = plugins[i].getClass().getName();
		}
	}
	
	/**
	 * set plugins for this queue
	 * @param plugins
	 */
	public void setPlugins(List<Plugin> plugins)
	{
		Plugin[] ps = new Plugin[plugins.size()];
		for (int i=0; i<plugins.size(); i++)
		{
			ps[i] = plugins.get(i); 
		}
		setPlugins(ps);
	}
	
	/**
	 * revive plugins from class names 
	 * by instantiating corresponding plugin objects using given main app
	 * @param app the main app
	 */
	public void revivePlugins(iMainApp app)
	{
		List<Plugin> pl = new ArrayList<Plugin>();
		for(String className : pluginClasses)
		{
			try
			{
				Plugin p = Plugin.revive(app, className);
				pl.add(p);
			}
			catch(Exception e)
			{
				System.err.println("revival error: " + className);
			}
		}
		setPlugins(pl);
	}
	
	/**
	 * serialize this queue into a file
	 * @param file
	 * @throws Exception
	 */
	public void serialize(File file) throws Exception
	{
		ObjectOutputStream oos = new ObjectOutputStream( 
			new BufferedOutputStream(new FileOutputStream(file)) 
		);
		oos.writeObject(this);
		oos.close();
	}
	
	/**
	 * restore serialized plugin queue from file, 
	 * queued plugins are revived by using given main app
	 * @param app the main app
	 * @param file the serialization file
	 * @return the restored plugin queue
	 * @throws Exception
	 */
	public static PluginQueue unserialize(iMainApp app, File file) throws Exception
	{
		ObjectInputStream ois = new ObjectInputStream(
			new BufferedInputStream(new FileInputStream(file))
		);
		PluginQueue q = (PluginQueue) ois.readObject();
		ois.close();
		q.revivePlugins(app);
		return q;
	}
	
	/**
	 * restore serialized plugin queue from file,<br>
	 * !!! plugins are not revived: use queue.revivePlugins(app) !!!
	 * @param file the serialization file
	 * @return the restored plugin queue
	 * @throws Exception
	 */
	public static PluginQueue unserialize(File file) throws Exception
	{
		ObjectInputStream ois = new ObjectInputStream(
			new BufferedInputStream(new FileInputStream(file))
		);
		PluginQueue q = (PluginQueue) ois.readObject();
		ois.close();
		return q;
	}
	
	/**
	 * save the plugin queue to a flat file having:<br>
	 * the name of queue in the first line followed
	 * by plugin class names each name in a separate line
	 * @param q
	 * @param f
	 */
	public static void saveToFlatFile(PluginQueue q, String fileName)
	{
		try
		{
			PrintStream out = new PrintStream( fileName );
			out.println(q.name.replaceAll("\n", "\\\\n"));
			for(String c : q.pluginClasses) out.println(c);
			out.flush();
			out.close();
		} 
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * load a plugin queue from a flat text file
	 * @param fileName
	 * @return
	 */
	public static PluginQueue loadFromFlatFile(String fileName)
	{
		try
		{
			BufferedReader br = 
				new BufferedReader(
					new InputStreamReader(
						new FileInputStream( fileName )
					)
				);
			
			PluginQueue q = new PluginQueue();
			String line = "";
			List<String> classNames = new ArrayList<String>();

			for(int i=0; (line=br.readLine())!=null; i++)
			{
				if(i==0) 
					q.name = line.replace("\\n", "\n");
				else
					classNames.add(line);
			}
			
			q.setPluginClasses(classNames);

			return q;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			return new PluginQueue();
		}
	}
	
	/**
	 * load queue from a flat text file and then revive plugins
	 * @param app
	 * @param fileName
	 * @return
	 */
	public static PluginQueue loadFromFlatFile(iMainApp app, String fileName)
	{
		PluginQueue q = loadFromFlatFile(fileName);
		q.revivePlugins(app);
		return q;
	}
}
