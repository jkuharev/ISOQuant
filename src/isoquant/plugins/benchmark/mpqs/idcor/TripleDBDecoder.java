/** ISOQuant, isoquant.plugins.benchmark.mpqs.idcor, 09.10.2013 */
package isoquant.plugins.benchmark.mpqs.idcor;

import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DB;
import de.mz.jk.jsix.mysql.StdOutSQLBatchExecutionAdapter;
import de.mz.jk.jsix.ui.iProcessProgressListener;

/**
 * <h3>{@link TripleDBDecoder}</h3>
 * @author kuharev
 * @version 09.10.2013 16:18:59
 */
public class TripleDBDecoder extends SingleActionPlugin4DB
{
	/**
		 * @param app
		 */
	public TripleDBDecoder(iMainApp app)
	{
		super(app);
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		p.mysql.executeSQLFile(getPackageResource("init_xkey_table.sql"), null);
		p.mysql.executeSQLFile(getPackageResource("insert_xkey_data.sql"),
				new SQLExecListener(app.getProcessProgressListener(), 65534)
				);
		p.mysql.executeSQLFile(getPackageResource("update_proteins.sql"), null);
		app.getProcessProgressListener().setProgressValue(0);
	}

	@Override public String getMenuItemText()
	{
		return "decode MPQS protein ID";
	}

	@Override public String getMenuItemIconName()
	{
		return "clean_db";
	}
}

class SQLExecListener extends StdOutSQLBatchExecutionAdapter
{
	private int cnt = 0;
	private int stepSize = 1000;
	private int maxValue = 1000;
	private iProcessProgressListener progeressListener = null;

	public SQLExecListener(iProcessProgressListener progeressListener, int maxValue)
	{
		this.progeressListener = progeressListener;
		this.maxValue = maxValue;
		progeressListener.setProgressMaxValue(maxValue);
	}

	@Override public void sqlStatementExecutedNotification(String sql, long ms)
	{
		cnt++;
		if (cnt % stepSize == 0)
			progeressListener.setProgressValue(cnt);
		else if (cnt >= maxValue)
			progeressListener.setProgressValue(maxValue);
		super.sqlStatementExecutedNotification(sql, ms);
	}
}
