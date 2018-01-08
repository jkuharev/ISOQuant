package isoquant.plugins.plgs.importing.expression;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import de.mz.jk.jsix.ui.TableFactory;
import de.mz.jk.plgs.data.ExpressionAnalysis;
import de.mz.jk.plgs.data.Group;
import de.mz.jk.plgs.data.Sample;
import isoquant.interfaces.iProjectManager;
import isoquant.kernel.db.DBProject;

/**
 * <h3>PLGSExpressionAnalysisTableCreator</h3>
 * defines a table for showing and selecting expression analyses to be imported
 * @author Joerg Kuharev
 * @version 11.01.2011 14:51:19
 *
 */
public class ExpressionAnalysisTableCreator
{
	private TableFactory atm = null;
	private Boolean[]	editables = new Boolean[]{true,	false, false, false, false, false};
	private String[]	colTitles = new String[]{"include", "name", "description", "samples", "runs", "already imported"};
	private Object[][]	data = null;
	private List<DBProject> existingProjects = null;
	private String[] dbPrefixes = null; // dbPrefix ist irgendwie null ????
	
	private DBProject project = null;
	private iProjectManager prjMgr = null;
	
	public ExpressionAnalysisTableCreator(DBProject p, iProjectManager prjMgr) 
	{	
		this.prjMgr = prjMgr;
		this.project = p;
		this.existingProjects = prjMgr.getProjects();
		data = new Object[p.data.expressionAnalyses.size()][];
		dbPrefixes = new String[data.length];
		for(int row=0; row<data.length; row++)
		{
			ExpressionAnalysis ea = p.data.expressionAnalyses.get( row );
			int nSamples = 0;
			int nRuns = 0;
			for(Group g : ea.groups){
				nSamples += g.samples.size();
				for(Sample s : g.samples){
					nRuns += s.workflows.size();
				}
			}
			String dbNamePrefix = prjMgr.suggestSchemaNamePrefix( p.data.id, ea.id );
			data[row] = new Object[]{true, ea.name, ea.description, nSamples, nRuns, (exists(dbNamePrefix))?"yes":"no"};
			dbPrefixes[row] = dbNamePrefix;
		}	
		atm = new TableFactory(data, colTitles, editables);
	}
	
	public DBProject getProject(){return project;}
	
	private boolean exists(String dbNamePrefix)
	{
		try {
			for(DBProject p : existingProjects) 
				if (p.data.db.contains( dbNamePrefix )) return true;
		} catch (Exception e) {
			// ups, something wrong with existing projects ?
		}
		return false;
	}

	public JComponent getTableComponent()
	{
		return atm.getScrollableTable(true);
	}
	
	public List<DBProject> getSelectedExpressionAnalysesAsProjects()
	{
		List<DBProject> res = new ArrayList<DBProject>();
		for(int i=0; i<data.length; i++)
		{
			if( (Boolean) data[i][0] )
			{
				DBProject p = new DBProject();
				p.data.id = project.data.id;
				p.data.root = project.data.root;
				p.data.title = project.data.title;
				p.data.titlePrefix = project.data.titlePrefix;
				p.data.titleSuffix = project.data.titleSuffix;
				p.data.expressionAnalyses.add( project.data.expressionAnalyses.get( i ) );
				p.data.expressionAnalysisIDs.add( project.data.expressionAnalysisIDs.get( i ) );
				p.data.selectedExpressionAnalysisIDs.add( project.data.expressionAnalysisIDs.get( i ) );
				p.data.db = prjMgr.getNextSchemaNameForPrefix( dbPrefixes[i] );
				res.add(p);
			}
		}
		return res;
	}
}
