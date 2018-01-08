/** ISOQuant, isoquant.plugins.plgs.importing.design.tree.model, 07.04.2011*/
package isoquant.plugins.plgs.importing.design.tree.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.plgs.data.ExpressionAnalysis;
import de.mz.jk.plgs.data.Group;
import de.mz.jk.plgs.data.Sample;
import de.mz.jk.plgs.data.Workflow;
import isoquant.kernel.db.DBProject;

/**
 * <h3>{@link ProjectDesignDynamicTreeModel}</h3>
 * allow dynamically changes in structure of designed project 
 * @author Joerg Kuharev
 * @version 07.04.2011 09:22:13
 */
public class ProjectDesignDynamicTreeModel extends ProjectDesignTreeModel
{
	/**
	 * @param p
	 * @param tree
	 */
	public ProjectDesignDynamicTreeModel(DBProject p, JTree tree)
	{
		super(p, tree);
		ea.id = "user designed " + XJava.timeStamp("yyyyMMdd-HHmmss");
		ea.description = ea.name = ea.id;
		p.data.expressionAnalysisIDs.add( ea.id );
		p.data.selectedExpressionAnalysisIDs.add( ea.id );
	}
	
	@Override public Object append(TreePath parentPath, Object node)
	{
		return addNode(parentPath.getLastPathComponent(), parentPath, node);
	}

	@Override public Object append(TreePath parentPath, Object[] nodes)
	{		
		Object tar = parentPath.getLastPathComponent();
		if(nodes.length<1) return tar;
		for(Object node : nodes)
		{
			tar = addNode(tar, parentPath, node);
		}
		return tar;
	}
	
	private Object addNode(Object parentObject, TreePath parentPath, Object node)
	{
		if(node instanceof ExpressionAnalysis)
		{
			List<Group> groups = new ArrayList<Group>( ((ExpressionAnalysis)node).groups );
			for(Group g : groups)
			{
				parentObject = addGroup( g ); //, parentPath);
			}
		}
		else
		if(node instanceof Group)
		{
			parentObject = addGroup( (Group)node ); //, parentPath);
		}
		else
		if(node instanceof Sample)
		{
			parentObject = addSample( (Sample)node, parentObject, parentPath );
		}
		else
		if(node instanceof Workflow)
		{
			parentObject = addWorkflow( (Workflow)node, parentObject, parentPath );
		}

		return parentObject;
	}


	/**
	 * @param workflow
	 * @param parentObject
	 * @param srcPath
	 * @param tarPath
	 * @return
	 */
	private Object addWorkflow(Workflow workflow, Object parentObject, TreePath tarPath)
	{
		Sample tarSample = null;
		
		if( parentObject instanceof Sample ) // target sample already defined
		{
			tarSample = (Sample)parentObject;
		}
		else 
		if( tarPath.getPathCount()>2 ) // target sample exists
		{
			tarSample = (Sample) tarPath.getPathComponent(2);
		} 
		else // make new target sample 
		{
			Group tarGrp = (tarPath.getPathCount()>1) ? (Group) tarPath.getPathComponent(1): addNewGroup();
			tarSample = addNewSample( tarGrp );
			System.out.println("creating new sample");
		}
		
		tarSample.addWorkflow( workflow );
		
		return tarSample;
	}
	
	/**
	 * add sample to an existing group or create a new one
	 * @param sample
	 * @param parentObject
	 * @param tarPath
	 * @return the group to which the sample is added
	 */
	private Object addSample(Sample sample, Object parentObject, TreePath tarPath)
	{
		Group tarGrp = null;

		if( parentObject instanceof Group )
		{
			tarGrp = (Group)parentObject;
		}
		else
		{
			tarGrp = ( tarPath.getPathCount()>1 ) ? (Group) tarPath.getPathComponent(1) : addNewGroup();
		}

		// clone sample (why???)
		// sample = sample.getClone(true);
		// sample.setUniqueIdentity();
		
		// add sample to group
		tarGrp.addSample( sample );
		
		return tarGrp;
	}
	
	/**
	 * add group to expression analysis
	 * @param group
	 * @param tarPath
	 * @return
	 */
	private Object addGroup(Group group)//, TreePath tarPath)
	{
		ea.addGroup( group );
		return ea;
	}

	/**
	 * create new group and add it to expression analysis
	 * @return the group
	 */
	public Group addNewGroup()
	{
		Group g = new Group(ea, "new group " + ++newGroupCount);
		return g;
	}
	
	/**
	 * create new sample and add it to a host group
	 * @param parentGroup
	 * @return the sample
	 */
	public Sample addNewSample(Group parentGroup)
	{
		Sample s = new Sample(parentGroup, "new sample " + ++newSampleCount);
		return s;
	}
	
	/**
	 * create new sample in new group
	 * @return the sample
	 */
	public Sample addNewSample()
	{
		Group g = addNewGroup();
		Sample s = addNewSample(g);
		return s;
	}
}
