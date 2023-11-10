package org.schedule.management.implementationone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.schedule.management.specification.models.Appointment;
import org.schedule.management.specification.models.ConfigMapping;
import org.schedule.management.specification.models.Room;
import org.schedule.management.specification.models.ScheduleSpecification;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
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

    public void exportDataCSV(String fileName, String configpath){

        List<ConfigMapping> configMap = importConfig(configpath);
        configMap.sort(Comparator.comparingInt(ConfigMapping::getIndex));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(this.getMetaData().getDateFormat());
        FileWriter fileWriter = null;
        CSVPrinter csvPrinter = null;
        //appointments.sort(Appointment::compareTo);
        try {
            fileWriter = new FileWriter(fileName);
            csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
            // csvPrinter.printRecord(headers);ubaci glupi heder
            for (Appointment appointment : this.getAppointments()) {

                List<String> toAdd = new ArrayList<>();
                for (ConfigMapping row : configMap) {
                    String userLbl = row.getUserLabel();

                    switch (row.getPrimaryLabel()) {
                        case "room" -> toAdd.add(appointment.getRoom().getName());
                        case "startDate" -> toAdd.add(appointment.getDateFrom().format(formatter));
                        case "endDate" -> toAdd.add(appointment.getDateTo().format(formatter));
                        case "relatedData" -> toAdd.add(appointment.getRelatedData().get(userLbl));
                        case "day" -> toAdd.add(appointment.getDay().toString());
                    }
                }
                csvPrinter.printRecord(toAdd);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                csvPrinter.close();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void exportDataJSON(List<Appointment> appointments, String fileName){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (PrintStream writer = new PrintStream(fileName)) {
            gson.toJson(appointments, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void importDataJSON() {

    }

    @Override
    public void exportDataPDF(String fileName) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Export Student Information
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 600);
                contentStream.showText("Schedule Information");
                //contentStream.setFont(PDType1Font.TIMES_ROMAN, 12);
                contentStream.newLineAtOffset(0, -20);
                //contentStream.beginText();
                String headers = "Name, Surname, Group, Index";
                for (Appointment appointment : getAppointments()) {
                    String scheduleData = appointment.getRoom().getName() + ", "
                            + appointment.getDateFrom() + ", "
                            + appointment.getDateTo() + ", "
                            + appointment.getRelatedData();
                    contentStream.showText(scheduleData);
                    contentStream.newLineAtOffset(0, -15);
                }

                // Export Room Information
                //contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(0, -30);
                contentStream.showText("Room Information");
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(0, -20);
                ///contentStream.beginText();
                String roomHeaders = "Name, Capacity, Other";
                for (Room room : getMetaData().getRooms()) {
                    String roomData = room.getName() + ", " + room.getCapacity() + ", " + room.getEquipment();
                    //contentStream.showText(roomData);
                    contentStream.newLineAtOffset(0, -15);
                }

            }
            document.save("p.pdf");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
