/** ISOQuant, isoquant.plugins.processing.fractions, 07.05.2012 */
package isoquant.plugins.processing.fractions;

import isoquant.app.Defaults;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DB;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.utilities.Settings;

/**
 * <h3>{@link LCFractionsTimeShifter}</h3>
 * @author kuharev
 * @version 07.05.2012 11:43:13
 */
public class LCFractionsTimeShifter extends SingleActionPlugin4DB
{
	public LCFractionsTimeShifter(iMainApp app)
	{
		super(app);
	}
	private boolean fractionShiftingEnabled = false;

	@Override public void loadSettings(Settings cfg)
	{
		fractionShiftingEnabled = cfg.getBooleanValue("process.emrt.rt.shiftByFraction", fractionShiftingEnabled, !Defaults.DEBUG);
	}

	@Override public void runPluginAction(DBProject p) throws Exception
	{
		if (!fractionShiftingEnabled) return;
		MySQL db = p.mysql;
		// do nothing if times already shifted!
		if (db.columnExists("mass_spectrum", "oldRT"))
		{
			System.out.println("Fraction times are already shifted!");
			System.out.println("skipping fraction based time shifting ...");
			return;
		}
		// do nothing if no different fractions present
		if (db.getFirstInt("SELECT COUNT( DISTINCT fraction ) FROM `mass_spectrum` ", 1) < 2)
		{
			System.out.println("This project does not contain merged LC fractions!");
			System.out.println("skipping fraction based time shifting ...");
			return;
		}
// db.addColumn("mass_spectrum", "oldRT", "FLOAT DEFAULT 0", true);
// db.executeSQL("UPDATE mass_spectrum SET oldRT=RT", false);
//
// double timeShiftFactor =
// db.getFirstInt("SELECT FLOOR( CEILING(MAX(RT)/10) * 10 ) FROM `mass_spectrum`",
// 1);
//
// db.executeSQL("UPDATE mass_spectrum SET RT=(oldRT+fraction*"+timeShiftFactor+")");
		db.executeSQLFile(getPackageResource("shift.sql"));
	}

	@Override public String getPluginName()
	{
		return "Gel Fraction Shifter";
	}

	@Override public String getMenuItemText()
	{
		return "shift Gel Fractions Times";
	}

	@Override public String getMenuItemIconName()
	{
		return "fraction_shift";
	}
}
