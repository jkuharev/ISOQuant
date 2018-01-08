package isoquant.plugins.plgs.importing.importer;

import java.io.File;

import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.plgs.data.*;
import de.mz.jk.plgs.reader.*;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.log.LogEntry;

/**
 * <h3>ProjectImporter</h3>
 * read a PLGS project by expression analysis and import it into ISOQuant 1.0 database
 * @author Joerg Kuharev
 * @version 06.01.2011 16:00:12
 */
public class PLGSDataImporter
{
	private DBProject prj = null;
	private PLGSDataImportQueryHandler db = null;
	private ExpressionAnalysisReader eaReader = new ExpressionAnalysisReader();
	
	private String[] tables = new String[]
	{
			"clustered_emrt", "peptide", "protein",
			"query_mass", "low_energy", "cluster_average",
			"workflow", "workflow_metadata", "sample", "group", "expression_analysis", "project"
	};
	
	private int emrtIndex=0;
	private int msIndex=0;
	
//	private iProcessProgressListener progressListener=null;	
//	public void setProgressListener(iProcessProgressListener progressListener){this.progressListener = progressListener;}
//	public PLGSDataImporter(iProcessProgressListener progressListener){	setProgressListener(progressListener); }
	
	public PLGSDataImporter(){}
	
	private void optimizeTables() 
	{
		db.optimizeTables(tables);
	}

	/**
	 * imports project into database
	 * @param projectFile the project file
	 * @param readExpression should the expression analysis file be read or not<br>
	 * 			true: read EA data from file system<br> 
	 * 			false: use EA data from DBProject.expressionAnalyses<br>
	 * @param importExpressionResults should the expression analysis bluster file be read or not<br>
	 * 			true: read and import bluster data<br>
	 * 			false: ignore bluster results<br>
	 * @throws Exception
	 */
	public void importProject( File projectFile, boolean readExpression, boolean importExpressionResults ) throws Exception
	{
		DBProject p = new DBProject( ProjectReader.getProject( projectFile, false ) );
		importProject( p, readExpression, importExpressionResults );
	}

	/**
	 * import a project into database
	 * @param p the project
	 * @param readExpression should the expression analysis file be read or not<br>
	 * 			true: read EA data from file system<br> 
	 * 			false: use EA data from DBProject.expressionAnalyses<br>
	 * @param importExpressionResults should the expression analysis bluster file be read or not<br>
	 * 			true: read and import bluster data<br>
	 * 			false: ignore bluster results<br>
	 * @throws Exception
	 */
	public void importProject( DBProject p, boolean readExpression, boolean importExpressionResults ) throws Exception
	{
		this.db = new PLGSDataImportQueryHandler( p.mysql );
		
		this.emrtIndex = 0;
		this.msIndex = 0;
		this.prj = p;

		Bencher t = new Bencher().start();
		
		// remove all existing data from database
		db.clearDB();
		
		p.log.add( LogEntry.newEvent("project db created", "") );
		
		db.storeProject( p );
		p.data.dump();
		
		/**
		 * walk through selected expression analyses
		 */
		for ( String ea_id : p.data.selectedExpressionAnalysisIDs )
		{
			ExpressionAnalysis EA = null;
			if(readExpression)
			{
				eaReader.readExpressionAnalysis( p.data, ea_id );
				EA = eaReader.getExpressionAnalysis();
				p.data.expressionAnalyses.add( EA );
			} 
			else 
			{
				for ( ExpressionAnalysis a : p.data.expressionAnalyses )
				if(a.id.equals(ea_id))
				{
					EA = a;
					break;
				}
			}
			System.out.println("importing data for expression analysis '" + EA.name + "'");			
			
			EA.project = p.data;
			EA.project_index = p.data.index;
			
			importExpressionAnalysis( EA );
			
			// peakCount runs
			int nRuns=0; 
			for(Group g : EA.groups) for(Sample s : g.samples) nRuns += (s.workflows!=null) ? s.workflows.size() : 0;

			
			p.log.add(LogEntry.newMessage("data for "+nRuns+" runs imported",""));
			
			if( importExpressionResults )
			{
				importExpressionAnalysisResults( EA ); // clusters
				p.log.add(LogEntry.newMessage("expression analysis results imported",""));
			}
		}
		
		System.out.println("project importing duration: " + t.stop().getSecString() );
		p.log.add( LogEntry.newEvent("project imported", t.getSecString()) );
		
		optimizeTables();
	}
	
