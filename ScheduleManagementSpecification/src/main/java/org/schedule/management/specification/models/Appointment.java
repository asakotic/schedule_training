package org.schedule.management.specification.models;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Setter
@Getter
public class Appointment {
    private String day;
    private Room room;
    private List<RelatedDataAppointment> relatedData;
    private String dateFrom;
    private String dateTo;

    public Appointment(String day, Room room, List<RelatedDataAppointment> relatedData,
                       String dateFrom, String dateTo) {
        this.day = day;
        this.room = room;
        this.relatedData = relatedData;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }


    @Override
    public String toString() {
        return "Appointment{" +
                "day='" + day + '\'' +
                ", room=" + room +
                ", relatedData=" + relatedData +
                ", dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                '}';
    }
}
