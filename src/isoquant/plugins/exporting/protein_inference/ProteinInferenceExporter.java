/** ISOQuant, isoquant.plugins.exporting, 10.08.2012*/
package isoquant.plugins.exporting.protein_inference;

import java.io.File;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;

import javax.swing.JOptionPane;

import de.mz.jk.jsix.mysql.MySQL;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DBExportToFolder;

/**
 * <h3>{@link ProteinInferenceExporter}</h3>
 * @author kuharev
 * @version 10.08.2012 10:38:14
 */
public class ProteinInferenceExporter extends SingleActionPlugin4DBExportToFolder
{
	private final String colsep = "\t";
	
	@Override public void runExportAction(DBProject p, File tarDir)
	{
		try
		{
			String basicName = JOptionPane.showInputDialog(
				app.getGUI(),
				"Please input name basis for output files.\nReal file names will be constructed from\ngiven name basis by appending meaningful suffixes.",
					p.data.title.replaceAll( "\\W", "_" )
			).replaceAll("\\W", "_");
						
			PrintStream out = new PrintStream( new File(tarDir, basicName + "_all_before_filtering.csv") ); 
			sql2stream(p.mysql, out, "SELECT * FROM peptides_in_proteins_before_homology_filtering ORDER BY src_proteins DESC");
			out.flush();
			out.close();
			
			out = new PrintStream( new File(tarDir, basicName + "_all_after_filtering.csv") ); 
			sql2stream(p.mysql, out, "SELECT * FROM peptides_in_proteins ORDER BY src_proteins DESC");
			out.flush();
			out.close();
			
			p.mysql.executeSQLFile(this.getPackageResource("complex_networks.sql"));
			
			out = new PrintStream( new File(tarDir, basicName + "_complex_before_filtering.csv") ); 
			sql2stream(p.mysql, out, 
					"SELECT * FROM peptides_in_proteins_before_homology_filtering " +
					"JOIN proteins_in_reducible_networks USING(entry) " +
					"ORDER BY src_proteins DESC");
			out.flush();
			out.close();
			
			out = new PrintStream( new File(tarDir, basicName + "_complex_after_filtering.csv") ); 
			sql2stream(p.mysql, out,
					"SELECT * FROM peptides_in_proteins " +
					"JOIN proteins_in_reducible_networks USING(entry) " +
					"ORDER BY src_proteins DESC"		
			);
			out.flush();
			out.close();
			
			out = new PrintStream( new File(tarDir, basicName + "_node_types.csv") );
			out.println("id"+colsep+"type");
			List<String> ids = p.mysql.getStringValues("SELECT DISTINCT entry FROM peptides_in_proteins_before_homology_filtering");
			for(String id : ids){ out.println(id+ colsep+"protein"); }
			ids = p.mysql.getStringValues("SELECT DISTINCT sequence FROM peptides_in_proteins_before_homology_filtering");
			for(String id : ids){ out.println(id+ colsep+"peptide"); }			
			out.flush();
			out.close();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	private void sql2stream(MySQL db, PrintStream out, String sql)
	{
		try
		{
			ResultSet rs = db.executeSQL(sql);
	        ResultSetMetaData rsmd = rs.getMetaData();
	        int n = rsmd.getColumnCount();

	        for(int i=1; i<=n; i++)
	        {
	        	out.print( rsmd.getColumnName(i) + colsep );
	        }
	        out.println();
		    
			// walk through result lines
			for(int j=0; rs.next(); j++)
			{
				for(int i=1; i<=n; i++) 
				{
					out.print( rs.getString(i) + colsep );
				}
				out.println();
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	public ProteinInferenceExporter(iMainApp app){ super(app); }
	
	@Override public String getPluginName(){ return ProteinInferenceExporter.class.toString(); }
	@Override public String getFolderSelectionDialogTitle(){ return "folder to export protein inference data"; }
	@Override public String getMenuItemText(){return "export Protein Inference data";}
	@Override public String getMenuItemIconName(){return "csv";}
}
