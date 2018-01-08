/*
 * -----------------------------------------------------------------------------
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
 * -----------------------------------------------------------------------------
 */
/** ISOQuant, isoquant.plugins.db, 23.03.2012 */
package isoquant.plugins.db;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.ui.FormBuilder;
import de.mz.jk.jsix.ui.iProcessProgressListener;
import de.mz.jk.jsix.utilities.Bencher;
import isoquant.interfaces.iMainApp;
import isoquant.interfaces.iProjectManager;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

/**
 * enables renaming of projects
 * <h3>{@link ProjectDBCloner}</h3>
 * @author kuharev
 * @version 23.03.2012 10:48:15
 */
public class ProjectDBCloner extends SingleActionPlugin4DB
{
	private int[] repCountValues = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20 };
	private JPanel editorPanel = new JPanel();
	private JTextField txt4Name;
	private JTextField txt4Desc;
	private JCheckBox chbUseAll;
	private JComboBox repCount;

	public ProjectDBCloner(iMainApp app)
	{
		super(app);
		FormBuilder fb = new FormBuilder(editorPanel);
		repCount = fb.add("number of clones", new JComboBox(XJava.joinArray(repCountValues, ",").split(",")));
		repCount.setSelectedIndex(0);
		txt4Name = fb.add("title", new JTextField(25));
		txt4Desc = fb.add("description", new JTextField(25));
		fb.add("", "<html><i>you can use #i as placeholder for clone's index to<br>include it in title and description</i></html>", false, false);
		chbUseAll = fb.add("", new JCheckBox("include processed data"));
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		String title = "clone of " + p.data.title;
		String desc = XJava.decURL(p.mysql.getFirstValue("SELECT description FROM expression_analysis", 1));
		txt4Name.setText(title);
		txt4Desc.setText(desc);
		// show Dialog
		int res = JOptionPane.showConfirmDialog(
				app.getGUI(),
				editorPanel,
				"edit project clone properties",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE
				);
		// evaluate dialog
		if (res == JOptionPane.OK_OPTION)
		{
			int nClones = repCountValues[repCount.getSelectedIndex()];
			title = txt4Name.getText();
			desc = txt4Desc.getText();
			for (int i = 1; i <= nClones; i++)
			{
				String ci = (i < 10 ? "0" : "") + i;
				cloneProject(p, title.replaceAll("\\#i", ci), desc.replaceAll("\\#i", ci));
			}
		}
	}

	/**
	 * @param p
	 */
	private void cloneProject(DBProject p, String newTitle, String newDesc)
	{
		System.out.println( "cloning project: " + p.data.title );
		Bencher b = new Bencher(true);
		iProjectManager mgr = app.getProjectManager();
		DBProject np = p.clone();
		np.data.title = newTitle;
		np.data.db = "";
		mgr.addProject(np);
		List<String> tabs = (chbUseAll.isSelected()) ? p.mysql.listTables() : Arrays.asList(mgr.listBasicProjectTables());
		Map<String, String> types = p.mysql.listTableTypes();
		iProcessProgressListener ppl = app.getProcessProgressListener();
		ppl.setProgressMaxValue(tabs.size());
		for (int i = 0; i < tabs.size(); i++)
		{
			String src = tabs.get(i);
			ppl.setProgressValue(i);
			if (p.mysql.tableExists(src) && types.containsKey(src) && types.get(src).toLowerCase().contains("table"))
			{
				System.out.print("	cloning table " + src + " . . . ");
				Bencher tb = new Bencher(true);
				String tar = "`" + np.data.db + "`.`" + src + "`";
				p.mysql.optimizeTable(src);
				p.mysql.executeSQL("DROP TABLE IF EXISTS " + tar);
				p.mysql.executeSQL("CREATE TABLE " + tar + " LIKE `" + src + "`");
				p.mysql.executeSQL("INSERT IGNORE INTO " + tar + " SELECT * FROM `" + src + "` ");
				System.out.print(" updating indices . . . ");
				p.mysql.executeSQL("OPTIMIZE TABLE " + tar);
				System.out.println("[" + tb.stop().getSecString() + "]");
			}
		}
		// update in db project data
		np.mysql.getConnection();
		np.mysql.executeSQL("UPDATE project SET title='" + XJava.decURL(newTitle) + "'");
		np.mysql.executeSQL("UPDATE expression_analysis SET description='" + XJava.decURL(newDesc) + "', name='" + XJava.decURL(newDesc) + "'");
		np.mysql.closeConnection();
		System.out.println("project cloning duration: " + b.stop().getSecString());
		ppl.setProgressValue(0);
		app.updateDBProjects();
	}

	@Override public String getPluginName()
	{
		return "DBProject Cloner";
	}

	@Override public int getExecutionOrder()
	{
		return 0;
	}

	@Override public String getMenuItemText()
	{
		return "clone project";
	}

	@Override public String getMenuItemIconName()
	{
		return "db_clone";
	}
}
