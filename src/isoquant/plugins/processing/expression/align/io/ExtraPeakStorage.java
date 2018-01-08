/** ISOQuant, isoquant.plugins.processing.expression.align.io, 11.05.2011 */
package isoquant.plugins.processing.expression.align.io;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import de.mz.jk.jsix.mysql.MySQL;
import de.mz.jk.ms.align.com.IMSPeak;

/**
 * TODO das Alignment soll nicht ISOQuant-Daten-Tabellen benutzen,
 * sondern viel mehr als ein eigenst�ndiger Ablauf auf
 * die explizit daf�r vorgesehene io-Tabellen zugreifen. 
 * 

-- -----------------------------------------------------
-- Table `rtw_in`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `rtw_in` ;
CREATE  TABLE IF NOT EXISTS `rtw_in` 
(
	`peak_id` INT,
	`peak_list_id` INT,
	`time` FLOAT,
	`type` TINYINT,
	`mass` DOUBLE,
	`drift` FLOAT,
	PRIMARY KEY (`peak_id`, `type`),
	INDEX (`peak_list_id`),
	INDEX (`time`)
);

-- -----------------------------------------------------
-- Table `rtw_out`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `rtw_out` ;
CREATE  TABLE IF NOT EXISTS `rtw_out` 
(
	`peak_list_id` INT,
	`old_time` FLOAT,
	`new_time` FLOAT,
	PRIMARY KEY (`peak_list_id`),
	INDEX (`old_time`)
);

 * <h3>{@link ExtraPeakStorage}</h3>
 * @author Joerg Kuharev
 * @version 11.05.2011 13:19:09
 */
public class ExtraPeakStorage extends PeakStorage
{
	public static final String inputTable = "rtw_in";
	public static final String outputTable = "rtw_out";

	public ExtraPeakStorage(MySQL _db)
	{
		super(_db);
	}

	/**
	 * all run indexes in descending order by number of peaks
	 * @return list of indexes
	 */
	synchronized public List<Integer> getPeakListIndexes()
	{
		return db.getIntegerValues(
				"SELECT `peak_list_id` FROM `rtw_in` GROUP BY `peak_list_id` ORDER BY COUNT(`peak_id`) DESC"
				);
	}

	synchronized public List<IMSPeak> getPeaks(int peakListID, byte peakType)
	{
		List<IMSPeak> res = new ArrayList<IMSPeak>();
		try
		{
			ResultSet rs = db.executeSQL(
					"SELECT `peak_id`, `time`, `mass`, `drift` " +
							" FROM `rtw_in` WHERE `peak_list_id`=" + peakListID + " AND `type`=" + peakType +
							" ORDER BY `time` ASC"
					);
			while (rs.next())
			{
				res.add(
						new IMSPeak(
								rs.getInt("peak_id"),
								rs.getFloat( "mass" ),
								rs.getFloat( "time" ),
								rs.getFloat( "drift" ),
								peakType
						)
						);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
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
