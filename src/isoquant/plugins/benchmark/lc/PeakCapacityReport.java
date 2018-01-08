/** ISOQuant, isoquant.plugins.benchmark.lc, 08.03.2012*/
package isoquant.plugins.benchmark.lc;

import java.awt.Frame;
import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import de.mz.jk.jsix.libs.XExcel;
import de.mz.jk.jsix.libs.XFiles;
import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.ui.ListChooserDialog;
import de.mz.jk.jsix.ui.iProcessProgressListener;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.jsix.utilities.Settings;
import de.mz.jk.plgs.data.Workflow;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.db.IQDBUtils;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

/**
 * <h3>{@link PeakCapacityReport}</h3>
 * @author kuharev
 * @version 08.03.2012 16:14:00
 */
public class PeakCapacityReport extends SingleActionPlugin4DB
{
	private File outDir = null;
	
	int MIN_INTENSITY = 2000;
	int MAX_INTENSITY = 70000;
	float MIN_MASS = 800f;
	
	/**
	 * @param app
	 */
	public PeakCapacityReport(iMainApp app)
	{
		super(app);
	}
	
	private void loadConfig()
	{
		Settings cfg = app.getSettings();
		outDir  = new File( cfg.getStringValue("setup.report.dir", iMainApp.appDir.replace("\\\\", "/"), false) );
		
		MIN_INTENSITY = cfg.getIntValue("PEAK_CAPACITY_MIN_INTENSITY", MIN_INTENSITY, false);
		MAX_INTENSITY = cfg.getIntValue("PEAK_CAPACITY_MAX_INTENSITY", MAX_INTENSITY, false);
		MIN_MASS = cfg.getFloatValue("PEAK_CAPACITY_MIN_MASS", MIN_MASS, false);
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		iProcessProgressListener ppl = app.getProcessProgressListener();	
		
		loadConfig();
		XExcel xls=null;
		Bencher t = new Bencher(true);
		System.out.println( "creating peak capacity report for project '" + p.data.title + "' ..." );
		try{
			File file = XFiles.chooseFile(
				"choose file for peptide report", 
				true,
					new File( outDir, p.data.title.replace( "\\W", "_" ) + "_" + p.data.info.replace( "\\W", "_" ) + "_peak_capacity.xls" ),
				outDir,
				"xls",
				app.getGUI()
			);
			
			if(file==null) return;
			if(file.exists() && !XFiles.overwriteFileDialog(app.getGUI(), getPluginIcon())) return;
			
			xls = new XExcel( file );
			
			List<Workflow> allWS = IQDBUtils.getWorkflows(p);
			List<Workflow> ws = new ArrayList<Workflow>(allWS.size());
			String[] wsTitles = new String[allWS.size()];
			for(int i=0; i<wsTitles.length;i++) 
			{
				Workflow w = allWS.get(i); 
				wsTitles[i] = w.sample_description + "; " + w.title + "; " + w.acquired_name + "; ";
			}
			int[] usedRuns = ListChooserDialog.chooseItemsFrom((Frame)app.getGUI(), wsTitles, "what runs to use", "run chooser");
			for(int wsi : usedRuns){ ws.add(allWS.get(wsi)); }
			
			if(ppl!=null) 
			{
				ppl.setProgressMaxValue(ws.size());
				ppl.setProgressValue(0);
			}
			System.out.println("[" + XJava.repeatString("-", ws.size()) + "]");
			System.out.print("[");
			
			CellStyle titleStyle = xls.createCellStyle( "title", CellStyle.ALIGN_CENTER, 0, 14, Font.BOLDWEIGHT_BOLD );
			int oRowIndex = 0;
			Row oRow = xls.getSheet("overview").createRow( oRowIndex++ );
			xls.setCell(oRow, 0, "run name", titleStyle);
			xls.setCell(oRow, 1, "run index", titleStyle);
			xls.setCell(oRow, 2, "overall peak capacity", titleStyle);
			
			xls.getSheet("ALL RUNS");
			printConfigSheet(xls);
			
// find min and max rt
			ResultSet rs1 = p.mysql.executeSQL(
				"SELECT \n" + 
				"	FLOOR( min(retention_time) ) as minRT,\n" + 
				"   CEILING( max(retention_time) ) as maxRT\n" + 
				"FROM \n" + 
				"	`peptide` as p \n" + 
				"	JOIN `query_mass` as q ON p.`query_mass_index`=q.`index`\n" + 
				"	JOIN `low_energy` as l ON q.`low_energy_index`=l.`index`\n" + 
				"WHERE\n" + 
				" (q.intensity BETWEEN "+MIN_INTENSITY+" AND "+MAX_INTENSITY+") " +
				" AND " +
				" p.mass > " + MIN_MASS
			);
			rs1.next();
			
			/*
SELECT 
	FLOOR( min(retention_time) ) as minRT,
	CEILING( max(retention_time) ) as maxRT
FROM 
	`peptide` as p 
	JOIN `query_mass` as q ON p.`query_mass_index`=q.`index`
	JOIN `low_energy` as l ON q.`low_energy_index`=l.`index`
WHERE
 (q.intensity BETWEEN 300 AND 100000000)  AND  p.mass > 500
 */
				
			double minRT = rs1.getDouble("minRT");
			double maxRT = rs1.getDouble("maxRT");

			double winSize = 1.0;
			
			List<Double> times = XJava.fillDoubleList(minRT, maxRT, winSize);
			Map<Workflow, Map<Double, RTResultRow>> allRunsRes = new HashMap<Workflow, Map<Double,RTResultRow>>();
			for(Workflow w : ws)
			{
				if(ppl!=null) ppl.setProgressValue( oRowIndex );
				System.out.print("-");
//				double pc = findPeakCapacity( xls, p.mysql, w.index, minRT, maxRT );
				
				Map<Double, RTResultRow> runRes = getPeakCapacity( p.mysql, w, times, winSize );
				
				allRunsRes.put(w, runRes);				
				
				double pc = printRunSheet(xls, w, times, runRes);
				
				oRow = xls.getSheet("overview").createRow( oRowIndex );
					xls.setCell(oRow, 0, w.acquired_name);
					xls.setCell(oRow, 1, w.index);
					xls.setCell(oRow, 2, pc, null);
				
				oRowIndex++;
			}
			System.out.println("]");
			xls.autoSizeColumns("overview", 0, 2);
			
			printAllRunsSheet(xls, ws, times, allRunsRes);
			
			xls.save();
			
			if(ppl!=null) ppl.setProgressValue( 0 );
			
			app.getSettings().setValue("setup.report.dir", file.getAbsoluteFile().getParent());
			
		}
		catch(Exception e) 
		{
			e.printStackTrace();
		}
		
		System.out.println("peak capacity report creation duration: ["+t.stop().getSecString()+"]");
	}

