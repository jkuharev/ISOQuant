/** ISOQuant_1.0, isoquant.plugins.plgs.importing.design, 31.03.2011*/
package isoquant.plugins.plgs.importing.design.tree;

import isoquant.kernel.db.DBProject;
import isoquant.plugins.plgs.importing.design.tree.editor.ProjectDesignTreeCellRenderer;
import isoquant.plugins.plgs.importing.design.tree.model.ProjectDesignDynamicTreeModel;
import isoquant.plugins.plgs.importing.design.tree.selection.ProjectDesignTreeSelectionListener;
import isoquant.plugins.plgs.importing.design.tree.selection.ProjectDesignTreeSelectionModel;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

/**
 * <h3>{@link ProjectDesignISOQuantProjectTreePanel}</h3>
 * @author Joerg Kuharev
 * @version 31.03.2011 16:52:35
 */
public class ProjectDesignISOQuantProjectTreePanel extends JPanel
{
	private static final long serialVersionUID = 20110331L;
	
	private JTree tree = new JTree();
	private DBProject prj = null;

	public ProjectDesignISOQuantProjectTreePanel( DBProject project )
	{
		setBorder(BorderFactory.createTitledBorder("your free designed project structure"));
		setLayout(new BorderLayout());
		this.prj = project;
				
		tree.setModel( new ProjectDesignDynamicTreeModel(prj, tree) );
		tree.setCellRenderer( new ProjectDesignTreeCellRenderer() );
		tree.setSelectionModel( new ProjectDesignTreeSelectionModel() );
		tree.addTreeSelectionListener( new ProjectDesignTreeSelectionListener() );
		tree.setRowHeight( 0 );
		
		// uncomment for enabling inline node editor
		// tree.setCellEditor( new ProjectDesignTreeCellEditor() );
		// tree.setEditable( false );

		add( new JScrollPane(tree), BorderLayout.CENTER );
	}
	
	public JTree getTree(){ return tree; }
	public DBProject getProject(){ return prj; }
}
