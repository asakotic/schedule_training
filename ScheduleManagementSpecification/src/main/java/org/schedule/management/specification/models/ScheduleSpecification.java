package org.schedule.management.specification.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Getter
@Setter

public abstract class ScheduleSpecification {

    private MetaData metaData;
    private List<Appointment> appointments = new ArrayList<>();
    private List<String> headers;
    public abstract void importDataCSV(String file,String config) throws IOException;
    public abstract void importDataJSON(); // uzme sobe, uzme praznike, meta podaci
    public abstract void exportDataPDF();
    public abstract void addAppointment();
    public abstract void deleteAppointment();
    public abstract void rescheduleAppointment();
    public abstract void search();

    public void importMeta(){
        metaData = MetaData.importMeta();
        System.out.println(metaData);
    }
    public void importConfig(){
        //TODO ovo ne treba da bude tu ili da bude privatno
    }

    public boolean addRoom(String roomName, String capacity, Map<String, Integer> equipment){
        Room r = new Room(roomName, capacity, equipment);
        if(!metaData.getRooms().contains(r)){
            metaData.getRooms().add(r);
            return true;
        }
        //TODO: Exception
        return false;
    }


    public void exportDataCSV(ArrayList<Appointment> appointments, String fileName){
       // appointments.add(new Appointment("PON", MetaData.getInstance().getRooms().get(0), new ArrayList<>(), LocalDateTime.now().toString(), LocalDateTime.now().plusDays(1).toString()));
       // appointments.add(new Appointment("UTO", MetaData.getInstance().getRooms().get(0), new ArrayList<>(), LocalDateTime.now().toString(), LocalDateTime.now().plusDays(1).toString()));
       // appointments.add(new Appointment("SRE", MetaData.getInstance().getRooms().get(0), new ArrayList<>(), LocalDateTime.now().toString(), LocalDateTime.now().plusDays(1).toString()));
    }
    public void exportDataJSON(ArrayList<Appointment> appointments, String fileName){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (PrintStream writer = new PrintStream(fileName)) {
            gson.toJson(appointments, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
