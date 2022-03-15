package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.CRMSRunner;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link //TestModelEvent},
 * in addition to sending the {@link //DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {
    //we will probably want three blocking queue's and a threadpool that will work on those 3 queue's.
    private GPU GPU;


    public GPUService(String name,GPU GPU) {
        super(name);
        this.GPU=GPU;
    }

    @Override
    protected void initialize() {
        subscribeEvent(TrainModelEvent.class, c-> {
            GPU.setModel(c.getModel());//first set our model
            GPU.divideData(c.getModel());//divide the data from the model into batches.
            GPU.act();
            if(!GPU.getTerminated()) {
                complete(c, c.getModel());
            } else{
                terminate();
            }
        });

        subscribeEvent(TestModelEvent.class,c -> {
            if(c.getModel().getStudent().getStatus()== Student.Degree.PhD){
                if(Math.random()*10<8){
                    c.getModel().setResults(Model.Results.Good);
                }else  c.getModel().setResults(Model.Results.Bad);
            }
            if(c.getModel().getStudent().getStatus()== Student.Degree.MSc){
                if(Math.random()*10<6){
                    c.getModel().setResults(Model.Results.Good);
                }else c.getModel().setResults(Model.Results.Bad);
            }
            c.getModel().setStatus(Model.Status.Tested);
            complete(c,c.getModel());
        });
        //in case termination doesn't heppen while he is training
        subscribeBroadcast(TerminateBroadcast.class,c -> {
            terminate();
//            System.out.println("GPU"+this.getName()+"terminated");
        });


        CRMSRunner.threadInitCounter.countDown(); //by using countdown informing the timeService initialized
    }
}
