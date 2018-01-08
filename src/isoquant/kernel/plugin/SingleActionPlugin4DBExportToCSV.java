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
/** ISOQuant, isoquant.kernel.plugin, 18.12.2012 */
package isoquant.kernel.plugin;

import java.io.File;
import java.io.PrintStream;
import java.sql.ResultSet;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.utilities.Settings;
import isoquant.interfaces.iMainApp;
import isoquant.interfaces.plugin.iProjectReportingPlugin;

/**
 * abstract plugin base for exporting data into a file
 * 
 * <h3>{@link SingleActionPlugin4DBExportToCSV}</h3>
 * @author kuharev
 * @version 18.12.2012 13:43:29
 */
public abstract class SingleActionPlugin4DBExportToCSV extends SingleActionPlugin4DBExportToFile implements iProjectReportingPlugin
{
	public SingleActionPlugin4DBExportToCSV(iMainApp app)
	{
		super(app);
	}
	protected String quoteChar = "\"";
	protected String colSep = ",";
	protected String decPoint = ".";

	@Override public void loadSettings(Settings cfg)
	{
		outDir = new File(app.getSettings().getStringValue("setup.report.dir", iMainApp.appDir.replace("\\\\", "/"), false));

		decPoint = app.getSettings().getStringValue("setup.report.csv.decimalPoint", "'" + decPoint + "'", false);
		colSep = app.getSettings().getStringValue("setup.report.csv.columnSeparator", "'" + colSep + "'", false);
		quoteChar = app.getSettings().getStringValue("setup.report.csv.textQuote", "'" + quoteChar + "'", false);

		decPoint = XJava.stripQuotation(decPoint);
		colSep = XJava.stripQuotation(colSep);
		quoteChar = XJava.stripQuotation(quoteChar);
	}

	public void printTxtCell(PrintStream out, ResultSet rs, String col, boolean addColSep)
	{
		try
		{
			printTxtCell(out, XJava.decURL(rs.getString(col)));
		}
		catch (Exception e)
		{}
		if (addColSep) out.print( colSep );
	}

	/** text cell plus column separator */
	public void printTxtCell(PrintStream out, ResultSet rs, String col)
	{
		printTxtCell( out, rs, col, true );
	}

	public void printNumCell(PrintStream out, ResultSet rs, String col, boolean addColSep)
	{
		try
		{
			printNumCell( out, rs.getString( col ) );
		}
		catch (Exception e)
		{}
		if (addColSep) out.print( colSep );
	}

	/** numeric cell plus column separator */
	public void printNumCell(PrintStream out, ResultSet rs, String col)
	{
		printNumCell( out, rs, col, true );
	}

	/** apply current quote settings to a string value and write it to out */
	public void printTxtCell(PrintStream out, String value)
	{
		out.print(quoteChar);
		try
		{
			String txt = XJava.decURL(value);
			out.print(txt.replaceAll(quoteChar, " ").replaceAll("\n", ""));
		}
		catch (Exception e)
		{}
		out.print(quoteChar);
	}

	/** apply current decimal point settings to a numeric value and write it to out */
	public void printNumCell(PrintStream out, String value)
	{
		try
		{
			out.print(value.replaceAll("\\.", decPoint));
		}
		catch (Exception e)
		{}
	}

	/** print column separator */
	public void printColSep(PrintStream out)
	{
		out.print(colSep);
	}

	@Override public String[] getFileExtensions()
	{
		return new String[] { "csv; Comma Separated Values File" };
	}

	@Override public String getMenuItemIconName()
	{
		return "csv";
	}

	@Override public String getFileChooserTitle()
	{
		return "select CSV file for " + getPluginName();
	}
}
