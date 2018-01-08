package isoquant.plugins.processing.linkage;

import java.sql.Statement;

import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.utilities.Bencher;
import de.mz.jk.jsix.utilities.Settings;
import isoquant.interfaces.iMainApp;
import isoquant.interfaces.log.iLogManager;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.log.LogEntry;
import isoquant.kernel.plugin.SingleActionPlugin4DB;

/**
 * <h3>{@link EMRTTableLinker}</h3>
 * builds links between DB entities in ISOQuant DBProject Database
 * @author Joerg Kuharev
 * @version 20.01.2011 15:51:56
 */
public class EMRTTableLinker extends SingleActionPlugin4DB
{
	public EMRTTableLinker(iMainApp app) 
	{
		super(app);
	}
	
	@Override public String getMenuItemIconName(){return "link";}
	@Override public String getMenuItemText(){return "link clustered emrt to low energy";}
	@Override public String getPluginName(){return "Clustered EMRT to Low Energy Linker";}
	
	private double OPTION_DELTA_MASS_MAX_VALUE = 0.0005;
	private double OPTION_DELTA_RT_MAX_VALUE = 0.1;
	private int OPTION_MASS_STEP_SIZE = 10;
	private double OPTION_MASS_OVERLAP_SIZE = 0.9;
	
	private String[] tables = new String[]{
			"clustered_emrt","peptide","protein",
			"query_mass","low_energy","cluster_average",
			"workflow","sample","group","expression_analysis","project"
	};
	
	private MySQL prjDB = null;
	
	@Override public void runPluginAction(DBProject p) throws Exception
	{
		MySQL db = p.mysql;
		progressListener.setStatus( "linking data for '" + p.data.title + "'" );
		
		logParameters(p.log);
		
		Bencher t = new Bencher().start();
		prjDB = db;
		optimizeTables();
		linkEMRT2LE();
		optimizeTables();
		
		System.out.println("project linkage duration: " + t.stop().getSecString());
		p.log.add( LogEntry.newEvent("project data linked", t.stop().getSecString()) );
		progressListener.setStatus("data linkage done");
	}
	
	private void logParameters(iLogManager log)
	{
		log.add(LogEntry.newParameter("process.emrt.peptideLinkage.mass.maxDelta", OPTION_DELTA_MASS_MAX_VALUE+""));
		log.add(LogEntry.newParameter("process.emrt.peptideLinkage.rt.maxDelta", OPTION_DELTA_RT_MAX_VALUE+""));
		log.add(LogEntry.newParameter("process.emrt.peptideLinkage.mass.step", OPTION_MASS_STEP_SIZE+""));
		log.add(LogEntry.newParameter("process.emrt.peptideLinkage.mass.overlap", OPTION_MASS_OVERLAP_SIZE+""));
	}

	@Override public void loadSettings(Settings cfg)
	{
		OPTION_DELTA_MASS_MAX_VALUE =	cfg.getDoubleValue("process.emrt.peptideLinkage.mass.maxDelta", OPTION_DELTA_MASS_MAX_VALUE, true);
		OPTION_DELTA_RT_MAX_VALUE =		cfg.getDoubleValue("process.emrt.peptideLinkage.rt.maxDelta", OPTION_DELTA_RT_MAX_VALUE, true);		
		OPTION_MASS_STEP_SIZE =			cfg.getIntValue("process.emrt.peptideLinkage.mass.step", OPTION_MASS_STEP_SIZE, true);
		OPTION_MASS_OVERLAP_SIZE =		cfg.getDoubleValue("process.emrt.peptideLinkage.mass.overlap", OPTION_MASS_OVERLAP_SIZE, true);
	}
	
	private void optimizeTables() 
	{
		Bencher t = new Bencher().start();
		System.out.print("optimizing tables ... ");
		for(String table:tables)
		{
			prjDB.optimizeTable(table);
		}
		System.out.println( t.stop().getSec() + "s");
	}
	
	public void linkEMRT2LE()
	{
		System.out.print("linking clustered_emrt to low_energy ... ");
		Bencher t = new Bencher().start();
		// get min(mass) and max(mass) from emrt
		double min = Double.parseDouble( prjDB.getFirstValue("clustered_emrt", "min(mass)", "1") );
		double max = Double.parseDouble( prjDB.getFirstValue("clustered_emrt", "max(mass)", "1") );
		min = Math.min(min, Double.parseDouble( prjDB.getFirstValue("low_energy", "min(mass)", "1") ));
		max = Math.max(max, Double.parseDouble( prjDB.getFirstValue("low_energy", "max(mass)", "1") ))+OPTION_MASS_STEP_SIZE;
		
		progressListener.setProgressMaxValue((int)max);
		
		for(double low=min; low<max; low+=OPTION_MASS_STEP_SIZE)
		{
			progressListener.setProgressValue((int)low);
			updateEMRT2LE(
				low, 
				low + OPTION_MASS_STEP_SIZE + OPTION_MASS_OVERLAP_SIZE, 
				OPTION_DELTA_MASS_MAX_VALUE, 
				OPTION_DELTA_RT_MAX_VALUE
			);
		}
		prjDB.optimizeTable("clustered_emrt");
		
		progressListener.setProgressValue(0);
		System.out.println(t.stop().getSec() + "s");
	}

		/**
		 * partially links clustered_emrt to low_energy
		 * @param massFrom lower mass criterion
		 * @param massTo upper mass criterion
		 * @param deltaMass mass difference limit
		 * @param deltaRT retention time difference limit
		 * @return number of changed rows (`clustered_emrt`.`low_energy_index`=ce2le.le_i)
		 */
		public int updateEMRT2LE(double massFrom, double massTo, double deltaMass, double deltaRT)
		{
			Statement stmt = prjDB.getStatement();
			String sql = 
				"UPDATE `clustered_emrt` RIGHT JOIN  \n"+
				"(SELECT ce.index as ce_i, le.index as le_i \n"+
				"FROM (SELECT `index`,`workflow_index`, `rt`, `mass` FROM `clustered_emrt` WHERE `mass` BETWEEN "+massFrom+" AND "+massTo+") `ce` \n"+
				"INNER JOIN (SELECT `index`,`workflow_index`, `retention_time_rounded`, `mass` FROM `low_energy` WHERE `mass` BETWEEN "+massFrom+" AND "+massTo+") " +
				"`le` ON ce.workflow_index = le.workflow_index \n"+
				"WHERE Abs(`ce`.`rt`-`le`.`retention_time_rounded`)<="+deltaRT+
				" AND Abs(`ce`.`mass`-`le`.`mass`)<="+deltaMass+") ce2le \n"+
				"ON `clustered_emrt`.index=ce2le.ce_i \n"+
				"SET `clustered_emrt`.`low_energy_index`=ce2le.le_i;";
			int res=0;
			try{res=stmt.executeUpdate(sql);}catch(Exception e){System.err.println(sql);}	
			return res;
		}

		@Override public int getExecutionOrder(){return 2;}
}
