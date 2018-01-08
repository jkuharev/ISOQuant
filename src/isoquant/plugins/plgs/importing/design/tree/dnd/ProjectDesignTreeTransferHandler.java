/** ISOQuant, isoquant.plugins.plgs.importing.design.tree.dnd, 07.04.2011*/
package isoquant.plugins.plgs.importing.design.tree.dnd;

import isoquant.plugins.plgs.importing.design.ProjectDesignPanel;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

/**
 * <h3>{@link ProjectDesignTreeTransferHandler}</h3>
 * @author Joerg Kuharev
 * @version 07.04.2011 15:02:51
 */
public class ProjectDesignTreeTransferHandler extends TransferHandler implements Transferable
{
	private static final long serialVersionUID = 20110407L;

	public static void main(String[] args) throws Exception
	{
		ProjectDesignPanel.main(args);
	}
	
	JTree tree;
	String cmd="";

	public ProjectDesignTreeTransferHandler(JTree tree, String command)
	{
		this.cmd = command;
		this.tree = tree;
		
		tree.setDragEnabled( true );
		tree.setDropMode( DropMode.USE_SELECTION );
		
		tree.setTransferHandler(this);
	}
	
	@Override public int getSourceActions(JComponent c){ return COPY; }
	@Override protected Transferable createTransferable(JComponent c){ return this; }

	public static DataFlavor TREE_PATH_FLAVOR = new DataFlavor(TreePath.class, "Tree Path");
	public static DataFlavor TREE_PATH_ARRAY_FLAVOR = new DataFlavor(TreePath[].class, "Tree Path Array");
	public static DataFlavor[] flavors = { TREE_PATH_FLAVOR, TREE_PATH_ARRAY_FLAVOR };
	
	@Override public Object getTransferData(DataFlavor flavor){return cmd;}	
	@Override public DataFlavor[] getTransferDataFlavors(){return flavors;}
	
	@Override public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		for(DataFlavor df : flavors) if(flavor.equals(df)) return true;
		return false;
	}
}
