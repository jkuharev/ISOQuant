/** ISOQuant_1.0, isoquant.plugins.plgs.importing.design.tree.editor, 31.03.2011*/
package isoquant.plugins.plgs.importing.design.tree.editor;

import javax.swing.JTextField;

import de.mz.jk.jsix.ui.FormBuilder;
import de.mz.jk.plgs.data.ExpressionAnalysis;

/**
 * <h3>{@link ExpressionAnalysisEditorPanel}</h3>
 * @author Joerg Kuharev
 * @version 31.03.2011 14:34:52
 */
public class ExpressionAnalysisEditorPanel extends UserTypeEditor<ExpressionAnalysis>
{
	private static final long serialVersionUID = 20110331L;
	private JTextField txt4Name;
	private JTextField txt4Desc;

	@Override public void loadContent()
	{
		txt4Name.setText(userObject.project.title);
		txt4Desc.setText(userObject.description);
	}

	@Override public void saveContent()
	{
		userObject.project.title = txt4Name.getText();
		userObject.description = txt4Desc.getText();
		userObject.name = txt4Desc.getText();
	}

	@Override public void initEditorComponents(FormBuilder fb)
	{
		txt4Name = fb.add("name", new JTextField(25));
		txt4Desc = fb.add("description", new JTextField(25));
	}

	@Override public String getTitle()
	{
		return "DBProject";
	}
}
