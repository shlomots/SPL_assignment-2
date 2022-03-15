package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class DataBatch {
    Data dataIBelongTo;
    int startIndex;
    GPU GPU;
    DataBatch(Data dataIBelongTo, int startIndex, GPU GPU){
        this.dataIBelongTo=dataIBelongTo;
        this.startIndex=startIndex;
        this.GPU=GPU;
    }

    public Data getDataIBelongTo() {
        return dataIBelongTo;
    }

    public GPU getGPU() {
        return GPU;
    }
}