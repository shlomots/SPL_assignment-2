package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class GPU {
    /**
     * Enum representing the type of the GPU.
     */
    public enum Type {RTX3090, RTX2080, GTX1080}
    private final Cluster cluster=Cluster.getInstance();;
    private final Type type;
    private Model model;
    private LinkedList<DataBatch> unprocessedDataBatches = new LinkedList<>();
    private LinkedList<DataBatch> VRAM = new LinkedList<>();
    private int sentToTheCluster=0;
    private int ticksToWait;
    private int VRAMSize;
    private int time;
    private int trainedDataBatches;
    private boolean terminated=false;


    //maybe we need a vram and a disc here.

    //constructor
    //we will chane the input to trainModelEvent

    public GPU(Type type1){
        //first set proper Vram size.
        if(type1==Type.RTX3090){
            VRAMSize = 32;
            ticksToWait=1;

        }
        if(type1==Type.RTX2080){
            VRAMSize = 16;
            ticksToWait=2;
        }
        if(type1==Type.GTX1080){
            VRAMSize =8;
            ticksToWait=4;
        }
        //then set the type of the GPU
        this.type = type1;
        //now assign this GPU to the hashMap inside cluster.
        BlockingQueue<DataBatch> queue= new LinkedBlockingQueue<>();
        cluster.getGPUToQueue().putIfAbsent(this,queue);
    }

    //methods
    public Type getType() {
        return type;
    }

    public int getTime() {
        return time;
    }

    public LinkedList<DataBatch> getUnprocessedDataBatches() {
        return unprocessedDataBatches;
    }

    public int getSentToTheCluster() {
        return sentToTheCluster;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public void divideData(Model model){
        for (int i=0;i<model.getData().getSize();i=i+1000){
            unprocessedDataBatches.add(new DataBatch(model.getData(), i,this));
        }
    }
    public void act(){
            while(model.getStatus() != Model.Status.Trained&!terminated){
                //send what you can
                if(sentToTheCluster< VRAMSize){
                    sendDataBatchesToCluster();
                }
                //receive what you can
                receiveDataFromCluster();
                //train what you can
                if(!VRAM.isEmpty()){
                    trainModel();
                }
            }

    }




    /**
     * @pre: data!=null;
     * @post data.getSize() < @pre data.getSize()
     */
    public void sendDataBatchesToCluster(){
        //will take our data, and start sending it in batches to the cluster.
        while (sentToTheCluster< VRAMSize & !unprocessedDataBatches.isEmpty()){
            DataBatch dataBatch = unprocessedDataBatches.remove();
            cluster.getUnprocessedDataBatches().add(dataBatch);
            sentToTheCluster++;
        }
    }

    /**
     * @pre data.getSize()>data.getProcessed()
     * @post data.getProcessed()>data.getProcessed()
     */

    public void receiveDataFromCluster(){
        //take batches while their something to take and you have where to put it.
        while(!cluster.getGPUToQueue().get(this).isEmpty()& VRAM.size()< VRAMSize){
            try{
                VRAM.add(cluster.getGPUToQueue().get(this).take());
            }catch (InterruptedException e){
                break;
            }
        }
    }


    /**
     * @pre Model.getType()==preTrained
     * @post model.getType()==Trained
     */

    //this function is the one that's going to use the tickbroadcast.
    public synchronized void trainModel(){
        while(!VRAM.isEmpty()&!(model.getStatus()==Model.Status.Trained)){//train all you have in the Vram,make sure it's not trained already.
            int currentTime=time;//look at the time
            DataBatch dataToTrain= VRAM.remove();//take the next DataBatch
            while (time<currentTime+ticksToWait&!terminated){//while you didn't get enough ticks, wait.
                try{
                   // System.out.println("im training"+"my name is"+this.getType().toString());
                    if(!terminated){
                        wait();
                    }
                }catch (InterruptedException e){}
            }
            //if it's all the work that had to be done, say the model is trained.
            //check that you trained enough dataBatches based on the data size.
            trainedDataBatches++;
            sentToTheCluster--;
            if(model.getData().getSize()%1000==0){
                if(model.getData().getSize()/1000==trainedDataBatches){
                    model.setStatus(Model.Status.Trained);
                }else if(model.getData().getSize()/1000+1==trainedDataBatches){
                    model.setStatus(Model.Status.Trained);
                }
            }
        }
    }

    public void setTime(int time) {
        this.time = time;
        notifier();
    }

    public void setTerminated(boolean terminated) {
        this.terminated = terminated;
    }
    public boolean getTerminated(){
        return terminated;
    }

    public synchronized void notifier(){
        notify();
    }

    public Cluster getCluster() {
        return cluster;
    }
    /*   public void setFuture(){
        //sets the future of the event.
    }



    public void sendToDisc(DataBatch unprocessedBatch){
        //will send the unprocessed dataBatch to the disc.
    }

    public void sendToClusterMultiple(){
        //will run through the disc and keep sending data batches to the cluster, using the sendDataBatch Method, as long there is enough room in the vram
    }

    public void sendDataBatch(DataBatch dataBatch){
        //sends the data batch into the cluster
    }

    public void receiveDataBatch(DataBatch[] dataBatches){
        //receives the dataBatches from the cluster and puts it in the vrum, this function will probably be used by the cluster. and will use the sendToVrum finction.
    }

    public void sendToVrum(DataBatch processedBatch){
        //will send the processed dataBatch to the vrum.
    }

    public void trainModel(){
        //trains the model in the trainModelEvent with the processed data
     }

     public void setFuture(){
        //sets the future of the event with completion.
     }
// to be continued?
*/


}