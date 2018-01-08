/** ISOQuant_1.0, isoquant.plugins.plgs.importing.design.tree.editor, 31.03.2011*/
package isoquant.plugins.plgs.importing.design.tree.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

/**
 * <h3>{@link ProjectDesignTreeInternalNodeEditorPanel}</h3>
 * @author Joerg Kuharev
 * @version 31.03.2011 09:13:08
 * @param <Type>
 */
public class ProjectDesignTreeInternalNodeEditorPanel<Type> extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 20110331L;
	
	JPanel pnlTitle = new JPanel( new BorderLayout() );
	JPanel pnlCommand = new JPanel( new FlowLayout(FlowLayout.CENTER) );
	JPanel pnlEditor = new JPanel();
	
	JLabel lblTitleText = new JLabel("", JLabel.CENTER);
	JLabel lblTitleIcon = new JLabel();
	
	JButton btnSave = new JButton("save");
	JButton btnReset = new JButton("reset");

	private UserTypeEditor<Type> userTypeEditor = null;
	
	/**
	 * init basic editor panel
	 */
	public ProjectDesignTreeInternalNodeEditorPanel( UserTypeEditor<Type> userTypeEditor )
	{
		this.userTypeEditor = userTypeEditor;
		
		btnSave.addActionListener(this);
		btnReset.addActionListener(this);
		
		pnlTitle.add( lblTitleIcon, BorderLayout.WEST );
		pnlTitle.add( lblTitleText, BorderLayout.CENTER );
		
		pnlTitle.setBorder(BorderFactory.createEtchedBorder());
		pnlTitle.setBackground(Color.BLUE.darker());
		lblTitleText.setForeground(Color.yellow.brighter());
		
		this.setBorder(BorderFactory.createLineBorder(Color.BLUE.darker(), 2));
		
		pnlCommand.add( btnSave );
		pnlCommand.add( btnReset );
		
		pnlCommand.setBorder(BorderFactory.createEtchedBorder());
		
		pnlEditor.add( userTypeEditor.getEditorPanel() );
		
		setLayout( new BorderLayout() );
		add( pnlTitle, BorderLayout.NORTH );
		add( pnlEditor, BorderLayout.CENTER );
		add( pnlCommand, BorderLayout.SOUTH );
		
		setTitleText( userTypeEditor.getTitle() );
	}
	
	@Override public void actionPerformed(ActionEvent e)
	{
		if(e.getSource().equals(btnSave))
		{
			if(userTypeEditor!=null) userTypeEditor.saveContent();
		}
		else if(e.getSource().equals(btnReset))
		{
			if(userTypeEditor!=null) userTypeEditor.loadContent();
		}
	}
	
	/** @return 
	 * @return the user object */
	public Type getUserObject()
	{ 
		return (Type) userTypeEditor.getUserObject(); 
	}
	
	/** set user object */
	public void setUserObject(Type userObject)
	{
		userTypeEditor.setUserObject(userObject);
		userTypeEditor.loadContent(); 
	}

	/** set the icon shown in the top left corner */
	public void setTitleIcon(Icon icon)
	{
		lblTitleIcon.setIcon(icon); 
	}
	
	/** set the text shown in the middle of title row */
	public void setTitleText(String text)
	{
		lblTitleText.setText(text); 
	}
}
