/** ISOQuant, isoquant.plugins.plgs.importing.design.tree.model, 07.04.2011*/
package isoquant.plugins.plgs.importing.design.tree.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import de.mz.jk.plgs.data.ExpressionAnalysis;
import de.mz.jk.plgs.data.Group;
import de.mz.jk.plgs.data.Sample;
import de.mz.jk.plgs.data.Workflow;
import isoquant.kernel.db.DBProject;

/**
 * <h3>{@link ProjectDesignStaticTreeModel}</h3>
 * @author Joerg Kuharev
 * @version 07.04.2011 15:43:08
 */
public class ProjectDesignStaticTreeModel extends ProjectDesignTreeModel
{
	public ProjectDesignStaticTreeModel(DBProject p, JTree tree)
	{
		super(p, tree);
		createLookUpTable();
	}

	private Map<Workflow, TreePath> lookUp = new HashMap<Workflow, TreePath>();
	
	/** store clones of original nodes in a look up table */ 
	private void createLookUpTable()
	{
		for(Group g : ea.groups)
		{
			Group _g = new Group( g );
			for(Sample s : g.samples)
			{
				Sample _s = new Sample( s );
				_s.group = _g;
				for(Workflow w : s.workflows)
				{
					lookUp.put( w, new TreePath(new Object[]{ea, _g, _s, w}) );
				}
			}
		}
	}
	
	@Override public Object append(TreePath parentPath, Object node)
	{
		List<Workflow> runs = new ArrayList<Workflow>();
		
		if(node instanceof ExpressionAnalysis)
		{
			for(Group g : ((ExpressionAnalysis)node).groups)
				for(Sample s : g.samples)
					for(Workflow w : s.workflows)
						runs.add(w);
		}
		else if(node instanceof Group)
		{
			for(Sample s : ((Group)node).samples)
				for(Workflow w : s.workflows)
					runs.add(w);
		}
		else if(node instanceof Sample)
		{
			for(Workflow w : ((Sample)node).workflows)
				runs.add(w);
		}
		else if(node instanceof Workflow)
		{
			runs.add((Workflow) node);
		}
		
		restoreRuns( runs );
		
		return lastRestoredNode;
	}
	
	Object lastRestoredNode = null;

	@Override public Object append(TreePath parentPath, Object[] nodes)
	{
		for(Object node : nodes) 
		{
			append(parentPath, node);
		}
		
		return lastRestoredNode;
	}
	
	/**
	 * @param runs
	 * @return
	 */
	private void restoreRuns(List<Workflow> runs)
	{
		for(Workflow run : runs)
		{
			restoreRun(run);
		}
	}

	/**
	 * @param run
	 */
	private void restoreRun(Workflow run)
	{
		TreePath path = lookUp.get( run );
		
		if( path==null )
		{
			addUnknownWorkflow( run );
			return;
		}

		// ea = path.getPathComponent(0)
		Group g = (Group) path.getPathComponent(1);
		Sample s = (Sample) path.getPathComponent(2);
		
		// ea always exists
		
		// group exists? if not: clone original group
		if( ! ea.groups.contains(g) )
		{
			g = new Group( g );
			ea.addGroup( g );
		}
		else
		{
			g = ea.groups.get( ea.groups.indexOf(g) );
		}
		
		// sample exists? if not clone original sample
		if(! g.samples.contains(s) )
		{
			s = new Sample( s );
			g.addSample( s );
		}
		else
		{
			s = g.samples.get( g.samples.indexOf(s) );
		}
		
		// add run to sample
		s.addWorkflow( run );
		
		lastRestoredNode = run;
	}

	private TreePath unknownWorkflowSamplePath = null;
	private void addUnknownWorkflow(Workflow run)
	{
		if(unknownWorkflowSamplePath == null)
		{
			Group g = new Group(ea, "unknown runs group");
			Sample s = new Sample(g, "unknown runs sample");
			unknownWorkflowSamplePath = new TreePath( new Object[]{ea, g, s} );
		}
		
		((Sample) unknownWorkflowSamplePath.getPathComponent(2)).addWorkflow(run);
	}
}
