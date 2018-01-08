/** ISOQuant, isoquant.plugins.plgs.importing.design.tree.menu, 11.04.2011*/
package isoquant.plugins.plgs.importing.design.tree.menu;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import de.mz.jk.plgs.data.ExpressionAnalysis;
import de.mz.jk.plgs.data.Group;
import de.mz.jk.plgs.data.Sample;
import de.mz.jk.plgs.data.Workflow;
import isoquant.plugins.plgs.importing.design.tree.model.ProjectDesignDynamicTreeModel;
import isoquant.plugins.plgs.importing.design.tree.model.ProjectDesignStaticTreeModel;

/**
 * <h3>{@link ProjectDesignContextMenu}</h3>
 * @author Joerg Kuharev
 * @version 11.04.2011 10:52:35
 */
public class ProjectDesignContextMenu implements MouseListener, ActionListener
{
	/**  */
	private static final long serialVersionUID = 20110411L;

	private JTree tarTree = null;
	private ProjectDesignDynamicTreeModel tarModel = null;
	
	private JTree srcTree = null;
	private ProjectDesignStaticTreeModel srcModel = null;
	
	private JMenuItem mEdit = new JMenuItem("edit");
	private JMenuItem mRemove = new JMenuItem("remove");
	private JMenuItem mAddSample = new JMenuItem("add sample");
	private JMenuItem mAddGroup = new JMenuItem("add group");
	
	private Object selNode = null;
	private TreePath selPath = null;
	private TreePath[] selPaths = null;
	
	private ProjectDesignNodeEditor editor = null;
	
	private JPopupMenu menu = new JPopupMenu();
	
	/**
	 * @param tarTree
	 */
	public ProjectDesignContextMenu(JTree srcTree, JTree tarTree)
	{
		this.srcTree = srcTree;
		srcModel = ((ProjectDesignStaticTreeModel) srcTree.getModel());
		
		this.tarTree = tarTree;
		tarModel = ((ProjectDesignDynamicTreeModel) tarTree.getModel());
		
		tarTree.addMouseListener( this );
		
		mEdit.addActionListener(this);
		mRemove.addActionListener(this);
		mAddGroup.addActionListener(this);
		mAddSample.addActionListener(this);
		
		editor = new ProjectDesignNodeEditor();
	}
	
	@Override public void mouseEntered(MouseEvent e){}
	@Override public void mouseExited(MouseEvent e){}
	@Override public void mouseClicked(MouseEvent e){}
	
	/** called by OSX */
	@Override public void mousePressed(MouseEvent e){ mouseReleased(e); }

	/** called by Windows */
	@Override public void mouseReleased(MouseEvent e)
	{
		if( e.isPopupTrigger() ) 
		{
		 	selectPointed( e.getPoint() );
			showMenu(e.getX(), e.getY());
			e.consume();
	    }
	}
	
	private void showMenu(int x, int y)
	{
		menu.removeAll();
		if(selNode instanceof Workflow)
		{
			menu.add(mRemove);
		}
		else
		if(selNode instanceof Sample)
		{
			menu.add(mEdit);
			menu.add(mRemove);
		}
		else
		if(selNode instanceof Group)
		{
			menu.add(mEdit);
			menu.add(mAddSample);
			menu.add(mRemove);
		}
		else
		if(selNode instanceof ExpressionAnalysis)
		{
			menu.add(mEdit);
			menu.add(mAddGroup);
			menu.add(mRemove);
		}
		
		menu.show(tarTree, x, y);
	}

	/**
	 * @param point
	 */
	private void selectPointed(Point loc)
	{
		selPath = tarTree.getClosestPathForLocation(loc.x, loc.y);
		if( ! tarTree.isPathSelected(selPath) ) tarTree.setSelectionPath(selPath);
		selPaths = tarTree.getSelectionPaths();
		selNode = selPath.getLastPathComponent();
	}

	@Override public void actionPerformed(ActionEvent e)
	{	
		if( e.getSource().equals(mEdit) )
		{
			editNode();
		}
		else
		if( e.getSource().equals(mRemove) )
		{
			removeNodes();
		}
		else
		if( e.getSource().equals(mAddSample) )
		{
			addSample();
		}
		else
		if( e.getSource().equals(mAddGroup) )
		{
			addGroup();
		}
		srcTree.updateUI();
		tarTree.updateUI();
	}
	
	/**
	 * 
	 */
	private void addGroup()
	{
		tarModel.addNewGroup();
	}

	/**
	 * 
	 */
	private void addSample()
	{
		try{
			Group g = (Group) selPath.getPathComponent(1);
			tarModel.addNewSample(g);
		} catch (Exception e) {
			tarModel.addNewSample();
		}
	}

	/**
	 * 
	 */
	private void removeNodes()
	{
		Object[] nodes = tarModel.remove(selPaths);
		srcModel.append(null, nodes);
	}

	/**
	 * 
	 */
	private void editNode()
	{
		editor.editNode( tarTree, selNode );
		tarTree.updateUI();
	}
}
