package bgu.spl.mics;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class FutureTest {
    private Future<String> future;

    @Before
    public void setUp() throws Exception {
        future = new Future<>();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGet() {
        assertFalse(future.isDone());
        future.resolve("");
        assertEquals(future.get(),"");
    }

    @Test
    public void testResolve() {
        assertFalse(future.isDone());//first check the status for done is false.
        future.resolve("");//put "" in the result field.
        assertTrue(future.isDone());//check the resolve indeed updated the status, done, to be true.
        assertEquals(future.get(),"");//check that the right string was inserted.
    }

    @Test
    public void testIsDone() {
        assertFalse(future.isDone());//first check the status for done is false.
        future.resolve("");
        assertTrue(future.isDone());
    }

    @Test
    public void testGet2() throws InterruptedException
    {
        assertFalse(future.isDone());
        future.resolve("");
        assertEquals(future.get(100,TimeUnit.MILLISECONDS),"");
    }
}