/** ISOQuant, isoquant.plugins.db, 23.03.2012*/
package isoquant.plugins.db;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.ui.FormBuilder;
import isoquant.interfaces.iMainApp;
import isoquant.interfaces.iProjectManager;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

/**
 * enables renaming of projects
 * <h3>{@link ProjectRenamer}</h3>
 * @author kuharev
 * @version 23.03.2012 10:48:15
 */
public class ProjectRenamer extends SingleActionPlugin4DB 
{
	public ProjectRenamer(iMainApp app)
	{
		super(app);
		FormBuilder fb = new FormBuilder(editorPanel);
		txt4Name = fb.add("title", new JTextField(25) );
		txt4Desc = fb.add("description", new JTextField(25) );
	}

	private JPanel editorPanel = new JPanel();
	private JTextField txt4Name;
	private JTextField txt4Desc;
	
	@Override public void runPluginAction(DBProject p) throws Exception
	{
		String title = p.data.title;
		String desc = XJava.decURL( p.mysql.getFirstValue("SELECT description FROM expression_analysis", 1) );
		
		txt4Name.setText(title);
		txt4Desc.setText(desc);
		
		int res = JOptionPane.showConfirmDialog(
			app.getGUI(), 
			editorPanel, 
			"edit project properties", 
			JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.PLAIN_MESSAGE
		);
		
		if(res==JOptionPane.OK_OPTION) 
		{
			String newTitle = XJava.encURL( txt4Name.getText() );
			String newDesc = XJava.encURL( txt4Desc.getText() );
			
			p.mysql.executeSQL( "UPDATE project SET title='"+newTitle+"'" );
			p.mysql.executeSQL( "UPDATE expression_analysis SET description='"+newDesc+"', name='"+newDesc+"'" );
			
			iProjectManager mgr = app.getProjectManager();
			mgr.getManagementDB().executeSQL( "UPDATE project SET title='" + newTitle + "' WHERE `index`=" + p.data.index );
			
			app.updateDBProjects();
		}
	}

	@Override public String getPluginName(){return "DBProject Renamer";}
	@Override public int getExecutionOrder(){return 0;}
	@Override public String getMenuItemText(){return "rename project";}
	@Override public String getMenuItemIconName(){return "rename";}	
}
