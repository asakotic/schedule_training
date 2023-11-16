package org.schedule.management.specification.models;


public class Manager {

    private static ScheduleSpecification scheduleSpecification;

    public static ScheduleSpecification getScheduleSpecification() {
        return scheduleSpecification;
    }

    /**
     * Changes implemention
     * @param scheduleSpecification sets new specification
     */
    public static void setScheduleSpecification(ScheduleSpecification scheduleSpecification) {
        Manager.scheduleSpecification = scheduleSpecification;
    }
}
