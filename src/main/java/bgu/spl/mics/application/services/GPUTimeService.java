package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.CRMSRunner;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.GPU;

public class GPUTimeService extends MicroService {
    //fields
    GPU GPU;

    //constructors
    public GPUTimeService(String name, GPU GPU){
        super(name);
        this.GPU=GPU;
    }
    @Override
    protected void initialize(){
        subscribeBroadcast(TickBroadcast.class,c -> {
            this.GPU.setTime(c.getTime());
        });

        subscribeBroadcast(TerminateBroadcast.class, c -> {
//            System.out.println(getName()+"terminated");
            GPU.setTerminated(true);
            GPU.notifier();
            terminate();
        });

        CRMSRunner.threadInitCounter.countDown(); //by using countdown informing the timeService initialized
    }
}