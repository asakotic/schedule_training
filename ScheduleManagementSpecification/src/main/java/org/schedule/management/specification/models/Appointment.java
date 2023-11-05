package org.schedule.management.specification.models;

import lombok.Getter;
import lombok.Setter;
import lombok.With;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class Appointment {
    private DayOfWeek day;
    private Room room;
    private Map<String,String> relatedData = new HashMap<>();
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;

    public Appointment(DayOfWeek day, Room room, Map<String, String> relatedData, LocalDateTime dateFrom, LocalDateTime dateTo) {
        this.day = day;
        this.room = room;
        this.relatedData = relatedData;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    public Appointment() {
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "day='" + day + '\'' +
                ", room=" + room +
                ", relatedData=" + relatedData +
                ", dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                '}' + '\n';
    }

    public Appointment copy(){
        Appointment a = new Appointment(
                this.day,
                this.room,
                new HashMap<>(relatedData),
                this.dateFrom,
                this.dateTo
        );

        return a;
    }
}
