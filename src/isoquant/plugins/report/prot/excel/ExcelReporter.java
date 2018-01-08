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

import javax.swing.JOptionPane;

import de.mz.jk.jsix.utilities.Settings;
import isoquant.interfaces.iMainApp;
import isoquant.interfaces.plugin.iProjectReportingPlugin;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DBExportToFile;
import isoquant.plugins.report.prot.excel.ExcelReportCreator.AbsQuanSheet;

public class ExcelReporter extends SingleActionPlugin4DBExportToFile implements iProjectReportingPlugin
{
	private boolean OPTION_CREATE_RT_ALIGNMENT_SHEET = false;
	private boolean OPTION_CREATE_ALL_PROTEINS_SHEET = false;
	private boolean OPTION_SHOW_EXTRA_SHEET_FMOLUG = true;
	private boolean OPTION_SHOW_EXTRA_SHEET_PPM = true;
	private boolean OPTION_SHOW_EXTRA_SHEET_FMOL = true;
	private boolean OPTION_SHOW_EXTRA_SHEET_NG = true;
	private boolean OPTION_SHOW_PLGS_PROTEIN_QUANTIFICATION = false;

	public ExcelReporter(iMainApp app)
	{
		super(app);
	}

	@Override public void loadSettings(Settings cfg)
	{
		OPTION_CREATE_RT_ALIGNMENT_SHEET = cfg.getBooleanValue("setup.report.xls.showRTAlignment", OPTION_CREATE_RT_ALIGNMENT_SHEET, false);
		OPTION_CREATE_ALL_PROTEINS_SHEET = cfg.getBooleanValue("setup.report.xls.showAllProteins", OPTION_CREATE_ALL_PROTEINS_SHEET, false);
		// fmolug, ppm, absqfmol, absqng
		OPTION_SHOW_EXTRA_SHEET_FMOLUG = cfg.getBooleanValue("setup.report.xls.showAbsQuantFMOLUG", OPTION_SHOW_EXTRA_SHEET_FMOLUG, false);
		OPTION_SHOW_EXTRA_SHEET_PPM = cfg.getBooleanValue("setup.report.xls.showAbsQuantPPM", OPTION_SHOW_EXTRA_SHEET_PPM, false);
		OPTION_SHOW_EXTRA_SHEET_FMOL = cfg.getBooleanValue("setup.report.xls.showAbsQuantFMOL", OPTION_SHOW_EXTRA_SHEET_FMOL, false);
		OPTION_SHOW_EXTRA_SHEET_NG = cfg.getBooleanValue("setup.report.xls.showAbsQuantNG", OPTION_SHOW_EXTRA_SHEET_NG, false);
		//
		OPTION_SHOW_PLGS_PROTEIN_QUANTIFICATION = cfg.getBooleanValue("setup.report.xls.showPLGSQuant", OPTION_SHOW_PLGS_PROTEIN_QUANTIFICATION, false);
	}

	@Override public void runExportAction(DBProject prj, File tarFile)
	{
		try
		{
			ExcelReportCreator rpg = new ExcelReportCreator(prj);
			rpg.setProgressListener(app.getProcessProgressListener());
			rpg.showAllProteinsSheet(OPTION_CREATE_ALL_PROTEINS_SHEET);
			rpg.showRTAlignmentSheet(OPTION_CREATE_RT_ALIGNMENT_SHEET);
			rpg.showAbsQuanExtraSheet(AbsQuanSheet.fmolug, OPTION_SHOW_EXTRA_SHEET_FMOLUG);
			rpg.showAbsQuanExtraSheet(AbsQuanSheet.ppm, OPTION_SHOW_EXTRA_SHEET_PPM);
			rpg.showAbsQuanExtraSheet(AbsQuanSheet.absqfmol, OPTION_SHOW_EXTRA_SHEET_FMOL);
			rpg.showAbsQuanExtraSheet(AbsQuanSheet.absqng, OPTION_SHOW_EXTRA_SHEET_NG);
			rpg.showPLGSQuant(OPTION_SHOW_PLGS_PROTEIN_QUANTIFICATION);
			rpg.createReport(tarFile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(
					app.getGUI(),
					"Report file was not created!\n\n" +
							"Please complete project processing then try report creation again!",
					"Report creation failed!",
					JOptionPane.ERROR_MESSAGE
					);
		}
	}

	@Override public String getMenuItemIconName()
	{
		return "xls";
	}

	@Override public String getMenuItemText()
	{
		return "extended protein quantification (Excel)";
	}

	@Override public String getPluginName()
	{
		return "Excel Report Generator";
	}

	@Override public String getFileChooserTitle()
	{
		return "Choose a file for creating report";
	}

	@Override public String[] getFileExtensions()
	{
		return new String[] { "xlsx; Excel 2007 compatible spread sheet file", "xls; Excel 97/2000 compatible spread sheet file" };
	}

	@Override public String getHintFileNameSuffix()
	{
		return "_quantification_report";
	}

	@Override public int getExecutionOrder()
	{
		return 0;
	}
}
