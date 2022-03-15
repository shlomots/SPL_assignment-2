package bgu.spl.mics.application.services;
import java.util.concurrent.TimeUnit;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.CRMSRunner;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link //TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
//
public class TimeService extends MicroService{
	private int speed;
	private int time;
	private int terminateTime;

	public TimeService(String name, int speed, int terminateTime) {
		super(name);
		time = 0;
		this.speed = speed;
		this.terminateTime = terminateTime;
	}

	public int getTime() {
		return time;
	}

	@Override
	protected synchronized void initialize() {
		while (time<terminateTime){
			time = time+1;
			sendBroadcast(new TickBroadcast(time));
			try{
				wait(speed);
			}catch (InterruptedException e){}
		}
		sendBroadcast(new TerminateBroadcast());
//		System.out.println("terminateBroadcast");
		terminate();

	}

}
