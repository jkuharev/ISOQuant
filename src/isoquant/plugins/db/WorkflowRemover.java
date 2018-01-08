/** ISOQuant, isoquant.plugins.processing.restructuring, 12.03.2013*/
package isoquant.plugins.db;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.*;

import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

/**
 * <h3>{@link WorkflowRemover}</h3>
 * @author kuharev
 * @version 12.03.2013 09:20:04
 */
public class WorkflowRemover extends SingleActionPlugin4DB
{
	static UIDefaults defs = UIManager.getDefaults();
	
	private ProjectCleaner cleaner = null;
	
	/**
	 * @param app
	 */
	public WorkflowRemover(iMainApp app)
	{
		super(app);
		cleaner = new ProjectCleaner( app );
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		showDialog(Collections.singletonList(p));
	}

	@Override public void actionPerformed(ActionEvent e)
	{
		showDialog( app.getSelectedDBProjects() );
	}
	
	private void showDialog(List<DBProject> ps)
	{
		JDialog win = new JDialog((Frame) app.getGUI());
		win.setTitle("DBProject attributes");
		win.setModal(true);
		
		JTabbedPane tabPane = new JTabbedPane();
		
		for(DBProject p : ps) 
		{
			tabPane.addTab( p.data.title, new WorkflowRemoverPanel( p, cleaner ) );
		}
		
		win.setContentPane( tabPane );			
		win.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		
		win.pack();
		win.setSize( win.getHeight(), win.getWidth() + 50 );
		win.setLocationRelativeTo( app.getGUI() );
		win.setVisible( true );
	}
	
	@Override public String getPluginName(){ return "Workflow Remover"; }
	@Override public String getMenuItemText(){ return "remove runs from project"; }
	@Override public String getMenuItemIconName(){ return "remove_runs"; }
	@Override public int getExecutionOrder(){return 0;}
}
