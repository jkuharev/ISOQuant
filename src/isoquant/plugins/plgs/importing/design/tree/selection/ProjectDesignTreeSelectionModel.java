/** ISOQuant_1.0, isoquant.plugins.plgs.importing.design, 30.03.2011*/
package isoquant.plugins.plgs.importing.design.tree.selection;

import javax.swing.tree.DefaultTreeSelectionModel;

/**
 * <h3>{@link ProjectDesignTreeSelectionModel}</h3>
 * @author Joerg Kuharev
 * @version 30.03.2011 16:26:52
 */
public class ProjectDesignTreeSelectionModel extends DefaultTreeSelectionModel
{
	private static final long serialVersionUID = 20110330L;

	public ProjectDesignTreeSelectionModel()
	{
		super();
		setSelectionMode(DISCONTIGUOUS_TREE_SELECTION);
	}
}
