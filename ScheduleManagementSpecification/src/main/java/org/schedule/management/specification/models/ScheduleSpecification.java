package org.schedule.management.specification.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.schedule.management.specification.adapters.LocalDateTimeAdapter;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Getter
@Setter

public abstract class ScheduleSpecification {

    private MetaData metaData;
    private List<Appointment> appointments = new ArrayList<>();
    private List<String> headers;
    public abstract void importDataCSV(String file,String config) throws IOException;
    public abstract void importDataJSON() throws IOException; // uzme sobe, uzme praznike, meta podaci
    public abstract void exportDataPDF(String fileName);

    public List<Appointment> filterDate(List<Appointment> appointments, LocalDate dateFrom, LocalDate dateTo){
        List<Appointment> group = new ArrayList<>();

        for(Appointment a : appointments){
            if(!a.getDateFrom().toLocalDate().isBefore(dateFrom) && !a.getDateTo().toLocalDate().isAfter(dateTo))
                group.add(a);
        }

        return group;
    }
    public List<Appointment> filterCapacity(List<Appointment> appointments, boolean greater, int capacity){
        List<Appointment> group = new ArrayList<>();

        for(Appointment a : appointments){
            if(greater && capacity < Integer.parseInt(a.getRoom().getCapacity()))
                group.add(a);
            if(!greater && capacity > Integer.parseInt(a.getRoom().getCapacity()))
                group.add(a);
        }
        return group;
    }

    public List<Appointment> filterByRoom(List<Appointment> appointments, List<String> rooms){
        List<Appointment> group = new ArrayList<>();

        for(Appointment a : appointments){
            if(rooms.contains(a.getRoom().getName()))
                group.add(a);
        }

        return group;
    }

    public List<Appointment> filterByReservedAppointments(){
        return appointments;
    }

    public List<Appointment> resetFilter(){
        return appointments;
    }

    public void searchByAvailableAppointments(){


    }

    public  void search(){




    }
    public void importMeta(){
        metaData = MetaData.importMeta();
        System.out.println(metaData);
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
        this.appointments.add(appointment);
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


    protected List<ConfigMapping> importConfig(String configPath){
        List<ConfigMapping> map = new ArrayList<>();
        int br = 0;
        File file;
        Scanner sc = null;
        try {
            file = new File(configPath);
            sc = new Scanner(file);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] split = line.split(" ", 3);
                ConfigMapping cm = new ConfigMapping(Integer.parseInt(split[0]), split[1], split[2]);
                map.add(cm);
                if(br++ != cm.getIndex()) return null; //TODO BACI EKSEPSNNNNN
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
    public void exportDataCSV(String fileName, String configpath, List<Appointment> appointments){

        List<ConfigMapping> configMap = importConfig(configpath);
        configMap.sort(Comparator.comparingInt(ConfigMapping::getIndex));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(this.getMetaData().getDateFormat());
        FileWriter fileWriter = null;
        CSVPrinter csvPrinter = null;
        getAppointments().sort(Appointment::compareTo);
        try {
            fileWriter = new FileWriter(fileName);
            csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
            for (Appointment appointment : appointments) {

                List<String> toAdd = new ArrayList<>();
                for (ConfigMapping row : configMap) {
                    String userLbl = row.getUserLabel();

                    switch (row.getPrimaryLabel()) {
                        case "room" -> toAdd.add(appointment.getRoom().getName());
                        case "startDate" -> toAdd.add(appointment.getDateFrom().format(formatter));
                        case "endDate" -> toAdd.add(appointment.getDateTo().format(formatter));
                        case "relatedData" -> toAdd.add(appointment.getRelatedData().get(userLbl));
                        case "day" -> toAdd.add(appointment.getDay().toString());
                    }
                }
                csvPrinter.printRecord(toAdd);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                csvPrinter.close();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void exportDataJSON(List<Appointment> appointments, String fileName){
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
        try (PrintStream writer = new PrintStream(fileName)) {
            gson.toJson(appointments, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
