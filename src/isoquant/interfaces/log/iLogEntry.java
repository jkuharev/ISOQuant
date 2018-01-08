package isoquant.interfaces.log;

/**
 * <h3>iLogEntry</h3>
 * entry row of `history` table
 * @author Joerg Kuharev
 * @version 03.01.2011 14:08:08
 */
public interface iLogEntry
{
	/**
	 * <h3>Type</h3>
	 * type of log entry
	 * @author Joerg Kuharev
	 * @version 03.01.2011 14:33:02
	 */
	public enum Type
	{
		event, 
		parameter, 
		message;
		
		public static Type fromString(String typeString)
		{
			String ts = typeString.toLowerCase();
			return
				(ts.startsWith("event") ) ? event : 
				(ts.startsWith("param") ) ? parameter : message;
		}
	}

	/**
	 * unique log entry id from db 
	 * @return the id
	 */
	public int getID();
	
	/**
	 * type of this log entry
	 * @return the type
	 */
	public Type getType();
	
	/**
	 * time stamp of this log entry 
	 * @return the time stamp
	 */
	public String getTime();
	
	/**
	 * value of this log entry
	 * containing the name of an event, 
	 * a text message or the name of a parameter
	 * @return the value
	 */
	public String getValue();
	
	/**
	 * additional notes or the value of a parameter
	 * @return free text notes or the value of parameter
	 */
	public String getNote();
	
	/**
	 * assign full information, e.g. while reading from db
	 * @param id
	 * @param time
	 * @param type
	 * @param value
	 * @param note
	 * @return this log entry
	 */
	public iLogEntry set(int id, String time, Type type, String value, String note);
	
	/**
	 * assign partial information, e.g. while creating new log entry
	 * @param type
	 * @param value
	 * @param note
	 * @return this log entry
	 */
	public iLogEntry set(Type type, String value, String note);
}
