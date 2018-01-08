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
/** ISOQuant, isoquant.plugins.exporting, 16.09.2011 */
package isoquant.plugins.report.prot.csv;

import java.io.File;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.plgs.data.Workflow;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.db.IQDBUtils;
import isoquant.kernel.plugin.SingleActionPlugin4DBExportToCSV;

/**
 * <h3>{@link SimpleProteinRunQuantificationReporter}</h3>
 * create a CSV Protein Report
 * @author Joerg Kuharev
 * @version 16.09.2011 09:22:08
 */
public class SimpleProteinRunQuantificationReporter extends SingleActionPlugin4DBExportToCSV
{

	public SimpleProteinRunQuantificationReporter(iMainApp app)
	{
		super(app);
	}

	@Override public void runExportAction(DBProject p, File tarFile) throws Exception
	{
		PrintStream out = new PrintStream( tarFile );
		List<Workflow> workflowList = IQDBUtils.getWorkflows( p );
		printHeadLine( out, workflowList );
		try
		{
			ResultSet rs = p.mysql.executeSQL(
					"SELECT entry, GROUP_CONCAT(`workflow_index`, ':', FLOOR(`top3_avg_inten`) SEPARATOR ';') as intensities " +
							"FROM finalquant GROUP BY entry"
					);
			while (rs.next())
			{
				printDataLine( out, rs, workflowList );
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		out.flush();
		out.close();
	}

	private void printDataLine(PrintStream out, ResultSet rs, List<Workflow> workflowList) throws Exception
	{
		printTxtCell( out, XJava.decURL( rs.getString( "entry" ) ) );
		Map<Integer, String> r2i = IQDBUtils.extractI2SMap( rs.getString( "intensities" ) );
		for (Workflow w : workflowList)
		{
			printColSep( out );
			printNumCell( out, ( r2i.containsKey( w.index ) ) ? ( r2i.get( w.index ) ) : "0" );
		}
		out.println();
	}

	private void printHeadLine(PrintStream out, List<Workflow> workflowList)
	{
		out.print(quoteChar + "entry" + quoteChar);
		for (Workflow w : workflowList)
		{
			String name = XJava.decURL( w.replicate_name );
			if (!quoteChar.equals("")) name.replaceAll(quoteChar, "_");
			out.print(colSep + quoteChar + name + quoteChar);
		}
		out.println();
	}

	@Override public String getMenuItemText()
	{
		return "simple protein quantification (CSV)";
	}

	@Override public String getHintFileNameSuffix()
	{
		return "_simple_protein_quantification_report";
	}
}
