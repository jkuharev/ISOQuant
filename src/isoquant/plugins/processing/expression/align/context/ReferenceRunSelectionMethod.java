/** ISOQuant, isoquant.plugins.processing.expression.align.context, Sep 7, 2016*/
package isoquant.plugins.processing.expression.align.context;

/**
 * <h3>{@link ReferenceRunSelectionMethod}</h3>
 * @author jkuharev
 * @version Sep 7, 2016 2:02:10 PM
 */
public enum ReferenceRunSelectionMethod
{
	AUTO, MANUAL;

	/**
	 * string to method interpretation
	 * @param methodString strings containing "auto" result in AUTO, everything else results in MANUAL 
	 * @return method
	 */
	public static ReferenceRunSelectionMethod fromString(String methodString)
	{
		String s = methodString.toLowerCase();
		if (s.contains( "auto" ))
			return AUTO;
		else
			return MANUAL;
	}
}
