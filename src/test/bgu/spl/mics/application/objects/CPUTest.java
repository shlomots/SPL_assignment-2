package bgu.spl.mics.application.objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.*;

public class CPUTest {

    private CPU cpu;

    @Before
    public void setUp() throws Exception {
        cpu = new CPU(1);
    }


    @Test
    public void testTakeBatch() {
        cpu.takeBatch();
        assertFalse(cpu.getDataToProcess().isEmpty());
    }


}