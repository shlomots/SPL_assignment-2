package bgu.spl.mics;

import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;
import bgu.spl.mics.example.services.ExampleService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageBusImplTest {
    private MessageBusImpl ourBus;

    @Before
    public void setUp() throws Exception {
        ourBus = new MessageBusImpl();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSubscribeEvent() {
        /**
         * this method will be tested in the testSendEvent method.
         */
    }

    @Test
    public void testSubscribeBroadcast() {
        /**
         * this method will be tested in the testSendBroadcast method.
         */
    }

    @Test
    public void testComplete() {
        //we need here a micro service which will be subscribed to our event.
        ExampleService g1 = new ExampleService("m1");
        ExampleEvent e1= new ExampleEvent("test");
        ourBus.register(g1);//first create a q for g1
        ourBus.subscribeEvent(e1.getClass(),g1);
        ourBus.sendEvent(e1);
        ourBus.complete(e1,"finish");
        Future<String> f1 = ourBus.sendEvent(e1);
        assertNotNull(f1.get());
        assertEquals(f1.get(),"finish");
    }

    @Test
    public void testSendBroadcast() {
        ExampleService g1 = new ExampleService("g1");
        ExampleService g2 = new ExampleService("g2");
        ourBus.register(g1);
        ourBus.register(g2);
        ExampleBroadcast e1= new ExampleBroadcast("e1");
        ourBus.subscribeBroadcast(ExampleBroadcast.class,g1);
        ourBus.subscribeBroadcast(ExampleBroadcast.class,g2);
        ourBus.sendBroadcast(e1);
        try{
            assertEquals(ourBus.awaitMessage(g1),e1);
            assertEquals(ourBus.awaitMessage(g2),e1);
        }catch (InterruptedException exp){
            fail("broadcast didnt reach its target");
        }
    }

    @Test
    public void testSendEvent() {
        ExampleService g1 = new ExampleService("g1");
        ExampleService g2 = new ExampleService("g2");
        ExampleEvent e1 = new ExampleEvent("e1");
        assertNull(ourBus.sendEvent(e1));//needs to be null
        //now check the right rubin
        ourBus.register(g1);
        ourBus.register(g2);
        ExampleEvent[] events= new ExampleEvent[4];
        ourBus.subscribeEvent(ExampleEvent.class,g1);
        ourBus.subscribeEvent(ExampleEvent.class,g2);
        for (int i =0;i<events.length;i++){
            events[i]= new ExampleEvent("i");
            Future<String> f1 = ourBus.sendEvent(events[i]);
            assertNotNull(f1);
        }
        try{
            assertEquals(ourBus.awaitMessage(g1),events[0]);
            assertEquals(ourBus.awaitMessage(g2),events[1]);
            assertEquals(ourBus.awaitMessage(g1),events[2]);
            assertEquals(ourBus.awaitMessage(g2),events[3]);
        } catch (InterruptedException exp){
            fail("message didnt get to the destination");
        }


    }

    @Test
    public void testRegister() {
        /**
         * this method will be tested in the testSendEvent method and the testSendBroadcast.
         */
    }

    @Test
    public void testUnregister() {
        ExampleService g1 = new ExampleService("g1");
        ourBus.register(g1);
        ExampleEvent e1 = new ExampleEvent("e1");
        ExampleEvent e2 = new ExampleEvent("e2");
        ourBus.subscribeEvent(ExampleEvent.class,g1);
        ourBus.sendEvent(e1);
        try{
            ExampleEvent et= (ExampleEvent) ourBus.awaitMessage(g1);
            assertEquals(e1,et);//check await message.
        }catch (InterruptedException exp){
            fail("message didnt get to the destination");
        }
        assertNull(ourBus.sendEvent(e2));
    }

    @Test
    public void testAwaitMessage() {
        /**
         * this method is tested in the testUnregister method.
         */
    }
}