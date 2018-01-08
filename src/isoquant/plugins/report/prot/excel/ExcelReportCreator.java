/*******************************************************************************
 * THIS FILE IS PART OF ISOQUANT SOFTWARE PROJECT WRITTEN BY JOERG KUHAREV
 * 
 * Copyright (c) 2009 - 2013, JOERG KUHAREV and STEFAN TENZER
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 * must display the following acknowledgment:
 * This product includes software developed by JOERG KUHAREV and STEFAN TENZER.
 * 4. Neither the name "ISOQuant" nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY JOERG KUHAREV ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL JOERG KUHAREV BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package isoquant.plugins.report.prot.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.ui.iProcessProgressListener;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.plgs.data.Workflow;
import isoquant.interfaces.log.iLogEntry;
import isoquant.kernel.db.DBProject;

public class ExcelReportCreator
{
	public ExcelReportQueryHandler dbh = null;
	public DBProject prj = null;
	private String qntSheetName = "TOP3 quantification";
	private String corSheetName = "correlation";
	private String detSheetName = "peptide details";
	private String pepTypeSheetName = "sum of inten for peptide types";
	private String workflowSheetName = "project details";
	private String outDir = System.getProperty("user.dir");
	private boolean showRTAlignmentSheet = false;
	private boolean showAllProteinsSheet = false;
	private boolean showPLGSQuant = false;

	public ExcelReportCreator(DBProject p) throws Exception
	{
		prj = p;
		dbh = new ExcelReportQueryHandler(p.mysql);
	}

	public void showPLGSQuant(boolean showPLGSQuant)
	{
		this.showPLGSQuant = showPLGSQuant;
	}

	public void showAllProteinsSheet(boolean showAllProteinsSheet)
	{
		this.showAllProteinsSheet = showAllProteinsSheet;
	}

	public void showRTAlignmentSheet(boolean showRTAlignmentSheet)
	{
		this.showRTAlignmentSheet = showRTAlignmentSheet;
	}
	/** set of extra sheets to show */
	Set<AbsQuanSheet> extraQntSheets = new HashSet<AbsQuanSheet>();

	/**
	 * available extra sheets
	 * <h3>{@link AbsQuanSheet}</h3>
	 * @author kuharev
	 * @version 21.10.2011 12:37:03
	 */
	public static enum AbsQuanSheet
	{
		fmolug, ppm, absqfmol, absqng;
	}

	/**
	 * should we show an extra sheet
	 * @param sheet the sheet 
	 * @param show if true the sheet is shown otherwise hidden
	 */
	public void showAbsQuanExtraSheet(AbsQuanSheet sheet, boolean show)
	{
		if (show)
			extraQntSheets.add(sheet);
		else extraQntSheets.remove(sheet);
	}
	private iProcessProgressListener progressListener = null;

	public void setProgressListener(iProcessProgressListener progressListener)
	{
		this.progressListener = progressListener;
	}

	public void createReport() throws Exception
	{
		createReport(new File(
				outDir + File.separatorChar + prj.data.title.replaceAll( "\\W+", "_" ) +
						"_report_" + XJava.timeStamp("yyyyMMdd-HHmmss") + ".xls"));
	}

	private Cell setCell(Row r, int col, Number val)
	{
		Cell c = r.createCell(col);
		if (val != null)
			c.setCellValue( val.doubleValue() );
		// else c.setCellValue( "" );
		c.setCellStyle(styleStd);
		return c;
	}

	private Cell setCell(Row r, int col, String val)
	{
		Cell c = r.createCell(col);
		if (val != null)
			c.setCellValue( val );
		// else c.setCellValue( "" );
		c.setCellStyle(styleStd);
		return c;
	}

	private Cell setCell(Row r, int col, String val, CellStyle s)
	{
		Cell c = setCell(r, col, val);
		c.setCellStyle((s != null) ? s : styleStd);
		return c;
	}

	private Cell setCell(Row r, int col, Double val, CellStyle s)
	{
		Cell c = r.createCell(col);
		if (val != null)
			c.setCellValue( val );
		// else c.setCellValue( null );
		c.setCellStyle( ( s != null ) ? s : styleStd );
		return c;
	}

	public void createReport(File xlsFile) throws Exception
	{
		Workbook workBook = (xlsFile.getName().toLowerCase().endsWith("xlsx")) ? new XSSFWorkbook() : new HSSFWorkbook();
		initStyles(workBook);
		createBasicSheets(workBook);
		// createDetailsSheet(workBook);
		createPeptideTypesSheet(workBook);
		createProjectInfoSheet(workBook);
		if (showAllProteinsSheet)
			createSheetFromDataList(workBook, "all proteins PLGS", dbh.getProteinMasterList(), 0, 0, false);
		if (showRTAlignmentSheet)
			createRTAlignmentSheet(workBook);
		createConfigurationSheet(workBook);
		if (dbh.extraQntTableExists())
			for (AbsQuanSheet sheet : extraQntSheets)
			{
				createExtraProteinQuantificationSheet(workBook, sheet);
			}
		else
		{
			System.out.println("\tskipping extra quantification sheets ...");
		}
		if (showPLGSQuant)
		{
			createSheetFromDataList(workBook, "PLGS ppm", dbh.getPLGSPivotPPM(), 0, 0, true);
			createSheetFromDataList(workBook, "PLGS fmol", dbh.getPLGSPivotFMOL(), 0, 0, true);
		}
		// homology filter trace
		createProteinHomologySheet(workBook);
		// write excel file
		FileOutputStream out = new FileOutputStream(xlsFile);
		workBook.write(out);
		out.close();
		System.out.println("File " + xlsFile.getAbsolutePath() + " successfully created.");
	}
	private CellStyle styleLabH, styleLabV, styleDec0, styleDec1, styleDec2, styleStd;

	private void initStyles(Workbook workBook)
	{
		DataFormat df = workBook.createDataFormat();
		Font fLBL = workBook.createFont();
		fLBL.setFontHeightInPoints((short) 10);
		fLBL.setBoldweight(Font.BOLDWEIGHT_BOLD);
		Font fDAT = workBook.createFont();
		fDAT.setFontHeightInPoints((short) 9);
		styleLabH = workBook.createCellStyle();
		styleLabH.setFont(fLBL);
		styleLabH.setVerticalAlignment(CellStyle.VERTICAL_BOTTOM);
		styleLabV = workBook.createCellStyle();
		styleLabV.setAlignment(CellStyle.VERTICAL_BOTTOM);
		styleLabV.setRotation((short) 90);
		styleLabV.setFont(fLBL);
		styleStd = workBook.createCellStyle();
		styleStd.setFont(fDAT);
		styleDec0 = workBook.createCellStyle();
		styleDec0.setDataFormat(df.getFormat("#"));
		styleDec0.setFont(fDAT);
		styleDec1 = workBook.createCellStyle();
		styleDec1.setDataFormat(df.getFormat("0.0"));
		styleDec1.setFont(fDAT);
		styleDec2 = workBook.createCellStyle();
		styleDec2.setDataFormat(df.getFormat("0.00"));
		styleDec2.setFont(fDAT);
	}

	private void createProteinHomologySheet(Workbook workBook)
	{
		List<List<String>> list = dbh.getProteinHomology();
		if (list == null) return;
		createSheetFromDataList(workBook, "protein homology", list, 0, 0, false);
	}

	private void createRTAlignmentSheet(Workbook workBook)
	{
		// check if table RTW exists
		if (!prj.mysql.tableExists("rtw")) return;
		System.out.print("\tcreating sheet 'RT Alignment' ...");
		Bencher timer = new Bencher().start();
		List<Workflow> runs = dbh.getWorkflows();
		Sheet sheet = workBook.createSheet("RT Alignment");
		Row titleRow = sheet.createRow(0);
		setCell(titleRow, 0, "Reference Retention Time", styleLabV);
		List<Double> refRTs = dbh.getRTWRef();
		int nRows = refRTs.size();
		for (int i = 0; i < nRows; i++)
		{
			Row r = sheet.createRow(i + 1);
			setCell(r, 0, (double) refRTs.get(i), null);
		}
		int col = 0;
		for (Workflow run : runs)
		{
			col++;
			setCell(titleRow, col, run.replicate_name, styleLabV);
			List<Double> rts = dbh.getRTW(run.index);
			for (int i = 0; i < nRows; i++)
			{
				try
				{
					setCell(sheet.getRow(i + 1), col, rts.get(i), null);
				}
				catch (Exception e)
				{}
			}
		}
		for (int i = 0; i < col; i++)
			sheet.autoSizeColumn(i);
		System.out.println("[" + timer.stop().getSec() + "s]");
	}

	private void createPeptideTypesSheet(Workbook workBook)
	{
		System.out.print("\tcreating sheet '" + pepTypeSheetName + "' ...");
		Bencher timer = new Bencher().start();
		int rowOffset = 0;
		int colOffset = 0;
		int row = rowOffset;
		int col = colOffset;
		Sheet sheet = workBook.createSheet(pepTypeSheetName);
		// create title row
		Row titleRow = sheet.createRow(row++);
		List<Integer> wi = new ArrayList<Integer>();
		List<Workflow> ws = dbh.getWorkflows();
		setCell(titleRow, col++, "peptide type", styleLabH);
		for (int i = 0; i < ws.size(); i++)
		{
			setCell(titleRow, col++, ws.get(i).replicate_name, styleLabV);
			wi.add(ws.get(i).index);
		}
		// which types are there?
		List<String> types = dbh.getPeptideTypes();
		Map<String, Map<Integer, Double>> data = dbh.getSumOfIntensitiesFromPeptideTypesPerWorkflow();
		for (String t : types)
		{
			col = colOffset;
			Row r = sheet.createRow(row++);
			setCell(r, col++, t);
			for (Integer w : wi)
			{
				Map<Integer, Double> typeMap = data.get(t);
				Double sum = typeMap.get(w);
				col++;
				try
				{
					// int iSum = sum.intValue();
					setCell( r, col, sum.doubleValue() );
				}
				catch (Exception e)
				{
					setCell( r, col, 0 );
				}
			}
		}
		for (int i = colOffset; i < col; i++)
			sheet.autoSizeColumn(i);
		//
		System.out.println("[" + timer.stop().getSec() + "s]");
	}

	private Font createFont(Workbook workBook, int fontSize, short fontWeight)
	{
		Font font = workBook.createFont();
		font.setFontHeightInPoints((short) fontSize);
		font.setBoldweight(fontWeight);
		return font;
	}

	private void createProjectInfoSheet(Workbook workBook)
	{
		Sheet sheet = workBook.createSheet(workflowSheetName);
		int ri = 0;
		Row r = sheet.createRow(ri++);
		setCell(r, 0, "project data", styleLabH);
		r = sheet.createRow(ri++);
		setCell(r, 0, "title", styleLabH);
		setCell( r, 1, prj.data.title );
		r = sheet.createRow(ri++);
		setCell(r, 0, "description", styleLabH);
		setCell( r, 1, prj.data.info );
		r = sheet.createRow(ri++);
		setCell(r, 0, "root folder", styleLabH);
		setCell( r, 1, prj.data.root );
		r = sheet.createRow(ri++);
		setCell(r, 0, "db schema", styleLabH);
		setCell( r, 1, prj.data.db );
		r = sheet.createRow(++ri);
		setCell(r, 0, "workflow data", styleLabH);
		List<List<String>> data = dbh.getWorkflowDetails();
		createSheetFromDataList(workBook, workflowSheetName, data, ++ri, 0, false);
	}

	private Sheet createSheetFromDataList(Workbook workBook, String sheetName, List<List<String>> data, int rowOffset, int colOffset, boolean verticalTitle)
	{
		System.out.print("\tcreating sheet '" + sheetName + "' ... ");
		Bencher timer = new Bencher().start();
		int row = rowOffset;
		int col = colOffset;
		int maxCol = col;
		Sheet resSheet = workBook.getSheet(sheetName);
		if (resSheet == null)
			resSheet = workBook.createSheet(sheetName);
		if (progressListener != null) progressListener.setProgressMaxValue(data.size());
		for (List<String> line : data)
		{
			if (progressListener != null && row % 100 == 0) progressListener.setProgressValue(row);
			Row r = resSheet.createRow(row);
			int c = col;
			for (String cell : line)
			{
				if (row == rowOffset) // title cells
				{
					setCell(r, c, cell, verticalTitle ? styleLabV : styleLabH);
				}
				else
				{
					if (cell == null)
						setCell(r, c, "");
					else if (cell.matches("[0-9]+\\.[0-9]+")) // DOUBLE
					{
						setCell( r, c, Double.parseDouble( cell ), null );
					}
					else if (cell.matches("[0-9]+")) // INTEGER
					{
						setCell( r, c, Integer.parseInt( cell ) );
					}
					else setCell(r, c, XJava.decURL(cell));
				}
				// c++;
				if (++c > maxCol) maxCol = c;
			}
			row++;
			if (row >= 65536)
			{
				System.err.println("\nexcel row number limit (65536 rows) for sheet '" + sheetName + "' exceeded!\n");
				System.out.println("\t");
				break;
			}
		}
		for (int i = colOffset; i < maxCol; i++)
		{
			resSheet.autoSizeColumn(i);
		}
		System.out.println("[" + timer.stop().getSec() + "s]");
		if (progressListener != null) progressListener.setProgressValue(0);
		return resSheet;
	}

	private void createConfigurationSheet(Workbook workBook)
	{
		List<List<String>> data = new ArrayList<List<String>>();
		List<String> row = new ArrayList<String>();
		row.add("ISOQuant Configuration");
		row.add("time");
		row.add("parameter");
		row.add("value");
		data.add(row);
		data.add(Collections.EMPTY_LIST);
		List<iLogEntry> logEntries = prj.log.get();
		for (iLogEntry le : logEntries)
			if (le.getType().equals(iLogEntry.Type.parameter))
			{
				row = new ArrayList<String>();
				row.add("");
				row.add(le.getTime());
				row.add(le.getValue());
				row.add(le.getNote());
				data.add(row);
			}
		createSheetFromDataList(workBook, "config", data, 1, 1, false);
	}

	private void createDetailsSheet(Workbook workBook)
	{
		List<List<String>> data = dbh.getQuantifiedEMRTInformation();
		createSheetFromDataList(workBook, detSheetName, data, 0, 0, true);
	}

	private void setRow(Row row, int colOffset, List<String> data)
	{
		for (String cell : data)
		{
			setCell(row, ++colOffset, cell);
		}
	}

	/**
	 * TODO add sheet containing top3 mean/median per sample + t-testTimeAlignment p-values
	 * @param workBook
	 */
	private void createBasicSheets(Workbook workBook)
	{
		System.out.print("\tcreating sheets '" + qntSheetName + "' and '" + corSheetName + "' ... ");
		Bencher timer = new Bencher().start();
		Sheet qntSheet = workBook.createSheet(qntSheetName);
		Sheet corSheet = workBook.createSheet(corSheetName);
		int rowShift = 2;
		int colShift = 0;
		Row r;
		Cell c;
		// column indices
		int colDescription = colShift++;
		int colPI = colShift++;
		int colMW = colShift++;
		int colScore = colShift++;
		int colAccession = colShift++;
		int colRPC = colShift++;
		int colCoverage = colShift++;
		int colFDR = colShift++;
		int colEntry = colShift++;
		// row for sample captions
		Row capRow = qntSheet.createRow(rowShift - 2);
		// row for workflow labels
		Row lblRow = qntSheet.createRow(rowShift - 1);
		setCell(lblRow, colEntry, "entry", styleLabV);
		setCell(lblRow, colAccession, "accession", styleLabV);
		setCell(lblRow, colRPC, "reported peptides", styleLabV);
		setCell(lblRow, colScore, "max score", styleLabV);
		setCell(lblRow, colMW, "mw", styleLabV);
		setCell(lblRow, colPI, "IEP", styleLabV);
		setCell(lblRow, colDescription, "description", styleLabV);
		setCell(lblRow, colCoverage, "sequence coverage", styleLabV);
		setCell(lblRow, colFDR, "FDR level", styleLabV);
		List<Workflow> runs = dbh.getWorkflows();
		List<Integer> sampleIndexes = dbh.getSampleIndexes();
		List<Map<String, String>> proteins = dbh.getQuantifiedProteins();
		Map<String, String> fdr = dbh.getProteinFDR();
		Map<String, String> coverage = dbh.getProteinCoverage();
		List<String> entryList = new ArrayList<String>();
		List<Row> entryRows = new ArrayList<Row>();
		for (int i = 1; i <= proteins.size(); i++)
		{
			Map<String, String> prot = proteins.get(i - 1);
			String id = prot.get("entry");
			int rowNumber = rowShift + i;
			// add entry to lookup list
			entryList.add( prot.get( "entry" ) );
			// row for entries and corresponding intensities
			r = qntSheet.createRow(rowNumber - 1);
			// --------------- BEGIN INFO BLOCK
			setCell(r, colEntry, XJava.decURL(id), styleStd);
			setCell(r, colAccession, XJava.decURL(prot.get("accession")));
			setCell(r, colRPC, Integer.parseInt(prot.get("rpc")));
			setCell(r, colScore, Double.parseDouble(prot.get("score")), styleDec2);
			setCell(r, colMW, Double.parseDouble(prot.get("mw")), styleDec2);
			setCell(r, colPI, Double.parseDouble(prot.get("pi")), styleDec2);
			setCell(r, colDescription, XJava.decURL(prot.get("description")));
			try
			{
				setCell( r, colCoverage, Double.parseDouble( coverage.get( id ) ), styleDec2 );
			}
			catch (Exception e)
			{}
			try
			{
				setCell( r, colFDR, Double.parseDouble( fdr.get( id ) ), styleStd );
			}
			catch (Exception e)
			{}
			// --------------- END INFO BLOCK
			int oldSampleIndex = runs.get(0).sample_index;
			int newSampleIndex = oldSampleIndex;
			int firstCol = colShift;
			int lastCol = firstCol;
			for (int j = 0; j < runs.size(); j++)
			{
				newSampleIndex = runs.get(j).sample_index;
				if (oldSampleIndex != newSampleIndex)
				{
					// write
					c = r.createCell(colShift + runs.size() + sampleIndexes.indexOf(oldSampleIndex));
					c.setCellStyle(styleDec0);
					c.setCellFormula( safeAverageFormula( i2a( firstCol ) + rowNumber, i2a( lastCol ) + rowNumber ) );
					// reset
					oldSampleIndex = newSampleIndex;
					firstCol = colShift + j;
				}
				lastCol = colShift + j;
			}
			// commit last sample average
			c = r.createCell(colShift + runs.size() + sampleIndexes.indexOf(newSampleIndex));
			c.setCellStyle(styleDec0);
			c.setCellFormula( safeAverageFormula( i2a( firstCol ) + rowNumber, i2a( lastCol ) + rowNumber ) );
			// add to entry rows list
			entryRows.add(r);
		}
		// correlation sheet: create rows for workflow labels
		Row corCapRow = corSheet.createRow(rowShift - 2);
		Row corLblRow = corSheet.createRow(rowShift - 1);
		for (int i = 0; i < runs.size(); i++)
		{
			Workflow run = runs.get(i);
			int runCol = colShift + i;
			// ********** write to qnt sheet ************************
			// write sample description
			setCell(capRow, runCol, run.sample_description, styleLabV);
			// write workflow name
			setCell(lblRow, runCol, run.replicate_name, styleLabV);
			// map workflow's entry->intensity
			Map<String, Double> entry2inten = dbh.getQuantDataForRun(run.index);
			// walk through all entry->intensity pairs
			for (String entry : entry2inten.keySet())
			{
				try
				{
					int rowIndex = entryList.indexOf( entry );
					Row entryRow = entryRows.get( rowIndex );
					// write intensity
					Double _val = entry2inten.get( entry );
					setCell( entryRow, runCol, _val != 0 ? _val : null, styleDec0 );
				}
				catch (Exception e)
				{

				}
			}
			qntSheet.autoSizeColumn(runCol);
			// /
// -------------------------------------------------------------------------
			// ************** write to correlation sheet ************
			// write column workflow labels
			setCell(corCapRow, runCol, run.sample_description, styleLabV);
			setCell(corLblRow, runCol, run.replicate_name, styleLabV);
			// row for correlation values
			Row corRow = corSheet.createRow(rowShift + i);
			// write row workflow label
			setCell(corRow, colShift - 1, run.replicate_name, styleLabH);
			// create cell range A
			String A = // current column (shift=shift+1 because Excel counts
// from 1)
			"'" + qntSheetName + "'!" + i2a(runCol) + (rowShift + 1) + // from
					":" + i2a(runCol) + (rowShift + proteins.size()); // to
			// walk through columns
			for (int j = 0; j < runs.size(); j++)
			{
				// create cell range B
				String B = // each column
				"'" + qntSheetName + "'!" + i2a(colShift + j) + (rowShift + 1) + // from
						":" + i2a(colShift + j) + (rowShift + proteins.size()); // to
				// write correlation formula
				c = corRow.createCell(j + colShift);
				c.setCellFormula("CORREL(" + A + "," + B + ")"); // correlation
// between ranges A & B
				c.setCellStyle(styleDec2);
			}
			corSheet.autoSizeColumn(runCol);
// -------------------------------------------------------------------------
			// ********** write sample average titles *********************
			if ((i == runs.size() - 1) || run.sample_index != runs.get(i + 1).sample_index)
			{
				int ci = colShift + runs.size() + sampleIndexes.indexOf(run.sample_index);
				setCell( capRow, ci, run.sample_description, styleLabV );
				setCell( lblRow, ci, "AVERAGE " + run.sample.name, styleLabV );
			}
// -------------------------------------------------------------------------
		}
// -------------------------------------------------------------------------
		// add conditional formatting for correlation data
		SheetConditionalFormatting formating = corSheet.getSheetConditionalFormatting();
		ConditionalFormattingRule rule1 =
				formating.createConditionalFormattingRule(ComparisonOperator.GT, "0.95", null);
		PatternFormatting pFmt1 = rule1.createPatternFormatting();
		pFmt1.setFillBackgroundColor(HSSFColor.LIGHT_GREEN.index);
		ConditionalFormattingRule rule2 =
				formating.createConditionalFormattingRule(ComparisonOperator.BETWEEN, "0.9", "0.95");
		PatternFormatting pFmt2 = rule2.createPatternFormatting();
		pFmt2.setFillBackgroundColor(HSSFColor.LIGHT_YELLOW.index);
		ConditionalFormattingRule[] rules = { rule1, rule2 };
		CellRangeAddress[] regions = {
				new CellRangeAddress(
						(short) rowShift, (short) (rowShift + runs.size()),
						(short) colShift, (short) (colShift + runs.size())
				)
		};
		formating.addConditionalFormatting(regions, rules);
		// /
// -------------------------------------------------------------------------
		qntSheet.autoSizeColumn(colEntry);
		qntSheet.autoSizeColumn(colAccession);
		qntSheet.autoSizeColumn(colRPC);
		qntSheet.autoSizeColumn(colCoverage);
		qntSheet.autoSizeColumn(colScore);
		qntSheet.autoSizeColumn(colMW);
		corSheet.autoSizeColumn(colShift - 1);
		System.out.println("[" + timer.stop().getSec() + "s]");
	}

	/**
	 * equal to CellReference.convertNumToColString(col);
	 * @param col
	 * @return
	 */
	private String i2a(int col)
	{
		return CellReference.convertNumToColString(col);
	}

	/**
	 * TODO add sheet containing top3 mean/median per sample + t-test p-values
	 * @param workBook
	 */
	private void createExtraProteinQuantificationSheet(Workbook workBook, AbsQuanSheet extraSheet)
	{
		String sheetName = extraSheet.toString();
		System.out.print("\tcreating extra absolute quantification sheet '" + sheetName + "' ...");
		Bencher timer = new Bencher().start();
		Sheet sheet = workBook.createSheet(sheetName);
		int rowShift = 2, colShift = 0;
		Row r;
		Cell c;
		int colDescription = colShift++;
		int colPI = colShift++;
		int colMW = colShift++;
		int colScore = colShift++;
		int colAccession = colShift++;
		int colRPC = colShift++;
		int colCoverage = colShift++;
		int colFDR = colShift++;
		int colEntry = colShift++;
		// row for sample captions
		Row capRow = sheet.createRow(rowShift - 2);
		// row for workflow labels
		Row lblRow = sheet.createRow(rowShift - 1);
		setCell(lblRow, colEntry, "entry", styleLabV);
		setCell(lblRow, colAccession, "accession", styleLabV);
		setCell(lblRow, colRPC, "reported peptides", styleLabV);
		setCell(lblRow, colScore, "max score", styleLabV);
		setCell(lblRow, colMW, "mw", styleLabV);
		setCell(lblRow, colPI, "IEP", styleLabV);
		setCell(lblRow, colDescription, "description", styleLabV);
		setCell(lblRow, colCoverage, "sequence coverage", styleLabV);
		setCell(lblRow, colFDR, "FDR level", styleLabV);
		List<Workflow> runs = dbh.getWorkflows();
		List<Integer> sampleIndexes = dbh.getSampleIndexes();
		List<Map<String, String>> proteins = dbh.getQuantifiedProteins();
		Map<String, String> fdr = dbh.getProteinFDR();
		Map<String, String> coverage = dbh.getProteinCoverage();
		List<String> entryList = new ArrayList<String>();
		List<Row> entryRows = new ArrayList<Row>();
		for (int i = 1; i <= proteins.size(); i++)
		{
			Map<String, String> prot = proteins.get(i - 1);
			String id = prot.get("entry");
			int rowNumber = rowShift + i;
			// add entry to lookup list
			entryList.add(id);
			// row for entries and corresponding intensities
			r = sheet.createRow(rowNumber - 1);
			// --------------- BEGIN INFO BLOCK
			setCell(r, colEntry, XJava.decURL(id), styleStd);
			setCell(r, colAccession, XJava.decURL(prot.get("accession")));
			setCell(r, colRPC, Integer.parseInt(prot.get("rpc")));
			setCell(r, colScore, Double.parseDouble(prot.get("score")), styleDec2);
			setCell(r, colMW, Double.parseDouble(prot.get("mw")), styleDec2);
			setCell(r, colPI, Double.parseDouble(prot.get("pi")), styleDec2);
			setCell(r, colDescription, XJava.decURL(prot.get("description")));
			if (coverage.containsKey( id )) try
			{
				setCell( r, colCoverage, Double.parseDouble( coverage.get( id ) ), styleDec2 );
			}
			catch (Exception e)
			{};
			if (fdr.containsKey( id )) try
			{
				setCell( r, colFDR, Double.parseDouble( fdr.get( id ) ), styleStd );
			}
			catch (Exception e)
			{};
			// --------------- END INFO BLOCK
			int oldSampleIndex = runs.get(0).sample_index;
			int newSampleIndex = oldSampleIndex;
			int firstCol = colShift;
			int lastCol = firstCol;
			for (int j = 0; j < runs.size(); j++)
			{
				newSampleIndex = runs.get(j).sample_index;
				if (oldSampleIndex != newSampleIndex)
				{
					// write
					c = r.createCell(colShift + runs.size() + sampleIndexes.indexOf(oldSampleIndex));
					c.setCellStyle(styleDec2);
					c.setCellFormula( safeAverageFormula( i2a( firstCol ) + rowNumber, i2a( lastCol ) + rowNumber ) );
					// reset
					oldSampleIndex = newSampleIndex;
					firstCol = colShift + j;
				}
				lastCol = colShift + j;
			}
			// commit last sample average
			c = r.createCell(colShift + runs.size() + sampleIndexes.indexOf(newSampleIndex));
			c.setCellStyle(styleDec2);
			c.setCellFormula( safeAverageFormula( i2a( firstCol ) + rowNumber, i2a( lastCol ) + rowNumber ) );
			// add to entry rows list
			entryRows.add(r);
		}
		for (int i = 0; i < runs.size(); i++)
		{
			Workflow run = runs.get(i);
			int runCol = colShift + i;
			// ********** write to qnt sheet ************************
			// write sample description
			setCell(capRow, runCol, run.sample_description, styleLabV);
			// write workflow name
			setCell(lblRow, runCol, run.replicate_name, styleLabV);
			// map workflow's entry->intensity
			Map<String, Double> entry2inten = dbh.getExtraQuantDataForRun(run.index, extraSheet.toString());
			// walk through all entry->intensity pairs
			for (String entry : entry2inten.keySet())
			{
				Row entryRow = entryRows.get(entryList.indexOf(entry));
				// write intensity
				setCell( entryRow, runCol, entry2inten.get( entry ) != 0 ? entry2inten.get( entry ) : null, styleDec2 );
			}
			sheet.autoSizeColumn(runCol);
// -------------------------------------------------------------------------
			// ********** write sample average titles *********************
			if ((i == runs.size() - 1) || run.sample_index != runs.get(i + 1).sample_index)
			{
				int ci = colShift + runs.size() + sampleIndexes.indexOf(run.sample_index);
				setCell(capRow, ci, run.sample_description, styleLabV);
				setCell( lblRow, ci, "AVERAGE " + run.sample.name, styleLabV );
			}
// -------------------------------------------------------------------------
		}
// -------------------------------------------------------------------------
		sheet.autoSizeColumn(colEntry);
		sheet.autoSizeColumn(colAccession);
		sheet.autoSizeColumn(colRPC);
		sheet.autoSizeColumn(colScore);
		sheet.autoSizeColumn(colMW);
		System.out.println("[" + timer.stop().getSec() + "s]");
	}

	private String safeAverageFormula(String fromCell, String toCell)
	{
		String _range_ = fromCell + ":" + toCell;
		return "IFERROR(SUM(" + _range_ + ")/COUNTIF(" + _range_ + ",\">0\"),)";
	}
}
