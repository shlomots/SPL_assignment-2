package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import javax.swing.text.Document;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {

    public static CountDownLatch threadInitCounter; //verifying that all the microservices initialize except of timeService

    public static void main(String[] args){
        File input = new File(args[0]);
        //construct all the objects
        List<Student> students = new ArrayList<>();
        List<StudentService> studentServices = new ArrayList<>();
        List<GPU> GPUs = new ArrayList<>();
        List<GPUService> GPUServices = new ArrayList<>();
        List<GPUTimeService> GPUTimeServices = new ArrayList<>();
        List<CPU> CPUs = new ArrayList<>();
        List<CPUService> CPUServices = new ArrayList<>();
        List<ConfrenceInformation> conferences = new ArrayList<>();
        List<ConferenceService> conferenceServices = new ArrayList<>();
        TimeService timeService = null;
        int tickTime = 0;
        int duration = 0;
        try {
            JsonElement fileElement = JsonParser.parseReader(new FileReader(input));
            JsonObject fileObject = fileElement.getAsJsonObject();
            //process all students
            JsonArray jsonArrayOfStudents = fileObject.get("Students").getAsJsonArray();

            for(JsonElement studentElement : jsonArrayOfStudents){
                //get the json students
                JsonObject studentJsonObject = studentElement.getAsJsonObject();
                //extract data
                String name = studentJsonObject.get("name").getAsString();
                String department = studentJsonObject.get("department").getAsString();
                String degreeString = studentJsonObject.get("status").getAsString();
                Student.Degree status;
                if (Objects.equals(degreeString, "MSc"))
                    status = Student.Degree.MSc;
                else
                    status = Student.Degree.PhD;

                Student student = new Student(name, department, status);
                students.add(student);

                //get array of models
                JsonArray jsonArrayOfModels = studentJsonObject.get("models").getAsJsonArray();
                for (JsonElement modelElement : jsonArrayOfModels){
                    JsonObject modelJsonObject = modelElement.getAsJsonObject();
                    //extract data
                    String modelName = modelJsonObject.get("name").getAsString();
                    String typeString = modelJsonObject.get("type").getAsString();
                    Data.Type type;
                    if (Objects.equals(typeString,"Images"))
                        type = Data.Type.Images;
                    else if (Objects.equals(typeString,"Text"))
                        type = Data.Type.Text;
                    else
                        type = Data.Type.Tabular;
                    int size = modelJsonObject.get("size").getAsInt();

                    //add this model to the student
                    Data dataOfModel = new Data(type, size);
                    Model model = new Model(modelName, dataOfModel, student);
                    student.setModels(model);
                }
                studentServices.add(new StudentService(name,student));
            }

            //process all GPUs
            JsonArray jsonArrayOfGPUs = fileObject.get("GPUS").getAsJsonArray();
            int i = 1;
            for(JsonElement GPUElement : jsonArrayOfGPUs){
                //extract data
                String GPUTypeString = GPUElement.getAsString();
                GPU.Type GPUType;
                if (Objects.equals(GPUTypeString, "RTX3090"))
                    GPUType = GPU.Type.RTX3090;
                else if (Objects.equals(GPUTypeString, "RTX2080"))
                    GPUType = GPU.Type.RTX2080;
                else
                    GPUType = GPU.Type.GTX1080;
                GPU gpu = new GPU(GPUType);
                GPUs.add(gpu);
                String gpuName = "GPU" + i;
                GPUServices.add(new GPUService(gpuName,gpu));
                GPUTimeServices.add(new GPUTimeService(gpuName,gpu));
                i++;
          }

            //process all the CPUs
            JsonArray jsonArrayOfCPUs = fileObject.get("CPUS").getAsJsonArray();
            int j = 1;
            for(JsonElement CPUElement : jsonArrayOfCPUs){
                //extract data
                int CPUTCores = CPUElement.getAsInt();
                CPU cpu = new CPU(CPUTCores);
                CPUs.add(cpu);
                String gpuName = "CPU" + j;
                CPUServices.add(new CPUService(gpuName,cpu));
                j++;
            }

            //process all conferences
            JsonArray jsonArrayOfConferences = fileObject.get("Conferences").getAsJsonArray();
            int lastConferenceDate = 0;
            for(JsonElement conferenceElement : jsonArrayOfConferences) {
                //get the json conferences
                JsonObject conferenceJsonObject = conferenceElement.getAsJsonObject();
                //extract data
                String name = conferenceJsonObject.get("name").getAsString();
                int date = conferenceJsonObject.get("date").getAsInt();

                ConfrenceInformation conferenceInformation = new ConfrenceInformation(name,date, lastConferenceDate);
                conferences.add(conferenceInformation);
                conferenceServices.add(new ConferenceService(name, conferenceInformation));
                lastConferenceDate = date; //set the last conference date of the next conference as the date of this conference;
            }

            //tickTime and duration
            tickTime = fileObject.get("TickTime").getAsInt();
            duration = fileObject.get("Duration").getAsInt();

            timeService = new TimeService("timeService", tickTime, duration);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        threadInitCounter = new CountDownLatch(GPUServices.size() + GPUTimeServices.size() +
                CPUServices.size() + studentServices.size() + conferenceServices.size());

        List<Thread> allThread = new ArrayList<>(); //a list of all the threads
        for (GPUService gpuService : GPUServices){
            Thread g = new Thread(gpuService);
            allThread.add(g);
            g.start();
        }

        for (GPUTimeService gpuTimeService : GPUTimeServices){
            Thread g1 = new Thread(gpuTimeService);
            allThread.add(g1);
            g1.start();
        }

        for (CPUService cpuService : CPUServices){
            Thread c = new Thread(cpuService);
            allThread.add(c);
            c.start();
        }

        for (StudentService studentService : studentServices){
            Thread s = new Thread(studentService);
            allThread.add(s);
            s.start();
        }

        for (ConferenceService conferenceService : conferenceServices){
            Thread con = new Thread(conferenceService);
            allThread.add(con);
            con.start();
        }

        Thread timeThread = new Thread(timeService); //only construct but doesn't start yet

        try {
            threadInitCounter.await();
            timeThread.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            timeThread.join();
//            System.out.println(timeThread.getName()+"finish");
            for (Thread thread : allThread){
                thread.join();
//                System.out.println(thread.getName()+"finish");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        //create output
        JsonObject output = new JsonObject();
//        Gson output = new Gson();
        //this will be the pair for students
        JsonArray studentsInformation=new JsonArray();
        //here im going to fill the studentsInformation the array to pair to students.
        for(int i=0;i<students.size();i++){
            //start creating the first block of information in the array.
            Student currStudent = students.get(i);
            JsonObject currStudentInformation=new JsonObject();
            currStudentInformation.addProperty("name",currStudent.getName());
            currStudentInformation.addProperty("department",currStudent.getDepartment());
            currStudentInformation.addProperty("status", currStudent.getStatus().toString());
            currStudentInformation.addProperty("publications",currStudent.getPublications());
            currStudentInformation.addProperty("paperRead",currStudent.getPapersRead());
            JsonArray modelsData = new JsonArray();
            //each iteration here will create an object represnting the data of a trained model.
            //in the end of it' we will have a modelsData array containing object that each contains the relevent information we need.
            for(int j = 0 ; j<currStudent.getModels().size() ; j++){
                Model currentModel=currStudent.getModels().get(j);
                if(currentModel.getStatus()== Model.Status.Tested){
                    JsonObject currentModelData = new JsonObject();
                    currentModelData.addProperty("name",currentModel.getName());
                    JsonObject currentModelDataInformation = new JsonObject();
                    currentModelDataInformation.addProperty("type",currentModel.getData().getType().toString());
                    currentModelDataInformation.addProperty("size",currentModel.getData().getSize());
                    currentModelData.add("data",currentModelDataInformation);
                    currentModelData.addProperty("status",currentModel.getStatus().toString());
                    currentModelData.addProperty("results",currentModel.getResults().toString());
                    modelsData.add(currentModelData);
                }
            }
            //now we want to pair
            currStudentInformation.add("trainedModels",modelsData);
            studentsInformation.add(currStudentInformation);
        }
        output.add("students",studentsInformation);

        //this will be the pair of conferences
        JsonArray conferencesInformation = new JsonArray();
        for(int i=0;i<conferences.size();i++){
            ConfrenceInformation currConferenceInformation= conferences.get(i);//$hope not problematic
            JsonObject currConferenceInformationInformation=new JsonObject();
            currConferenceInformationInformation.addProperty("name",currConferenceInformation.getName());
            currConferenceInformationInformation.addProperty("date",currConferenceInformation.getDate());
            //this will be the pair for publications
            JsonArray currConferencePublications = new JsonArray();
            //this for loop wil create it
            for(int j=0;j<currConferenceInformation.getResults().size();j++){
                Model currModel = currConferenceInformation.getResults().get(j);//$hope not problematic
                JsonObject currModelPublication = new JsonObject();
                currModelPublication.addProperty("name",currModel.getName());
                JsonObject ModelDataInformation = new JsonObject();
                ModelDataInformation.addProperty("type",currModel.getData().getType().toString());
                ModelDataInformation.addProperty("size",currModel.getData().getSize());
                currModelPublication.add("data",ModelDataInformation);
                currModelPublication.addProperty("status",currModel.getStatus().toString());
                currModelPublication.addProperty("results",currModel.getResults().toString());
                currConferencePublications.add(currModelPublication);
            }
            currConferenceInformationInformation.add("publications",currConferencePublications);
            conferencesInformation.add(currConferenceInformationInformation);
        }
        output.add("conferences",conferencesInformation);
        //now we count the CPU and GPU
        int CPUSTime=0;
        int GPUSTime=0;
        for(int i=0;i<CPUs.size();i++){
            CPUSTime=CPUSTime+CPUs.get(i).getTotalProcessTime();
        }
        for(int i=0;i<GPUs.size();i++){
            GPUSTime=GPUSTime+GPUs.get(i).getTime();
        }
        output.addProperty("CPUSTime",CPUSTime);
        output.addProperty("GPUSTime",GPUSTime);
        output.addProperty("batchesProcessed",GPUs.get(0).getCluster().getStatistics().getProcessedData());
        try(FileWriter file = new FileWriter("output")) {
            Gson gson1 = new GsonBuilder().setPrettyPrinting().create();
            String json = gson1.toJson(output);
            file.write(json);
            file.flush();
        }catch (IOException e){}
    }
}
