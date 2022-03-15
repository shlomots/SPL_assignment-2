package bgu.spl.mics.application.objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.*;

public class GPUTest {
    private GPU g1= new GPU(GPU.Type.RTX2080);
    private Data data = new Data(Data.Type.Images,5500);
    private Student haim = new Student("haim","mathamatics", Student.Degree.MSc);
    private Model model = new Model("hello",data,haim);

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void divideData() {
        g1.divideData(model);
        assertNotNull(g1.getUnprocessedDataBatches());
    }

    @Test
    public void act() {
        g1.act();
        assertTrue(model.getStatus()== Model.Status.Trained);
    }

    @Test
    public void sendDataBatchesToCluster() {
        g1.sendDataBatchesToCluster();
        assertTrue(g1.getSentToTheCluster()!=0);
    }

    @Test
    public void receiveDataFromCluster() {
        //this function will be tested in other function
    }

    @Test
    public void trainModel() {
        //this function will be tested in other function act
    }

    @Test
    public void notifier() {
        //this function is tested in act
    }
}