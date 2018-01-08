/** ISOQuant, isoquant.plugins.processing.restructuring, 12.03.2013*/
package isoquant.plugins.db;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.*;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.ui.FormBuilder;
import de.mz.jk.jsix.ui.TableFactory;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.plgs.data.Workflow;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.db.IQDBUtils;

/**
 * <h3>{@link WorkflowRemoverPanel}</h3>
 * @author kuharev
 * @version 12.03.2013 09:34:14
 */
public class WorkflowRemoverPanel extends JPanel implements ActionListener
{
	static UIDefaults defs = UIManager.getDefaults();
	
	private DBProject p = null;
	private JButton btn = new JButton("remove selected workflows");
	private JScrollPane scp = new JScrollPane();

	private JTable runTable = null;
	private List<Workflow> runs = null;

	private ProjectCleaner cleaner = null;
	
	public WorkflowRemoverPanel(DBProject p, ProjectCleaner cleaner)
	{
		this.p = p;
		this.cleaner = cleaner;
		
		this.setLayout(new BorderLayout());
		this.add(getAboutProjectPane(), BorderLayout.NORTH);
		this.add(scp, BorderLayout.CENTER);
		this.add(getCmdBar(), BorderLayout.SOUTH);
		updateRuns();
	}
	
	private JPanel getCmdBar()
	{
		JPanel pnl = new JPanel();
//		pnl.setFloatable(false);
		pnl.add( btn );
		btn.addActionListener(this);
		return pnl;
	}

	private void updateRuns()
	{
		runTable = getRunTable( );
		runTable.setRowSelectionAllowed(true);
		runTable.setColumnSelectionAllowed(false);
		runTable.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
		scp.setViewportView( runTable );
	}

	private JComponent getAboutProjectPane()
	{
		FormBuilder fb = new FormBuilder();
			fb.setBoldCaptions(true);
		fb.add( "title:", field( p.data.title ) );
		fb.add( "description:", field( p.data.info ) );
		fb.add( "additional info:", field( p.data.titleSuffix ) );
		fb.add( "project id:", field( p.data.id ) );
		fb.add( "database schema:", field( p.data.db ) );
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
	
	private JTable getRunTable()
	{
		String[] captions = {"index", "title", "replicate name", "sample description", "input file", "acquired name"};
		runs = IQDBUtils.getWorkflows(p);
		int nRows = runs.size();
		String[][] td = new String[nRows][captions.length];
		
		for(int row = 0; row<nRows; row++)
		{
			Workflow w = runs.get( row );
			int col = 0;
			td[row][col++] = ""+w.index;
			td[row][col++] = ""+w.title;
			td[row][col++] = ""+w.replicate_name;
			td[row][col++] = ""+w.sample_description;
			td[row][col++] = ""+w.input_file;
			td[row][col++] = ""+w.acquired_name;
		}
		
		TableFactory tf = new TableFactory( td, captions );
		return tf.getTable(true);
	}

	@Override public void actionPerformed(ActionEvent e)
	{
		int[] selIndices = runTable.getSelectedRows();
		
		if( selIndices.length<1 ) return;
		
		int res = JOptionPane.showConfirmDialog( // parentComponent, message, title, optionType, messageType, icon)
			this, 
				"You have selected " + selIndices.length + " workflows to remove from database.\n" +
				"THIS CAN NOT BE UNDONE!\n" +
				"YOU WILL HAVE TO COMPLETELY REPROCESS THIS PROJECT!\n" +
				"Do you really want to remove selected runs?",
			"remove selected?",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.WARNING_MESSAGE
		);
		
		if(res == JOptionPane.YES_OPTION)
		{	
			Bencher t = new Bencher(true);
			System.out.println("removing runs: ");
			
			Integer[] runIs = new Integer[selIndices.length];
			
			for(int i=0; i<selIndices.length; i++)
				runIs[i] = runs.get(selIndices[i]).index;
			
			removeRuns( runIs );
			
			System.out.println("removal duration: " + t.stop().getSecString() );
			
			cleanProject();
			updateRuns();
		}
	}

	/**
	 * 
	 */
	private void cleanProject()
	{
		try
		{
			cleaner.runPluginAction(p);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @param workflow
	 */
	private void removeRuns(Integer[] runIndices)
	{
		MySQL db = p.mysql;
		String indexList = XJava.joinArray( runIndices, "," );
		db.executeSQL("DELETE FROM workflow WHERE `index` in ("+indexList+")");
		db.optimizeTable("workflow");
		String[] tabs = "low_energy,mass_spectrum,query_mass,peptide,protein".split(",");
		for(String tab : tabs)
		{
			Bencher t = new Bencher(true);
			System.out.print("	"+tab+" ... ");
			db.optimizeTable(tab);
			db.executeSQL("DELETE FROM "+tab+" WHERE `workflow_index` in ("+indexList+")");
			db.optimizeTable(tab);
			System.out.println("["+t.stop().getSecString()+"]");
		}		
	}
}
