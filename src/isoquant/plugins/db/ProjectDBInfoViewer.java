/** ISOQuant, isoquant.plugins.db, 10.10.2011*/
package isoquant.plugins.db;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.*;

import javax.swing.*;

import de.mz.jk.jsix.ui.FormBuilder;
import de.mz.jk.jsix.ui.TableFactory;
import de.mz.jk.plgs.data.Workflow;
import isoquant.interfaces.iMainApp;
import isoquant.interfaces.log.iLogEntry;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.db.IQDBUtils;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

/**
 * <h3>{@link ProjectDBInfoViewer}</h3>
 * @author kuharev
 * @version 10.10.2011 11:01:59
 */
public class ProjectDBInfoViewer extends SingleActionPlugin4DB
{
	static UIDefaults defs = UIManager.getDefaults();
	
	public ProjectDBInfoViewer(iMainApp app){	super(app);	}
	
	@Override public void runPluginAction(DBProject p) throws Exception
	{
		showAbout(Collections.singletonList(p));
	}

	@Override public void actionPerformed(ActionEvent e)
	{
		showAbout( app.getSelectedDBProjects() );
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
		win.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
		
		win.pack();
		win.setSize( win.getHeight(), win.getWidth() + 50 );
		win.setLocationRelativeTo( app.getGUI() );
		win.setVisible( true );
	}

	private JComponent getAboutProjectPane(DBProject p)
	{
		FormBuilder fb = new FormBuilder();
		fb.setBoldCaptions( true );
		fb.add( "title:", field( p.data.title ) );
		fb.add( "description:", field( p.data.info ) );
		fb.add( "additional info:", field( p.data.titleSuffix ) );
		fb.add( "project id:", field( p.data.id ) );
		fb.add( "database schema:", field( p.data.db ) );
		fb.add( "root folder:", field( p.data.root ) );
		fb.add( "parameters:", new JScrollPane( getParamTable( p.log.get() ) ) );
		fb.add( "workflows:", new JScrollPane( getRunTable( p ) ) );
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
	
	private JTable getParamTable(List<iLogEntry> logs)
	{
		Map<String, String> pars = new HashMap<String, String>();
		for(iLogEntry e : logs)
		{
			if( e.getType().equals( iLogEntry.Type.parameter ) )
			{
				pars.put( e.getValue(), e.getNote() );
			}
		}
		
		String[][] p = new String[pars.size()][2];
		Object[] parNames = pars.keySet().toArray();
		Arrays.sort(parNames);
		for(int i=0; i<parNames.length; i++)
		{
			p[i][0] = (String) parNames[i];
			p[i][1] = pars.get( parNames[i] );
		}
		
		TableFactory tf = new TableFactory( p, new String[]{"parameter", "value"}, new Boolean[]{false, false});
		return tf.getTable(true);
	}
	
	private JTable getRunTable(DBProject p)
	{
		String[] captions = {"index", "title", "replicate name", "sample description", "input file", "acquired name"};
		List<Workflow> ws = IQDBUtils.getWorkflows(p);
		int nRows = ws.size();
		String[][] td = new String[nRows][captions.length];
		for(int row = 0; row<nRows; row++)
		{
			Workflow w = ws.get(row);
			int col = 0;
			td[row][col++] = ""+w.index;
			td[row][col++] = ""+w.title;
			td[row][col++] = ""+w.replicate_name;
			td[row][col++] = ""+w.sample_description;
			td[row][col++] = ""+w.input_file;
			td[row][col++] = ""+w.acquired_name;
		}
		
		TableFactory tf = new TableFactory( td, captions);
		return tf.getTable(true);
	}

	@Override public String getPluginName(){return "DBProject Database Info Viewer";}
	@Override public int getExecutionOrder(){return 0;}
	@Override public String getMenuItemText(){return "show info";}
	@Override public String getMenuItemIconName(){return "info";}
}
