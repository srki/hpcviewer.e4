package edu.rice.cs.hpcviewer.ui.parts.topdown;

import javax.annotation.PostConstruct;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.filter.service.FilterStateProvider;
import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.internal.ScopeTreeViewer;
import edu.rice.cs.hpcviewer.ui.internal.ViewerDataEvent;
import edu.rice.cs.hpcviewer.ui.parts.IViewBuilder;
import edu.rice.cs.hpcviewer.ui.parts.editor.PartFactory;

public class TopDownItem extends CTabItem implements EventHandler
{

	protected EPartService  partService;
	protected EModelService modelService;
	protected MApplication  app;
	protected IEventBroker  eventBroker;
	protected EMenuService  menuService;
	
	protected DatabaseCollection databaseAddOn;
	protected PartFactory partFactory;

	private IViewBuilder contentViewer;
	
	/** Each view needs to store the experiment database.
	 * In case it needs to populate the table, we know which database 
	 * to be loaded. */
	private BaseExperiment  experiment;
	
	/** This variable is a flag whether a table is already populated or not.
	 * If the root is null, it isn't populated
	 */
	private RootScope       root;

	public TopDownItem(CTabFolder parent, int style) {
		super(parent, style);
		setText("Top-down");
	}

	public void setService(EPartService partService, 
			IEventBroker broker,
			DatabaseCollection database,
			PartFactory   partFactory) {
		
		this.partService = partService;
		this.eventBroker = broker;
		this.databaseAddOn = database;
		this.partFactory = partFactory;
	}
	
	
	@PostConstruct
	public void createContent(Composite parent) {
		contentViewer = new TopDownContentViewer(partService, eventBroker, databaseAddOn, partFactory);
    	contentViewer.createContent(parent, menuService);

		
		// subscribe to user action events
		eventBroker.subscribe(ViewerDataEvent.TOPIC_HPC_REMOVE_DATABASE, this);
		eventBroker.subscribe(ViewerDataEvent.TOPIC_HIDE_SHOW_COLUMN,    this);
		eventBroker.subscribe(ViewerDataEvent.TOPIC_HPC_ADD_NEW_METRIC,  this);
		eventBroker.subscribe(ViewerDataEvent.TOPIC_HPC_METRIC_UPDATE,   this);
		
		// subscribe to filter events
		eventBroker.subscribe(FilterStateProvider.FILTER_REFRESH_PROVIDER, this);
	}
	
	public BaseExperiment getExperiment() {
		return experiment;
	}
	
	public void setInput(Object input) {
		
		if (!(input instanceof BaseExperiment))
			return;
					
		// important: needs to store the experiment database for further usage
		// when the view is becoming visible
		this.experiment = (BaseExperiment) input;
				
		// TODO: this process takes time
		root = createRoot(experiment);
		contentViewer.setData(root);
	}

	
	@Override
	public void handleEvent(Event event) {
		ScopeTreeViewer treeViewer = contentViewer.getTreeViewer();
		if (treeViewer.getTree().isDisposed())
			return;

		Object obj = event.getProperty(IEventBroker.DATA);
		if (obj == null || getExperiment() == null || root == null)
			return;
		
		if (!(obj instanceof ViewerDataEvent)) {

			if (event.getTopic().equals(FilterStateProvider.FILTER_REFRESH_PROVIDER)) {
				FilterStateProvider.filterExperiment((Experiment) experiment);
				
				// TODO: this process takes time
				root = createRoot(experiment);
				contentViewer.setData(root);
			}
			return;
		}
		
		ViewerDataEvent eventInfo = (ViewerDataEvent) obj;
		if (getExperiment() != eventInfo.experiment) 
			return;
		
		String topic = event.getTopic();
		if (topic.equals(ViewerDataEvent.TOPIC_HIDE_SHOW_COLUMN)) {
			treeViewer.setColumnsStatus((boolean[]) eventInfo.data);
			
		} else if (topic.equals(ViewerDataEvent.TOPIC_HPC_ADD_NEW_METRIC)) {
			treeViewer.addUserMetricColumn((BaseMetric) eventInfo.data);

		} else if (topic.equals(ViewerDataEvent.TOPIC_HPC_REMOVE_DATABASE)) {
			// mark that this part will be destroyed
			experiment = null;

		} else if (topic.equals(ViewerDataEvent.TOPIC_HPC_METRIC_UPDATE)) {
			treeViewer.refreshColumnTitle();
		}
	}

	protected RootScope createRoot(BaseExperiment experiment) {

		// for top-down tree, we don't need to create the tree
		// the tree is already in experiment.xml. 
		
		return experiment.getRootScope(RootScopeType.CallingContextTree);
	}

}
