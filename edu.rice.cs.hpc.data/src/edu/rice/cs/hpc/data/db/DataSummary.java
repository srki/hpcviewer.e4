
package edu.rice.cs.hpc.data.db;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Random;

import edu.rice.cs.hpc.data.util.Constants;
import edu.rice.cs.hpc.data.experiment.BaseExperimentWithMetrics;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;

/*********************************************
 * 
 * Class to handle summary.db file generated by hpcprof
 *
 *********************************************/
public class DataSummary extends DataCommon 
{
	// --------------------------------------------------------------------
	// constants
	// --------------------------------------------------------------------
	
	//private final static String SUMMARY_NAME = "hpctoolkit summary metrics";
	static final private int METRIC_ENTRY_SIZE = Constants.SIZEOF_FLOAT + Constants.SIZEOF_INT;
	static final public float DEFAULT_METRIC  = 0.0f;
	
	// --------------------------------------------------------------------
	// object variable
	// --------------------------------------------------------------------
	
	private long offset_start;
	private long offset_size;
	private long metric_start;
	private long metric_size;
	
	private int size_offset;
	private int size_metid;
	private int size_metval;

	private int   [][]metric_id;
	private float [][]metric_val;
	
	// --------------------------------------------------------------------
	// Public methods
	// --------------------------------------------------------------------
	
	/***
	 *  <p>Opening for data summary metric file</p>
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.db.DataCommon#open(java.lang.String)
	 */
	@Override
	public void open(final String file)
			throws IOException
	{
		super.open(file);
		
		fillOffsetTable(file);
	}
	

	@Override
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.db.DataCommon#printInfo(java.io.PrintStream)
	 */
	public void printInfo( PrintStream out)
	{
		super.printInfo(out);
		out.println("Offset start: " + offset_start);
		out.println("Offset size: "  + offset_size);
		
		out.println("metric start: " + metric_start);
		out.println("metric size: "  + metric_size);
		
		out.println("size offset: "  + size_offset);
		out.println("size met id: "  + size_metid);
		out.println("size met val: " + size_metval);
		
		out.println("\n");
		int cct = 1;
		out.format("[%5d] ", cct);
		printMetrics(out, cct);

		// print random metrics
		for (int i=0; i<15; i++)
		{
			Random r = new Random();
			cct  = r.nextInt((int) num_cctid);
			out.format("[%5d] ", cct);
			printMetrics(out, cct);
		}

	}
	
	/*******
	 * print a list of metrics for a given CCT index
	 * 
	 * @param out : the outpur stream
	 * @param cct : CCT index
	 */
	private void printMetrics(PrintStream out, int cct)
	{
		int []metrics = metric_id[cct];
		for(int i=0; i<metrics.length; i++)
		{
			out.format("(%d, %1.2e)\t", metric_id[cct][i], metric_val[cct][i]);
		}
		out.println();
	}
	
	
	/**********
	 * Reading a set of metrics from the file for a given CCT 
	 * This method does not support concurrency. The caller is
	 * responsible to handle mutual exclusion.
	 * 
	 * @param cct_id
	 * @return
	 * @throws IOException
	 */
	public MetricValue[] getMetrics(int cct_id, BaseExperimentWithMetrics experiment) 
			throws IOException
	{
		MetricValue []values = new MetricValue[experiment.getMetricCount()];
		
		for(int i=0; i<values.length; i++)
		{
			values[i] = MetricValue.NONE;
		}
		
		int []metrics = metric_id[cct_id];
		for (int i=0; i<metrics.length; i++)
		{
			int id = metrics[i];
			BaseMetric metric = experiment.getMetric(String.valueOf(id));
			int index = metric.getIndex();
			values[index] = new MetricValue(metric_val[cct_id][i]);
		}
	
		return values;
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.db.DataCommon#dispose()
	 */
	public void dispose() throws IOException
	{
		metric_id = null;
		metric_val = null;
	}
	

	// --------------------------------------------------------------------
	// Protected methods
	// --------------------------------------------------------------------
	
	@Override
	protected boolean isTypeFormatCorrect(long type) {
		return type==1;
	}

	@Override
	protected boolean isFileHeaderCorrect(String header) {
		// suggestion from Mark: ignore the header file name
		return true; //header.startsWith(SUMMARY_NAME);
	}

	@Override
	protected boolean readNextHeader(FileChannel input) 
			throws IOException
	{
		ByteBuffer buffer = ByteBuffer.allocate(256);
		int numBytes      = input.read(buffer);
		if (numBytes > 0) 
		{
			buffer.flip();
			
			offset_start = buffer.getLong();
			offset_size  = buffer.getLong();
			metric_start = buffer.getLong();
			metric_size  = buffer.getLong();
			
			size_offset  = buffer.getInt();
			size_metid   = buffer.getInt();
			size_metval  = buffer.getInt();
		}		
		return false;
	}

	// --------------------------------------------------------------------
	// Private methods
	// --------------------------------------------------------------------
	
	private void fillOffsetTable(final String filename)
			throws IOException
	{
		final RandomAccessFile file = new RandomAccessFile(filename, "r");
		final FileChannel channel	= file.getChannel();
		// map all the table into memory. 
		// This statement can be problematic if the offset_size is huge
		
		MappedByteBuffer mappedBuffer = channel.map(MapMode.READ_ONLY, offset_start, offset_size);
		LongBuffer longBuffer = mappedBuffer.asLongBuffer();
		
		final int []cct_table = new int[(int) num_cctid+1];
		
		for (int i=0; i<=num_cctid; i++)
		{
			cct_table[i] = (int) longBuffer.get(i);
		}
		
		metric_id 	  = new int  [(int)num_cctid][];
		metric_val 	  = new float[(int)num_cctid][];
		byte []buffer = new byte [(int) metric_size];
		
		file.seek(metric_start);
		file.readFully(buffer);
		
		int offset = 0;
		for (int i=0; i<num_cctid; i++)
		{
			int offset_size  = (cct_table[i+1] - cct_table[i]);
			int num_metrics  = offset_size / METRIC_ENTRY_SIZE;
			
			metric_id[i] 	 = new int  [num_metrics]; 
			metric_val[i] 	 = new float[num_metrics]; 
			
			for (int j=0; j<num_metrics; j++)
			{
				ByteBuffer bb = ByteBuffer.wrap(buffer, offset, Constants.SIZEOF_INT);
				metric_id[i][j] = bb.getInt();
				offset += Constants.SIZEOF_INT;
				
				bb = ByteBuffer.wrap(buffer, offset, Constants.SIZEOF_FLOAT);
				metric_val[i][j] = bb.getFloat();
				offset += Constants.SIZEOF_FLOAT;
			}
		}
		file.close();
	}
	
	

	/***************************
	 * unit test 
	 * 
	 * @param argv
	 ***************************/
	public static void main(String []argv)
	{
		final String DEFAULT_FILE = "/home/la5/data/new-database/db-lulesh-new/summary.db";
		final String filename;
		if (argv != null && argv.length>0)
			filename = argv[0];
		else
			filename = DEFAULT_FILE;
		
		final DataSummary summary_data = new DataSummary();
		try {
			summary_data.open(filename);			
			summary_data.printInfo(System.out);
			summary_data.dispose();	
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
