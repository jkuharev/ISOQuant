/** ISOQuant, isoquant.plugins.plgs.importing.design.tree.editor, 11.04.2011*/
package isoquant.plugins.plgs.importing.design.tree.editor;


import javax.swing.JPanel;

import de.mz.jk.jsix.ui.FormBuilder;

/**
 * <h3>{@link UserTypeEditor}</h3>
 * @author Joerg Kuharev
 * @version 11.04.2011 12:50:24
 */
public abstract class UserTypeEditor<Type>
{
	protected Type userObject = null;
	private JPanel panel = new JPanel();
	
	/** construct editor panel */
	public UserTypeEditor()
	{
		initEditorComponents( new FormBuilder( panel ) );
	}
	
	/** return graphical interface of this editor component */
	public JPanel getEditorPanel()
	{
		return panel;
	}
	
	/** get user object */
	public Type getUserObject()
	{
		return userObject;
	}
	
	/** set user object */
	public void setUserObject(Type userObject)
	{
		this.userObject = userObject;
	}
	
	/** title text for beeing shown in title of editor component */
	public abstract String getTitle();
	
	/** set user object and load editor content */
	public void load(Type userObject)
	{
		setUserObject(userObject);
		load();
	}
	
	/** load editor content */
	public void load()
	{
		if(userObject!=null) loadContent();
	}
	
	/** save editor content to user object */
	public void save()
	{
		if(userObject!=null) saveContent();
	}
	
	/**
	 * load Editor content from user object
	 */
	protected abstract void loadContent();
	
	/**
	 * save changes to user object
	 */
	protected abstract void saveContent();
	
	/**
	 * place user object's attributes editing components onto the container panel
	 * @param fb the form builder
	 */
	protected abstract void initEditorComponents(FormBuilder fb);
}
