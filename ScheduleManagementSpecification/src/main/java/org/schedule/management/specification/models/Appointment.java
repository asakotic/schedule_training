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
public class Appointment implements Comparable<Appointment>{
    private DayOfWeek day;
    private Room room;
    private Map<String,String> relatedData = new HashMap<>();
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;

    /**
     * Creates new instance of Appointment
     * @param day First day of Appointment
     * @param room Room of appointment
     * @param relatedData Related data
     * @param dateFrom Date and time when appointment starts
     * @param dateTo Date and time when appointment ends
     */
    public Appointment(DayOfWeek day, Room room, Map<String, String> relatedData, LocalDateTime dateFrom, LocalDateTime dateTo) {
        this.day = day;
        this.room = room;
        this.relatedData = relatedData;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    /**
     * Creates new instance of Appointment
     * @param room Room of appointment
     * @param dateFrom Date and time when appointment starts
     * @param dateTo Date and time when appointment ends
     */
    public Appointment(Room room, LocalDateTime dateFrom, LocalDateTime dateTo) {
        this.room = room;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.setDay(dateFrom.getDayOfWeek());
    }

    /**
     * Creates new instance of appointment
     */
    public Appointment() {
    }

    /**
     * Returns appointment values
     * @return Returns String which contains appointment values
     */
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

    /**
     * Creates new copy of appointment
     * @return Returns new instance of appointment
     */
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

    /**
     *
     * Compares two Appointments
     * @param o the object to be compared.
     * @return compared value
     */
    @Override
    public int compareTo(Appointment o) {
        int a = this.getRoom().getName().compareTo(o.room.getName());
        if(a != 0) return a;

        a = this.getDateFrom().toLocalDate().compareTo(o.dateFrom.toLocalDate());
        if(a != 0) return a;

        return this.getDateFrom().toLocalTime().compareTo(o.dateFrom.toLocalTime());
    }
}
