/** ISOQuant_1.0, isoquant.plugins.plgs.importing.design, 24.03.2011 */
package isoquant.plugins.plgs.importing.design;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import de.mz.jk.plgs.data.*;
import de.mz.jk.plgs.reader.ProjectReader;
import isoquant.kernel.db.DBProject;
import isoquant.plugins.plgs.importing.design.tree.ProjectDesignISOQuantProjectTreePanel;
import isoquant.plugins.plgs.importing.design.tree.ProjectDesignPLGSProjectTreePanel;
import isoquant.plugins.plgs.importing.design.tree.dnd.ProjectDesignDualTreeDragAndDropHandler;
import isoquant.plugins.plgs.importing.design.tree.menu.ProjectDesignContextMenu;

/**
 * <h3>{@link ProjectDesignPanel}</h3>
 * panel containing GUI for designing a single project
 * by assigning groups/samples/runs from a PLGS project.
 * The designed project is shown on the right side,
 * the original PLGS project is on the left.
 * @author Joerg Kuharev
 * @version 24.03.2011 13:01:23
 */
public class ProjectDesignPanel extends JPanel implements ComponentListener, ActionListener
{
	private static final long serialVersionUID = 20110324L;

	public static void main(String[] args) throws Exception
	{
// String runPath =
// "/Volumes/RAID0/PLGS2.4/root/Proj__12514907738010_21391414877363146/_12514908018190_15061650301830354/";
		String prjPath = "/Volumes/DAT/2013-04 Human Yeast Ecoli 1P FDR/Proj__13966189271230_9093339492956815/";
		File prjDir = new File(prjPath);
		File prjFile = new File( prjDir.getAbsolutePath() + File.separator + "Project.xml" );
		DBProject p = new DBProject( ProjectReader.getProject( prjFile, false ) );
		JFrame win = new JFrame();
		win.setSize(640, 480);
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ProjectDesignPanel pdp = new ProjectDesignPanel(p);
		win.add(pdp);
		win.setVisible(true);
	}
	private DBProject plgsPrj = null;
	private DBProject iqPrj = null;
	private JSplitPane splitPane = new JSplitPane();
	private JTree srcTree = null;
	private JTree tarTree = null;
	private JToolBar toolBar = new JToolBar();
	private JButton btnAdd = new JButton("=>");
	private JButton btnDel = new JButton("<=");
	private JButton[] btns = { btnAdd, btnDel };
	private ProjectDesignPLGSProjectTreePanel srcTreePanel = null;
	private ProjectDesignISOQuantProjectTreePanel tarTreePanel = null;
	private JPanel leftPane = new JPanel(new BorderLayout());
	private JPanel rightPane = new JPanel(new BorderLayout());
	private Dimension defBtnSize = new Dimension(40, 40);
	private DBProject inputProject = null;

	public ProjectDesignPanel(DBProject p) throws Exception
	{
		this.inputProject = p;
		this.plgsPrj = new DBProject( ProjectReader.getProject( new File( p.data.getProjectFilePath() ), true ) );
		iqPrj = p.clone();
		srcTreePanel = new ProjectDesignPLGSProjectTreePanel(plgsPrj);
		srcTree = srcTreePanel.getTree();
		tarTreePanel = new ProjectDesignISOQuantProjectTreePanel(iqPrj);
		tarTree = tarTreePanel.getTree();
		initGUI();
		new ProjectDesignDualTreeDragAndDropHandler(srcTree, tarTree);
		new ProjectDesignContextMenu(srcTree, tarTree);
		addComponentListener(this);
	}

	/**
	 * @TODO we are testing if this is ok, not to read project xml
	 * @param p
	 * @param reReadProjectXML
	 * @throws Exception
	 */
	public ProjectDesignPanel(DBProject p, boolean reReadProjectXML) throws Exception
	{
		this.inputProject = p;

		if (reReadProjectXML)
			this.plgsPrj = new DBProject( ProjectReader.getProject( new File( p.data.getProjectFilePath() ), true ) );
		else
		{
			this.plgsPrj = p.clone();
			this.plgsPrj.data.samples = new ArrayList<Sample>( p.data.samples );
		}

		packageLooseSamples( plgsPrj.data );

		iqPrj = p.clone();
		srcTreePanel = new ProjectDesignPLGSProjectTreePanel( plgsPrj );
		srcTree = srcTreePanel.getTree();
		tarTreePanel = new ProjectDesignISOQuantProjectTreePanel( iqPrj );
		tarTree = tarTreePanel.getTree();
		initGUI();
		new ProjectDesignDualTreeDragAndDropHandler( srcTree, tarTree );
		new ProjectDesignContextMenu( srcTree, tarTree );
		addComponentListener( this );
	}

	public static void packageLooseSamples(Project p)
	{
		List<Sample> looseSamples = new ArrayList( p.samples.size() );
		for ( Sample s : p.samples )
		{ // collect samples that are not mapped to any sample group
			if (s.group == null) looseSamples.add( s );
		}

		if (looseSamples.size() > 0)
		{ // we have unmapped samples in this project
			ExpressionAnalysis ea = new ExpressionAnalysis( p, "default analysis" );
			Group g = new Group( ea, "default sample group" );
			g.samples.addAll( looseSamples );
		}
		System.out.println( "" );
	}

	/**
	 * @return the inputProject
	 */
	public DBProject getInputProject()
	{
		return inputProject;
	}

	/** retrieve designed project */
	public DBProject getDesignedProject()
	{
		cleanProject(iqPrj);
		return iqPrj;
	}

	/** remove empty analyses, groups and samples from the structure */
	private void cleanProject(DBProject p)
	{
		List<ExpressionAnalysis> as = new ArrayList<ExpressionAnalysis>( p.data.expressionAnalyses );
		for (ExpressionAnalysis a : as)
		{
			List<Group> gs = new ArrayList<Group>(a.groups);
			for (Group g : gs)
			{
				List<Sample> ss = new ArrayList<Sample>(g.samples);
				for (Sample s : ss)
				{
					if (s.workflows.size() < 1)
						g.samples.remove(s);
					else
					{
						int i = 1;
						for (Workflow w : s.workflows)
						{
							w.replicate_name = s.name + " " + i++;
						}
					}
				}
				if (g.samples.size() < 1) a.groups.remove(g);
			}
			if (a.groups.size() < 1) p.data.expressionAnalyses.remove( a );
		}
	}

	private void initGUI()
	{
		initButtons();
		toolBar.setOrientation(SwingConstants.VERTICAL);
		toolBar.add(btnAdd);
		toolBar.add(btnDel);
		this.addComponentListener(this);
		setLayout(new BorderLayout());
		leftPane.add(srcTreePanel, BorderLayout.CENTER);
		rightPane.add(tarTreePanel, BorderLayout.CENTER);
		splitPane.setLeftComponent(leftPane);
		splitPane.setRightComponent(rightPane);
		add(splitPane, BorderLayout.CENTER);
		splitPane.setDividerLocation(0.5);
	}

	/**
	 * 
	 */
	private void initButtons()
	{
		for (JButton btn : btns)
		{
			btn.setPreferredSize(defBtnSize);
			btn.addActionListener(this);
		}
	}

	@Override public void componentHidden(ComponentEvent e)
	{}

	@Override public void componentMoved(ComponentEvent e)
	{}

	@Override public void componentResized(ComponentEvent e)
	{
		splitPane.setDividerLocation(0.5);
	}

	@Override public void componentShown(ComponentEvent e)
	{
		splitPane.setDividerLocation(0.5);
	}

	@Override public void actionPerformed(ActionEvent e)
	{}
}
