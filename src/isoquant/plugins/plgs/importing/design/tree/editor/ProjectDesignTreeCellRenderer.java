/** ISOQuant_1.0, isoquant.plugins.plgs.importing.design, 30.03.2011*/
package isoquant.plugins.plgs.importing.design.tree.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import de.mz.jk.plgs.data.ExpressionAnalysis;
import de.mz.jk.plgs.data.Group;
import de.mz.jk.plgs.data.Sample;
import de.mz.jk.plgs.data.Workflow;

/**
 * <h3>{@link ProjectDesignTreeCellRenderer}</h3>
 * @author Joerg Kuharev
 * @version 30.03.2011 16:19:14
 */
public class ProjectDesignTreeCellRenderer implements TreeCellRenderer
{
	DefaultTreeCellRenderer defRend = new DefaultTreeCellRenderer();
	JPanel cellPanel = new JPanel(new BorderLayout());
	JPanel textPanel = new JPanel(new BorderLayout());
	JLabel iconLabel = new JLabel();
	JLabel levelLabel = new JLabel("project");
	JLabel nameLabel = new JLabel("unnamed");
	Border borderSelected = BorderFactory.createLineBorder(Color.BLUE, 2);
	Border borderNormal = BorderFactory.createLineBorder(Color.WHITE);
	
	public ProjectDesignTreeCellRenderer()
	{
		levelLabel.setForeground( Color.BLUE );
		levelLabel.setFont( levelLabel.getFont().deriveFont(Font.BOLD) );
		
		textPanel.add( levelLabel, BorderLayout.WEST );
		textPanel.add( nameLabel, BorderLayout.EAST );
		
		cellPanel.add( iconLabel, BorderLayout.WEST );
		cellPanel.add( textPanel, BorderLayout.CENTER );
	}
	
	@Override public Component getTreeCellRendererComponent(JTree tree, Object node, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		if ((node != null)) 
		{
			iconLabel.setIcon(defRend.getDefaultOpenIcon());
			
			if(selected)
			{
				cellPanel.setBorder(borderSelected);
				
				cellPanel.setBackground(Color.LIGHT_GRAY);
				textPanel.setBackground(Color.LIGHT_GRAY);
				nameLabel.setForeground(Color.WHITE);
			}
			else
			{
				cellPanel.setBorder(borderNormal);
				cellPanel.setBackground(Color.WHITE);
				textPanel.setBackground(Color.WHITE);
				nameLabel.setForeground(Color.BLACK);
			}
			
			
			int res = -1;
			if(node instanceof ExpressionAnalysis)
			{
				ExpressionAnalysis _n = (ExpressionAnalysis)node;
				levelLabel.setText(" project: ");
				nameLabel.setText(_n.project.title);
			}
			else
			if(node instanceof Group)
			{
				Group _n = (Group)node;
				levelLabel.setText(" group: ");
				nameLabel.setText(_n.name);
			}
			else
			if(node instanceof Sample)
			{
				Sample _n = (Sample)node;
				levelLabel.setText(" sample: ");
				nameLabel.setText(_n.name);
			}
			else
			if(node instanceof Workflow)
			{
				Workflow _n = (Workflow)node;
				iconLabel.setIcon(defRend.getDefaultLeafIcon());
				levelLabel.setText(" run: ");
				nameLabel.setText(_n.sample_description + "||" + _n.acquired_name + "||" + _n.title);
				/*
				 * nameLabel.setText(
					"<html><table>" +
						"<tr><td align=right>input file:</td><td>" + _n.input_file + "</td></tr>" +
						"<tr><td align=right>sample description:</td><td>" + _n.sample_description + "</td></tr>" +
						"<tr><td align=right>replicate name:</td><td>" + _n.replicate_name + "</td></tr>" +
					"</table></html>"
				);
				*/
			}
			
			return cellPanel;
		}
		
		return defRend.getTreeCellRendererComponent(tree, node, selected, expanded, leaf, row, hasFocus);
	}
}
