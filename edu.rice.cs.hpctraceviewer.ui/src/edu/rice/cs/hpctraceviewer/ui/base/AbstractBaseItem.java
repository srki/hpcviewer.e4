package edu.rice.cs.hpctraceviewer.ui.base;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;


/***************************************************
 * 
 * Basic class for a tab item
 *
 ***************************************************/
public abstract class AbstractBaseItem extends CTabItem implements ITraceItem 
{

	public AbstractBaseItem(CTabFolder parent, int style) {
		super(parent, style);
	}
}
