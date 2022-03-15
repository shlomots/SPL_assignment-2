package bgu.spl.mics.application.objects;

import java.util.LinkedList;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {

    private int cores;
    private LinkedList<DataBatch> dataToProcess = new LinkedList<>();
    private Cluster cluster = Cluster.getInstance();
    private int time = 0;
    private int startTime;
    private int processTime;
    private int totalProcessTime = 0;
    private int processedDataBatches = 0;


    public CPU(int cores){
        this.cores = cores;
    }

    public int getCores() {
        return cores;
    }

    public int getTotalProcessTime() {
        return totalProcessTime;
    }

    public int getProcessedDataBatches() {
        return processedDataBatches;
    }

    public LinkedList<DataBatch> getDataToProcess() {
        return dataToProcess;
    }

    public void updateTime(int time){
        this.time = time;
    }

    public void act(){
        if (dataToProcess.isEmpty()){
            takeBatch();
        }
        else {
            process();
        }
    }

    //takes dataBatch from the cluster
    /**
     * @post: data.size()==@pre(size())+1
     */
    public void takeBatch(){
        DataBatch batchToTake = cluster.takeData(); ////take batch from the cluster
        if (batchToTake!=null){ //dataBatch can be null if there is no data to process
            dataToProcess.add(batchToTake);
            startTime = this.time;
            computeProcessTime(); //compute how much time it will take to process this data
            processedDataBatches++;
            cluster.getStatistics().addToPrecessedData();
        }
    }

    public void computeProcessTime(){
        Data.Type dataType = dataToProcess.peek().getDataIBelongTo().getType();
        processTime = 32/cores;
        if (dataType == Data.Type.Images){
            processTime = processTime*4;
        }
        else if (dataType == Data.Type.Text){
            processTime = processTime*2;
        }
    }

    //process a dataBatch and eventually send it back from the collection and delete it
    /**
     * @pre: !data.isEmpty
     * @post: @post: data.size()==@pre(size())-1
     */
    public void process(){
        totalProcessTime++;
        cluster.getStatistics().addToCPUTimeUsed();
        if (time == startTime+processTime){ //the CPU is done processing this DataBatch
            cluster.addProcessedDataBatch(dataToProcess.poll()); //return the processed DataBatch to the cluster
        }
    }

}
