package org.schedule.management.specification.models;

import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class ScheduleSpecification {
    public abstract void importDataCSV();
    public abstract void importDataJSON(); // uzme sobe, uzme praznike, meta podaci
    public abstract void exportDataCSV();
    public abstract void exportDataJSON();
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

    public void importConfig(){

    }
}
