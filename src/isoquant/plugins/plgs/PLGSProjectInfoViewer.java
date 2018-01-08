/** ISOQuant, isoquant.plugins.plgs, 10.10.2011*/
package isoquant.plugins.plgs;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.*;

import de.mz.jk.jsix.ui.FormBuilder;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4FS;

/**
 * <h3>{@link PLGSProjectInfoViewer}</h3>
 * @author kuharev
 * @version 10.10.2011 14:18:15
 */
public class PLGSProjectInfoViewer extends SingleActionPlugin4FS
{
	static UIDefaults defs = UIManager.getDefaults();
	
	public PLGSProjectInfoViewer(iMainApp app)
	{
		super(app);
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		showAbout(Collections.singletonList(p));
	}

	@Override public void actionPerformed(ActionEvent e)
	{
		showAbout( app.getSelectedFSProjects() );
	}
	
	private void showAbout(List<DBProject> ps)
	{
		JDialog win = new JDialog((Frame) app.getGUI());
		win.setTitle("DBProject attributes");
		win.setModal(true);
		
		JTabbedPane tabPane = new JTabbedPane();
		
		for(DBProject p : ps) 
		{
			JComponent pnl = getAboutProjectPane(p);
			tabPane.addTab( p.data.title, pnl );
		}
		
		win.setContentPane( tabPane );			
		win.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		Component gui = app.getGUI();
		win.pack();
		// win.setSize(win.getHeight(), win.getWidth() + 50);
		win.setLocationRelativeTo(app.getGUI());
		win.setVisible(true);
	}
	
	private JComponent getAboutProjectPane(DBProject p)
	{	
		FormBuilder fb = new FormBuilder();
			fb.setBoldCaptions(true);
		fb.add( "title:", field( p.data.title ) );
		fb.add( "info:", field( p.data.titleSuffix ) );
		fb.add( "project id:", field( p.data.id ) );
		fb.add( "root folder:", field( p.data.root ) );

		return fb.getFormContainer();
	}
	
	private JTextField field(String content)
	{
		JTextField f = new JTextField(content);
		f.setEditable(false);
		f.setBackground(defs.getColor("Label.background"));
		f.setForeground(defs.getColor("Label.foreground"));
		f.setBorder(BorderFactory.createLoweredBevelBorder());
		return f;
	}

	@Override public String getPluginName(){return "DBProject Database Info Viewer";}
	@Override public int getExecutionOrder(){return 0;}
	@Override public String getMenuItemText(){return "about project";}
	@Override public String getMenuItemIconName(){return "info";}
}
