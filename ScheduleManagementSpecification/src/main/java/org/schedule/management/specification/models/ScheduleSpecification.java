package org.schedule.management.specification.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Getter
@Setter

public abstract class ScheduleSpecification {

    private MetaData metaData;
    private List<Appointment> appointments = new ArrayList<>();
    private List<String> headers;
    public abstract void importDataCSV(String file,String config) throws IOException;
    public abstract void importDataJSON(); // uzme sobe, uzme praznike, meta podaci
    public abstract void exportDataPDF();

    public  void search(){}

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
    public  boolean addAppointment(Appointment appointment){
        LocalDateTime start = appointment.getDateFrom();
        LocalDateTime end = appointment.getDateTo();
        if (start.isAfter(end))return false;
        for(Appointment a : this.getAppointments()){
            if(!a.getRoom().getName().equals(appointment.getRoom().getName())) continue;
            LocalDateTime astart= a.getDateFrom();
            LocalDateTime aend= a.getDateFrom();
            if(start.isAfter(astart) && start.isBefore(aend) ||
                end.isBefore(aend) && end.isAfter(astart) ||
                start.isBefore(astart) && end.isAfter(aend) ||
                start.isAfter(astart) && end.isBefore(aend)) {
                return false;
            }
        }
       // this.appointments.add(appointment);
        return true;
    }
    public  boolean deleteAppointment(LocalDateTime from, LocalDateTime to, String roomName){
        boolean flag = false;
        Appointment removeA = null;
        for(Appointment a: this.appointments){
            if(a.getDateFrom().equals(from) &&
               a.getDateTo().equals(to) &&
               a.getRoom().getName().equals(roomName)){
                flag = true;
                removeA = a;
                break;
            }
        }
        if(flag)appointments.remove(removeA);
        return flag;
    }
    public  boolean rescheduleAppointment(LocalDateTime from, LocalDateTime to,String room, Appointment appointment){
       return deleteAppointment(from,to,room)
               && addAppointment(appointment);
    }


    public void exportDataCSV(ArrayList<Appointment> appointments, String fileName){
       // appointments.add(new Appointment("PON", MetaData.getInstance().getRooms().get(0), new ArrayList<>(), LocalDateTime.now().toString(), LocalDateTime.now().plusDays(1).toString()));
       // appointments.add(new Appointment("UTO", MetaData.getInstance().getRooms().get(0), new ArrayList<>(), LocalDateTime.now().toString(), LocalDateTime.now().plusDays(1).toString()));
       // appointments.add(new Appointment("SRE", MetaData.getInstance().getRooms().get(0), new ArrayList<>(), LocalDateTime.now().toString(), LocalDateTime.now().plusDays(1).toString()));
    }
    public void exportDataJSON(List<Appointment> appointments, String fileName){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (PrintStream writer = new PrintStream(fileName)) {
            gson.toJson(appointments, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    protected List<ConfigMapping>  importConfig(String configPath){
        List<ConfigMapping> map = new ArrayList<>();

        File file;
        Scanner sc = null;
        try {
            file = new File(configPath);
            sc = new Scanner(file);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] split = line.split(" ", 3);
                ConfigMapping cm = new ConfigMapping(Integer.valueOf(split[0]), split[1], split[2]);
                map.add(cm);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            if (sc != null) {
                sc.close();
            }
        }
        return map;
    }

}
