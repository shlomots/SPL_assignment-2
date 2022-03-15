package bgu.spl.mics.application.messages;
import bgu.spl.mics.application.objects.Model;

import bgu.spl.mics.Event;

public class TestModelEvent implements Event<Model> {
    private Model model;

    public TestModelEvent(Model model){
        this.model = model;
    }

    public Model getModel() {
        return model;
    }
}
