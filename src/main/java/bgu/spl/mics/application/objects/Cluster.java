package bgu.spl.mics.application.objects;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {
	private Statistics statistics = new Statistics();
	private ConcurrentHashMap<GPU, BlockingQueue<DataBatch>> GPUToQueue = new ConcurrentHashMap<>();
	private ConcurrentLinkedQueue<CPU> collectionOfCPU = new ConcurrentLinkedQueue<>();
	private LinkedBlockingQueue<DataBatch> unprocessedDataBatches = new LinkedBlockingQueue<>();

	//as shown in class this is thread safe.
	private static class SingeltonHolder{
		private static final Cluster instance=new Cluster();
	}

	/**
     * Retrieves the single instance of this class.
     */
	public static Cluster getInstance() {
		return Cluster.SingeltonHolder.instance;
	}

	public ConcurrentHashMap<GPU, BlockingQueue<DataBatch>> getGPUToQueue() {
		return GPUToQueue;
	}

	public LinkedBlockingQueue<DataBatch> getUnprocessedDataBatches() {
		return unprocessedDataBatches;
	}

	public Statistics getStatistics() {
		return statistics;
	}

	public synchronized DataBatch takeData(){
		DataBatch dataBatchToProcess = null;
		if (!unprocessedDataBatches.isEmpty()){
			try {
				dataBatchToProcess = unprocessedDataBatches.take();
			}catch (InterruptedException e){}
		}
		return dataBatchToProcess;
	}

	public void addProcessedDataBatch(DataBatch processedDataBatch){
		//add the processed dataBatch to the GPU queue it belongs.
		GPUToQueue.get(processedDataBatch.getGPU()).add(processedDataBatch);
	}

}
