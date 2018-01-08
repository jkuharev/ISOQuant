/** ISOQuant, isoquant.plugins.report.mpqs, 12.12.2011*/
package isoquant.plugins.benchmark.mpqs;

import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DBExportToCSV;

import java.io.File;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mz.jk.jsix.mysql.MySQL;

/**
 * <h3>{@link MPQS_Peptide_Reporter}</h3>
 * @author kuharev
 * @version 12.12.2011 12:28:16
 */
public class MPQS_Peptide_Reporter extends SingleActionPlugin4DBExportToCSV
{
	public MPQS_Peptide_Reporter(iMainApp app)
	{
		super(app);
	}

	@Override public String getMenuItemText(){return "MPQS peptide sample average quantification";}
	@Override public String getPluginName(){return "MPQS Peptide Sample Average Reporter";}
	@Override public int getExecutionOrder(){return 256;}
	@Override public String getHintFileNameSuffix(){return "_peptide_sample_quant";}
	
	@Override public void runExportAction(DBProject prj, File tarFile)
	{
		try
		{
			createReport(prj, tarFile);
// createLog(prj, new File(tarFile.getAbsolutePath()+".log"));
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @param p
	 * @param file
	 * @throws Exception 
	 */
	private void createReport(DBProject p, File file) throws Exception
	{
		MySQL db = p.mysql;
		
		List<Integer> sampleIndexes = db.getIntegerValues("SELECT DISTINCT sample_index FROM finalquant ORDER BY sample_index ASC");
		
		ResultSet rs = db.executeSQL(
			"SELECT \n" + 
			"	RIGHT(entry, 5) as species, " +
			"	cluster_average_index, " +
			"	intensities, " +
			"	pro.*, " +
			"	pep.*\n" + 
			"FROM " +
			"	`cluster_info_samples` \n" + 
			"JOIN cluster_info_proteins as pro USING(`cluster_average_index`)\n" + 
			"JOIN cluster_info_peptide as pep USING(`cluster_average_index`)"
		);
		
		ResultSetMetaData meta = rs.getMetaData();
		int n = meta.getColumnCount();
		
		PrintStream out = new PrintStream(file);
		
		for(int i=1; i<=n; i++)
		{
			if(i==3) // concatenated sample intensities 
			{
				for(int s : sampleIndexes)
				{
					printTxtCell(out, "s_" + s );
					out.print(colSep);
				}
			}
			else
			{
				printTxtCell(out, meta.getColumnName(i) );
				if(i<n) out.print(colSep);
			}
		}
		out.println();
		
		while(rs.next())
		{
			for(int i=1; i<=n; i++)
			{
				if(i==3) // concatenated sample intensities
				{
					Map<Integer, String> map = extractKeyValueMap( rs.getString(i) );
					for(int s : sampleIndexes)
					{
						printNumCell(out, (map.containsKey(s)) ? map.get(s) : "0");
						out.print(colSep);
					}
				}
				else
				{
					printTxtCell(out, rs.getString(i));
					if(i<n) out.print(colSep);
				}
			}
			out.println();
		}
		
		out.flush();
		out.close();
	}
		
	private Map<Integer, String> extractKeyValueMap(String string)
	{
		if(string.endsWith(";")) string = string.substring(0, string.length()-1);
		Map<Integer, String> res = new HashMap<Integer, String>();
		String[] elements = string.split(";");
		for(String e : elements)
		{
			String[] kv = e.split(":");
			res.put( Integer.parseInt(kv[0]), kv[1] );
		}
		return res;
	}	
	
	private void createLog(DBProject p, File file) throws Exception
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
		
		out.println("-------------------------");
		out.println("total\t" + sum);
		
		out.flush();
		out.close();
	}
}
