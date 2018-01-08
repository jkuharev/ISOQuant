/** ISOQuant, isoquant.plugins.processing.fractions, 07.05.2012 */
package isoquant.plugins.processing.fractions;

import isoquant.app.Defaults;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.db.DBProject;
import isoquant.kernel.plugin.SingleActionPlugin4DB;
import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.jsix.utilities.Settings;

/**
 * <h3>{@link LCFractionsTimeUnshifter}</h3>
 * @author kuharev
 * @version 07.05.2012 11:43:13
 */
public class LCFractionsTimeUnshifter extends SingleActionPlugin4DB
{
	public LCFractionsTimeUnshifter(iMainApp app)
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
		// skip actions if column oldRT in table mass_spectrum does not exist!
		if (!db.columnExists("mass_spectrum", "oldRT"))
		{
			System.out.println("Fraction times are not shifted!");
			System.out.println("skipping unshifting fraction times ...");
			return;
		}
		db.executeSQLFile(getPackageResource("unshift.sql"));
	}

	@Override public String getPluginName()
	{
		return "Gel Fraction Unshifter";
	}

	@Override public String getMenuItemText()
	{
		return "unshift Gel Fractions Times";
	}

	@Override public String getMenuItemIconName()
	{
		return "fraction_unshift";
	}
}
