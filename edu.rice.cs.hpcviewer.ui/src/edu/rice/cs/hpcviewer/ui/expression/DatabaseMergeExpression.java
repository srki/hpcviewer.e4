 
package edu.rice.cs.hpcviewer.ui.expression;

import javax.inject.Inject;
import org.eclipse.e4.core.di.annotations.Evaluate;
import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;

public class DatabaseMergeExpression 
{
	@Inject DatabaseCollection database;
	
	@Evaluate
	public boolean evaluate() {
		return database.getNumDatabase()>1;
	}
}