	private void printConfigSheet(XExcel xls)
	{
		Sheet sheet = xls.getSheet("config");
		int row = 0;
		Row r;
		
		r = sheet.createRow( row++ );
		xls.setCell(r, 0, "MIN_INTENSITY");
		xls.setCell(r, 1, MIN_INTENSITY);
		
		r = sheet.createRow( row++ );
		xls.setCell(r, 0, "MAX_INTENSITY");
		xls.setCell(r, 1, MAX_INTENSITY);
		
		r = sheet.createRow( row++ );
		xls.setCell(r, 0, "MIN_MASS");
		xls.setCell(r, 1, MIN_MASS, null);
	}
	
	/**
	 * @param xls
	 * @param times 
	 * @param ws
	 * @param allRunsRes
	 */
	private void printAllRunsSheet(XExcel xls, List<Workflow> runs, List<Double> times, Map<Workflow, Map<Double, RTResultRow>> allRunsRes)
	{
		Sheet sheet = xls.getSheet("ALL RUNS");
		CellStyle tStyle = xls.getCellStyle("title");
		int rowCount = 0;
		Row row = sheet.createRow( rowCount );
		int c= 0;
		xls.setCell(row, c++, "time");
		for(Workflow run : runs){xls.setCell(row, c++, "allPWHM"+", run " + run.index);}
		for(Workflow run : runs){xls.setCell(row, c++, "pepPWHM"+", run " + run.index);}
		
		for(Workflow run : runs){xls.setCell(row, c++, "allFWHM"+", run " + run.index);}
		for(Workflow run : runs){xls.setCell(row, c++, "pepFWHM"+", run " + run.index);}
		
		for(Workflow run : runs){xls.setCell(row, c++, "nPeaks"+", run " + run.index);}
		for(Workflow run : runs){xls.setCell(row, c++, "nPeps"+", run " + run.index);}
		
		for(Workflow run : runs){xls.setCell(row, c++, "all PC"+", run " + run.index);}
		for(Workflow run : runs){xls.setCell(row, c++, "pep PC"+", run " + run.index);}
		
		for(double rt : times)
		{
			rowCount++;
			row = sheet.createRow( rowCount );
			c = 0;
			xls.setCell(row, c++, rt, null);
			for(Workflow run : runs){xls.setCell(row, c++, allRunsRes.get(run).get(rt).peakWidthAtHalfMax, null);}
			for(Workflow run : runs){xls.setCell(row, c++, allRunsRes.get(run).get(rt).peptidePeakWidthAtHalfMax, null);}
			
			for(Workflow run : runs){xls.setCell(row, c++, allRunsRes.get(run).get(rt).peakFWHM, null);}
			for(Workflow run : runs){xls.setCell(row, c++, allRunsRes.get(run).get(rt).peptidePeakFWHM, null);}
			
			for(Workflow run : runs){xls.setCell(row, c++, allRunsRes.get(run).get(rt).numberOfPeaks, null);}
			for(Workflow run : runs){xls.setCell(row, c++, allRunsRes.get(run).get(rt).numberOfPeptides, null);}
			
			for(Workflow run : runs){xls.setCell(row, c++, allRunsRes.get(run).get(rt).peakCapacity, null);}
			for(Workflow run : runs){xls.setCell(row, c++, allRunsRes.get(run).get(rt).peptidePeakCapacity, null);}
		}
	}

