package org.schedule.management.specification.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.schedule.management.specification.adapters.LocalDateTimeAdapter;

import java.io.*;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

@Getter
@Setter

public abstract class ScheduleSpecification {

    private MetaData metaData;
    private List<Appointment> appointments = new ArrayList<>();
    private List<String> headers;

    public abstract void importDataCSV(String file, String config) throws IOException;
    public abstract void importDataJSON(String filePath) throws IOException; // uzme sobe, uzme praznike, meta podaci
    public abstract void exportDataPDF(String fileName, List<Appointment> appointments);
    public abstract  void exportDataJSON(String fileName, List<Appointment> appointments);
    public abstract void exportDataCSV(String fileName, String configPath, List<Appointment> appointments);
    public abstract void exportDataConsole(List<Appointment> appointments);

    //da ima racunar, da ima vise od 10 racunara, da nema racunar
    public List<Appointment> filterEquipment(List<Appointment> appointments, String equipment, int quantity) {
        List<Appointment> group = new ArrayList<>();

        for (Appointment a : appointments) {
            if (quantity == 0 && !(a.getRoom().getEquipment().containsKey(equipment))) {
                group.add(a);
                continue;
            }
            if ((a.getRoom().getEquipment().containsKey(equipment)) &&
                    a.getRoom().getEquipment().get(equipment) >= quantity) {
                group.add(a);
            }
        }

        return group;
    }

    public List<Appointment> filterRelatedData(List<Appointment> appointments, String relatedDataKey, String relatedDataValue) {
        List<Appointment> group = new ArrayList<>();

        for (Appointment a : appointments) {
            if (a.getRelatedData().containsKey(relatedDataKey) && a.getRelatedData().get(relatedDataKey).contains(relatedDataValue)) {
                group.add(a); //related data
            }
        }

        return group;
    }

    public List<Appointment> filterDate(List<Appointment> appointments, LocalDate dateFrom, LocalDate dateTo) {
        List<Appointment> group = new ArrayList<>();

        for (Appointment a : appointments) {
            if (!a.getDateFrom().toLocalDate().isBefore(dateFrom) && !a.getDateTo().toLocalDate().isAfter(dateTo))
                group.add(a);
        }

        return group;
    }

    public List<Appointment> filterCapacity(List<Appointment> appointments, boolean greater, int capacity) {
        List<Appointment> group = new ArrayList<>();

        for (Appointment a : appointments) {
            if (greater && capacity < Integer.parseInt(a.getRoom().getCapacity()))
                group.add(a);
            if (!greater && capacity > Integer.parseInt(a.getRoom().getCapacity()))
                group.add(a);
        }

        return group;
    }

    public List<Appointment> filterByRoom(List<Appointment> appointments, List<String> rooms) {
        List<Appointment> group = new ArrayList<>();

        for (Appointment a : appointments) {
            if (rooms.contains(a.getRoom().getName()))
                group.add(a);
        }

        return group;
    }

    public List<Appointment> filterByReservedAppointments() {
        return appointments;
    }

    public List<Appointment> resetFilter() {
        return appointments;
    }

    public List<Appointment> checkRelatedDataAvailable(List<Appointment> appointments, String relatedDataKey, String relatedDataValue) {

        List<Appointment> filter = filterRelatedData(this.getAppointments(), relatedDataKey, relatedDataValue); // sve gde je neko zauzet
        List<Appointment> group = new ArrayList<>();

        ListIterator<Appointment> i = appointments.listIterator();

        while(i.hasNext()){
            Appointment a = i.next();
            boolean check = false;
            for (Appointment f : filter) {
                if (f.getDateFrom().isAfter(a.getDateFrom()) && f.getDateTo().isBefore(a.getDateTo())) { //ovo je kada se termin profesora nalazi u intervalu dva datuma
                    Appointment pom = new Appointment(a.getRoom(), f.getDateTo(), a.getDateTo());
                    a.setDateTo(f.getDateFrom());
                    i.set(a);
                    i.add(pom);
                    check = true;
                    break;
                }
                if(f.getDateTo().isBefore(a.getDateTo()) && f.getDateFrom().isAfter(a.getDateFrom())){
                    i.remove();
                    break;
                }
                if(f.getDateFrom().isAfter(a.getDateFrom()) && f.getDateTo().isAfter(a.getDateTo())
                        && !f.getDateFrom().isBefore(a.getDateTo())){
                    a.setDateTo(f.getDateFrom());
                    i.set(a);
                    break;
                }
                if(f.getDateFrom().isBefore(a.getDateFrom()) && f.getDateTo().isAfter(a.getDateFrom()) &&
                !f.getDateTo().isAfter(a.getDateTo())){
                    a.setDateFrom(f.getDateTo());
                    i.set(a);
                    break;
                }
            }
            if(check) i.previous();
        }
        return appointments;
    }

