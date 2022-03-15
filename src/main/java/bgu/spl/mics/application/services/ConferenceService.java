package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.CRMSRunner;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.ConfrenceInformation;

import java.util.concurrent.TimeUnit;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link PublishConferenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {
    //fields
    private ConfrenceInformation confrenceInformation;

    public ConferenceService(String name, ConfrenceInformation confrenceInformation) {
        super(name);
        this.confrenceInformation = confrenceInformation;
    }

    @Override
    protected void initialize() {
        subscribeEvent(PublishResultsEvent.class,c -> {//if we get a result from a student
            confrenceInformation.getResults().add(c.getModel());

        });

        subscribeBroadcast(TickBroadcast.class,c -> {//if we get a tick
            if(c.getTime()==confrenceInformation.getDate()){//if it's time to publish the results
                PublishConferenceBroadcast publishConferenceBroadcast = new PublishConferenceBroadcast(confrenceInformation.getResults());//create the broadcast
                sendBroadcast(publishConferenceBroadcast);//publish them
                System.out.println(this.getName()+ "terminated");
                this.terminate();
            }
        });

        CRMSRunner.threadInitCounter.countDown(); //by using countdown informing the timeService initialized
    }
}