/** ISOQuant_1.0, isoquant.plugins.plgs.importing.design, 30.03.2011*/
package isoquant.plugins.plgs.importing.design.tree.selection;


import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.mz.jk.plgs.data.Workflow;

/**
 * <h3>{@link ProjectDesignTreeSelectionListener}</h3>
 * listen to the TreeSelectionModel and 
 * remove different types of nodes from the selection
 * for allowing only selection of homogenous nodes<br>
 * @author Joerg Kuharev
 * @version 30.03.2011 16:42:16
 */
public class ProjectDesignTreeSelectionListener implements TreeSelectionListener
{
	public static final boolean DEBUG = false;

	@Override public void valueChanged( TreeSelectionEvent e )
	{
		TreeSelectionModel model = (e.getSource() instanceof TreeSelectionModel) 
			? (TreeSelectionModel) e.getSource()
			: ((JTree)(e.getSource())).getSelectionModel();

		if( e.getNewLeadSelectionPath()==null ) return;
		Class selType = e.getNewLeadSelectionPath().getLastPathComponent().getClass();
		
		TreePath[] paths = model.getSelectionPaths();
		
		if(DEBUG)
		{
			if(selType==Workflow.class)
			{
				System.out.println(
					"------------------------------ you have selected ------------------------------\n"+
					((Workflow)(e.getNewLeadSelectionPath().getLastPathComponent())).toString()
				);
			}
		}
		
		for(TreePath path : paths)
		{
			// remove different types of nodes from selection  
			if( !selType.equals( path.getLastPathComponent().getClass() ) )
			{ 
				model.removeSelectionPath(path);
			}
		}
	}
}
