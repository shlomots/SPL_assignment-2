package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {
    /**
     * Enum representing the Degree the student is studying for.
     */
    public enum Degree {
        MSc, PhD
    }

    private String name;
    private String department;
    private Degree status;
    private int publications = 0;
    private int papersRead = 0;
    private ArrayList<Model> models = new ArrayList<>();

    public Student(String name, String department, Degree status){
        this.name = name;
        this.department = department;
        this.status = status;
    }

    public void setPapersRead(int add){
        this.papersRead += add;
    }

    public void setPapersReadEnd(){
        this.papersRead = this.papersRead-this.publications;
    }

    public void setPublications(){
        (this.publications)++;
    }

    public ArrayList<Model> getModels(){
        return models;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public Degree getStatus() {
        return status;
    }

    public int getPublications() {
        return publications;
    }

    public int getPapersRead() {
        return papersRead;
    }

    public void setModels(Model model){
        models.add(model);
    }
}
