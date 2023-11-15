package org.schedule.management.specification.models;


public class Manager {

    private static ScheduleSpecification scheduleSpecification;


    public static ScheduleSpecification getScheduleSpecification() {
        return scheduleSpecification;
    }

    public static void setScheduleSpecification(ScheduleSpecification scheduleSpecification) {
        Manager.scheduleSpecification = scheduleSpecification;
    }
}
