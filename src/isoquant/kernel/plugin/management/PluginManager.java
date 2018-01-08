package isoquant.kernel.plugin.management;

import isoquant.interfaces.iMainApp;
import isoquant.interfaces.plugin.iPluginManager;
import isoquant.kernel.plugin.Plugin;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * <h3>PluginManager</h3>
 * plugin management
 * 
 * @author Joerg Kuharev
 * @version 18.01.2011 13:30:48
 */
public class PluginManager implements iPluginManager
{
	public static void main(String[] args)
	{
		List<String> pluginClasses = findPluginClasses();
		int n = 0;
		for(String c : pluginClasses) System.out.println( ++n + "\t" + c);
	}
	
	private List<Plugin> pluginList = new ArrayList<Plugin>();
	private iMainApp app=null;
	
	public static String[] defaultPlugins = null;
	
	/**
	 * create plugin manager by main app and a list of plugins 
	 */
	public PluginManager(iMainApp app, String[] pluginClassNames)
	{
		init(app, pluginClassNames);
	}
	
	/**
	 * load list of plugins from application settings or use default plugins
	 * @param app
	 */
	public PluginManager(iMainApp app)
	{
		String[] classNames = null;
		
		// use dafaults
		classNames = defaultPlugins;
		
/*		app.getSettings().setArray("ISOQUANT_PLUGINS", defaultPlugins);	
 TODO dynamic plugin loading
 * DO NOT FORGET TO COMMENT UPPER LINE AND UNCOMMENT FOLLOWING 
		try{
			classNames = app.getSettings().getArray("ISOQUANT_PLUGINS", defaultPlugins);
		}catch(Exception e){
			classNames = defaultPlugins;
		}
*/
		init(app, classNames);
	}

	/**
	 * initialize by a main app and list of plugins
	 * @param app
	 * @param pluginClassNames
	 */
	private void init(iMainApp app, String[] pluginClassNames)
	{
		this.app = app;
		if(pluginClassNames!=null && pluginClassNames.length>0)
		for(String pc : pluginClassNames){ addPlugin(pc); }
	}
	
	
	/**
	 * add an initialized plugin object
	 * @param plugin
	 */
	@Override public void addPlugin(Plugin plugin) { pluginList.add(plugin); }

	@Override public Plugin addPlugin(String className) 
	{
		Plugin p = null;
		try {
			System.out.print("loading plugin '" + className + "' ... ");
			p = Plugin.revive(app, className);
			addPlugin(p);
			System.out.println("[ok]");
		} catch (Exception e) {
			System.out.println("[failed]");
			e.printStackTrace();
		}
		return p;
	}
	
	@Override public List<Plugin> getPlugins(){return pluginList;}

	@Override public List<Plugin> getPlugins(String name) 
	{
		List<Plugin> res = new ArrayList<Plugin>();
		for(Plugin p : pluginList) 
		{
			if( p.getPluginName().equals(name) ) res.add(p);
		}
		return res;
	}
	
	/**
	 * explore application root for Plugin implementations
	 * @return list of Plugin implementations
	 */
	public static List<String> findPluginClasses()
	{
		List<String> res = new ArrayList<String>();
		List<Class> allC = findClasses(new File(ClassLoader.getSystemResource("isoquant").getFile()).getParentFile(), ""); 
		for(Class c : allC)
		{
			if( isValidPluginClass(c) ) res.add( c.getName() );
		}
		return res;
	}
	
	public static boolean isValidPluginClass(Class c)
	{
		// reject interface and  
		if(c.isInterface() || Modifier.isAbstract( c.getModifiers() ) ) return false;
		// find if class implements iPlugin
		return Plugin.class.isAssignableFrom(c);
	}

	private static List<Class> findClasses(File directory, String packageName)
    {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) return classes;
        File[] files = directory.listFiles();
        
        for (File file : files) 
        {
            if (file.isDirectory()) 
            {
                assert !file.getName().contains(".");
                classes.addAll(
                	findClasses(file, 
                		((packageName==null || packageName=="") ? "" : packageName + "." ) + 
                		file.getName()
                	)
                );
            }
            else 
            if (file.getName().endsWith(".class")) 
            {
            	try
				{
					classes.add(Class.forName( 
						((packageName==null || packageName=="") ? "" : packageName + "." ) 	
						+ file.getName().substring(0, file.getName().length() - 6))
					);
				} catch (ClassNotFoundException e)
				{
					e.printStackTrace();
				}
            }
            else
            {
            	// some other file type
            }
        }
        return classes;
    }

}
