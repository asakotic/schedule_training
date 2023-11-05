package org.schedule.management.specification.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.schedule.management.specification.adapters.DateAdapter;
import org.schedule.management.specification.adapters.TimeAdapter;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class MetaData {

    private String dateFormat;
    private List<Room> rooms;
    private LocalDate scheduleValidFrom;
    private LocalDate scheduleValidTo;
    private List<String> holidays;
    private Map<DayOfWeek,WorkingHours> workingHours;
    private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,new DateAdapter())
            .registerTypeAdapter(LocalTime.class,new TimeAdapter())
            .create();

    public static MetaData importMeta(){
        try (Reader reader = new FileReader("C:\\Users\\jovvu\\IdeaProjects\\schedule-management-component-implementation\\ScheduleManagementSpecification\\src\\main\\resources\\metadata.json")) {
            MetaData staff = gson.fromJson(reader, MetaData.class);

            ArrayList<String> holidaysPom = new ArrayList<>();
            for(String date : staff.holidays){
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
            staff.holidays = holidaysPom;
            return staff;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
