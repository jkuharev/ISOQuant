/** ISOQuant_1.0, isoquant.plugins.plgs.importing.design.tree.model, 04.04.2011 */
package isoquant.plugins.plgs.importing.design.tree.dnd;

import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;

import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import isoquant.plugins.plgs.importing.design.tree.model.ProjectDesignTreeModel;

/**
 * <h3>{@link ProjectDesignDualTreeDragAndDropHandler}</h3>
 * @author Joerg Kuharev
 * @version 04.04.2011 11:28:41
 */
public class ProjectDesignDualTreeDragAndDropHandler extends DropTargetAdapter
{

	private JTree tarTree = null;
	private JTree srcTree = null;
	
	public ProjectDesignDualTreeDragAndDropHandler( JTree srcTree, JTree tarTree )
	{
		this.tarTree = tarTree;
		this.srcTree = srcTree;
		
		new ProjectDesignTreeTransferHandler( srcTree, "src" );
		new ProjectDesignTreeTransferHandler( tarTree, "tar" );
		
		tarTree.setDropTarget( new DropTarget(tarTree, TransferHandler.MOVE, this) );
		srcTree.setDropTarget( new DropTarget(srcTree, TransferHandler.MOVE, this) );
	}
	
	@Override public void drop(DropTargetDropEvent e)
	{
		try
		{
			String cmd = e.getTransferable().getTransferData( ProjectDesignTreeTransferHandler.TREE_PATH_ARRAY_FLAVOR ).toString();
		
			JTree _tarTree = ((JTree)((DropTarget)e.getSource()).getComponent());
			JTree _srcTree = ((cmd.equals("src")) ? srcTree : tarTree);
			
			TreePath[] srcPaths = _srcTree.getSelectionPaths();
			TreePath tarPath = _tarTree.getClosestPathForLocation( e.getLocation().x, e.getLocation().y );
			
			/*
			Object[] srcNodes = new Object[srcPaths.length];
			for (int i=0; i<srcNodes.length; i++)
			{
				srcNodes[i] = srcPaths[i].getLastPathComponent();
			}
			*/
			
			ProjectDesignTreeModel srcModel = ((ProjectDesignTreeModel)_srcTree.getModel());
			ProjectDesignTreeModel tarModel = ((ProjectDesignTreeModel)_tarTree.getModel());
			
			Object[] srcNodes = srcModel.remove( srcPaths );			
			Object realParentNode = tarModel.append( tarPath, srcNodes );
			
			srcTree.updateUI();
			tarTree.updateUI();
			
			TreePath realParentPath = tarModel.getPath(realParentNode);
			
			tarTree.expandPath( realParentPath );
			tarTree.scrollPathToVisible( realParentPath );
			
			e.acceptDrop( e.getDropAction() );
		} 
		catch (Exception ex)
		{
			ex.printStackTrace();
			// System.err.println( ex.getMessage() );
			e.rejectDrop();
		}
		e.dropComplete(true);
	}
}
