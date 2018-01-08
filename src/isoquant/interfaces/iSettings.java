package isoquant.interfaces;

public interface iSettings {

	/**
	 * load a key named value from project global configuration file,<br>
	 * if the key does not exist a key->value pair will be created
	 * using defaultValue and it will be returned
	 * @param key
	 * @param defaultValue
	 * @return value of key named property
	 */
	public abstract String getValue(String key, String defaultValue);

	/**
	 * load a key named value from project global configuration file,<br>
	 * if the key does not exist a key->value pair will be created
	 * using defaultValue and it will be returned
	 * @param key
	 * @param defaultValue
	 * @return value of key named property
	 */
	public abstract double getValue(String key, double defaultValue);

	/**
	 * load a key named value from project global configuration file,<br>
	 * if the key does not exist a key->value pair will be created
	 * using defaultValue and it will be returned
	 * @param key
	 * @param defaultValue
	 * @return value of key named property
	 */
	public abstract int getValue(String key, int defaultValue);

	/**
	 * load a key named value from project global configuration file,<br>
	 * if the key does not exist a key->value pair will be created
	 * using defaultValue and it will be returned
	 * @param key
	 * @param defaultValue
	 * @return value of key named property
	 */
	public abstract boolean getValue(String key, boolean defaultValue);

	/**
	 * load a key named value from project global configuration file
	 * @param key
	 * @return value of key named property
	 * @throws Exception 
	 */
	public abstract String getValue(String key) throws Exception;

	/**
	 * save a key named value to project global configuration file
	 * @param key
	 * @param value
	 */
	public abstract void setValue(String key, String value);

	/**
	 * store array of objects' string representations
	 * @param key
	 * @param values array of objects
	 */
	public abstract void setArray(String key, Object[] values);

	/**
	 * store array of objects' string representations
	 * @param key
	 * @param values array of objects
	 */
	public abstract String[] getArray(String key, Object[] defaultValues);

	/**
	 * remove a key/value pair from configuration file
	 * @param key
	 */
	public abstract void remove(String key);

}