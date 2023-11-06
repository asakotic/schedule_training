package org.schedule.management.implementationtwo;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.schedule.management.specification.models.Appointment;
import org.schedule.management.specification.models.ConfigMapping;
import org.schedule.management.specification.models.Room;
import org.schedule.management.specification.models.ScheduleSpecification;

import java.io.FileReader;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ScheduleImpl extends ScheduleSpecification {
    @Override
    public void importDataCSV(String filepath, String configpath) throws IOException {
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
                        break;
                    case "endDate":
                        endDateTime = LocalDateTime.parse(i.get(index), formatter);
                        break;
                    case "relatedData":
                        ap.getRelatedData().put(userLbl, i.get(index));
                        break;
                    case "day":
                        ap.setDay(DayOfWeek.valueOf(i.get(index)));
                        break;
                }
            }
            if(startDateTime == null || endDateTime == null || ap.getDay() ==null) return;// TODO baci eksepsn

            if(!ap.getDay().equals(startDateTime.getDayOfWeek())){
                if(ap.getDay().getValue() > startDateTime.getDayOfWeek().getValue()){
                    startDateTime=  startDateTime
                            .plusDays(startDateTime.getDayOfWeek().getValue() + 7 -ap.getDay().getValue());
                }
            }

            LocalTime ltEnd = endDateTime.toLocalTime();

            ap.setDateFrom(startDateTime);
            ap.setDateTo(startDateTime.withHour(ltEnd.getHour()).withMinute(ltEnd.getMinute()));
            this.getAppointments().add(ap);

            startDateTime = startDateTime.plusDays(7);
            while(startDateTime.isBefore(endDateTime)){
                Appointment a = ap.copy();
                a.setDateFrom(startDateTime);
                a.setDateTo(startDateTime.withHour(ltEnd.getHour()).withMinute(ltEnd.getMinute()));
                a.setDay(startDateTime.getDayOfWeek());
                if(!this.getMetaData().getHolidays().contains(a.getDateFrom().toLocalDate().toString()))
                    this.getAppointments().add(a);
                startDateTime = startDateTime.plusDays(7);
            }
        }
    }

    @Override
    public void importDataJSON() {

    }

    @Override
    public void exportDataPDF() {

    }

}
