/** ISOQuant_1.0, isoquant.plugins.configuration, 07.03.2011*/
package isoquant.plugins.configuration;


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;

import de.mz.jk.jsix.libs.XFiles;
import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.ui.JXButton;
import de.mz.jk.jsix.utilities.ResourceLoader;

/**
 * <h3>{@link TextEditorDialog}</h3>
 * @author Joerg Kuharev
 * @version 07.03.2011 10:46:53
 */
public class TextEditorDialog implements ActionListener
{
	public static void main(String[] args)
	{
		TextEditorDialog d = new TextEditorDialog("ich bin ein Text", null, "");
		
		d.showAndWait();
		
		if(d.isAborted())
			System.out.println("user aborted editing");
		else
			System.out.println( d.getEditedText() );
	}
	
	private Window parent = null;
	private String originalText = "";
	private boolean aborted = true;
	
	private File hintFile = new File(XJava.dateStamp()+".txt");
	private File hintDir = new File(".");
	private String fileExt = "ini";
	
	private JDialog dlg = null;
	private JButton btnSave = new JXButton( ResourceLoader.getIcon("small_play"), "use", "use this configuration");
	private JButton btnCancel = new JXButton( ResourceLoader.getIcon("small_stop"), "skip", "skip changes" );
	private JButton btnSaveTo = new JXButton( ResourceLoader.getIcon("small_save"), "export", "save to file" );
	private JButton btnLoadFrom = new JXButton( ResourceLoader.getIcon("small_open"), "import", "load from file");
    private JPanel btnPanel = new JPanel( new BorderLayout() );
    private JPanel leftBtnPane = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
    private JPanel rightBtnPane = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
    private JTextArea txt = new JTextArea();
    private JScrollPane scrPane = new JScrollPane( txt );
	
    /** */
    public TextEditorDialog(String editableText, Window parentFrame, String dlgTitle) 
    {
    	originalText = editableText;
    	txt.setText(editableText);
    	
    	parent = parentFrame;
    	
    	dlg = (parent==null) ? new JDialog() : new JDialog(parent);
    	
    	dlg.setTitle(dlgTitle);
    	dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dlg.setSize(640, 480);
		dlg.setModal(true);
    	
        dlg.setLayout( new BorderLayout() );
        
        btnPanel.setBorder(BorderFactory.createEtchedBorder());
        
        leftBtnPane.add(btnLoadFrom);
        leftBtnPane.add(btnSaveTo);
        
        rightBtnPane.add(btnCancel);
        rightBtnPane.add(btnSave);
        
        btnPanel.add(leftBtnPane, BorderLayout.WEST);
        btnPanel.add(rightBtnPane, BorderLayout.EAST);
        
        dlg.add(scrPane, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        
        btnLoadFrom.addActionListener(this);
        btnSaveTo.addActionListener(this);
        btnSave.addActionListener(this);
        btnCancel.addActionListener(this);
    }
    
    /** set hint paths for file chooser dialogs */
    public void setFileHint(File dir, File file)
    {
    	hintDir = dir;
    	hintFile = file;
    }
    
    /** define what file extension should be used */
    public void setFileExtension(String ext)
    {
    	fileExt = ext;
    }
    
    /** last saved file */
    public File getLastFile()
    {
    	return hintFile;
    }
    
    /**
     * show modal dialog and wait for closing
     * @return true if edited text should be accepted
     */
    public boolean showAndWait()
    {
    	dlg.setLocationRelativeTo(parent);
    	dlg.setVisible(true);
    	return !aborted;
    }

    /** 
     * @return true if editing dialog was aborted by user
     */
    public boolean isAborted(){return aborted;}
    
    /**
	 * @return edited text
	 */
	public String getEditedText()
	{
		return txt.getText();
	}

	@Override public void actionPerformed(ActionEvent e)
	{
		Object o = e.getSource();
		if( o.equals(btnCancel) ) 
		{
			txt.setText(originalText);
			dlg.dispose();
		}
		else
		if( o.equals(btnSave) ) 
		{
			aborted = false;
			dlg.dispose();
		}
		else 
		if( o.equals(btnSaveTo) ) 
		{
			File file = XFiles.chooseFile("save to file", true, hintFile, hintDir, fileExt, null);
			if(file!=null)
			{
				try
				{
					XFiles.writeFile( file, txt.getText() );
					hintFile = file;
					hintDir = file.getParentFile();
				} catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}
		}
		else 
		if( o.equals(btnLoadFrom) ) 
		{
			File file = XFiles.chooseFile("load from file", false, hintFile, hintDir, fileExt, null);
			if(file!=null)
			try
			{
				txt.setText( XFiles.readFile(file) );
				hintFile = file;
				hintDir = file.getParentFile();
			}catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
}
