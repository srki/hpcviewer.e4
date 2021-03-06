package edu.rice.cs.hpctraceviewer.ui.summary;

import java.util.AbstractMap;

import org.eclipse.swt.graphics.PaletteData;

import edu.rice.cs.hpctraceviewer.data.ColorTable;

public class SummaryData 
{
	public PaletteData palette;
	public AbstractMap<Integer, Integer> mapPixelToCount;
	public ColorTable colorTable;
	public int totalPixels;
	
	
	public SummaryData(
			PaletteData palette,
			AbstractMap<Integer, Integer> mapPixelToCount, 
			ColorTable colorTable, 
			int totalPixels) {
		
		this.palette = palette;
		this.mapPixelToCount = mapPixelToCount;
		this.colorTable = colorTable;
		this.totalPixels = totalPixels;
	}
}
