package org.schedule.management.specification.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ScheduleSpecification {

    private List<Room> rooms = new ArrayList<>();

    public abstract void importDataCSV();
    public abstract void importDataJSON();
    public abstract void exportDataCSV();
    public abstract void exportDataJSON();
    public abstract void exportDataPDF();
    public abstract void addAppointment();
    public abstract void deleteAppointment();
    public abstract void rescheduleAppointment();
    public abstract void search();

    public boolean addRoom(String roomName, String capacity, Map<String, Integer> equipment){
        Room r = new Room(roomName, capacity, equipment);
        if(!rooms.contains(r)){
            rooms.add(r);
            return true;
        }
        //TODO: Exception
        return false;
    }

    public void importRooms(){

    }

    public void exportRooms(){

    }

    public void importSpecialDates(){


    }

    public void exportSpecialDates(){

    }



}
