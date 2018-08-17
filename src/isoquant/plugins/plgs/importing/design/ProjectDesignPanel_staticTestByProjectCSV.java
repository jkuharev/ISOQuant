/** ISOQuant, isoquant.plugins.plgs.importing.design, Aug 17, 2018*/
package isoquant.plugins.plgs.importing.design;

import java.io.File;

import javax.swing.JFrame;

import de.mz.jk.plgs.data.Project;
import isoquant.kernel.db.DBProject;
import isoquant.plugins.plgs.importing.design.tree.selection.ProjectDesignTreeSelectionListener;
import isoquant.plugins.plgs.plain.PlainPLGSDataProjectCreator;

/**
 * <h3>{@link ProjectDesignPanel_staticTestByProjectCSV}</h3>
 * @author jkuharev
 * @version Aug 17, 2018 9:05:56 AM
 */
public class ProjectDesignPanel_staticTestByProjectCSV
{
	public static void main(String[] args) throws Exception
	{
		ProjectDesignTreeSelectionListener.DEBUG = true;
		String prjPath = "/Volumes/DAT/2013-04 Human Yeast Ecoli 1P FDR/Proj__13966189271230_9093339492956815/";
		File prjDir = new File( prjPath );
		File csvFile = new File( prjDir.getAbsolutePath() + File.separator + "project.csv" );
		
		PlainPLGSDataProjectCreator prjMaker = new PlainPLGSDataProjectCreator( csvFile );
		Project plainProject = prjMaker.getProject();
		DBProject p = new DBProject( plainProject );

		ProjectDesignPanel pdp = new ProjectDesignPanel( p, false );
		
		JFrame win = new JFrame();
		win.setSize( 640, 480 );
		win.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		win.add( pdp );
		win.setVisible( true );
	}
}
