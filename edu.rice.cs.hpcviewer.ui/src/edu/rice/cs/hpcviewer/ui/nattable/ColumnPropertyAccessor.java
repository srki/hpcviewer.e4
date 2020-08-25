package edu.rice.cs.hpcviewer.ui.nattable;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public class ColumnPropertyAccessor implements IColumnPropertyAccessor<Scope> 
{
	private final Experiment experiment;
	
	public ColumnPropertyAccessor(Experiment experiment) {
		this.experiment = experiment;
	}

	@Override
	public Object getDataValue(Scope rowObject, int columnIndex) {
		if (columnIndex > experiment.getMetricCount())
			return null;
		
		if (columnIndex == 0)
			return rowObject.getName();
		
		return rowObject.getMetricValue(columnIndex-1);
	}

	@Override
	public void setDataValue(Scope rowObject, int columnIndex, Object newValue) {
		if (columnIndex > experiment.getMetricCount())
			return;

		if (columnIndex == 0) {
			return;
		}
			
		rowObject.setMetricValue(columnIndex-1, (MetricValue) newValue);
	}

	@Override
	public int getColumnCount() {
		return experiment.getMetricCount()+1;
	}

	@Override
	public String getColumnProperty(int columnIndex) {
		if (columnIndex > 0)
			return experiment.getMetric(columnIndex-1).getDisplayName();

		return "Scope";
	}

	@Override
	public int getColumnIndex(String propertyName) {
		for(int i=0; i<experiment.getMetricCount(); i++) {
			BaseMetric metric = experiment.getMetric(i);
			if (metric.getDisplayName().equals(propertyName))
				return i;
		}
		return 0;
	}

}
