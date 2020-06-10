package edu.rice.cs.hpcviewer.ui.parts;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.IPartListener;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.internal.FlatContentViewer;
import edu.rice.cs.hpcviewer.ui.parts.editor.ViewEventHandler;

public class FlatPart implements IBaseView, IPartListener
{
	static final public String ID = "edu.rice.cs.hpcviewer.ui.part.flat";
	static final public String IDdesc = "edu.rice.cs.hpcviewer.ui.partdescriptor.flat";

	@Inject EPartService partService;
	@Inject EModelService modelService;
	@Inject MApplication  app;

	@Inject IEventBroker broker;
	@Inject DatabaseCollection databaseAddOn;

	private ViewEventHandler eventHandler;
	private IContentViewer   contentViewer;

	private Experiment experiment;

	private RootScope root;
	
	public FlatPart() {
		root = null;
		experiment = null;
	}

    @PostConstruct
    public void createControls(Composite parent, EMenuService menuService) {
		eventHandler = new ViewEventHandler(this, broker, partService);
		
    	contentViewer = new FlatContentViewer(partService, modelService, app, broker, databaseAddOn);
    	contentViewer.createContent(parent, menuService);
    	
		partService.addPartListener(this);
		
		if (!databaseAddOn.isEmpty()) {
			setExperiment(databaseAddOn.getLast());
		}
    }
    
	@PreDestroy
	public void preDestroy() {
		eventHandler.dispose();
		partService.removePartListener(this);
	}

	@Override
	public void setExperiment(BaseExperiment experiment) {

		this.experiment = (Experiment) experiment;
		
		RootScope rootCCT  = experiment.getRootScope(RootScopeType.CallingContextTree);
		RootScope rootFlat = experiment.getRootScope(RootScopeType.Flat);
		
		root = this.experiment.createFlatView(rootCCT, rootFlat);
		
		contentViewer.setData(root);
	}

	@Override
	public String getViewType() {
		return Experiment.TITLE_FLAT_VIEW;
	}

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public void partActivated(MPart part) {}

	@Override
	public void partBroughtToTop(MPart part) {}

	@Override
	public void partDeactivated(MPart part) {}

	@Override
	public void partHidden(MPart part) {}

	@Override
	public void partVisible(MPart part) {}

}
