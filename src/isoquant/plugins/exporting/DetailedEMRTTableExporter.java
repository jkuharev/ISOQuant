/** ISOQuant, isoquant.plugins.exporting, 13.02.2012*/
package isoquant.plugins.exporting;

import java.io.File;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mz.jk.jsix.libs.XFiles;
import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.plgs.data.Workflow;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.db.IQDBUtils;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

/**
 * <h3>{@link DetailedEMRTTableExporter}</h3>
 * @author kuharev
 * @version 13.02.2012 10:11:44
 */
public class DetailedEMRTTableExporter extends SingleActionPlugin4DB
{
	private File outDir = null;
	private String quoteChar = "\"";
	private String colSep = ",";
	private String decPoint = ".";
	
	public DetailedEMRTTableExporter(iMainApp app)
	{
		super(app);
		loadConfig();
	}

	private void loadConfig()
	{
		outDir  = new File( app.getSettings().getStringValue("setup.report.dir", iMainApp.appDir.replace("\\\\", "/"), false) );
		
		decPoint = app.getSettings().getStringValue("setup.report.csv.decimalPoint", "'"+decPoint+"'", false);
		colSep = app.getSettings().getStringValue("setup.report.csv.columnSeparator", "'"+colSep+"'", false);
		quoteChar = app.getSettings().getStringValue("setup.report.csv.textQuote", "'"+quoteChar+"'", false);
		
		decPoint = XJava.stripQuotation(decPoint);
		colSep = XJava.stripQuotation(colSep);
		quoteChar = XJava.stripQuotation(quoteChar);
	}

	@Override public String getPluginName(){ return "EMRT Table Exporter"; }
	@Override public int getExecutionOrder(){ return 0; }
	@Override public String getMenuItemText(){ return "export EMRT Table"; }
	@Override public String getMenuItemIconName(){ return "csv"; }
	
	@Override public void runPluginAction(DBProject p) throws Exception
	{
		loadConfig();
		PrintStream out=null;
		Bencher t = new Bencher(true);
		System.out.println( "EMRT Table export for project '" + p.data.title + "' ..." );
		try{
			File file = XFiles.chooseFile(
				"choose file for EMRT Table export", 
				true,
					new File( outDir, p.data.title.replace( "\\W", "_" ) + "_emrt_table.csv" ),
				outDir,
				"csv",
				app.getGUI()
			);
			
			if(file==null) return;
			if(file.exists() && !XFiles.overwriteFileDialog(app.getGUI(), getPluginIcon())) return;
			
			out = new PrintStream(file);			
			createCSVReport( p, out );
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(out!=null) out.close();
		}
		System.out.println("EMRT Table export duration: ["+t.stop().getSecString()+"]");		
	}	

	private void createCSVReport(DBProject p, PrintStream out)
	{
		// pool emrt table
		p.mysql.executeSQLFile( getPackageResource("emrt_export_prep.sql") );

		List<Workflow> workflowList = IQDBUtils.getWorkflows(p);
		printHeadLine(out, workflowList);
		
		int numOfClusters = Integer.parseInt(
			p.mysql.getFirstValue("SELECT COUNT( DISTINCT cluster_average_index ) FROM clustered_emrt_pooled", 1)
		);
		
		app.getProcessProgressListener().setProgressMaxValue(numOfClusters);
		int perCent = numOfClusters / 80;
		
		ResultSet rs = p.mysql.executeSQL(
			"SELECT\n" + 
			"	cluster_average_index as `cluster_index`,\n" + 
			"	COUNT(DISTINCT workflow_index) as cluster_size,\n" + 
			"	GROUP_CONCAT(\n" + 
			"		`workflow_index`, ':', `mass`, ':', `rt`, ':', ref_rt, ':', inten, ':', cor_inten\n" + 
			"		ORDER BY `workflow_index` ASC SEPARATOR ';'\n" + 
			"	) as grouped_data\n" + 
			"FROM\n" + 
			"	clustered_emrt_pooled\n" + 
			"GROUP BY\n" + 
			"	cluster_average_index\n"
		);
		
		try
		{
			for(int row=0; rs.next(); row++)
			{
				printDataLine(out, rs, workflowList);
				if(row % perCent == 0)
				{
					app.getProcessProgressListener().setProgressValue(row);
					System.out.print(".");
				}
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println();
		
		app.getProcessProgressListener().setProgressValue(0);
	}

	private void printDataLine(PrintStream out, ResultSet rs, List<Workflow> workflowList) throws Exception
	{
		printNumCell(out, rs, "cluster_index");
		printNumCell(out, rs, "cluster_size");
				
		Map<Integer, String[]> runDataMap = extractRunDataMap( rs.getString("grouped_data") );
		for(Workflow w : workflowList)
		{
			if( runDataMap.containsKey(w.index) )
			{
				String[] runData = runDataMap.get(w.index);
				for(int c=0; c<runData.length; c++)
				{
					try 
					{
						String cellValue = runData[c];
						out.print( cellValue.replaceAll("\\.", decPoint) );
					}
					catch(Exception e) {
						// print empty value
						out.print( colSep );
						e.printStackTrace(); 
					}
					out.print( colSep );
				}
			}
			else
			{
				// 5 empty values
				out.print( colSep + colSep + colSep + colSep + colSep );
			}
		}

		out.println();
	}

	private void printNumCell(PrintStream out, ResultSet rs, String col)
	{
		try
		{
			String txt = rs.getString(col);
			out.print( txt.replaceAll("\\.", decPoint) );
		} catch (Exception e){ }
		out.print( colSep );
	}
	
	private Map<Integer, String[]> extractRunDataMap(String string)
	{
		if(string.endsWith(";")) string = string.substring(0, string.length()-1);
		Map<Integer, String[]> res = new HashMap<Integer, String[]>();
		String[] elements = string.split(";");
		for(String e : elements)
		{
			String[] kv = e.split(":");
			// workflow_index, `mass`, `rt`, `ref_rt`, `inten`, `cor_inten`
			//		0			1		2		3		4			5
			String[] data = { kv[1], kv[2], kv[3], kv[4], kv[5] };
			res.put( Integer.parseInt(kv[0]), data);
		}
		return res;
	}

	private void printHeadLine(PrintStream out, List<Workflow> workflowList)
	{
		out.print(quoteChar+"cluster_index"+quoteChar+colSep);
		out.print(quoteChar+"cluster_size"+quoteChar+colSep);

		// `mass`, `rt`, `ref_rt`, `inten`, `cor_inten`
		for( Workflow w : workflowList )
		{
			out.print(quoteChar+"run"+w.index+"_mass"+quoteChar+colSep);
			out.print(quoteChar+"run"+w.index+"_rt"+quoteChar+colSep);
			out.print(quoteChar+"run"+w.index+"_ref_rt"+quoteChar+colSep);
			out.print(quoteChar+"run"+w.index+"_inten"+quoteChar+colSep);
			out.print(quoteChar+"run"+w.index+"_cor_inten"+quoteChar+colSep);
		}
		out.println();
	}
}