    public List<Appointment> searchByAvailableAppointments() {
        List<Appointment> group = new ArrayList<>();
        List<Room> rooms = metaData.getRooms();


        for (Room a : rooms) {
            LocalDate startDateTime = metaData.getScheduleValidFrom();
            LocalDate endDateTime = metaData.getScheduleValidTo();
            LocalTime tmp = metaData.getWorkingHours().get(startDateTime.getDayOfWeek()).getOpeningTime();

            for (Appointment appointment : appointments) {
                LocalTime endWorkingHours = metaData.getWorkingHours().get(startDateTime.getDayOfWeek()).getClosingTime();

                if (!a.equals(appointment.getRoom())) continue;

                if (startDateTime.isBefore(appointment.getDateFrom().toLocalDate())) {
                    LocalDateTime ldt = LocalDateTime.of(startDateTime, tmp);
                    Appointment bla = new Appointment(a, ldt, appointment.getDateFrom());
                    group.add(bla);
                    System.out.println("ispis1" + bla);
                    startDateTime = appointment.getDateTo().toLocalDate();
                } else if (startDateTime.equals(appointment.getDateFrom().toLocalDate()) &&
                        tmp.isBefore(appointment.getDateFrom().toLocalTime())) {
                    LocalDateTime ldt = LocalDateTime.of(startDateTime, tmp);
                    Appointment bla = new Appointment(a, ldt, appointment.getDateFrom());
                    group.add(bla);
                    System.out.println("ispis2" + bla);
                }
                tmp = appointment.getDateTo().toLocalTime();
                if (!tmp.isBefore(endWorkingHours)){
                    startDateTime = startDateTime.plusDays(1);
                    tmp = metaData.getWorkingHours().get(startDateTime.getDayOfWeek()).getOpeningTime();
                }
            }
            if(tmp.isBefore(metaData.getWorkingHours().get(endDateTime.getDayOfWeek()).getClosingTime()) ||
                startDateTime.isBefore(endDateTime)){
                group.add(new Appointment(a, startDateTime.atTime(tmp),
                        endDateTime.atTime(metaData.getWorkingHours().get(endDateTime.getDayOfWeek()).getClosingTime())));
            }

        }
        return group;
    }

    public void importMeta(String metaDataPath) {
        metaData = MetaData.importMeta(metaDataPath);
        System.out.println(metaData);
    }

    public boolean addRoom(String roomName, String capacity, Map<String, Integer> equipment) {
        Room r = new Room(roomName, capacity, equipment);
        if (!metaData.getRooms().contains(r)) {
            metaData.getRooms().add(r);
            return true;
        }
        //TODO: Exception
        return false;
    }

    public boolean addAppointment(Appointment appointment) {
        LocalDateTime start = appointment.getDateFrom();
        LocalDateTime end = appointment.getDateTo();
        if (start.isAfter(end)) return false;
        for (Appointment a : this.getAppointments()) {
            if (!a.getRoom().getName().equals(appointment.getRoom().getName())) continue;
            LocalDateTime aStart = a.getDateFrom();
            LocalDateTime aEnd = a.getDateFrom();
            if (start.isAfter(aStart) && start.isBefore(aEnd) ||
                    end.isBefore(aEnd) && end.isAfter(aStart) ||
                    start.isBefore(aStart) && end.isAfter(aEnd) ||
                    start.isAfter(aStart) && end.isBefore(aEnd)) {
                return false;
            }
        }
        this.appointments.add(appointment);
        return true;
    }

    public boolean deleteAppointment(LocalDateTime from, LocalDateTime to, String roomName) {
        boolean flag = false;
        Appointment removeA = null;
        for (Appointment a : this.appointments) {
            if (a.getDateFrom().equals(from) &&
                    a.getDateTo().equals(to) &&
                    a.getRoom().getName().equals(roomName)) {
                flag = true;
                removeA = a;
                break;
            }
        }
        if (flag) appointments.remove(removeA);
        return flag;
    }

    public boolean rescheduleAppointment(LocalDateTime from, LocalDateTime to, String room, Appointment appointment) {
        return deleteAppointment(from, to, room)
                && addAppointment(appointment);
    }

    protected List<ConfigMapping> importConfig(String configPath) {
        List<ConfigMapping> map = new ArrayList<>();
        int br = 0;
        File file;
        Scanner sc = null;
        try {
            file = new File(configPath);
            sc = new Scanner(file);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] split = line.split(" ", 3);
                ConfigMapping cm = new ConfigMapping(Integer.parseInt(split[0]), split[1], split[2]);
                map.add(cm);
                if (br++ != cm.getIndex()) return null; //TODO BACI EKSEPSNNNNN
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (sc != null) {
                sc.close();
            }
        }
        return map;
    }

}
