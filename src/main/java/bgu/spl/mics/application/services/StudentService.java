package bgu.spl.mics.application.services;
import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.CRMSRunner;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

/**
 * Student is responsible for sending the {@link //TrainModelEvent},
 * {@link //TestModelEvent} and {@link //PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class StudentService extends MicroService {
    private Student student;
    private MessageBusImpl messageBus = MessageBusImpl.getInstance();
    private Model thisModel;
    Future<Model> result;
    int i = 1;
    boolean isDone = false;

    public StudentService(String name, Student student) {
        super(name);
        this.student = student;
        thisModel = student.getModels().get(0);
        result = null;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(PublishConferenceBroadcast.class, callback -> {
            student.setPapersRead(callback.getResults().size());
        });

        subscribeBroadcast(TickBroadcast.class, callback -> {
            if (!isDone){ //there are still models to train and test
                if (thisModel.getStatus()== Model.Status.PreTrained){ //this model hasn't been sent yet
                    result = sendEvent(new TrainModelEvent(thisModel));
                    thisModel.setStatus(Model.Status.Training);
                }
                else if (result!=null && result.isDone()){
                    if (thisModel.getStatus()== Model.Status.Trained) { //this model has done training
                        result = sendEvent(new TestModelEvent(thisModel)); //test this model
                    }
                    if (thisModel.getStatus()== Model.Status.Tested){ //this model has done testing
                        if (thisModel.getResults()== Model.Results.Good){ //if the model is good publish it
                            sendEvent(new PublishResultsEvent(thisModel));
                            student.setPublications();
                        }
                        if (i<student.getModels().size()){ //get the next model if there are still models to train
                            thisModel = student.getModels().get(i);
                            i++;
                        }
                        else if (i == student.getModels().size()){
                            isDone = true;
                        }
                    }
                }
            }
        });

        subscribeBroadcast(TerminateBroadcast.class, callback ->{
            student.setPapersReadEnd(); //set the number of papers read excluding his own
//            System.out.println(getName()+"terminated");
            terminate();
        });

        CRMSRunner.threadInitCounter.countDown(); //by using countdown informing the timeService initialized
    }

}
