package bgu.spl.mics;

import bgu.spl.mics.application.messages.TerminateBroadcast;

import java.util.HashMap;
import java.util.concurrent.*;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {
	//fields
	private ConcurrentHashMap<MicroService,BlockingQueue<Message>> microServiceToQueue= new ConcurrentHashMap<>();
	private ConcurrentHashMap<Class<? extends Message>,BlockingQueue<MicroService>> messageToQueue= new ConcurrentHashMap<>();
	private ConcurrentHashMap<Event<?>,Future> eventToFuture =new ConcurrentHashMap<>();//$maybe write Future differently.

	//as shown in class this is thread safe.
	private static class SingeltonMessageBus{
		private static final MessageBusImpl instance=new MessageBusImpl();
	}

	public static MessageBusImpl getInstance(){
		return SingeltonMessageBus.instance;
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		subscribeMessage(type,m);
	}
	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		subscribeMessage(type,m);
	}

	private void subscribeMessage(Class<?extends Message> type,MicroService m){
		if(!microServiceToQueue.containsKey(m)){
			throw new NullPointerException("no such Micro Service was registered");
		}
		//create a new queue for this message type if absent.
		messageToQueue.putIfAbsent(type,new LinkedBlockingQueue<>());
		BlockingQueue<MicroService> tmp=messageToQueue.get(type);
		//if the microService m isn't allready subscribed, subscribe it.
		if(tmp!=null&&!tmp.contains(m)){
			tmp.add(m);
		}

	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		//first check if such an event is waiting to be completed
		if(!eventToFuture.containsKey(e)){
			throw new NullPointerException("no such event is waiting to be completed");
		}
		else eventToFuture.get(e).resolve(result);//$why to be yellow

	}

	@Override
	public void sendBroadcast(Broadcast b) {
//		if (b.getClass()== TerminateBroadcast.class) {
//			for (MicroService m : messageToQueue.get(b.getClass())) {
//				System.out.println(m.getName()+"terminated");
//				m.terminate();
//			}
//		}
//		else {//try to add the message to each of the microservices queue's, but catch the exception if none have been subscribed yet.
			try{
				for(MicroService m:messageToQueue.get(b.getClass())){
					microServiceToQueue.get(m).add(b);
				}
			}catch(NullPointerException np){
				System.out.println("no one subscribed to this broadcast");
			}
//		}
	}


	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		//for synchronization soon
		if(messageToQueue.get(e.getClass())==null){
			return null;
		}
		synchronized (messageToQueue.get(e.getClass())){//we want the queue to be synchronized so no one changes it while we choose the microservice to complete the event.
			if(messageToQueue.get(e.getClass()).isEmpty()){//make sure his queue isn't empty, it can't be null
				return null;// because it was already initialized and in no point would it be removed through our code.
			}
			/*
			 * remove the microService from the event queue.
			 * put the event in the queue of the microService.
			 * return microService to the e.class queue for round rubin.
			 */
			MicroService ourService=messageToQueue.get(e.getClass()).remove();
			microServiceToQueue.get(ourService).add(e);
			messageToQueue.get(e.getClass()).add(ourService);
			//map the event to the future.
			Future<T> f1 = new Future<>();
			eventToFuture.putIfAbsent(e,f1);
			return f1;
		}
	}

	@Override
	public void register(MicroService m) {
		BlockingQueue<Message> q1 = new LinkedBlockingQueue<>();
		microServiceToQueue.putIfAbsent(m,q1);
	}

	@Override
	public void unregister(MicroService m) {
		//remove m out of all the queue's he is in
		for(Class<? extends Message> message:messageToQueue.keySet()){
			messageToQueue.get(message).remove(m);
		}
		//remove m from microServicesToQueue
		microServiceToQueue.remove(m);
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		if(microServiceToQueue.get(m)!=null){
			return microServiceToQueue.get(m).take();//the take function is a blocking function.
		}else
			throw new IllegalArgumentException("no such microService is registered");
	}



}