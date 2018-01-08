package isoquant.plugins.plgs.importing.importer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.plgs.data.*;
import isoquant.kernel.db.DBProject;

public class PLGSDataImportQueryHandler 
{
	public final String STRUCTURE_SQL_FILE_NAME = "isoquant/kernel/db/project.sql";
	private MySQL db = null;
	private final int queueLen = 50; // maximum rows in a multirow sql statement
	
	public PLGSDataImportQueryHandler(MySQL db) throws Exception 
	{
		this.db = db;
		db.getConnection();
	}
	
	public MySQL getDB()
	{
		return db;
	}

	public void reconnectToDatabase(boolean verbose)
	{
		db.closeConnection( verbose );
		db.getConnection( verbose );
	}

	public void clearDB()
	{
		db.executeSQLFile(STRUCTURE_SQL_FILE_NAME, null);
	}
	// -----------------------------------------------------------------------------
	/**
	 * removes all content from named tables
	 * e.g.
	 	deleteContents(
	 		String[]{"clustered_emrt","peptide","protein",
			"query_mass","low_energy","cluster_average",
			"workflow","sample","group","expression_analysis","project"}
		);
	 * @param tables
	 */
	public void deleteContents(String[] tables)
	{
		for(String table: tables)  db.truncateTable(table);
	}
	// -----------------------------------------------------------------------------
	/** store project data: id, title, root, db; and get project_index */
	public void storeProject(DBProject prj) throws Exception
	{
		try
		{
			prj.data.index = getProjectIndex( prj );
			return;
		}
		catch (Exception e)
		{}
		db.executeSQL(
			"INSERT INTO project (id, title, root, db) " +
						"VALUES ('" + prj.data.id + "','" + XJava.encURL( prj.data.title ) + "','" + XJava.encURL( prj.data.root ) + "','" + prj.data.db
						+ "');"
		);
		prj.data.index = getProjectIndex( prj );
	}
	public int getProjectIndex(DBProject prj) throws Exception
	{
		try{
			return Integer.parseInt( db.getFirstValue( "project", "`index`", "id='" + prj.data.id + "'" ) );
		} catch (Exception e) {
			throw new Exception("DBProject index not found.");
		}
	}
	// -----------------------------------------------------------------------------
	/**
	 * don't forget to update project_id
	 */
	public void storeExpressionAnalysis( ExpressionAnalysis exAn ) throws Exception
	{
		try{exAn.index=getExpressionAnalysisIndex(exAn);return;}catch(Exception e){}
		db.executeSQL( 
			"INSERT INTO `expression_analysis` (`project_index`, id, name, description) " +
			"VALUES ('"+exAn.project_index+"', '"+exAn.id+"','"+XJava.encURL(exAn.name)+"','"+XJava.encURL(exAn.description)+"');"
		);
		try{exAn.index=getExpressionAnalysisIndex(exAn);}catch(Exception e){}
	}
	private int getExpressionAnalysisIndex(ExpressionAnalysis exAn) throws Exception 
	{
		try{
			return Integer.parseInt( db.getFirstValue("expression_analysis", "`index`", "`project_index`='"+exAn.project_index+"' AND id='"+exAn.id+"'") );
		}catch (Exception e) {
			throw new Exception("ExpressionAnalysis index not found.");
		}		
	}
	// -----------------------------------------------------------------------------
	public void storeGroup( Group g ) throws Exception
	{
		try{g.index=getGroupIndex(g); return;}catch(Exception e){}
		db.executeSQL(
			"INSERT INTO `group` (`expression_analysis_index`, id, name) " +
			"VALUES ('"+g.expression_analysis_index+"', '"+g.id+"','"+XJava.encURL(g.name)+"');"
		);
		try{g.index=getGroupIndex(g);}catch(Exception e){}
	}
	private int getGroupIndex(Group g) throws Exception 
	{
		try{
			return Integer.parseInt( 
				db.getFirstValue("`group`", "`index`", "`expression_analysis_index`='"+
					g.expression_analysis_index+"' AND id='"+g.id+"' AND name='"+XJava.encURL(g.name)+"'") 
			);
		}catch (Exception e) {
			throw new Exception("Group index not found.");
		}	
	}
	// -----------------------------------------------------------------------------
	public void storeSample( Sample s ) throws Exception
	{
		try{s.index=getSampleIndex( s ); return;}catch(Exception e){}
		db.executeSQL( 
			"INSERT INTO `sample` (`group_index`, id, name) " + 
			"VALUES ('"+s.group_index+"', '"+s.id+"','"+XJava.encURL(s.name)+"');"
		);
		try{s.index=getSampleIndex(s);}catch(Exception e){throw e;}
	}
	private int getSampleIndex(Sample s) throws Exception 
	{
		try{
			return Integer.parseInt( 
				db.getFirstValue("`sample`", "`index`", "`group_index`="+
					s.group_index+" AND id='"+s.id+"' AND name='"+XJava.encURL(s.name)+"'") 
			);
		}catch (Exception e) {
			throw new Exception("Sample index not found.");
		}	
	}
	// -----------------------------------------------------------------------------
	public int getNextWorkflowIndex()
	{
		return getMaxValue("workflow", "index") + 1;
	}
	public void storeWorkflow( Workflow w ) throws Exception
	{
		try{w.index=getWorkflowIndex(w); return;}catch(Exception e){}
		db.executeSQL(
			"INSERT INTO `workflow` SET " +
			"`index`='"+w.index+"',"+
			"`sample_index`='"+w.sample_index+"',"+ 
			"`id`='"+w.id+"',"+ 
			"`title`='"+ XJava.encURL( w.title ) +"',"+ 
			"`sample_tracking_id`='"+w.sample_tracking_id+"',"+ 
			"`sample_description`='"+XJava.encURL( w.sample_description ) +"',"+ 
			"`input_file`='"+ XJava.encURL( w.input_file ) +"',"+ 
			"`acquired_name`='"+ XJava.encURL( w.acquired_name ) +"'," + 
			"`abs_quan_response_factor`='" + w.abs_quan_response_factor +"',"+ 
			"`replicate_name`='"+ XJava.encURL( w.replicate_name ) +"';"
		);
	}

