package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Statistics {
    private ConcurrentLinkedQueue<String> modelsNames = new ConcurrentLinkedQueue<>();
    private AtomicInteger processedData = new AtomicInteger(0);
    private AtomicInteger CPUTimeUsed = new AtomicInteger(0);
    private AtomicInteger GPUTimeUsed = new AtomicInteger(0);

    public Statistics(){}

    public void addToPrecessedData(){
        int val;
        do{
            val = processedData.get();
        }while (!processedData.compareAndSet(val, val+1));
    }

    public void addToCPUTimeUsed(){
        int val;
        do{
            val = CPUTimeUsed.get();
        }while (!CPUTimeUsed.compareAndSet(val, val+1));
    }

    public void addToGPUTimeUsed(){
        int val;
        do{
            val = GPUTimeUsed.get();
        }while (!GPUTimeUsed.compareAndSet(val, val+1));
    }

    public AtomicInteger getProcessedData() {
        return processedData;
    }
}
