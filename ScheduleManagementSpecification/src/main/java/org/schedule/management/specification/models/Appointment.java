package org.schedule.management.specification.models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Setter
@Getter
public class Appointment {
    private String day;
    private String time;
    private Room room;
    private List<RelatedDataAppointment> relatedData;
    private Date dateFrom;
    private Date dateTo;

    public Appointment(String day, String time, Room room, List<RelatedDataAppointment> relatedData, Date dateFrom) {
        this.day = day;
        this.time = time;
        this.room = room;
        this.relatedData = relatedData;
        this.dateFrom = dateFrom;
    }

    public Appointment(String day, String time, Room room, List<RelatedDataAppointment> relatedData, Date dateFrom, Date dateTo) {
        this.day = day;
        this.time = time;
        this.room = room;
        this.relatedData = relatedData;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }
}
