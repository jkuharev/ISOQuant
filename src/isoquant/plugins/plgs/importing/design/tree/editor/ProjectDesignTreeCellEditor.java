/** ISOQuant_1.0, isoquant.plugins.plgs.importing.design, 30.03.2011*/
package isoquant.plugins.plgs.importing.design.tree.editor;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.tree.TreeCellEditor;

import de.mz.jk.plgs.data.ExpressionAnalysis;
import de.mz.jk.plgs.data.Group;
import de.mz.jk.plgs.data.Sample;
import de.mz.jk.plgs.data.Workflow;

/**
 * <h3>{@link ProjectDesignTreeCellEditor}</h3>
 * @author Joerg Kuharev
 * @version 30.03.2011 17:07:09
 */
public class ProjectDesignTreeCellEditor implements TreeCellEditor
{
	private ProjectDesignTreeInternalNodeEditorPanel<Workflow> workflowEditor = 
		new ProjectDesignTreeInternalNodeEditorPanel<Workflow>( new WorkflowEditorPanel() );
	
	private ProjectDesignTreeInternalNodeEditorPanel<Sample> sampleEditor = 
		new ProjectDesignTreeInternalNodeEditorPanel<Sample>( new SampleEditorPanel() );
	
	private ProjectDesignTreeInternalNodeEditorPanel<Group> groupEditor = 
		new ProjectDesignTreeInternalNodeEditorPanel<Group>( new GroupEditorPanel() );
	
	private ProjectDesignTreeInternalNodeEditorPanel<ExpressionAnalysis> eaEditor = 
		new ProjectDesignTreeInternalNodeEditorPanel<ExpressionAnalysis>( new ExpressionAnalysisEditorPanel() );
	
	@Override public Component getTreeCellEditorComponent( JTree tree, Object node, boolean isSelected, boolean expanded, boolean leaf, int row )
	{
		Component res = tree.getCellRenderer().getTreeCellRendererComponent( tree, node, true, true, leaf, row, true );		
		
		if(isSelected)
		{
			if(node instanceof ExpressionAnalysis)
			{
				res = getExpressionAnalysisEditor( (ExpressionAnalysis)node );
			}
			else
			if(node instanceof Group)
			{
				res = getGroupEditor( (Group)node );
			}
			else
			if(node instanceof Sample)
			{
				res = getSampleEditor( (Sample)node );
			}
			else 
			if(node instanceof Workflow){ res = getWorkflowEditor((Workflow)node); }
		}
		
		return res;
	}

	/**
	 * @param node
	 * @return
	 */
	private Component getWorkflowEditor(Workflow node)
	{
		workflowEditor.setUserObject(node);
		return workflowEditor;
	}

	/**
	 * @param node
	 * @return
	 */
	private Component getSampleEditor(Sample node)
	{
		sampleEditor.setUserObject(node);
		return sampleEditor;
	}

	/**
	 * @param node
	 * @return
	 */
	private Component getGroupEditor(Group node)
	{
		groupEditor.setUserObject(node);
		return groupEditor;
	}

	/**
	 * @param node
	 * @return
	 */
	private Component getExpressionAnalysisEditor(ExpressionAnalysis node)
	{
		eaEditor.setUserObject(node);
		return eaEditor;
	}

	@Override public void addCellEditorListener( CellEditorListener l ){}
	@Override public void removeCellEditorListener( CellEditorListener l ){}
	@Override public boolean shouldSelectCell(EventObject e){return true;}

	@Override public void cancelCellEditing(){}
	@Override public Object getCellEditorValue(){ return ""; }
	
	@Override public boolean isCellEditable( EventObject e )
	{
		JTree tree = (JTree) e.getSource();
		return !( tree.getSelectionPath().getLastPathComponent() instanceof Workflow );
	}
	
	@Override public boolean stopCellEditing()
	{
		return true;
	}
}
