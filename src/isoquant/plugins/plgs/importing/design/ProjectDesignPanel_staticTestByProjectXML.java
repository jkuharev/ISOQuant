/** ISOQuant, isoquant.plugins.plgs.importing.design, Aug 17, 2018*/
package isoquant.plugins.plgs.importing.design;

import java.io.File;

import javax.swing.JFrame;

import de.mz.jk.plgs.reader.ProjectReader;
import isoquant.kernel.db.DBProject;
import isoquant.plugins.plgs.importing.design.tree.selection.ProjectDesignTreeSelectionListener;

/**
 * <h3>{@link ProjectDesignPanel_staticTestByProjectXML}</h3>
 * @author jkuharev
 * @version Aug 17, 2018 9:05:56 AM
 */
public class ProjectDesignPanel_staticTestByProjectXML
{
	public static void main(String[] args) throws Exception
	{
		ProjectDesignTreeSelectionListener.DEBUG = true;
		String prjPath = "/Volumes/DAT/2013-04 Human Yeast Ecoli 1P FDR/Proj__13966189271230_9093339492956815/";
		File prjDir = new File( prjPath );
		File prjFile = new File( prjDir.getAbsolutePath() + File.separator + "Project.xml" );
		DBProject p = new DBProject( ProjectReader.getProject( prjFile, false ) );
		JFrame win = new JFrame();
		win.setSize( 640, 480 );
		win.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		ProjectDesignPanel pdp = new ProjectDesignPanel( p );
		win.add( pdp );
		win.setVisible( true );
	}
}