	// -----------------------------------------------------------------------------
	private int getWorkflowIndex(Workflow w) throws Exception 
	{
		try{
			return Integer.parseInt( db.getFirstValue("`workflow`", "`index`", "`sample_index`='"+w.sample_index+"' AND id='"+w.id+"'") );
		}catch (Exception e) {
			throw new Exception("Workflow index not found.");
		}	
	}

	// -----------------------------------------------------------------------------
	public void storeWorkflowMetaData(int workflowIndex, String dataType, Map<String, String> data)
	{
		for ( String k : data.keySet() )
		{
			db.executeSQL(
				"REPLACE INTO `workflow_metadata` SET " +
				"	`workflow_index`="+workflowIndex+", " +
				"	`type`='"+dataType+"', " +
				"	`name`='"+k+"', " +
				"	`value`='"+XJava.encURL( data.get( k ) ) +"'"
			);
		}
	}
	// -----------------------------------------------------------------------------

	/**
	 * get max value from a column of given table<br>  
	 * @param table
	 * @param colum
	 * @return next auto increment value
	 */
	private int getMaxValue(String table, String column)
	{
		if(column==null || column.length()<1) column="index";
		ResultSet rs = db.executeSQL("SELECT (IF(ai IS NULL, 0, ai)) AS AUTOINC FROM (SELECT (max(`"+column+"`)) AS ai FROM `"+table+"`) AS t");
		try{ return (rs.next()) ? rs.getInt("AUTOINC") : 1; } 
		catch (SQLException e){ return 1; }
	}

