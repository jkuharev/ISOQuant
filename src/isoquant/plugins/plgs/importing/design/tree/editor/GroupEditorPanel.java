/** ISOQuant_1.0, isoquant.plugins.plgs.importing.design.tree.editor, 31.03.2011*/
package isoquant.plugins.plgs.importing.design.tree.editor;

import javax.swing.JTextField;

import de.mz.jk.jsix.ui.FormBuilder;
import de.mz.jk.plgs.data.Group;

/**
 * <h3>{@link GroupEditorPanel}</h3>
 * @author Joerg Kuharev
 * @version 31.03.2011 14:28:46
 */
public class GroupEditorPanel extends UserTypeEditor<Group>
{
	private static final long serialVersionUID = 20110331L;
	private JTextField txt4Name;

	@Override protected void loadContent()
	{
		txt4Name.setText(userObject.name);
	}

	@Override protected void saveContent()
	{
		userObject.name = txt4Name.getText();
	}

	@Override public void initEditorComponents(FormBuilder fb)
	{
		txt4Name = fb.add("name", new JTextField(25));
	}

	@Override public String getTitle()
	{
		return "Group of Samples";
	}
}
