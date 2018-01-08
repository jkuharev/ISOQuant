/*******************************************************************************
 * THIS FILE IS PART OF ISOQUANT SOFTWARE PROJECT WRITTEN BY JOERG KUHAREV
 * 
 * Copyright (c) 2009 - 2013, JOERG KUHAREV and STEFAN TENZER
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 * must display the following acknowledgment:
 * This product includes software developed by JOERG KUHAREV and STEFAN TENZER.
 * 4. Neither the name "ISOQuant" nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY JOERG KUHAREV ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL JOERG KUHAREV BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
/** ISOQuant, isoquant.plugins.exporting, 04.08.2011 */
package isoquant.plugins.exporting;

import java.io.File;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import de.mz.jk.jsix.libs.XFiles;
import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.jsix.utilities.Settings;
import isoquant.app.Defaults;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

/**
 * <h3>{@link ProjectDataExporter}</h3>
 * @author Joerg Kuharev
 * @version 04.08.2011 13:51:10
 */
public class ProjectDataExporter extends SingleActionPlugin4DB
{
	int maxNumOfTableEntries = 100;
	private File outDir = null;
	private final String MODE_BASIC = "basic data";
	private final String MODE_FULL = "full data";
	private final String MODE_SLICE = "overview slice";

	public ProjectDataExporter(iMainApp app)
	{
		super(app);
	}

	@Override public void loadSettings(Settings cfg)
	{
		maxNumOfTableEntries = cfg.getIntValue("setup.db.export.tableSize", maxNumOfTableEntries, true);
		outDir = new File(cfg.getStringValue("setup.report.dir", iMainApp.appDir.replace("\\\\", "/"), false));
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		PrintStream out = null;
		Bencher t = new Bencher(true);
		System.out.println( "exporting data for project '" + p.data.title + "' ..." );
		try
		{
			File file = XFiles.chooseFile(
					"choose file for project database export",
					true,
					new File( outDir, p.data.title.replace( "\\W", "_" ) + ".iqdb" ),
					outDir,
					"iqdb",
					app.getGUI()
					);
			if (file == null) return;
			if (file.exists() && !XFiles.overwriteFileDialog(app.getGUI(), getPluginIcon())) return;
			String mode = chooseExportMode();
			if (mode == null) return;
			out = new PrintStream(file);
			out.println("-- ISOQuant version: " + Defaults.version());
			out.println( "-- ISOQuant project: '" + p.data.title + "'" );
			out.println( "-- schema=" + p.data.db );
			out.println("-- time=" + XJava.timeStamp());
			out.println();
			exportSchema(p.mysql, out);
			List<String> allTables = p.mysql.listTables();
			Map<String, String> allTablesTypes = p.mysql.listTableTypes();
			// export structure
			for (String table : allTables)
				exportTableStructure(p.mysql, table, out);
			List<String> tables = (mode == MODE_BASIC) ? Arrays.asList(app.getProjectManager().listBasicProjectTables()) : allTables;
			for (String table : tables)
			{
				if (allTablesTypes.containsKey(table) && allTablesTypes.get(table).toLowerCase().contains("table"))
					exportTableData(p.mysql, table, out, (mode == MODE_SLICE) ? maxNumOfTableEntries : 0);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (out != null) out.close();
		}
		System.out.println("data export duration: [" + t.stop().getSecString() + "]");
	}

	/**
	 * 
	 */
	private String chooseExportMode()
	{
		String[] options = { MODE_SLICE, MODE_FULL, MODE_BASIC };
		// save unprocessed?
		int userChoice = JOptionPane.showOptionDialog(
				// parentComponent, message, title, optionType, messageType,
// icon, options, initialValue)(
				null,
				"<html><b>What data state do you want to export?</b><hr>" +
						"<table>" +
						"\t<tr><td><b>'" + MODE_FULL + "'</b></td><td>- big file, </td><td>current data state.</td></tr>" +
						"\t<tr><td><b>'" + MODE_BASIC + "'</b></td><td>- small file, </td><td> unprocessed basic data.</td></tr>" +
						"\t<tr><td><b>'" + MODE_SLICE + "'</b></td><td>- tiny file, </td><td>first 100 entries of each database table.</td></tr>" +
						"\t<tr><td></td><td></td><td>!!! for error reporting only !!!</td></tr>" +
						"</table>",
				"export state choice",
				JOptionPane.PLAIN_MESSAGE,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[0]
				);
		return (userChoice < 0) ? null : options[userChoice];
	}

	/**
	 * @param prj
	 * @param out
	 * @throws Exception 
	 */
	private void exportSchema(MySQL db, PrintStream out) throws Exception
	{
		out.println("-- project management entry");
		out.println("DELETE FROM mass_projects.project WHERE db='" + db.getSchema() + "';");
		ResultSet rs = db.executeSQL("SELECT * FROM mass_projects.project WHERE db='" + db.getSchema() + "'");
		rs.next();
		out.println("INSERT INTO mass_projects.project SET " +
				// "`time`='"+rs.getString("time")+"'" +
				"`id`='" + rs.getString("id") + "'" +
				",`title`='" + rs.getString("title") + "'" +
				",`root`='" + rs.getString("root") + "'" +
				",`db`='" + rs.getString("db") + "'" +
				",`state`='" + rs.getString("state") + "';"
				);
		out.println("DROP DATABASE IF EXISTS `" + db.getSchema() + "`;");
		out.println("CREATE DATABASE `" + db.getSchema() + "`;");
		out.println("USE `" + db.getSchema() + "`;");
		out.println();
	}

	/**
	 * @param db
	 * @param table
	 * @param out 
	 */
	private void exportTableStructure(MySQL db, String table, PrintStream out) throws Exception
	{
		System.out.println("\texporting structure for table `" + table + "` ... ");
		out.println("-- structure for table `" + table + "`");
		out.println("DROP TABLE IF EXISTS `" + table + "`;");
		out.println(db.getFirstValue("SHOW CREATE TABLE `" + table + "`", 2) + ";\n");
	}

	/**
	 * @param db
	 * @param table
	 * @param out 
	 */
	private void exportTableData(MySQL db, String table, PrintStream out, int limitSize) throws Exception
	{
		System.out.println("\texporting data for `" + table + "` ... ");
		out.println("-- data for table `" + table + "`");
		List<String> cols = db.listColumns(table);
		ResultSet rs = db.executeSQL("SELECT * FROM `" + table + "`" + ((limitSize > 0) ? " LIMIT " + limitSize : ""));
		while (rs.next())
		{
			String insert = "INSERT INTO `" + table + "` VALUES ('" + rs.getString(1) + "'";
			for (int i = 2; i <= cols.size(); i++)
			{
				String value = rs.getString(i);
				insert += (value == null) ? ", null" : ",'" + value + "'";
			}
			insert += ");";
			out.println(insert);
		}
		out.println("OPTIMIZE TABLE `" + table + "`;");
	}

	@Override public String getMenuItemIconName()
	{
		return "export";
	}

	@Override public String getMenuItemText()
	{
		return "export to file";
	}

	@Override public int getExecutionOrder()
	{
		return 0;
	}

	@Override public String getPluginName()
	{
		return "DBProject Database Exporter";
	}
}
