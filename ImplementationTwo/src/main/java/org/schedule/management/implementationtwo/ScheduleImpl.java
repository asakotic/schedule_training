package org.schedule.management.implementationtwo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.text.*;

import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.schedule.management.specification.adapters.LocalDateTimeAdapter;
import org.schedule.management.specification.exceptions.CSVDateNullException;
import org.schedule.management.specification.exceptions.InvalidDateFormatException;
import org.schedule.management.specification.exceptions.InvalidIndexException;
import org.schedule.management.specification.exceptions.NotWorkingTimeException;
import org.schedule.management.specification.models.Appointment;
import org.schedule.management.specification.models.ConfigMapping;
import org.schedule.management.specification.models.Room;
import org.schedule.management.specification.models.ScheduleSpecification;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class ScheduleImpl extends ScheduleSpecification {
    @Override
    public void importDataCSV(String filePath, String configPath) throws IOException, InvalidIndexException, CSVDateNullException, NotWorkingTimeException {
        List<ConfigMapping> configMap = importConfig(configPath);
        FileReader fr = new FileReader(filePath);
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
                    case "relatedData" -> {ap.getRelatedData().put(userLbl, i.get(index)); getListRelatedData().add(userLbl);}
                    case "day" -> ap.setDay(DayOfWeek.valueOf(i.get(index)));
                }
            }
            if(startDateTime == null || endDateTime == null || ap.getDay() ==null) throw new CSVDateNullException();

            LocalTime startWorkingtime = this.getMetaData().getWorkingHours().get(startDateTime.getDayOfWeek()).getOpeningTime();
            LocalTime endWorkingTime = this.getMetaData().getWorkingHours().get(startDateTime.getDayOfWeek()).getClosingTime();

            if(startDateTime.toLocalTime().isBefore(startWorkingtime) ||
                    endDateTime.toLocalTime().isAfter(endWorkingTime)) {
                throw new NotWorkingTimeException();
            }

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
                if(!this.getMetaData().getHolidays().contains(a.getDateFrom().toLocalDate().toString()))
                    this.getAppointments().add(a);
                startDateTime = startDateTime.plusDays(7);
            }
        }
        System.out.println(getAppointments());
    }

    public List<Appointment> degroup(List<Appointment> appointments){
        List<Appointment> group = new ArrayList<>();

        for(Appointment a : appointments){
            LocalDateTime lcd1 = a.getDateFrom();
            LocalDateTime lcd2 = a.getDateTo();
            while(lcd1.isBefore(lcd2)){
                Appointment pom = a.copy();
                pom.setDateFrom(lcd1);
                pom.setDateTo(lcd1.toLocalDate().atTime(lcd2.toLocalTime()));
                group.add(pom);
                lcd1 = lcd1.plusDays(7);
            }
        }
        return group;
    }

    @Override
    public void importDataJSON(String filePath) throws IOException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();

        List<Appointment> appointments;
        try (FileReader fr = new FileReader(filePath)) {
            appointments = gson.fromJson(fr, new TypeToken<List<Appointment>>(){}.getType());
        }

        appointments.sort(Appointment::compareTo);


        getAppointments().addAll(degroup(appointments));
        System.out.println(getAppointments());
    }

    private boolean checkSameAppointments(Appointment a, Appointment b){
        return a.getRoom().equals(b.getRoom()) && a.getDateFrom().toLocalTime().equals(b.getDateFrom().toLocalTime())
                && a.getDateTo().toLocalTime().equals(b.getDateTo().toLocalTime()) &&
                a.getRelatedData().equals(b.getRelatedData()) && a.getDay().equals(b.getDay()) &&
                (Duration.between(a.getDateTo(), b.getDateFrom()).toDays() <= 7 && Duration.between(a.getDateTo(), b.getDateFrom()).toDays() > 0);
    }

    private void checkGroup(List<Appointment> group, Appointment a){
        for(Appointment appointment : group){
            if(checkSameAppointments(appointment, a)){
              appointment.setDateTo(a.getDateTo());
              return;
            }
        }
        group.add(a.copy());
    }

    private List<Appointment> createGroup(List<Appointment> appointments){
        List<Appointment> group = new ArrayList<>();
        for (Appointment a : appointments) checkGroup(group, a.copy());
        return group;
    }

    @Override
    public void exportDataPDF(String fileName, List<Appointment> appointments) {
        try{
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));

            document.open();
            Font font = FontFactory.getFont(FontFactory.TIMES, 9, BaseColor.BLACK);
            List<Appointment> group = createGroup(appointments);
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
    public void exportDataJSON(String fileName, List<Appointment> appointments) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
        try (PrintStream writer = new PrintStream(fileName)) {
            gson.toJson(createGroup(appointments), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportDataCSV(String fileName, String configPath, List<Appointment> appointments) throws InvalidIndexException, FileNotFoundException {

        List<ConfigMapping> configMap = importConfig(configPath);
        configMap.sort(Comparator.comparingInt(ConfigMapping::getIndex));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(this.getMetaData().getDateFormat());
        FileWriter fileWriter = null;
        CSVPrinter csvPrinter = null;
        appointments.sort(Appointment::compareTo);
        try {
            fileWriter = new FileWriter(fileName);
            csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
            for (Appointment appointment : createGroup(appointments)) {

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
        } finally {
            try {
                csvPrinter.close();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void exportDataConsole(List<Appointment> appointments) {
        System.out.println(createGroup(appointments));
    }

}