	final class RTResultRow
	{
		double 
			retentionTime,
			peakWidthAtHalfMax,
			peptidePeakWidthAtHalfMax,
			numberOfPeaks,
			numberOfPeptides,
			peakCapacity,
			peptidePeakCapacity,
			peakFWHM,
			peptidePeakFWHM;
	}
	
	/**
	 * find peak capacities (and related values) for a single run at each given time point within given window size
	 * 
	 * @param db
	 * @param run
	 * @param times
	 * @param winSize
	 * @return a map of (time -> RTResultRow)
	 * @throws Exception
	 */
	private Map<Double, RTResultRow> getPeakCapacity(MySQL db, Workflow run, List<Double> times, double winSize) throws Exception
	{
		Map<Double, RTResultRow> runResTable = new HashMap<Double, RTResultRow>( times.size() );
		
		double peakCapacitySum = 0.0;
		double halfWinSize = winSize/2.0;

		for( double rt : times )
		{
			ResultSet rs = db.executeSQL(
				"SELECT \n" + 
					"COUNT(ms.`index`) AS peakCount \n" +
					", SUM(`InfDownRT` - `InfUpRT`) AS sumPWHM " +
			        ", SUM((`InfDownRT` - `InfUpRT`)*(1-ISNULL(pep.`index`))) AS sumPPWHM " +
			        ", SUM(`FWHM`) AS sumPFWHM " +
			        ", SUM(`FWHM`*(1-ISNULL(pep.`index`))) AS sumPPFWHM " +
					", COUNT(pep.`index`) as peptideCount " +
				"FROM " +
				"	`mass_spectrum` as ms LEFT JOIN query_mass as qm ON ms.low_energy_index=qm.low_energy_index \n" +
				"	LEFT JOIN peptide as pep ON qm.`index`=pep.query_mass_index " +
				" WHERE ms.workflow_index="+run.index+" AND \n" +
				"	(ms.RT BETWEEN " + (rt-halfWinSize) + " AND " + (rt+halfWinSize) + ") AND \n" +
				"	(ms.Intensity BETWEEN "+MIN_INTENSITY+" AND "+MAX_INTENSITY+") AND \n" +
				"	(ms.Mass > " + MIN_MASS + ") \n" +
				" ORDER BY RT ASC"
			);
			rs.next();

			RTResultRow rtRes = new RTResultRow();

			rtRes.numberOfPeaks = rs.getInt("peakCount");
			rtRes.numberOfPeptides = rs.getInt("peptideCount");
			
			rtRes.peakWidthAtHalfMax = (rtRes.numberOfPeaks>0) ? rs.getDouble("sumPWHM") / rtRes.numberOfPeaks : 0;
			rtRes.peptidePeakWidthAtHalfMax = (rtRes.numberOfPeptides>0) ? rs.getDouble("sumPPWHM") / rtRes.numberOfPeptides : 0;
			
			rtRes.peakCapacity = (rtRes.peakWidthAtHalfMax>0) ? winSize/rtRes.peakWidthAtHalfMax : 0.0;
			rtRes.peptidePeakCapacity = (rtRes.peptidePeakWidthAtHalfMax>0) ? winSize/rtRes.peptidePeakWidthAtHalfMax : 0.0;
		
			rtRes.peakFWHM = (rtRes.numberOfPeaks>0) ? rs.getDouble("sumPFWHM") / rtRes.numberOfPeaks : 0;
			rtRes.peptidePeakFWHM = (rtRes.numberOfPeptides>0) ? rs.getDouble("sumPPFWHM") / rtRes.numberOfPeptides : 0;
			
			runResTable.put(rt, rtRes);
		}
		
		return runResTable;
	}
	
