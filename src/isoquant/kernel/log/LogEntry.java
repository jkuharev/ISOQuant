package isoquant.kernel.log;

import isoquant.interfaces.log.iLogEntry;

/**
 * <h3>LogEntry</h3>
 * unificate log entries by formatting events/messages/parameters
 * @author Joerg Kuharev
 * @version 03.01.2011 14:37:18
 */
public class LogEntry implements iLogEntry
{
	/**
	 * create a new log for event
	 * @param eventName name of event
	 * @param note additional notes
	 * @return log entry for event
	 */
	public static iLogEntry newEvent(String eventName, String note)
	{
		return new LogEntry().set(Type.event, eventName, note);
	}
	
	/**
	 * create new log entry for a free message
	 * @param message some free message
	 * @param note additional notes
	 * @return log entry for message
	 */
	public static iLogEntry newMessage(String message, String note)
	{
		return new LogEntry().set(Type.message, message, note);
	}
	
	/**
	 * create new log entry for a parameter
	 * @param paramName the name of parameter
	 * @param paramValue the value of paramater
	 * @return log entry for parameter
	 */
	public static iLogEntry newParameter(String paramName, Object paramValue)
	{
		return new LogEntry().set(Type.parameter, paramName, paramValue.toString() );
	}
	
	private int id 			= 0;
	private String	time	= "";
	private Type	type	= Type.message;
	private String	value 	= "";
	private String 	note 	= "";
	
	@Override public iLogEntry set(int id, String time, Type type, String value, String note)
	{
		this.id = id;
		this.time = time;
		this.type = type;
		this.value = value;
		this.note = note;
		return this;
	}
	
	@Override public iLogEntry set(Type type, String value, String note)
	{
		this.type = type;
		this.value = value;
		this.note = note;		
		return this;
	}
	
	@Override public int getID(){return id;}
	@Override public String getTime(){return time;}
	@Override public Type getType(){return type;}
	@Override public String getValue(){	return (value!=null) ? value : "";}
	@Override public String getNote(){return (note!=null) ? note : "";}
}