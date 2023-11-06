package org.schedule.management.implementationone;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.schedule.management.specification.models.Appointment;
import org.schedule.management.specification.models.ConfigMapping;
import org.schedule.management.specification.models.Room;
import org.schedule.management.specification.models.ScheduleSpecification;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ScheduleImpl extends ScheduleSpecification {

    @Override
    public void importDataCSV(String filepath,String configpath) throws IOException {
        List<ConfigMapping> configMap = importConfig(configpath);
        FileReader fr = new FileReader(filepath);
        CSVParser csvParser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(fr);
        this.setHeaders(csvParser.getHeaderNames());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(this.getMetaData().getDateFormat());

        for(CSVRecord i : csvParser){
            Appointment ap = new Appointment();
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;

            for (ConfigMapping row : configMap) {
                int index = row.getIndex();
                String userLbl = row.getUserLabel();


                switch (configMap.get(index).getPrimaryLabel()) {
                    case "room":
                        List<Room> rooms =  this.getMetaData().getRooms();
                        for(Room r: rooms){
                            if(r.getName().equals(i.get(index))){
                                ap.setRoom(r);
                            }
                        }
                        break;
                    case "startDate":
                        startDateTime = LocalDateTime.parse(i.get(index), formatter);
                        ap.setDay(startDateTime.getDayOfWeek());
                        break;
                    case "endDate":
                        endDateTime = LocalDateTime.parse(i.get(index), formatter);
                        break;
                    case "relatedData":
                        ap.getRelatedData().put(userLbl, i.get(index));
                        break;
                }
            }
            if(startDateTime == null || endDateTime == null || ap.getDay() == null) return;// TODO baci eksepsn
            LocalTime startWorkingtime = this.getMetaData().getWorkingHours().get(startDateTime.getDayOfWeek()).getOpeningTime();
            LocalTime endWorkingTime = this.getMetaData().getWorkingHours().get(startDateTime.getDayOfWeek()).getClosingTime();

            if(startDateTime.toLocalDate().equals(endDateTime.toLocalDate())){
                ap.setDateFrom(startDateTime);
                ap.setDateTo(endDateTime);
                if(startDateTime.toLocalTime().isBefore(startWorkingtime) ||
                        endDateTime.toLocalTime().isAfter(endWorkingTime)) {
                    //TODO baci eksepsn
                }
                this.getAppointments().add(ap);
            }else{
                ap.setDateFrom(startDateTime);
                startDateTime = startDateTime.withHour(endWorkingTime.getHour()).withMinute(endWorkingTime.getMinute());
                ap.setDateTo(startDateTime);
                this.getAppointments().add(ap);

                Appointment a = ap.copy();
                this.getAppointments().addAll(makeMore(ap,startDateTime.plusDays(1),endDateTime.minusDays(1)));

                Appointment b = ap.copy();
                startWorkingtime = this.getMetaData().getWorkingHours().get(endDateTime.getDayOfWeek()).getOpeningTime();
                b.setDateFrom(endDateTime.withHour(startWorkingtime.getHour()).withMinute(startWorkingtime.getMinute()));
                b.setDateTo(endDateTime);
                b.setDay(endDateTime.getDayOfWeek());
                this.getAppointments().add(b);
            }

        }
        this.getAppointments().sort(Appointment::compareTo);

    }
    private List<Appointment> makeMore(Appointment appointment,LocalDateTime startDate, LocalDateTime endDate){
        LocalTime startWorkingtime = this.getMetaData().getWorkingHours().get(startDate.getDayOfWeek()).getOpeningTime();
        LocalTime endWorkingTime = this.getMetaData().getWorkingHours().get(startDate.getDayOfWeek()).getClosingTime();
        List<Appointment> appointments = new ArrayList<>();
        while(!startDate.isAfter(endDate)){
            Appointment a = appointment.copy();
            a.setDateFrom(startDate.withHour(startWorkingtime.getHour()).withMinute(startWorkingtime.getMinute()));
            a.setDateTo(startDate.withHour(endWorkingTime.getHour()).withMinute(endWorkingTime.getMinute()));
            a.setDay(startDate.getDayOfWeek());
            appointments.add(appointment);
            startDate = startDate.plusDays(1);
            startWorkingtime = this.getMetaData().getWorkingHours().get(startDate.getDayOfWeek()).getOpeningTime();
            endWorkingTime = this.getMetaData().getWorkingHours().get(startDate.getDayOfWeek()).getClosingTime();
        }

        return appointments;
    }

    @Override
    public void importDataJSON() {

    }

    @Override
    public void exportDataPDF() {

    }

}
