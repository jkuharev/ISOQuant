/** ISOQuant, isoquant.plugins.processing.expression.align, 12.05.2011*/
package isoquant.plugins.processing.expression.align.context;

/**
 * <h3>{@link AlignmentProcessListener}</h3>
 * @author Joerg Kuharev
 * @version 12.05.2011 14:23:51
 */
public interface AlignmentProcessListener
{
	/** called before alignment process has started */
	public void beforeAlignment(AlignmentContext alignmentContext);
	
	/** called after alignment process has finished */
	public void afterAlignment(AlignmentContext alignmentContext);
}
