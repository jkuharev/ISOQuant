package isoquant.plugins.configuration;

import java.awt.Component;
import java.awt.Window;
import java.io.File;

import de.mz.jk.jsix.libs.XJava;
import isoquant.interfaces.iMainApp;
import isoquant.kernel.plugin.ToolBarPlugin;

/**
 * <h3>{@link ConfigurationEditor}</h3>
 * @author Joerg Kuharev
 * @version 24.02.2011 13:05:35
 */
public class ConfigurationEditor extends ToolBarPlugin
{
	File dir = new File(".");

	public ConfigurationEditor(iMainApp app)
	{
		super(app);
	}

	@Override public String getPluginName()
	{
		return "ISOQuant Configuration Editor";
	}

	@Override public String getIconName()
	{
		return "options";
	}

	@Override public int getExecutionOrder()
	{
		return 0;
	}

	@Override public void runPluginAction() throws Exception
	{
		/**
		Idee: 
		beim Speichern von Konfiguration durch app.getSettings().setValue(Key, defaultValue)
		soll der Typ und der Standardwert der Variablen in einer gesonderten Definitions-Datei gespeichert
		werden.
		Dieses Plugin liest die aktuelle Konfigurationsdatei ein und stellt die darin enthaltenen Variablen
		zur Bearbeitung in geeigneter Form unter Ber�cksichtigung des Standardwertes und Typs der Variablen
		innerhalb einer GUI zur Anpassung bereit.
		
		Ben�tigt:
			- GUI
			- M�glichkeit der Auflistung aller Variablen in app.getSettings()
			- M�glichkeit der Auflistung aller Variablen in der Definitionsdatei
		 */
		dir = new File(app.getSettings().getStringValue("setup.config.dir", app.appDir, false));
		editModal(app.getGUI());
	}

	/**
	 * 
	 */
	public void editModal(Component parentComponent)
	{
		String cfg = app.getSettings().readToString();
		TextEditorDialog dlg = new TextEditorDialog(cfg, (Window) parentComponent, getPluginName());
		dlg.setFileHint(dir, new File(XJava.dateStamp() + "_isoquant.ini"));
		if (dlg.showAndWait())
		{
			String newCfg = dlg.getEditedText();
			if (!cfg.equals(newCfg))
			{
				System.out.print("saving ISOQuant configuration ... ");
				app.getSettings().saveFromString(newCfg);
				System.out.println("[ok]");
				dir = dlg.getLastFile().getParentFile();
			}
		}
	}
// public void editModal()
// {
// String cfg = app.getSettings().readToString();
// txt.setText(cfg);
//
// JScrollPane scr = new JScrollPane(txt);
// scr.setPreferredSize(new Dimension(600, 400));
//
// JPanel mainPanel = new JPanel( new BorderLayout() );
// mainPanel.add(btnPanel, BorderLayout.SOUTH);
// mainPanel.add(scr, BorderLayout.CENTER);
//
// Object[] options = new Object[]{"Save", "Cancel"};
// int btn = JOptionPane.showOptionDialog(
// app.getGUI() ,
// mainPanel,
// "ISOQuant configuration",
// JOptionPane.OK_CANCEL_OPTION,
// JOptionPane.QUESTION_MESSAGE,
// this.getPluginIcon(),
// options,
// options[0]
// );
//
// if(btn==0)
// {
// String newCfg = txt.getText();
// if(!cfg.equals(newCfg))
// {
// System.out.print("saving ISOQuant configuration ... ");
// app.getSettings().saveFromString(newCfg);
// System.out.println("[ok]");
// }
// }
// }
}
