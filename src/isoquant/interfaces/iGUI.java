package isoquant.interfaces;

import java.awt.Component;

import de.mz.jk.jsix.ui.iProcessProgressListener;

/**
 * <h3>iGUI</h3>
 * abstract description of an ISOQuant GUI 
 * @author Joerg Kuharev
 * @version 23.12.2010 10:17:39
 */
public interface iGUI extends iDualProjectListManager
{	
	/**
	 * add an awt component to the tool bar
	 * @param c
	 */
	public abstract void addToolBarComponent(Component c);
	
	/**
	 * add an awt component to the tool bar
	 * @param c
	 */
	public abstract void addFSMenuComponent(Component c);
	
	/**
	 * add an awt component to the tool bar
	 * @param c
	 */
	public abstract void addDBMenuComponent(Component c);
	
	/**
	 * show message in status bar
	 * @param text
	 */
	public void setStatusMessage(String text);
	
	/**
	 * set max value for progress bar
	 * @param maxValue
	 */
	public void setProgressMaxValue(int maxValue);
	
	/**
	 * set current progress bar value
	 * @param value
	 */
	public void setProgressValue(int value);
	
	/**
	 * lock/unlock GUI by disabling/enabling user interactions
	 * @param locked <b>true</b> to lock (disable) GUI, <b>false</b> to unlock (enable) GUI 
	 */
	public void setLocked(boolean locked);
	
	/**
	 * @return graphical representation as java.awt.Component
	 */
	public Component getGUIComponent();
	
	/**
	 * get a default process progress listener
	 */
	public iProcessProgressListener getProcessProgressListener();
}
