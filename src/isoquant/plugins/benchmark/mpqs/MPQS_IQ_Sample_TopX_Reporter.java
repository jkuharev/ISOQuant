/** ISOQuant, isoquant.plugins.benchmark.mpqs, 17.01.2013*/
package isoquant.plugins.benchmark.mpqs;

import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DBExportToCSV;

import java.io.File;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import de.mz.jk.jsix.mysql.MySQL;

/**
 * <h3>{@link MPQS_IQ_Sample_TopX_Reporter}</h3>
 * @author kuharev
 * @version 17.01.2013 09:36:04
 */
public class MPQS_IQ_Sample_TopX_Reporter extends SingleActionPlugin4DBExportToCSV
{
	public MPQS_IQ_Sample_TopX_Reporter(iMainApp app){ super(app); }

	@Override public String getPluginName(){return "MPQS Protein Sample Average Reporter";}
	@Override public String getMenuItemText(){return "MPQS protein sample average quantification";}
	@Override public int getExecutionOrder(){return 256;}
	@Override public String getHintFileNameSuffix(){return "_protein_sample_quant";}
	
	@Override public void runExportAction(DBProject prj, File tarFile)
	{
		try
		{
			createReport(prj, tarFile);
			createLog(prj, new File(tarFile.getAbsolutePath()+".log"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void createReport(DBProject p, File file) throws Exception
	{
		MySQL db = p.mysql;
		PrintStream out = new PrintStream(file);

		db.executeSQLFile(getPackageResource("sample_protein_report.sql"), MySQL.defExecListener);
		
		ResultSet rs = db.executeSQL( "SELECT RIGHT(entry, 5) as species, sample_report.* FROM sample_report" );
		ResultSetMetaData meta = rs.getMetaData();
		int n = meta.getColumnCount();
		
		for(int i=1; i<=n; i++)
		{
			if(i>1) out.print( colSep );
			printTxtCell(out, meta.getColumnName(i) );
		}
		out.println();
		
		while(rs.next())
		{
			printTxtCell(out, rs.getString(1)); // entry
			out.print( colSep );
			printTxtCell(out, rs.getString(2)); // entry
			for(int i=3; i<=n; i++)
			{
				out.print( colSep );
				printNumCell( out, rs.getString(i) );
			}
			out.println();	
		}
		out.flush();
		out.close();
	}
	
	public void createLog(DBProject p, File file) throws Exception
	{
		MySQL db = p.mysql;
		PrintStream out = new PrintStream(file);
		
		out.println("# report statistics for average protein quantification\n");
		out.println("species\treported proteins");
		
		// peakCount species
		ResultSet rs = db.executeSQL(
			"SELECT RIGHT(entry, 5) as species, COUNT(entry) as freq \n" + 
			" FROM sample_report\n" + 
			" GROUP BY species\n" + 
			" ORDER BY freq DESC"
		);

		int sum = 0;
		while(rs.next())
		{
			String species = rs.getString(1);
			int freq = rs.getInt(2);
			out.println(species + "\t" + freq);
			sum += freq;
		}
		
		out.println("--------------------------------------------------------------------------------");
		out.println("total\t" + sum);
		
		out.println("\n\n\n");
		out.println("--------------------------------------------------------------------------------");
		out.println( app.getSettings().readToString() );
		out.println("--------------------------------------------------------------------------------");
		
		out.flush();
		out.close();
	}
}
