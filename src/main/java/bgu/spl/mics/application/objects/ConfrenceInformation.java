package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.PublishConferenceBroadcast;

import java.util.LinkedList;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConfrenceInformation {

    private String name;
    private int date;
    private int lastConferenceDate;
    private int time = 0;
    private LinkedList<Model> results = new LinkedList<>();

    public ConfrenceInformation(String name, int date, int lastConferenceDate){
        this.name = name;
        this.date = date;
        this.lastConferenceDate = lastConferenceDate;
    }

    public String getName() {
        return name;
    }

    public int getLastConferenceDate() {
        return lastConferenceDate;
    }

    public int getDate() {
        return date;
    }

    public LinkedList<Model> getResults() {
        return results;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
