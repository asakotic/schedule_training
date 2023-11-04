package org.schedule.management.specification.models;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MetaData {

    public String dateFormat;
    public ArrayList<Room> rooms;
    public String scheduleValidFrom;
    public String scheduleValidTo;
    public ArrayList<String> holidays;
    private static MetaData instance = null;
    public static MetaData getInstance(){
        if(instance == null){
            instance = new MetaData();
        }
        return instance;
    }

    private MetaData(){

    }

    public void importMeta(){
        Gson gson = new Gson();
        try (Reader reader = new FileReader("D:\\Education\\Racunarski Fakultet\\Treci semestar\\schedule-management-component-implementation\\ScheduleManagementSpecification\\src\\main\\resources\\metadata.json")) {
            MetaData staff = gson.fromJson(reader, MetaData.class);
            this.setHolidays(staff.holidays);
            this.setRooms(staff.rooms);
            this.setDateFormat(staff.dateFormat);
            this.setScheduleValidTo(staff.scheduleValidTo);
            this.setScheduleValidFrom(staff.scheduleValidFrom);
            ArrayList<String> holidaysPom = new ArrayList<>();
            for(String date : holidays){
                if(!date.contains("-")) {
                    holidaysPom.add(LocalDate.parse(date, DateTimeFormatter.ofPattern("dd:MM:yyyy")).toString());
                    continue;
                }

                LocalDate localDate1 = LocalDate.parse(date.split("-")[0], DateTimeFormatter.ofPattern("dd:MM:yyyy"));
                LocalDate localDate2 = LocalDate.parse(date.split("-")[1], DateTimeFormatter.ofPattern("dd:MM:yyyy"));

                List<String> totalDates = new ArrayList<>();
                while (!localDate1.isAfter(localDate2)) {
                    totalDates.add(localDate1.toString());
                    localDate1 = localDate1.plusDays(1);
                }

                holidaysPom.addAll(totalDates);
            }

            holidays = holidaysPom;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "MetaData{" +
                "dateFormat='" + dateFormat + '\'' +
                ", rooms=" + rooms +
                ", scheduleValidFrom=" + scheduleValidFrom +
                ", scheduleValidTo=" + scheduleValidTo +
                ", holidays=" + holidays +
                '}';
    }
}
