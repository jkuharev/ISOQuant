package isoquant.plugins.processing.preprocessing;

import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.jsix.utilities.Settings;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.log.LogEntry;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

public class DataPreparator extends SingleActionPlugin4DB
{
	private boolean depletePepFrag2 = false;
	private boolean depletePepCurated0 = false;
	
	public DataPreparator(iMainApp app) 
	{
		super(app);
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		MySQL db = p.mysql;
		Bencher t = new Bencher().start();
		progressListener.setStatus( "preparing data for '" + p.data.title + "'" );

		p.log.add(LogEntry.newParameter("process.peptide.deplete.PEP_FRAG_2", depletePepFrag2));
		p.log.add(LogEntry.newParameter("process.peptide.deplete.CURATED_0", depletePepCurated0));
		
		if( depletePepFrag2 )
		{
			progressListener.setStatus("depleting peptides having type PEP_FRAG_2 ...");
			db.executeSQLFile(getPackageResource("00_deplete_pep_type2.sql"), MySQL.defExecListener);
			p.log.add( LogEntry.newEvent("'PEP_FRAG_2' - peptides depleted!", "") );
		}

		if( depletePepCurated0 )
		{
			progressListener.setStatus("depleting peptides having curated score = 0 ...");
			db.executeSQLFile(getPackageResource("01_deplete_pep_curated0.sql"), MySQL.defExecListener);
			p.log.add( LogEntry.newEvent("'curated = 0' - peptides depleted!", "") );
		}
		
		db.executeSQLFile( getPackageResource("10_preprocessing.sql"), MySQL.defExecListener );
		
		progressListener.setStatus("data preparation done");
		p.log.add( LogEntry.newEvent("data preparation", t.stop().getSecString()) );
	}
	
	@Override public void loadSettings(Settings cfg)
	{
		depletePepFrag2  = cfg.getBooleanValue("process.peptide.deplete.PEP_FRAG_2", depletePepFrag2, false);
		depletePepCurated0 = cfg.getBooleanValue("process.peptide.deplete.CURATED_0", depletePepCurated0, false);
	}

	@Override public String getMenuItemIconName(){return "chip";}
	@Override public String getMenuItemText(){return "prepare data";}
	@Override public String getPluginName(){return "Data Preparator";}
	@Override public int getExecutionOrder(){return 2;}
}
