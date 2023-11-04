package org.schedule.management.specification;

import org.schedule.management.specification.models.Appointment;
import org.schedule.management.specification.models.ScheduleSpecification;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello!");
        ScheduleSpecification specification = new ScheduleSpecification() {
            @Override
            public void importDataCSV() {

            }

            @Override
            public void importDataJSON() {

            }

            @Override
            public void exportDataPDF() {

            }

            @Override
            public void addAppointment() {

            }

            @Override
            public void deleteAppointment() {

            }

            @Override
            public void rescheduleAppointment() {

            }

            @Override
            public void search() {

            }
        };

        specification.importMeta();
        specification.exportDataJSON(new ArrayList<>(), "1.json");
        specification.exportDataJSON(new ArrayList<>(), "1.csv");
    }
}