/** ISOQuant_1.0, isoquant.plugins.plgs.importing.design, 30.03.2011*/
package isoquant.plugins.plgs.importing.design.tree.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import de.mz.jk.plgs.data.ExpressionAnalysis;
import de.mz.jk.plgs.data.Group;
import de.mz.jk.plgs.data.Sample;
import de.mz.jk.plgs.data.Workflow;
import isoquant.kernel.db.DBProject;

/**
 * <h3>{@link ProjectDesignTreeModel}</h3>
 * abstract tree model for displaing PLGS object hierarchy in a JTree by keeping following relations: 
 * DBProject = ExpressionAnalysis -> Groups -> Samples -> Runs (Workflows)
 * @author Joerg Kuharev
 * @version 30.03.2011 13:37:48
 */
public abstract class ProjectDesignTreeModel implements TreeModel
{
	private static final long serialVersionUID = 20110330L;

	protected ExpressionAnalysis ea = null;
	protected DBProject prj = null;
	protected JTree tree = null;
	
	public int newGroupCount = 0;
	public int newSampleCount = 0;
	
	/**
	 * 
	 * @param p
	 * @param tree
	 */
	public ProjectDesignTreeModel(DBProject p, JTree tree)
	{
		this.prj = p;
		this.tree  = tree;
		
		ea = new ExpressionAnalysis( prj.data, prj.data.title );
		
		// load samples
		if (prj.data.samples.size() > 0)
		{
			Group g = new Group(ea, "default group");
						
			for ( Sample s : prj.data.samples )
			{
				g.addSample( s );
				
				for(Workflow w : s.workflows)
				{
					w.sample = s;
				}
			}
		}
	}
	
	/** @return the project */
	public DBProject getProject(){ return prj; }
	
	@Override public Object getChild(Object parent, int index)
	{
		try
		{
			Object res = null;
			if(parent instanceof ExpressionAnalysis)
			{
				res = ((ExpressionAnalysis)parent).groups.get(index);
			}
			else
			if(parent instanceof Group)
			{
				res = ((Group)parent).samples.get(index);
			}
			else
			if(parent instanceof Sample)
			{
				res = ((Sample)parent).workflows.get(index);
			}
			return res;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	@Override public int getChildCount(Object parent)
	{
		if(parent instanceof ExpressionAnalysis)
		{
			return ((ExpressionAnalysis)parent).groups.size();
		}
		else
		if(parent instanceof Group)
		{
			return ((Group)parent).samples.size();
		}
		else
		if(parent instanceof Sample)
		{
			return ((Sample)parent).workflows.size();
		}
		return 0;
	}

	@Override public int getIndexOfChild(Object parent, Object child)
	{
		try
		{
			int res = -1;
			if(parent instanceof ExpressionAnalysis)
			{
				res = ((ExpressionAnalysis)parent).groups.indexOf(child);
			}
			else
			if(parent instanceof Group)
			{
				res = ((Group)parent).samples.indexOf(child);
			}
			else
			if(parent instanceof Sample)
			{
				res = ((Sample)parent).workflows.indexOf(child);
			}
			return res;
		}
		catch (Exception e)
		{
			return -1;
		}
	}

	@Override public Object getRoot(){return ea;}
	@Override public boolean isLeaf(Object node){return (node instanceof Workflow);	}

	@Override public void addTreeModelListener(TreeModelListener l){}
	@Override public void removeTreeModelListener(TreeModelListener l){}
	@Override public void valueForPathChanged(TreePath path, Object newValue){}
	
	/**
	 * reconstruct a tree path from given node object
	 * @param node
	 * @return
	 */
	public TreePath getPath(Object node)
	{
		if(node instanceof Group)
		{
			return new TreePath(
				new Object[]{
					((Group)node).expressionAnalysis, 
					node
				}
			);
		}
		else
		if(node instanceof Sample)
		{
			return new TreePath(
				new Object[]{
					((Sample)node).group.expressionAnalysis,
					((Sample)node).group,
					node
				}
			);
		}
		else
		if(node instanceof Workflow)
		{
			return new TreePath(
				new Object[]{
					((Workflow)node).sample.group.expressionAnalysis,
					((Workflow)node).sample.group,
					((Workflow)node).sample,
					node
				}
			);
		}
		
		return ( node==null ) ? null : new TreePath( node );
	}
	
	/**
	 * append new child node to a local path
	 * @param targetPath local path receiving new child path
	 * @param node the child node to be appended
	 * @return node object from the (really) used parentPath
	 */
	public abstract Object append(TreePath targetPath, Object node);
	
	/**
	 * append new child paths to a local path
	 * @param targetPath local path receiving new child paths
	 * @param nodes the child nodes to be appended
	 * @return node object from the (really) used parentPath
	 */
	public abstract Object append(TreePath targetPath, Object[] nodes);
	
	/**
	 * remove a path from tree
	 * @param path
	 * @return the node object removed
	 */
	private List<Object> remove(TreePath path)
	{
		List<Object> res = new ArrayList<Object>();
		Object node = path.getLastPathComponent();
		
		if(node instanceof ExpressionAnalysis)
		{
			List<Group> grps = new ArrayList<Group>(ea.groups);
			for(Group g : grps)
			{
				ea.groups.remove( g );
				g.expressionAnalysis = null;
				res.add(g);
			}
			return res;
		}
		else
		if(node instanceof Group)
		{
			ea.removeGroup( (Group)node );
		}
		else
		if(node instanceof Sample)
		{
			Sample s = (Sample)node;
			if(s.group!=null) s.group.removeSample(s);
		}
		else
		if(node instanceof Workflow)
		{
			Workflow w = (Workflow)node;
			if( w.sample!=null ) w.sample.removeWorkflow(w);
		}
		
		if(node!=null) res.add( node );
		
		return res;
	}

	/**
	 * remove multiple paths from tree
	 * @param paths
	 * @return array of removed node objects
	 */
	public Object[] remove(TreePath[] paths)
	{
		List<Object> res = new ArrayList<Object>();
		for(TreePath path : paths)
		{
			res.addAll( remove(path) );
		}		
		return res.toArray();
	}
}
