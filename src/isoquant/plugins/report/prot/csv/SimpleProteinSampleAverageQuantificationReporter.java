/*******************************************************************************
 * THIS FILE IS PART OF ISOQUANT SOFTWARE PROJECT WRITTEN BY JOERG KUHAREV
 * 
 * Copyright (c) 2009 - 2013, JOERG KUHAREV and STEFAN TENZER
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgment:
 *    This product includes software developed by JOERG KUHAREV and STEFAN TENZER.
 * 4. Neither the name "ISOQuant" nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
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
/** ISOQuant, isoquant.plugins.report.mpqs, 12.12.2011*/
package isoquant.plugins.report.prot.csv;

import java.io.File;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import de.mz.jk.jsix.mysql.MySQL;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DBExportToCSV;

/**
 * <h3>{@link SimpleProteinSampleAverageQuantificationReporter}</h3>
 * @author kuharev
 * @version 12.12.2011 12:28:16
 */
public class SimpleProteinSampleAverageQuantificationReporter extends SingleActionPlugin4DBExportToCSV
{
	public SimpleProteinSampleAverageQuantificationReporter(iMainApp app)
	{
		super(app);
	}

	@Override public String getMenuItemText()
	{
		return "simple protein sample-average quantification (CSV)";
	}

	/**
	 * @param p
	 * @param file
	 * @throws Exception 
	 */
	@Override public void runExportAction(DBProject p, File file) throws Exception
	{
		MySQL db = p.mysql;
		db.executeSQLFile( getPackageResource( "sample_protein_report.sql" ), MySQL.defExecListener );
		ResultSet rs = db.executeSQL("SELECT * FROM sample_report");
		ResultSetMetaData meta = rs.getMetaData();
		int n = meta.getColumnCount();
		PrintStream out = new PrintStream(file);
		for (int i = 1; i <= n; i++)
		{
			if (i > 1) out.print(colSep);
			printTxtCell(out, meta.getColumnName(i));
		}
		out.println();
		while (rs.next())
		{
			printTxtCell(out, rs.getString(1));
			for (int i = 2; i <= n; i++)
			{
				out.print(colSep);
				printNumCell(out, rs.getString(i));
			}
			out.println();
		}
		out.flush();
		out.close();
	}

	@Override public String getHintFileNameSuffix()
	{
		return "_sample_protein_quantification_report";
	}
}
