package edu.rice.cs.hpcviewer.ui.internal;

import java.util.Comparator;
import java.util.List;

import ca.odell.glazedlists.TreeList;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public class ScopeTreeFormat implements TreeList.Format<Scope> {

	@Override
	public void getPath(List<Scope> path, Scope element) {

		if (element.getParent() != null) {
			path.add((Scope) element.getParent());
		}
		path.add(element);
	}

	@Override
	public boolean allowsChildren(Scope element) {
		return element.hasChildren();
	}

	@Override
	public Comparator<? super Scope> getComparator(int depth) {
		// TODO Auto-generated method stub
		return null;
	}

}
