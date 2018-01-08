/** ISOQuant_1.0, isoquant.plugins.configuration, 07.03.2011*/
package isoquant.plugins.configuration;

import java.awt.BorderLayout;

import javax.swing.*;

/**
 * <h3>{@link ConfigurationEditorPanel}</h3>
 * @author Joerg Kuharev
 * @version 07.03.2011 10:46:53
 */
public class ConfigurationEditorPanel extends JPanel 
{
	public static void main(String[] args)
	{
		JDialog dlg = new JDialog();
		dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dlg.setSize(640, 480);
		dlg.setContentPane( new ConfigurationEditorPanel() );
		dlg.setVisible(true);
	}	
	
    private JList propList = new JList();
    private JButton btnCancel = new JButton("Cancel");
    private JButton btnReset = new JButton("Reset");
    private JButton btnSave = new JButton("Save");
    private JButton btnSaveTo = new JButton("Save To");
    private JPanel buttonPanel = new JPanel();
    private JScrollPane propScrollPanel = new JScrollPane();
    private JComboBox profileChooserComboBox = new JComboBox();
	
    public ConfigurationEditorPanel() 
    {
        initComponents();
    }
                    
    private void initComponents() 
    {
        setLayout(new BorderLayout());

        profileChooserComboBox.setBorder( BorderFactory.createEtchedBorder() );
        profileChooserComboBox.setModel(
        	new DefaultComboBoxModel(new String[]{ "Item 1", "Item 2", "Item 3", "Item 4" })
        );

        propList.setBorder( BorderFactory.createEtchedBorder() );
        propList.setListData(
        	new String[]{"Variable 1", "2", "3", "..."}
        );
        
        buttonPanel.setBorder(BorderFactory.createEtchedBorder());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnSaveTo);
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnReset);
        
        add(profileChooserComboBox, BorderLayout.NORTH);
        add(new JScrollPane(propList), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
