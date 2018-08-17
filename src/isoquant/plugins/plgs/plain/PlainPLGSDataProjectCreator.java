package isoquant.plugins.plgs.plain;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import de.mz.jk.jsix.libs.XCSV;
import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.plgs.data.Project;
import de.mz.jk.plgs.data.Sample;
import de.mz.jk.plgs.data.Workflow;
import de.mz.jk.plgs.reader.WorkflowReader;

/** ISOQuantPlainPLGSImportPlugin, , Mar 29, 2018*/
/**
 * <h3>{@link PlainPLGSDataProjectCreator}</h3>
 * @author jkuharev
 * @version Mar 29, 2018 3:05:29 PM
 */
public class PlainPLGSDataProjectCreator
{
	public static final String col_acquiredName = "acquired_name",
			col_peptide3dXML = "peptide3d_xml",
			col_workflowXML = "iaDBs_xml",
			col_sampleDesc = "sample_description";
	public static String csvColSep = ",";
	public static String csvQuote = "\"";
	private File csvFile = null;

	public PlainPLGSDataProjectCreator(File csvFile)
	{
		this.csvFile = csvFile;
	}

	public Project getProject()
	{
		File rootFolder = csvFile.getParentFile();

		XCSV<String> csv = new XCSV.AllValuesAreStrings().setCsvFile( csvFile );
		csv.setColSep( csvColSep ).setQuote( csvQuote );
		csv.setUseColNames( true ).setUseRowNames( false );
		csv.readAtOnce();

		// fetch and index column names
		String[] cols = csv.getColNames();
		Map<String, Integer> c2i = new HashMap<String, Integer>();
		for(int i=0; i<cols.length; i++){ c2i.put( cols[i], i ); }
		// column_index = c2i.get(column_name);
		System.out.println( "project name: " + rootFolder.getName() );

		Project prj = new Project();
		prj.title = rootFolder.getName();
		prj.root = rootFolder.getParentFile().getAbsolutePath();
		prj.id = XJava.encURL( prj.title );

		Map<String, Sample> spls = new TreeMap<String, Sample>();
		Iterator<String[]> csvData = csv.getData().iterator();
		while (csvData.hasNext())
		{
			Object[] row = csvData.next();
			String acquiredName = row[c2i.get( col_acquiredName )].toString();
			String peptide3dXML = row[c2i.get( col_peptide3dXML )].toString();
			String workflowXML = row[c2i.get( col_workflowXML )].toString();
			String sampleDesc = row[c2i.get( col_sampleDesc )].toString();

			if (!spls.containsKey( sampleDesc ))
			{
				System.out.println( "creating sample '" + sampleDesc + "' ... " );
				Sample newSample = new Sample( sampleDesc );
				prj.samples.add( newSample );
				spls.put( sampleDesc, newSample );
			}
			Sample spl = spls.get( sampleDesc );

			Workflow run = null;
			File xmlFile = new File( workflowXML ).getAbsoluteFile();
			if (!xmlFile.exists()) xmlFile = new File( csvFile.getParentFile(), workflowXML );
			try
			{
				if (!xmlFile.exists()) throw new FileNotFoundException( "could not find xml file!" );
				run = WorkflowReader.getWorkflow( xmlFile, false );
			}
			catch (Exception e)
			{
				System.err.println( e.getMessage() );
				System.err.println( "file path: " + workflowXML );
				System.out.println( "root folder: " + csvFile.getParentFile().toString() );
				run = new Workflow();
				run.acquired_name = acquiredName;
				run.sample_description = sampleDesc;
				run.xmlFilePath = workflowXML;
				run.id = XJava.timeStamp();
			}

			System.out.println( "  adding run '" + acquiredName + "' ..." );
			spl.workflows.add( run );
		}
		return prj;
	}
}
