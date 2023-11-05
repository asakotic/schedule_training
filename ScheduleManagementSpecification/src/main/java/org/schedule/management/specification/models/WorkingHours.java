package org.schedule.management.specification.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Setter
@ToString
public class WorkingHours {
    private LocalTime openingTime;
    private LocalTime closingTime;

    public WorkingHours(LocalTime openingTime, LocalTime closingTime) {
        this.openingTime = openingTime;
        this.closingTime = closingTime;
    }
}
