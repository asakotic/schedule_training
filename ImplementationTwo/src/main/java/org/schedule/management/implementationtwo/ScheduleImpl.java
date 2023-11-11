package org.schedule.management.implementationtwo;

import com.itextpdf.text.*;

import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.schedule.management.specification.models.Appointment;
import org.schedule.management.specification.models.ConfigMapping;
import org.schedule.management.specification.models.Room;
import org.schedule.management.specification.models.ScheduleSpecification;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
                    case "room" -> {
                        List<Room> rooms = this.getMetaData().getRooms();
                        for (Room r : rooms) {
                            if (r.getName().equals(i.get(index))) {
                                ap.setRoom(r);
                            }
                        }
                    }
                    case "startDate" -> startDateTime = LocalDateTime.parse(i.get(index), formatter);
                    case "endDate" -> endDateTime = LocalDateTime.parse(i.get(index), formatter);
                    case "relatedData" -> ap.getRelatedData().put(userLbl, i.get(index));
                    case "day" -> ap.setDay(DayOfWeek.valueOf(i.get(index)));
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

    private boolean checkSameAppointments(Appointment a, Appointment b){
        return a.getRoom() == b.getRoom() && a.getDateFrom().toLocalTime() == b.getDateFrom().toLocalTime()
                && a.getDateTo().toLocalTime() == b.getDateTo().toLocalTime();
    }

    private void checkGroup(List<Appointment> group, Appointment a){
        for(Appointment appointment : group){
            if(checkSameAppointments(appointment, a)){
              appointment.setDateTo(a.getDateTo());
              return;
            }
        }
        group.add(a);
    }

    private List<Appointment> createGroup(){
        List<Appointment> group = new ArrayList<>();
        for (Appointment a : this.getAppointments()) checkGroup(group, a);
        return group;
    }

    @Override
    public void exportDataPDF(String fileName) {
        try{
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));

            document.open();
            Font font = FontFactory.getFont(FontFactory.TIMES, 9, BaseColor.BLACK);
            List<Appointment> group = createGroup();
            document.add(new Paragraph("Schedule Information", FontFactory.getFont(FontFactory.TIMES, 31, BaseColor.BLACK)));

            for(Appointment appointment : group){
                StringBuilder sb = new StringBuilder();
                sb.append(appointment.getRoom().getName());
                sb.append(" ");
                sb.append(appointment.getDateFrom().toLocalDate());
                sb.append(" ");
                sb.append(appointment.getDateTo().toLocalDate());
                sb.append(" ");
                sb.append(appointment.getDateFrom().toLocalTime());
                sb.append("-");
                sb.append(appointment.getDateTo().toLocalTime());
                sb.append(" ");
                sb.append(appointment.getRelatedData());
                sb.append(" ");
                Chunk chunk = new Chunk(sb.toString(), font);
                document.add(chunk);
                document.add(new Paragraph("\n"));
            }

            document.close();
        }catch (Exception e){
            System.out.println(e);
        }

    }
}
