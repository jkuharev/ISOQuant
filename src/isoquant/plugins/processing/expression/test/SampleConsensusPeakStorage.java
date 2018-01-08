/** ISOQuant, isoquant.plugins.processing.expression.test, 16.11.2011*/
package isoquant.plugins.processing.expression.test;

import java.util.List;

import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.ms.align.com.IMSPeak;
import isoquant.plugins.processing.expression.align.io.PeakStorage;

/**
 * <h3>{@link SampleConsensusPeakStorage}</h3>
 * @author kuharev
 * @version 16.11.2011 16:47:52
 */
public class SampleConsensusPeakStorage extends PeakStorage
{

	/**
	 * @param _db
	 */
	public SampleConsensusPeakStorage(MySQL _db)
	{
		super(_db);
	}

	@Override public List<IMSPeak> getAllPeaks(int runIndex)
	{
		return null;
	}

	@Override public int countAllPeaks(int runIndex)
	{
		return 0;
	}
}