	private double printRunSheet(XExcel xls, Workflow run, List<Double> times, Map<Double, RTResultRow> runRes)
	{
		double peakCapacitySum = 0.0;
		int rowIndex = 0;
		Sheet sheet = xls.getSheet("run " + run.index);
		CellStyle style = xls.getCellStyle("title");

		Row row = sheet.createRow( rowIndex );
		int c=0;
		xls.setCell(row, c++, "retention time", style);
		xls.setCell(row, c++, "peaks", style);
		xls.setCell(row, c++, "peps", style);
		xls.setCell(row, c++, "avg(PWHM)", style);
		xls.setCell(row, c++, "avg(FWHM)", style);
		xls.setCell(row, c++, "all PC", style);
		xls.setCell(row, c++, "avg(pepPWHM)", style);
		xls.setCell(row, c++, "avg(pepFWHM)", style);
		xls.setCell(row, c++, "pep PC", style);
		
		for(double rt : times)
		{
			RTResultRow rtRes = runRes.get(rt);
			
			peakCapacitySum += rtRes.peakCapacity;
			
			row = sheet.createRow(++rowIndex);
			c=0;
			xls.setCell(row, c++, rt, null);
			xls.setCell(row, c++, rtRes.numberOfPeaks, null);
			xls.setCell(row, c++, rtRes.numberOfPeptides, null);
			xls.setCell(row, c++, rtRes.peakWidthAtHalfMax, null);
			xls.setCell(row, c++, rtRes.peakFWHM, null);
			xls.setCell(row, c++, rtRes.peakCapacity, null);
			xls.setCell(row, c++, rtRes.peptidePeakWidthAtHalfMax, null);
			xls.setCell(row, c++, rtRes.peptidePeakFWHM, null);
			xls.setCell(row, c++, rtRes.peptidePeakCapacity, null);			
		}
		
		xls.autoSizeColumns(sheet, 0, 4);
		
		return peakCapacitySum;
	}

	@Override public String getPluginName()
	{
		return "Peak Capacity Report";
	}

	@Override public String getMenuItemText()
	{
		return "Peak Capacity Report";
	}
	@Override public String getMenuItemIconName(){ return "peak_width"; }
	@Override public int getExecutionOrder(){return 0;}
}
