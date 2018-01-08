package isoquant.kernel.plugin.queue;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class PluginQueueEditorPanel extends JPanel implements ActionListener 
{
	private static final long serialVersionUID = 20110220L;
	
	private JPanel leftPanel = new JPanel();
	private JPanel middlePanel = new JPanel();
	private JPanel rightPanel = new JPanel();
	
	private JList availablePlunginList = new JList(
		new String[]{"Plugin A", "B", "C", "..."}
	);
	private JList queuedPlunginList = new JList(
		new String[]{"Plugin A", "B", "C", "..."}
	);
	
	private JButton btnAddAllPlugins = new JButton(">>");
	private JButton btnAddPlugin = new JButton("> ");
	private JButton btnRemovePlugin = new JButton(" <");
	private JButton btnRemoveAllPlugins = new JButton("<<");
	private JButton[] btns = new JButton[]{btnAddAllPlugins, btnAddPlugin, btnRemovePlugin, btnRemoveAllPlugins};
	
	private JComboBox queueSelector = new JComboBox(
		new String[]{"Queue A", "Queue B", "C", "..."}
	);
	
	public static void main(String[] args) 
	{
		JDialog dlg = new JDialog();
		dlg.setSize(640, 480);
		dlg.setContentPane(
			new PluginQueueEditorPanel()
		);
		dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dlg.setVisible(true);
	}
	
	public PluginQueueEditorPanel() 
	{
		init();
	}
	
	public void init()
	{
		this.setLayout(new BorderLayout());
		
		leftPanel.add( new JScrollPane(availablePlunginList) );
		
		rightPanel.setLayout(new BorderLayout());
		rightPanel.add( queueSelector, BorderLayout.NORTH );
		rightPanel.add( new JScrollPane(queuedPlunginList), BorderLayout.CENTER );
		
		middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
		initButtons();
		
		this.add(leftPanel, BorderLayout.WEST);
		this.add(middlePanel, BorderLayout.CENTER);
		this.add(rightPanel, BorderLayout.EAST);
	}

	private void initButtons() 
	{
		for(JButton btn : btns)
		{
			btnAddAllPlugins.addActionListener(this);
			middlePanel.add(btn);
			btn.setFont(Font.decode(Font.MONOSPACED));
		}
	}

	@Override public void actionPerformed(ActionEvent e) 
	{
		
	}
}
