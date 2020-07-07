 
package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.util.Constants;
import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.internal.ViewerDataEvent;
import edu.rice.cs.hpcviewer.ui.metric.MetricPropertyDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.CanExecute;

public class ShowMetrics 
{
	static final private String ID_MENU_URI = "bundleclass://edu.rice.cs.hpcviewer.ui/" + ShowMetrics.class.getName();
	
	@Inject EPartService partService;
	@Inject DatabaseCollection database;
	
	
	
	@AboutToShow
	public void aboutToShow( List<MMenuElement> items, 
							 EModelService modelService, 
							 MWindow window ) {
		if (!canExecute())
			return;
		
		Iterator<BaseExperiment> iterator = database.getIterator(window);

		while(iterator.hasNext()) {
			Experiment exp = (Experiment) iterator.next();
			
			String path    = exp.getDefaultDirectory().getAbsolutePath();
			String label   = path;
			
			if (exp.isMergedDatabase()) {
				label = "[Merged] " + label;
			}
			MDirectMenuItem menu = modelService.createModelElement(MDirectMenuItem.class);
			
			menu.setElementId(path);
			menu.setLabel(label);
			menu.setContributionURI(ID_MENU_URI);
			
			items.add(menu);
		}		
	}
	
	
	@Execute
	public void execute(MDirectMenuItem menu, @Active Shell shell, IEventBroker eventBroker) {

		if (database == null || database.isEmpty())
			return;
			
		if (menu == null)
			return;

		String element  = menu.getElementId();
		String filePath = element + File.separator + Constants.DATABASE_FILENAME;

		Experiment exp = (Experiment) database.getExperiment(filePath);
		if (exp == null) {
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.debug("Database not found: " + filePath);
			return;
		}
		
		// duplicate metrics before sending to the dialog box.
		// we don't know if the user will confirm the modification or not, so it's better to save them first.
		// if the user decides to cancel the modification, we can restore the metrics back.
		
		BaseMetric[] metrics = exp.getMetrics();
		List<BaseMetric> copyMetrics = new ArrayList<BaseMetric>(metrics.length);
		
		for(BaseMetric metric: metrics) {
			copyMetrics.add(metric.duplicate());
		}
		
		MetricPropertyDialog dialog = new MetricPropertyDialog(shell, (Experiment) exp);
		int ret = dialog.open();
		if (ret == Dialog.OK) {
			ViewerDataEvent data = new ViewerDataEvent(exp, exp.getMetrics());
			eventBroker.post(ViewerDataEvent.TOPIC_HPC_METRIC_UPDATE, data);
		} else {
			// in case there is modification, we need to restore the metrics back
			exp.setMetrics(copyMetrics);
		}
	}
	
	
	@CanExecute
	public boolean canExecute() {		
		return database.getNumDatabase()>0;
	}
		
}