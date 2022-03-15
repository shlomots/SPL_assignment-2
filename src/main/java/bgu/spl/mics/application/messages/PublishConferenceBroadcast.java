package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.Model;

import java.util.LinkedList;

public class PublishConferenceBroadcast implements Broadcast {
    private LinkedList<Model> results;

    public PublishConferenceBroadcast(LinkedList<Model> results){
        this.results = results;
    }

    public LinkedList<Model> getResults(){
        return results;
    }

}
