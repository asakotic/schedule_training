package org.schedule.management.implementationone;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.schedule.management.specification.models.Appointment;
import org.schedule.management.specification.models.ConfigMapping;
import org.schedule.management.specification.models.Room;
import org.schedule.management.specification.models.ScheduleSpecification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ScheduleImpl extends ScheduleSpecification {


    private  List<ConfigMapping>  importConfig(String configPath){
        List<ConfigMapping> map = new ArrayList<>();

        File file;
        Scanner sc = null;
        try {
            file = new File(configPath);
            sc = new Scanner(file);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] split = line.split(" ", 3);
                ConfigMapping cm = new ConfigMapping(Integer.valueOf(split[0]), split[1], split[2]);
                map.add(cm);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            if (sc != null) {
                sc.close();
            }
        }
        return map;
    }
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
                        break;
                    case "endDate":
                        endDateTime = LocalDateTime.parse(i.get(index), formatter);
                        break;
                    case "relatedData":
                        ap.getRelatedData().put(userLbl, i.get(index));
                        break;
                }
            }
            ap.setDay(startDateTime.getDayOfWeek());
            if(startDateTime.toLocalDate().equals(endDateTime.toLocalDate())){
                ap.setDateFrom(startDateTime);
                ap.setDateTo(endDateTime);
                this.getAppointments().add(ap);
            }else{
                ap.setDateFrom(startDateTime);
                startDateTime = startDateTime.withHour(23).withMinute(59);
                ap.setDateTo(startDateTime);
                this.getAppointments().add(ap);

                Appointment a = ap.copy();
                this.getAppointments().addAll(makeMore(ap,startDateTime.plusDays(1),endDateTime.minusDays(1)));

                Appointment b = ap.copy();
                b.setDateFrom(endDateTime.withHour(0).withMinute(0));
                b.setDateTo(endDateTime);
                this.getAppointments().add(b);
            }

        }
    }
    private List<Appointment> makeMore(Appointment appointment,LocalDateTime startDate, LocalDateTime endDate){
        List<Appointment> appointments = new ArrayList<>();
        while(!startDate.isAfter(endDate)){
            Appointment a = appointment.copy();
            a.setDateFrom(startDate.withHour(0).withMinute(0));
            a.setDateTo(startDate.withHour(23).withMinute(59));
            appointments.add(appointment);
            startDate = startDate.plusDays(1);
        }

        return appointments;
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
}
