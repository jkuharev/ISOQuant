/** ISOQuant, isoquant.plugins.configuration, 10.07.2013*/
package isoquant.plugins.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.ui.TableFactory;
import de.mz.jk.jsix.utilities.Settings;
import de.mz.jk.jsix.utilities.XGUIUtils;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

/**
 * <h3>{@link LoadConfigParamsFromAttachement}</h3>
 * @author kuharev
 * @version 10.07.2013 09:29:52
 */
public class LoadConfigParamsFromAttachement extends SingleActionPlugin4DB
{
	public LoadConfigParamsFromAttachement(iMainApp app)
	{
		super(app);
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		if (!p.mysql.tableExists("config_storage"))
		{
			app.showErrorMessage("no ISOQuant configuration parameters were found in project attachement!");
			return;
		}
		Map<String, String> dbCfg = p.mysql.getMap("SELECT k, v FROM `config_storage` ORDER BY k", "k", "v");
		Settings iqCfg = app.getSettings();
		Map<String, String[]> paramsToSet = new HashMap<String, String[]>();
		for (Object key : iqCfg.getKeys())
		{
			String k = (String) key;
			if (dbCfg.containsKey(key))
			{
				// parameter name, current value, history value
				paramsToSet.put(k, new String[] { iqCfg.getValue(k), XJava.decURL(dbCfg.get(k)) });
			}
		}
		// do we have parameters to set?
		if (paramsToSet.size() < 1)
		{
			app.showErrorMessage("no ISOQuant configuration parameters were found in project attachement!");
			return;
		}
		// show the list of params to be changed for user interaction
		Object[][] editedData = showParamList(paramsToSet);
		// if user accepts do
		if (editedData == null) return;
		// write changes to config
		for (int i = 0; i < editedData.length; i++)
		{
			String k = (String) editedData[i][0];
			String v = (String) editedData[i][2];
			System.out.println(k + " = " + v);
			iqCfg.setValue(k, v);
		}
	}

	/**
	 * @param paramsToSet
	 */
	private Object[][] showParamList(Map<String, String[]> params)
	{
		Object[][] td = new Object[params.size()][];
		ArrayList<String> paramList = new ArrayList<String>(params.keySet());
		Collections.sort(paramList);
		for (int i = 0; i < paramList.size(); i++)
		{
			String k = paramList.get(i);
			String[] v = params.get(k);
			td[i] = new Object[] { k, v[0], v[1] };
		}
		TableFactory tf = new TableFactory(td, new Object[] { "parameter", "current value", "attached value" }, new Boolean[] { false, false, true });
		final JScrollPane tabPane = tf.getScrollableTable(true);
		XGUIUtils.makeParentDialogResizable(tabPane);
		int ok = JOptionPane.showConfirmDialog(
				app.getGUI(),
				tabPane,
				"parameters to import from attachement",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, getPluginIcon()
				);
		return ok == JOptionPane.OK_OPTION ? tf.getData() : null;
	}

	@Override public String getMenuItemText()
	{
		return "load config from attachement";
	}

	@Override public String getMenuItemIconName()
	{
		return "config_from_db";
	}
}
