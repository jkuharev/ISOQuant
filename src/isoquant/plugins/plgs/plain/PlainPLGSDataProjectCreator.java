package isoquant.plugins.plgs.plain;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import de.mz.jk.jsix.libs.XCSV;
import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.plgs.data.Project;
import de.mz.jk.plgs.data.Sample;
import de.mz.jk.plgs.data.Workflow;

/** ISOQuantPlainPLGSImportPlugin, , Mar 29, 2018*/
/**
 * <h3>{@link PlainPLGSDataProjectCreator}</h3>
 * @author jkuharev
 * @version Mar 29, 2018 3:05:29 PM
 */
public class PlainPLGSDataProjectCreator
{
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

		String[] cols = csv.getColNames();
		Map<String, Integer> c2i = new HashMap<String, Integer>();
		for(int i=0; i<cols.length; i++){ c2i.put( cols[i], i ); }

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
			String acquiredName = row[0].toString();
			String peptide3dXML = row[1].toString();
			String workflowXML = row[2].toString();
			String sampleDesc = row[3].toString();

			if (!spls.containsKey( sampleDesc ))
			{
				System.out.println( "creating sample '" + sampleDesc + "' ... " );
				Sample newSample = new Sample( sampleDesc );
				prj.samples.add( newSample );
				spls.put( sampleDesc, newSample );
			}
			Sample spl = spls.get( sampleDesc );

			Workflow run = new Workflow();
			run.acquired_name = row[c2i.get( "acquired_name" )].toString();

			System.out.println( "  adding run '" + run.acquired_name + "' ..." );
			spl.workflows.add( run );
		}
		return prj;
	}
}
