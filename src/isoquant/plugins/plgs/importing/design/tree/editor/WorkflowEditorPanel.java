/** ISOQuant_1.0, isoquant.plugins.plgs.importing.design.panel, 31.03.2011*/
package isoquant.plugins.plgs.importing.design.tree.editor;

import javax.swing.JLabel;
import javax.swing.JTextField;

import de.mz.jk.jsix.ui.FormBuilder;
import de.mz.jk.plgs.data.Workflow;

/**
 * <h3>{@link WorkflowEditorPanel}</h3>
 * @author Joerg Kuharev
 * @version 31.03.2011 09:08:17
 */
public class WorkflowEditorPanel extends UserTypeEditor<Workflow>
{	
	private static final long serialVersionUID = 20110331L;
	private JTextField txt4Title;
	private JTextField txt4Desc;
	private JLabel lbl4InputFile;
	private JTextField txt4AcquiredName;
	private JTextField txt4ReplicateName;

	public WorkflowEditorPanel()
	{
		super();
	}

	@Override public void initEditorComponents(FormBuilder f)
	{
		txt4Title = f.add( "title", new JTextField(25) );
		txt4Desc = f.add( "sample description", new JTextField(25) );
		lbl4InputFile = f.add( "input file", new JLabel() );
		txt4AcquiredName = f.add( "acquired name", new JTextField(25) );
		txt4ReplicateName = f.add( "replicate name", new JTextField(25) );
	}

	@Override public void loadContent()
	{
		txt4Title.setText(userObject.title);
		txt4Desc.setText(userObject.sample_description);
		lbl4InputFile.setText(userObject.input_file);
		txt4AcquiredName.setText(userObject.acquired_name);
		txt4ReplicateName.setText(userObject.replicate_name);
	}

	@Override public void saveContent()
	{
		userObject.title=txt4Title.getText();
		userObject.sample_description=txt4Desc.getText();
		userObject.acquired_name=txt4AcquiredName.getText();
		userObject.replicate_name=txt4ReplicateName.getText();
	}

	@Override public String getTitle()
	{
		return "Workflow";
	}
}

