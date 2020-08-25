package edu.rice.cs.hpcviewer.ui.nattable;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
import org.eclipse.nebula.widgets.nattable.tree.config.TreeLayerExpandCollapseKeyBindings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;


public class ScopeNatTree 
{
	private final static String COLUMN_SCOPE_LABEL = "Scope";
	private Composite container;
	private RootScope root;
	private NatTable natTable; 
	
	public ScopeNatTree(Composite parent, int style) {
		container = parent;
	}
	
	public RootScope getInput() {
		return root;
	}
	
	
	public void refresh() {
		natTable.redraw();
	}
	
	public void setInput(RootScope root) {

		this.root = root;
		
        Experiment experiment = (Experiment) root.getExperiment();
        
        String []propertyNames = new String[experiment.getMetricCount()+1];
        
        propertyNames[0] = COLUMN_SCOPE_LABEL;

        for (int i=1; i<propertyNames.length; i++) {
        	String name = experiment.getMetric(i-1).getDisplayName();
        	propertyNames[i] = name;
        }
        IColumnPropertyAccessor<Scope> accessor = new ColumnPropertyAccessor((Experiment) root.getExperiment());
        final ScopeTreeLayerStack layerStack = new ScopeTreeLayerStack(root, accessor);
        
        // build the column header layer
        IDataProvider columnDataProvider = new DefaultColumnHeaderDataProvider(propertyNames);
        DataLayer columnDataLayer = new DefaultColumnHeaderDataLayer(columnDataProvider);
        ILayer columnHeaderLayer = new ColumnHeaderLayer(columnDataLayer, layerStack, layerStack.getSelectionLayer());
        

        // build the row header layer
        IDataProvider rowHeaderDataProvider =
                new DefaultRowHeaderDataProvider(layerStack.getBodyDataProvider());
        DataLayer rowHeaderDataLayer =
                new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        ILayer rowHeaderLayer =
                new RowHeaderLayer(rowHeaderDataLayer, layerStack, layerStack.getSelectionLayer());

        // build the corner layer
        IDataProvider cornerDataProvider =
                new DefaultCornerDataProvider(columnDataProvider, rowHeaderDataProvider);
        DataLayer cornerDataLayer =
                new DataLayer(cornerDataProvider);
        ILayer cornerLayer =
                new CornerLayer(cornerDataLayer, rowHeaderLayer, columnHeaderLayer);

        // build the grid layer
        GridLayer gridLayer = new GridLayer(layerStack, columnHeaderLayer, rowHeaderLayer, cornerLayer);

        
        // turn the auto configuration off as we want to add our header menu
        // configuration
		natTable = new NatTable(container, SWT.BORDER, gridLayer, false);
		
        // create a new ConfigRegistry which will be needed for GlazedLists
        // handling
        ConfigRegistry configRegistry = new ConfigRegistry();

        // as the autoconfiguration of the NatTable is turned off, we have to
        // add the DefaultNatTableStyleConfiguration and the ConfigRegistry
        // manually
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());

        // adds the key bindings that allows pressing space bar to
        // expand/collapse tree nodes
        natTable.addConfiguration(
                new TreeLayerExpandCollapseKeyBindings(
                		layerStack.getTreeLayer(),
                		layerStack.getSelectionLayer()));

        natTable.configure();

		
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
        
        natTable.setLayerPainter(
                new NatGridLayerPainter(natTable, DataLayer.DEFAULT_ROW_HEIGHT));
	}
}
