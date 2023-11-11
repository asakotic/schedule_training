package org.schedule.management.implementationone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.schedule.management.specification.adapters.LocalDateTimeAdapter;
import org.schedule.management.specification.models.Appointment;
import org.schedule.management.specification.models.ConfigMapping;
import org.schedule.management.specification.models.Room;
import org.schedule.management.specification.models.ScheduleSpecification;

import java.io.*;
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
                    case "room" -> {
                        List<Room> rooms = this.getMetaData().getRooms();
                        for (Room r : rooms) {
                            if (r.getName().equals(i.get(index))) {
                                ap.setRoom(r);
                            }
                        }
                    }
                    case "startDate" -> {
                        startDateTime = LocalDateTime.parse(i.get(index), formatter);
                        ap.setDay(startDateTime.getDayOfWeek());
                    }
                    case "endDate" -> endDateTime = LocalDateTime.parse(i.get(index), formatter);
                    case "relatedData" -> ap.getRelatedData().put(userLbl, i.get(index));
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
                this.addAppointment(ap);
            }else{
                ap.setDateFrom(startDateTime);
                startDateTime = startDateTime.withHour(endWorkingTime.getHour()).withMinute(endWorkingTime.getMinute());
                ap.setDateTo(startDateTime);
                this.addAppointment(ap);

                Appointment a = ap.copy();
                makeMore(ap,startDateTime.plusDays(1),endDateTime.minusDays(1));

                Appointment b = ap.copy();
                startWorkingtime = this.getMetaData().getWorkingHours().get(endDateTime.getDayOfWeek()).getOpeningTime();
                b.setDateFrom(endDateTime.withHour(startWorkingtime.getHour()).withMinute(startWorkingtime.getMinute()));
                b.setDateTo(endDateTime);
                b.setDay(endDateTime.getDayOfWeek());
                this.addAppointment(b);
            }
        }
        this.getAppointments().sort(Appointment::compareTo);
    }
    private void makeMore(Appointment appointment,LocalDateTime startDate, LocalDateTime endDate){
        LocalTime startWorkingtime = this.getMetaData().getWorkingHours().get(startDate.getDayOfWeek()).getOpeningTime();
        LocalTime endWorkingTime = this.getMetaData().getWorkingHours().get(startDate.getDayOfWeek()).getClosingTime();
        while(!startDate.isAfter(endDate)){
            Appointment a = appointment.copy();
            a.setDateFrom(startDate.withHour(startWorkingtime.getHour()).withMinute(startWorkingtime.getMinute()));
            a.setDateTo(startDate.withHour(endWorkingTime.getHour()).withMinute(endWorkingTime.getMinute()));
            a.setDay(startDate.getDayOfWeek());
            addAppointment(appointment);
            startDate = startDate.plusDays(1);
            startWorkingtime = this.getMetaData().getWorkingHours().get(startDate.getDayOfWeek()).getOpeningTime();
            endWorkingTime = this.getMetaData().getWorkingHours().get(startDate.getDayOfWeek()).getClosingTime();
        }
    }
    @Override
    public void exportDataPDF(String fileName) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Info o rasporedu:
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 600);
                contentStream.showText("Schedule Information");
                contentStream.setFont(PDType1Font.HELVETICA, 8);
                contentStream.newLineAtOffset(0, -20);

                String header = "Room, Date, From , To, Other";
                contentStream.showText(header);
                contentStream.newLineAtOffset(0, -20);
                for (Appointment appointment : getAppointments()) {
                    String scheduleData = appointment.getRoom().getName() + ", "
                            + appointment.getDateFrom().toLocalDate() + ", "
                            + appointment.getDateFrom().toLocalTime() + " - "
                            + appointment.getDateTo().toLocalTime() + " "
                            + appointment.getRelatedData();

                    contentStream.showText(scheduleData);
                    contentStream.newLineAtOffset(0, -15);
                }

                // Info about rooms
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
                contentStream.newLineAtOffset(0, -30);
                contentStream.showText("Room Information");
                contentStream.setFont(PDType1Font.HELVETICA, 8);
                contentStream.newLineAtOffset(0, -20);

                String roomHeader = "Name, Capacity";
                contentStream.showText(roomHeader);
                contentStream.newLineAtOffset(0, -20);
                for (Room room : getMetaData().getRooms()) {
                    String roomData = room.getName() + ", "
                            + room.getCapacity() + "  "
                            + "seats";
                    System.out.println(roomData);
                    contentStream.showText(roomData);
                    contentStream.newLineAtOffset(0, -15);
                }

            }
            document.save(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void importDataJSON() throws IOException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();

        List<Appointment> appointments;
        try (FileReader fr = new FileReader("1.json")) {
            appointments = gson.fromJson(fr, new TypeToken<List<Appointment>>(){}.getType());
        }
        getAppointments().addAll(appointments);
    }

}
