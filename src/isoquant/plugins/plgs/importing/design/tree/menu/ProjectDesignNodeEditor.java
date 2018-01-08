/** ISOQuant, isoquant.plugins.plgs.importing.design.tree.menu, 11.04.2011*/
package isoquant.plugins.plgs.importing.design.tree.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;

import de.mz.jk.plgs.data.ExpressionAnalysis;
import de.mz.jk.plgs.data.Group;
import de.mz.jk.plgs.data.Sample;
import isoquant.plugins.plgs.importing.design.tree.editor.ExpressionAnalysisEditorPanel;
import isoquant.plugins.plgs.importing.design.tree.editor.GroupEditorPanel;
import isoquant.plugins.plgs.importing.design.tree.editor.SampleEditorPanel;
import isoquant.plugins.plgs.importing.design.tree.editor.UserTypeEditor;

/**
 * <h3>{@link ProjectDesignNodeEditor}</h3>
 * @author Joerg Kuharev
 * @version 11.04.2011 12:38:13
 */
public class ProjectDesignNodeEditor
{
	private UserTypeEditor<Sample> sampleEditor =  new SampleEditorPanel();
	private UserTypeEditor<Group> groupEditor = new GroupEditorPanel();
	private UserTypeEditor<ExpressionAnalysis> eaEditor = new ExpressionAnalysisEditorPanel();
	
	private Border border = BorderFactory.createEtchedBorder();
	
	public void editNode(Component parent, Object node)
	{
		UserTypeEditor ute = null;
		if(node instanceof ExpressionAnalysis)
		{
			ute = eaEditor;
		}
		else
		if(node instanceof Group)
		{
			ute = groupEditor;
		}
		else
		if(node instanceof Sample)
		{
			ute = sampleEditor;
		}
		else
		{
			return ;
		}
		
		JPanel panel = new JPanel( new BorderLayout() );
		JLabel panelLabel = new JLabel("please edit attributes then press [OK].");
		
		panelLabel.setForeground(Color.BLUE.darker());
		panelLabel.setHorizontalAlignment(JLabel.CENTER);
		
		JPanel edtPanel = ute.getEditorPanel();
		edtPanel.setBorder(border);
		ute.load( node );
		
		panel.add(edtPanel, BorderLayout.CENTER);
		panel.add(panelLabel, BorderLayout.SOUTH);
		
		int res = JOptionPane.showConfirmDialog(
			parent, 
			panel, 
			"edit " + ute.getTitle(), 
			JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.PLAIN_MESSAGE
		);
		if(res==JOptionPane.OK_OPTION) ute.save();
	}
	
}