	/**
	 * importing of a BlusterOutput.cvf file
	 * @param ea 
	 * @throws Exception
	 */
	private void importExpressionAnalysisResults(ExpressionAnalysis ea) throws Exception 
	{	
		Bencher t = new Bencher().start();
		// BlusterOutput-file finden
		// File resFile = BlusterReader.getFile(rootDir, prj.id, ea.id, ea.result_id); 
		// Reader erstellen
		BlusterReader br = new BlusterReader( new File( prj.data.root ), ea );
		
		// Datei öffnen, Titelzeile lesen
		br.open();
		
		// Workflows title <-> index Zuordnung finden/setzen
		for(Group g : ea.groups)
			for(Sample s : g.samples)
				for(Workflow w : s.workflows)
					br.setWorkflowIndex( w.replicate_name, w.index );
		
		System.out.print(
			"importing clustered data for '"+
			ea.project.title+"'.'" + ea.name + "' ... "
		);
		
		db.prepareClusterAverage();
		db.prepareClusteredEMRT();
		ClusterAverage ca;
		// für jeden cluster=BlusterReader.next()
		while( br.next() )
		{
			ca = br.getClusterAverage();
			ca.index = ca.cluster_id+1;// Indexer.getNext(prj.id+".cluster_average");
			// cluster speichern
			db.storeClusterAverage( ca );
			// clusteredEMRTs speichern
			for(ClusteredEMRT emrt : ca.clusteredEMRT)
			{
				emrt.clusterAverage = ca;
				emrt.index = ++emrtIndex; 
					// Indexer.getNext(prj.id+".clustered_emrt");
				db.storeClusteredEMRT( emrt );
			}
		}
		db.commitClusterAverage();
		db.commitClusteredEMRT();
			
		System.out.println(
			br.getNumberOfClusters() + " clusters imported! TIME: " + 
			t.stop().getTime(Bencher.SECONDS) + "s"
		);
	}
	
	/**
	 * imports an expression analysis
	 * @param ea
	 * @throws Exception
	 */
	private void importExpressionAnalysis(ExpressionAnalysis ea) throws Exception 
	{
		db.storeExpressionAnalysis( ea );
		for(Group g : ea.groups)
		{
			g.expression_analysis_index = ea.index;
			importGroup( g );
		}
	}

	/**
	 * imports a group
	 * @param group
	 * @throws Exception
	 */
	private void importGroup(Group group) throws Exception 
	{
		db.storeGroup(group);
		for(Sample s : group.samples)
		{
			s.group_index = group.index;
			importSample( s );
		}
	}

	/**
	 * imports a sample
	 * @param s
	 * @throws Exception
	 */
	private void importSample(Sample s) throws Exception 
	{
		db.storeSample(s);

		for(Workflow w : s.workflows)
		{
			Bencher t=new Bencher().start();
			w.sample_index = s.index;
			beforeWorkflowImport();
			importWorkflow( w );
			System.out.print("\t"+ w.index +":\t");			
			importWorkflowData( w );
			afterWorkflowImport();
			System.out.print("TIME: " + t.stop().getSec() +  "s; ");
			// free memory
			long oldMem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024/1024;
			clearWorkflowData( w );
			w = null;
			System.gc();
			long newMem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024/1024;
			System.out.println("MEM: "+oldMem+"M->"+newMem+"M;");
		}

	}

	/**
	 * @throws Exception 
	 * 
	 */
	private void afterWorkflowImport() throws Exception
	{
		db.commitLowEnergy();
		db.commitQueryMass();
		db.commitMassPeaks();
		db.commitPeptide();
		db.commitProtein();
	}

	/**
	 * 
	 */
	private void beforeWorkflowImport() throws Exception
	{
		db.reconnectToDatabase( false );
		db.prepareLowEnergy();
		db.prepareQueryMass();
		db.prepareMassPeak();
		db.preparePeptide();
		db.prepareProtein();
	}

	/**
	 * running through Workflow-Children and sets them null
	 * @param w
	 */
	private void clearWorkflowData(Workflow w) 
	{
		w.lowEnergies.clear();
		w.queryMasses.clear();
		w.proteins.clear();
		w.peptides.clear();
	}

