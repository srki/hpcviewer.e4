package edu.rice.cs.hpcviewer.ui.internal;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeData;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeRowModel;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.tree.ITreeRowModel;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.TreeRowModel;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TreeList;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public class ScopeTreeLayerStack extends AbstractLayerTransform 
{

    private final TreeLayer treeLayer;
    private final IRowDataProvider<Scope> bodyDataProvider;
    private final SelectionLayer selectionLayer;

	public ScopeTreeLayerStack(RootScope root) {
		EventList<Scope> eventList = new BasicEventList<Scope>();
		eventList.add(root);
		
		ScopeTreeColumnAccessor accessor = new ScopeTreeColumnAccessor((Experiment) root.getExperiment());
		bodyDataProvider = new ListDataProvider<Scope>(eventList, accessor);
		DataLayer dataLayer = new DataLayer(bodyDataProvider);
		
		ScopeTreeFormat treeFormat = new ScopeTreeFormat();
		
        ITreeRowModel<Scope> treeRowModel = new TreeRowModel<Scope>(root);

		treeLayer = new TreeLayer(selectionLayer, treeRowModel);
		
		GlazedListsEventLayer<Scope> glazedListEventLayer = new GlazedListsEventLayer<Scope>(dataLayer, eventList);
		selectionLayer = new SelectionLayer(glazedListEventLayer);
		
		GlazedListTreeData<Scope> treeData = new GlazedListTreeData<Scope>(treeList);
		ITreeRowModel<Scope> treeRowModel = new GlazedListTreeRowModel<Scope>(treeData);
		
		TreeLayer treeLayer = new TreeLayer(selectionLayer, treeRowModel);
		ViewportLayer viewPortLayer = new ViewportLayer(treeLayer);
		
		setUnderlyingLayer(viewPortLayer);
	}

	public SelectionLayer getSelectionLayer() {
		return selectionLayer;
	}
		

    public TreeLayer getTreeLayer() {
        return this.treeLayer;
    }

	public IDataProvider getBodyDataProvider() {
		return bodyDataProvider;
	}
}
