package bgu.spl.mics.application.objects;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model {
    private String name;
    private Data data;
    private Student student;
    private Status status = Status.PreTrained;
    private Results results = Results.None;

    public enum Status{
        PreTrained, Training, Trained, Tested
    }
    public enum Results{
        None, Good, Bad
    }

    public Model(String name, Data data, Student student){
        this.name = name;
        this.data = data;
        this.student = student;
    }

    public String getName() {
        return name;
    }

    public Status getStatus(){
        return this.status;
    }

    public Data getData() {
        return data;
    }

    public Results getResults(){
        return this.results;
    }

    public Student getStudent() {
        return student;
    }

    public void setStatus(Status status){
        this.status = status;
    }

    public void setResults(Results results) {
        this.results = results;
    }
}