	/**
	 * imports a workflow
	 * @param w
	 * @throws Exception
	 */
	private void importWorkflow(Workflow w) throws Exception 
	{
		WorkflowReader.readWorkflow( prj.data, w );
		w.index = db.getNextWorkflowIndex();
		db.storeWorkflow( w );
		db.storeWorkflowMetaData( w.index, "Workflow", w.metaInfo );
	}
	
	/**
	 * import workflows children: LE, QM, PE, PR
	 * @param w the workflow
	 * @throws Exception
	 */
	private void importWorkflowData(Workflow w) throws Exception
	{
		System.out.flush();
		// import order is important!!!
		// 1: LOW ENERGY
		importLowEnergy(w);
		// 2: QUERY MASS
		importQueryMass(w);
		// 3: PROTEIN
		importProteins(w);
		// 4: PEPTIDE
		importPeptides(w);
		// 5: MASS SPECTRUM
        importMassSpectrum(w);
		System.out.flush();
	}

	private void importMassSpectrum(Workflow w) throws Exception
	{
		IMassSpectrumReader msr = w.acquisitionMode.equals( Workflow.AcquisitionMode.DDA )
				? new DDAMassSpectrumReader()
				: new DIAMassSpectrumReader();
		msr.openMassSpectrum( prj.data.getProjectDirectoryPath(), w.sample_tracking_id );
		db.storeWorkflowMetaData( w.index, "MassSpectrum", msr.getMetaInfo() );
		// db.prepareMassPeak();
		while( msr.next() )
		{
			MassPeak mp = msr.getMassPeak();
			mp.workflow_index = w.index;
			mp.index = ++msIndex;
				// Indexer.getNext(prj.id+".mass_spectrum");
				// mp.index = mp.le_id + w.LeId2IndexShift; // does not work if COUNT(MS) > COUNT(LE)
			mp.parent_index = mp.id + w.LeId2IndexShift;
			db.storeMassPeak( mp );
		}
		// db.commitMassPeaks();
		System.out.print(msr.countValidPeaks()+"MS; ");
	}
	
	private void importLowEnergy(Workflow w) throws Exception 
	{
		w.LeId2IndexShift = db.getLowEnergyNextIndex();
		// System.out.print("(LE-Shift="+w.LeId2IndexShift+")");
		System.out.print( w.lowEnergies.size()+"LE; ");
		/** walks through all low energies */
		for(int i : w.lowEnergies.keySet())
		{
			LowEnergy LE = w.lowEnergies.get(i);
			LE.index = LE.id + w.LeId2IndexShift; // calculate own index
			LE.workflow_index = w.index;
			db.storeLowEnergy( LE );
		}
	}

	private void importQueryMass(Workflow w) throws Exception 
	{
		System.out.print(w.queryMasses.size()+"QM; ");
		w.QmId2IndexShift = db.getQueryMassNextIndex();
		/** walks through all query masses */
		for(int i : w.queryMasses.keySet())
		{
			QueryMass QM = w.queryMasses.get(i);
			QM.index = QM.id + w.QmId2IndexShift; // calculate own index
			QM.workflow_index = w.index;
			QM.low_energy_index = QM.low_energy_id + w.LeId2IndexShift; // calculable link index
			db.storeQueryMass( QM );
		}
	}

	private void importProteins(Workflow w) throws Exception 
	{
		System.out.print(w.proteins.size()+"PR; ");
		w.ProId2IndexShift = db.getProteinNextIndex();
		/** walks through all proteins */
		for(int i : w.proteins.keySet())
		{
			Protein P = w.proteins.get(i);
			P.index = P.id + w.ProId2IndexShift; // calculate own index
			P.workflow_index = w.index;
			db.storeProtein( P );
		}
	}

	private void importPeptides(Workflow w) throws Exception 
	{
		System.out.print(w.peptides.size()+"PE; ");
		w.PepId2IndexShift = db.getPeptideNextIndex();
		/** walks through all peptides */
		for(int i : w.peptides.keySet())
		{
			Peptide P = w.peptides.get(i);
			P.index = P.id + w.PepId2IndexShift; // calculate index
			P.workflow_index = w.index;
			P.protein_index = P.protein_id + w.ProId2IndexShift; // calculable link index
			P.query_mass_index = P.id + w.QmId2IndexShift; // calculable link index
			db.storePeptide( P );
		}
	}
}