	/**
	 * creates an in-memory temporary table by using the same structure as source table<br> 
	 * target table will use next available value after source table key value for
	 * auto increment keys
	 */
	private void createInMemoryMirror(String srcTab, String tarTab, String keyCol)
	{
		/**
		 * Idee:
		 * 1) 	Tempor�re Tabelle im Speicher erstellen, die alle Eigenschaften der
		 * 2)	Daten werden ausschlie�lich in die tempor�re Tabelle eingef�gt.
		 * 3)	Daten aus der tempor�ren Tabelle in die Originaltabelle einf�gen
		 * 4)	Tempor�re Tabelle bereinigen/l�schen.
		 * Eigenschaften einer Tabelle anzeigen: SHOW TABLE STATUS FROM 3Gb Like 'emrt';
		 */
		// drop potentially existing target table
		// String sql = "DROP TABLE IF EXISTS `"+tarTab+"`";
		// try{stmt.execute(sql);}catch(Exception e){System.out.println(sql); e.printStackTrace();}
		// create new target table
		db.executeSQL("CREATE TEMPORARY TABLE IF NOT EXISTS `"+tarTab+"` LIKE `"+srcTab+"`;");
		// move target table into memory and set next auto_increment value
		int nextKey = getMaxValue(srcTab, keyCol) + 1;
		db.executeSQL("ALTER TABLE `"+tarTab+"` ENGINE=MEMORY AUTO_INCREMENT="+nextKey+";");
	}
	
	/**
	 * copys all data from source to target 
	 * and deletes all rows from source table if needed
	 * @param srcTab
	 * @param tarTab
	 * @param clearSource source table will be cleared if true
	 */
	private void moveTableData(String srcTab, String tarTab, boolean clearSource)
	{
		// copy data from source table to target table
		db.executeSQL("INSERT INTO `"+tarTab+"` SELECT * FROM `"+srcTab+"`;");
		if(clearSource)	db.executeSQL( "TRUNCATE TABLE `"+srcTab+"`;" );		
	}	

	// -----------------------------------------------------------------------------
	
// ---- queued import ----
	public void prepareClusterAverage() 
	{
		createInMemoryMirror("cluster_average", "temp_cluster_average", "index");
	}

