package org.schedule.management.specification.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class ScheduleSpecification {

    private ArrayList<Appointment> appointments;
    public abstract void importDataCSV();
    public abstract void importDataJSON(); // uzme sobe, uzme praznike, meta podaci
    public abstract void exportDataPDF();
    public abstract void addAppointment();
    public abstract void deleteAppointment();
    public abstract void rescheduleAppointment();
    public abstract void search();

    public boolean addRoom(String roomName, String capacity, Map<String, Integer> equipment){
        Room r = new Room(roomName, capacity, equipment);
        if(!MetaData.getInstance().getRooms().contains(r)){
            MetaData.getInstance().getRooms().add(r);

            return true;
        }
        //TODO: Exception
        return false;
    }

    public void importMeta(){
        MetaData.getInstance().importMeta();
    }

    public void exportDataCSV(ArrayList<Appointment> appointments, String fileName){
        appointments.add(new Appointment("PON", MetaData.getInstance().getRooms().get(0), new ArrayList<>(), LocalDateTime.now().toString(), LocalDateTime.now().plusDays(1).toString()));
        appointments.add(new Appointment("UTO", MetaData.getInstance().getRooms().get(0), new ArrayList<>(), LocalDateTime.now().toString(), LocalDateTime.now().plusDays(1).toString()));
        appointments.add(new Appointment("SRE", MetaData.getInstance().getRooms().get(0), new ArrayList<>(), LocalDateTime.now().toString(), LocalDateTime.now().plusDays(1).toString()));



    }
    public void exportDataJSON(ArrayList<Appointment> appointments, String fileName){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (PrintStream writer = new PrintStream(fileName)) {
            gson.toJson(appointments, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void importConfig(){

    }
}
