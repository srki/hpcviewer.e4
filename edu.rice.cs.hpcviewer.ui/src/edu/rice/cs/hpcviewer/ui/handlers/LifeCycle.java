package edu.rice.cs.hpcviewer.ui.handlers;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.PreSave;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessRemovals;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.experiment.ExperimentManager;
import edu.rice.cs.hpcviewer.ui.resources.IconManager;

public class LifeCycle 
{
	@Inject EPartService partService;
	@Inject IEventBroker broker;
	@Inject EModelService modelService;

	@Inject DatabaseCollection databaseCollection;


	@PostContextCreate
	public void startup(IEclipseContext context, @Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		
		// setup a list of images 
		// Note: this is for Windows. On mac, we don't need this.
		
		Display display = Display.getDefault();
		
		Image listImages[] = new Image[IconManager.Image_Viewer.length];
		int i = 0;
		
		for (String imageName : IconManager.Image_Viewer) {
			try {
				URL url = FileLocator.toFileURL(new URL(imageName));
				listImages[i] = new Image(display, url.getFile());
				i++;
			} catch (IOException e) {
			}
		}
		Window.setDefaultImages(listImages);
		
		// handling the command line arguments:
		// if one of the arguments specify a file or a directory,
		// try to find the experiment.xml and load it.
		
		String args[] = Platform.getApplicationArgs();
		
		Shell myShell = display.getActiveShell();
		if (myShell == null) {
			myShell = new Shell(SWT.TOOL | SWT.NO_TRIM);
		}
		BaseExperiment experiment    = null;
		ExperimentManager expManager = new ExperimentManager();
		
		String path = null;
		
		for (String arg: args) {
			if (arg.charAt(0) != '-')
				path = arg;
		}
		if (path == null || path.length() < 1) {
			experiment    = expManager.openFileExperiment(myShell);
		} else {
			experiment = openDatabase(myShell, expManager, args[0]);
		}
		if (experiment == null)
			return;
		
		databaseCollection.addDatabase(experiment, null, partService, broker, modelService);
	}
	

	@PreSave
	void preSave(IEclipseContext workbenchContext) {
	}

	@ProcessAdditions
	void processAdditions(IEclipseContext workbenchContext) {
	}

	@ProcessRemovals
	void processRemovals(IEclipseContext workbenchContext) {}

	
	/****
	 * Find a database for a given path
	 * 
	 * @param shell the active shell
	 * @param expManager the experiment manager
	 * @param sPath path to the database
	 * @return
	 */
	private BaseExperiment openDatabase(Shell shell, ExperimentManager expManager, String sPath) {
    	IFileStore fileStore;

		try {
			fileStore = EFS.getLocalFileSystem().getStore(new URI(sPath));
		} catch (URISyntaxException e) {
			// somehow, URI may throw an exception for certain schemes. 
			// in this case, let's do it traditional way
			fileStore = EFS.getLocalFileSystem().getStore(new Path(sPath));
			e.printStackTrace();
		}
    	IFileInfo objFileInfo = fileStore.fetchInfo();

    	if (!objFileInfo.exists())
    		return null;

    	BaseExperiment experiment = null;
    	
    	if (objFileInfo.isDirectory()) {
    		experiment = expManager.openDatabaseFromDirectory(shell, sPath);
    	} else {
			EFS.getLocalFileSystem().fromLocalFile(new File(sPath));
			experiment = expManager.loadExperiment(shell, sPath);
    	}
    	return experiment;
	}
}