	String caValues = "";
	int caCounter = 0;
	/**
	 * store cluster average using MULTI-ROW-INSERTs,
	 * after last cluster has been queued call 'storeClusterAverage(null);'
	 * for commit buffered clusters to db
	 * @param ca
	 * @throws Exception
	 */
	public void storeClusterAverage(ClusterAverage ca) throws Exception 
	{
		if(ca!=null)
		{
			caValues += ((caCounter==0) ? "" : ", ") +
			"('"+
				ca.index+"','"+ca.expression_analysis_index+"','"+ca.cluster_id+"','"+ca.ave_mhp+"','"+
				ca.std_mhp+"','"+ca.ave_rt+"','"+ca.std_rt+"','"+ca.ave_inten+"','"+ca.std_inten+"','"+
				ca.ave_ref_rt+"','"+ca.std_ref_rt+"','"+ca.ave_charge+"','"+ca.std_charge+"','"+
				ca.total_rep_rate+
			"')";
			caCounter++;
		}
			
		if(caCounter>queueLen || (ca==null && caCounter>0))
		{
			db.executeSQL(
				"INSERT INTO `temp_cluster_average` " +
				"(`index`,`expression_analysis_index`,`cluster_id`,`ave_mhp`," +
				"`std_mhp`,`ave_rt`,`std_rt`,`ave_inten`,`std_inten`," +
				"`ave_ref_rt`,`std_ref_rt`,`ave_charge`,`std_charge`," +
				"`total_rep_rate`) VALUES " + caValues + ";"
			);
			
			caValues = "";
			caCounter=0;
		}
	}
	public void commitClusterAverage() throws Exception
	{
		storeClusterAverage(null);
		moveTableData("temp_cluster_average", "cluster_average", true);
	}
	// -----------------------------------------------------------------------------
	public void prepareClusteredEMRT() 
	{
		createInMemoryMirror("clustered_emrt", "temp_clustered_emrt", "index");
	}
	String emrtValues = "";
	int emrtCounter = 0;
	/**
	 * store clustered emrts using MULTI-ROW-INSERTs,
	 * after last cluster has been queued call 'storeClusteredEMRT(null);'
	 * for commit buffered emrts to db
	 * @param emrt
	 * @throws Exception
	 */
	public void storeClusteredEMRT(ClusteredEMRT emrt) throws Exception 
	{
		if(emrt!=null)
		{
			emrtValues += ((emrtCounter==0) ? "" : ", ") +
			"('"+
			emrt.index+"','"+emrt.workflow_index+"','"+emrt.clusterAverage.expression_analysis_index+"','"+
			emrt.clusterAverage.cluster_id+"','"+emrt.clusterAverage.index+"','"+
			emrt.mass+"','"+emrt.sd_mhp+"','"+emrt.inten+"','"+emrt.spec_index+"','"+emrt.charge+"','"+	emrt.rt+"','"+
			emrt.sd_rt+"','"+emrt.ref_rt+"','"+emrt.precursor_type+"','"+emrt.low_energy_index+"','"+emrt.inten+
			"')";
			emrtCounter++;
		}
			
		if(emrtCounter>queueLen || (emrt==null && emrtCounter>0))
		{
			db.executeSQL(
				"INSERT INTO `temp_clustered_emrt` ("+
				"`index`,`workflow_index`,`expression_analysis_index`," +
				"`cluster_id`,`cluster_average_index`,"+
				"`mass`,`sd_mhp`,`inten`,`spec_index`,`charge`,`rt`,"+
				"`sd_rt`,`ref_rt`,`precursor_type`,`low_energy_index`, `cor_inten`) " +
				"VALUES " + emrtValues + ";"
			);
			
			emrtValues = "";
			emrtCounter=0;
		} 
	}
	public void commitClusteredEMRT() throws Exception
	{
		storeClusteredEMRT(null); // commit buffer content
		moveTableData("temp_clustered_emrt", "clustered_emrt", true);
	}
	// -----------------------------------------------------------------------------
	public int getLowEnergyNextIndex() 
	{
		return leMaxIndex + 1;
	}
	public void prepareLowEnergy()
	{
		createInMemoryMirror("low_energy", "temp_low_energy", "index");
		leMaxIndex = 
			Math.max( 
				getMaxValue("low_energy", "index"),
				getMaxValue("mass_spectrum", "low_energy_index")
			);
	}
	String leValues = "";
	int leCounter = 0;
	int leMaxIndex = 0;
	public void storeLowEnergy(LowEnergy le) throws Exception 
	{
		if(le!=null) 
		{
			leValues += ((leCounter==0) ? "" : ", ") +
			"('"+le.index+"','"+le.workflow_index+"','"+le.id+"','"+le.charge+"','"+
			le.mass+"','" + le.retention_time+"','" + le.retention_time_rounded+"')";
			leCounter++;
			if(leMaxIndex<le.index) leMaxIndex=le.index;
		}
		
		if(leCounter>queueLen || (le==null && leCounter>0))
		{
			db.executeSQL(
				"INSERT INTO `temp_low_energy` (" +
				"`index`,`workflow_index`,`id`,`charge`,"+
				"`mass`,`retention_time`,`retention_time_rounded`) VALUES " + leValues + ";"
			);

			leValues = "";
			leCounter=0;
		}
	}
	public void commitLowEnergy() throws Exception
	{
		storeLowEnergy(null);
		moveTableData("temp_low_energy", "low_energy", true);
	}
	// -----------------------------------------------------------------------------
	public int getQueryMassNextIndex() 
	{
		return qmMaxIndex + 1;
	}
	public void prepareQueryMass() 
	{
		createInMemoryMirror("query_mass", "temp_query_mass", "index");
		qmMaxIndex = Math.max( getMaxValue("query_mass", "index"), qmMaxIndex);
	}
	String qmValues = "";
	int qmCounter = 0;
	int qmMaxIndex = 0;
	public void storeQueryMass(QueryMass qm) throws Exception 
	{
		if(qm!=null)
		{
			qmValues += ((qmCounter==0) ? "" : ", ") +
			"('"+qm.index+"','"+qm.workflow_index+"','"+qm.low_energy_index+
			"','"+qm.id+"','"+qm.low_energy_id+"','"+qm.intensity+"')";
			qmCounter++;
			if(qmMaxIndex<qm.index) qmMaxIndex = qm.index;
		}
		
		if(qmCounter>queueLen || (qm==null && qmCounter>0))
		{
			db.executeSQL(
				"INSERT INTO `temp_query_mass` (" +
				"`index`,`workflow_index`,`low_energy_index`," +
				"`id`,`low_energy_id`,`intensity`" +
				") VALUES " + qmValues + ";"
			);
			qmValues = "";
			qmCounter=0;
		}
	}
	public void commitQueryMass() throws Exception
	{
		storeQueryMass(null);
		moveTableData("temp_query_mass", "query_mass", true);
	}
	// -----------------------------------------------------------------------------
	public int getProteinNextIndex() 
	{
		return proMaxIndex + 1;
	}
	public void prepareProtein() 
	{
		proMaxIndex = getMaxValue("protein", "index");
		// do nothing, because mysql doesn't support BLOB/TEXT columns in MEMORY
		// createInMemoryMirror("protein", "temp_protein", "index");
	}
	String proValues = "";
	int proCounter = 0;
	int proMaxIndex = 0;
	public void storeProtein(Protein p) throws Exception 
	{
		if(p!=null) 
		{
			// cut strings to fit maximum length
			if (p.entry.length() > 100) p.entry = p.entry.substring( 0, 100 );
			if (p.accession.length() > 100) p.accession = p.accession.substring( 0, 100 );
			if (p.description.length() > 512) p.description = p.description.substring( 0, 512 );
			
			proValues += ((proCounter==0) ? "('" : ",('") + 
			p.index+"','"+p.workflow_index+"','"+p.id+"','"+p.auto_qc+"','"+p.curated+"','"+
			p.coverage+"','"+p.score+"','"+p.rms_mass_error_prec+"','"+
			p.rms_mass_error_frag+"','"+p.rms_rt_error_frag+"','"+p.entry+"','"+
			p.accession+"','"+p.description+"','"+p.mw+"','"+p.aq_fmoles+"','"+
			p.aq_ngrams+"','"+p.pi+"','"+p.sequence+"','" +p.peptides+"','" +p.products+"')";
			proCounter++;
			if(proMaxIndex<p.index) proMaxIndex=p.index;
		}
			
		if(proCounter>queueLen || (p==null && proCounter>0))
		{
			db.executeSQL(
				"INSERT INTO `protein` (" +
				"`index`,`workflow_index`,`id`,`auto_qc`,`curated`," +
				"`coverage`,`score`,`rms_mass_error_prec`," +
				"`rms_mass_error_frag`,`rms_rt_error_frag`,`entry`," +
				"`accession`,`description`,`mw`,`aq_fmoles`," +
				"`aq_ngrams`,`pi`,`sequence`,`peptides`,`products`" +
				") VALUES " +  proValues + ";"
			);
			
			proValues = "";
			proCounter = 0;
		}
	}
	public void commitProtein() throws Exception
	{
		storeProtein(null);		
	}
	// -----------------------------------------------------------------------------
	public int getPeptideNextIndex() 
	{
		return pepMaxIndex + 1;
	}
	public void preparePeptide()
	{
		createInMemoryMirror("peptide", "temp_peptide", "index");
		pepMaxIndex = getMaxValue("peptide", "index");
	}
	String pepValues = "";
	int pepCounter = 0;
	int pepMaxIndex = 0;
	public void storePeptide(Peptide p) throws Exception 
	{
		if(p!=null)
		{
			pepValues+= ((pepCounter==0) ? "('" : ",('") + 
				p.index+"','"+p.workflow_index+"','"+p.protein_index+"','"+p.query_mass_index+"','"+
				p.protein_id+"','" +p.id+"','" +p.mass+"','" +
				p.sequence+"','" +p.type+"','" +p.modifier+"','" +p.start+"','" +
				p.end+"','" +p.coverage+"','" +p.frag_string+"','" +p.rms_mass_error_prod+"','" +
				p.rms_rt_error_prod+"','" +p.auto_qc+"','" +p.curated+"','" +p.mass_error+"','" +
				p.mass_error_ppm+"','" +p.score+"','"+p.products+"')";
			pepCounter++;
			if(pepMaxIndex<p.index) pepMaxIndex=p.index;
		}
		
		if(pepCounter>queueLen || (p==null && pepCounter>0))
		{
			db.executeSQL(
				"INSERT INTO `temp_peptide` (" +
				"`index`,`workflow_index`,`protein_index`,`query_mass_index`," +
				"`protein_id`,`id`,`mass`,"+
				"`sequence`,`type`,`modifier`,`start`," +
				"`end`,`coverage`,`frag_string`,`rms_mass_error_prod`," +
				"`rms_rt_error_prod`,`auto_qc`,`curated`,`mass_error`," +
				"`mass_error_ppm`,`score`,`products`" + 
				") VALUES " + pepValues + ";"
			);
			
			pepValues = "";
			pepCounter=0;
		}
	}
	public void commitPeptide() throws Exception
	{
		storePeptide(null);
		moveTableData("temp_peptide", "peptide", true);
	}
	// -----------------------------------------------------------------------------
	public int getMassPeakNextIndex() 
	{
		return mpMaxIndex + 1;
	}
	public void prepareMassPeak()
	{
		createInMemoryMirror("mass_spectrum", "temp_mass_spectrum", "index");
		mpMaxIndex = getMaxValue("mass_spectrum", "index");
	}
	String mpQueue = "";
	int mpCounter = 0;
	int mpMaxIndex = 0;
	public void storeMassPeak(MassPeak m) throws Exception 
	{
		if(m!=null)
		{
			mpQueue += ((mpCounter==0) ? "('" : ",('") + 
				m.index+"','"+m.workflow_index+"','"+m.Mass+"','"+m.Intensity+"','"+m.Mobility+"','"+
					m.MassSD + "','" + m.IntensitySD + "','" + m.id + "','" + m.AverageCharge + "','" + m.Z + "','" + m.RT + "','" +
				m.RTSD+"','"+m.FWHM+"','"+m.ClusterLiftOffRT+"','"+m.LiftOffRT+"','"+
				m.InfUpRT+"','"+m.InfDownRT+"','"+m.TouchDownRT+"','"+m.ClusterTouchDownRT+
					"','" + m.parent_index + "','" + m.fraction + "')"
			;
			mpCounter++;
			if(mpMaxIndex<m.index) mpMaxIndex=m.index;
			if (leMaxIndex < m.parent_index) leMaxIndex = m.parent_index;
		}
		
		if( mpCounter>queueLen || (m==null && mpCounter>0) )
		{
			db.executeSQL(
				"INSERT INTO `temp_mass_spectrum` (" +
				"`index`,`workflow_index`,`Mass`,`Intensity`,`Mobility`,`MassSD`, " +
				"`IntensitySD`, `LE_ID`, `AverageCharge`, `Z`, `RT`, " +
				"`RTSD`, `FWHM`, `ClusterLiftOffRT`, `LiftOffRT`, " +
				"`InfUpRT`, `InfDownRT`, `TouchDownRT`, `ClusterTouchDownRT`, " +
				"`low_energy_index`, `fraction`) VALUES " + mpQueue + ";"
			);
			
			mpQueue = "";
			mpCounter=0;
		}
	}
	public void commitMassPeaks() throws Exception
	{
		storeMassPeak(null);
		moveTableData("temp_mass_spectrum", "mass_spectrum", true);
	}
	// -----------------------------------------------------------------------------

	/**
	 * @param tables
	 */
	public void optimizeTables(String[] tables)
	{
		System.out.print("optimizing tables ... ");
		Bencher t = new Bencher().start();
		for(String table:tables) db.optimizeTable(table);
		System.out.println( t.stop().getSecString() );		
	}
}
