package org.schedule.management.specification.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.schedule.management.specification.adapters.LocalDateTimeAdapter;
import org.schedule.management.specification.exceptions.*;

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
    private Set<String> listRelatedData = new HashSet<>();

    /**
     * Imports data from CSV file.
     * @param file FilePath of CSV file
     * @param config ConfigPath of config file for CSV
     * @throws IOException File does not exist
     * @throws InvalidIndexException Import config index does not exist
     * @throws CSVDateNullException Date does not exist in CSV
     * @throws InvalidDateFormatException Date has invalid date format in CSV
     * @throws NotWorkingTimeException Date is not in working time
     */
    public abstract void importDataCSV(String file, String config) throws IOException, InvalidIndexException, CSVDateNullException, InvalidDateFormatException, NotWorkingTimeException;

    /**
     * Imports data from JSON file.
     * @param filePath FilePath for JSON file
     * @throws IOException File does not exist
     */
    public abstract void importDataJSON(String filePath) throws IOException; // uzme sobe, uzme praznike, meta podaci

    /**
     * Exports data to PDF file
     * @param fileName File name for new PDF file
     * @param appointments List of appointments which will be in new PDF file
     */
    public abstract void exportDataPDF(String fileName, List<Appointment> appointments);

    /**
     * Exports data to JSON file
     * @param fileName File name for new JSON file
     * @param appointments List of appointments which will be in new JSON file
     */
    public abstract  void exportDataJSON(String fileName, List<Appointment> appointments);

    /**
     * Exports data to CSV file
     * @param fileName File name for new CSV file
     * @param configPath Config path for new CSV file
     * @param appointments List of appointments which will be in new CSV file
     * @throws InvalidIndexException Import config index does not exist
     * @throws FileNotFoundException Config file not found
     */
    public abstract void exportDataCSV(String fileName, String configPath, List<Appointment> appointments) throws InvalidIndexException, FileNotFoundException;

    /**
     * Exports data to console
     * @param appointments List of appointments which will be printed in console
     */
    public abstract void exportDataConsole(List<Appointment> appointments);

    /**
     * Adds list of appointments
     * @param room Room for appointments
     * @param dateFrom Date from
     * @param dateTo Date to
     * @return Return true if appointment is successfully added
     */
    public abstract boolean addAppointments(Room room, LocalDateTime dateFrom, LocalDateTime dateTo, Map<String, String> relatedData) throws NotWorkingTimeException;
    /**
     * Filter list of appointments by equipment
     * @param appointments List of appointments which will be filtered
     * @param equipment Equipment name
     * @param quantity Equipment quantity
     * @return Returns new list of filtered appointments
     */

    public List<Appointment> filterEquipment(List<Appointment> appointments, String equipment, int quantity) {
        List<Appointment> group = new ArrayList<>();

        for (Appointment a : appointments) {
            if (quantity == 0 && !a.getRoom().getEquipment().containsKey(equipment)) {
                group.add(a);
                continue;
            }
            if ((a.getRoom().getEquipment().containsKey(equipment)) &&
                    a.getRoom().getEquipment().get(equipment) >= quantity && quantity > 0) {
                group.add(a);
            }
        }

        return group;
    }

    /**
     * Filter list of appointments by related data
     * @param appointments List of appointments which will be filtered
     * @param relatedDataKey Related data key
     * @param relatedDataValue Related data value
     * @return Returns new list of filtered appointments
     */
    public List<Appointment> filterRelatedData(List<Appointment> appointments, String relatedDataKey, String relatedDataValue) {
        List<Appointment> group = new ArrayList<>();

        for (Appointment a : appointments) {
            if (a.getRelatedData().containsKey(relatedDataKey) && a.getRelatedData().get(relatedDataKey).contains(relatedDataValue)) {
                group.add(a); //related data
            }
        }

        return group;
    }

    /**
     * Filter list of appointments by date
     * @param appointments List of appointments which will be filtered
     * @param dateFrom Date from
     * @param dateTo Date to
     * @return Returns new list of filtered appointments
     */
    public List<Appointment> filterDate(List<Appointment> appointments, LocalDate dateFrom, LocalDate dateTo) {
        List<Appointment> group = new ArrayList<>();

        for (Appointment a : appointments) {
            if (!a.getDateFrom().toLocalDate().isBefore(dateFrom) && !a.getDateTo().toLocalDate().isAfter(dateTo))
                group.add(a);
        }

        return group;
    }

    /**
     * Filter list of appointments by room capacity
     * @param appointments List of appointments which will be filtered
     * @param greater If greater is true check if appointment room capacity is greater than capacity param
     * @param capacity Capacity param
     * @return Returns new list of filtered appointments
     */
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

    /**
     * Filter list of appointments by room
     * @param appointments List of appointments which will be filtered
     * @param rooms Room as parameter for filter
     * @return Returns new list of filtered appointments
     */
    public List<Appointment> filterByRoom(List<Appointment> appointments, Collection<Room> rooms) {
        List<Appointment> group = new ArrayList<>();

        for (Appointment a : appointments) {
            if (rooms.contains(a.getRoom()))
                group.add(a);
        }

        return group;
    }

    /**
     * Get reserved filtered data
     * @return Returns filtered data
     */
    public List<Appointment> filterByReservedAppointments() {
        return appointments;
    }

    /**
     * Reset applied filters
     * @return Returns original list
     */
    public List<Appointment> resetFilter() {
        return appointments;
    }

    /**
     * Filter list by related data for available appointments
     * @param appointments List of appointments which will be filtered
     * @param relatedDataKey Related data key
     * @param relatedDataValue Related data value
     * @return Returns new list of filtered appointments
     */
    public List<Appointment> checkRelatedDataAvailable(List<Appointment> appointments, String relatedDataKey, String relatedDataValue) {

        List<Appointment> filter = filterRelatedData(this.getAppointments(), relatedDataKey, relatedDataValue); // sve gde je neko zauzet

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
                if(!f.getDateTo().isBefore(a.getDateTo()) && !f.getDateFrom().isAfter(a.getDateFrom())){ // ako uzima ceo interval onda brisemo taj intervalo
                    i.remove();
                    break;
                }
                if(f.getDateFrom().isAfter(a.getDateFrom()) && f.getDateTo().isAfter(a.getDateTo())
                        && !f.getDateFrom().isAfter(a.getDateTo())){
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

    /**
     * Get available appointments
     * @return Returns all available appointments
     */
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
                    startDateTime = appointment.getDateTo().toLocalDate();
                } else if (startDateTime.equals(appointment.getDateFrom().toLocalDate()) &&
                        tmp.isBefore(appointment.getDateFrom().toLocalTime())) {
                    LocalDateTime ldt = LocalDateTime.of(startDateTime, tmp);
                    Appointment bla = new Appointment(a, ldt, appointment.getDateFrom());
                    group.add(bla);
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

    /**
     * Imports meta data
     * @param metaDataPath Meta data path
     */
    public void importMeta(String metaDataPath) {
        metaData = MetaData.importMeta(metaDataPath);
    }

    /**
     * Add new room
     * @param roomName Room name
     * @param capacity Room capacity
     * @param equipment Room equipment
     * @return Returns true if room is added successfully
     * @throws SameRoomNameException Return exception if that rooms exists
     */
    public boolean addRoom(String roomName, String capacity, Map<String, Integer> equipment) throws SameRoomNameException {
        Room r = new Room(roomName, capacity, equipment);
        for(Room room : metaData.getRooms()){
            if(room.getName().equalsIgnoreCase(r.getName()))
                throw new SameRoomNameException();
                return false;
        }

        metaData.getRooms().add(r);
        return true;
    }

    /**
     * Adds new appointment
     * @param appointment New appointment
     * @return Returns true if adding new appointment is possible
     */

    public boolean addAppointment(Appointment appointment) {
        LocalDateTime start = appointment.getDateFrom();
        LocalDateTime end = appointment.getDateTo();
        if (start.isAfter(end)) return false;
        for (Appointment a : this.getAppointments()) {
            if (!a.getRoom().getName().equals(appointment.getRoom().getName())) continue;
            LocalDateTime aStart = a.getDateFrom();
            LocalDateTime aEnd = a.getDateTo();
            if ((!start.isBefore(aStart) && !start.isAfter(aEnd)) ||
                (!end.isAfter(aEnd) && !end.isBefore(aStart)) ||
                (start.isBefore(aStart) && end.isAfter(aEnd)) ||
                (start.isAfter(aStart) && end.isBefore(aEnd))) {
                return false;
            }
        }
        this.appointments.add(appointment);
        return true;
    }

    /**
     * Reschedules appointment
     * @param appointment New appointment
     * @param old Old appointment
     * @return Returns true if rescheduling is possible
     */
    public boolean rescheduleAppointment(Appointment appointment, Appointment old) {
        getAppointments().remove(old);

        if(addAppointment(appointment))
            return true;

        getAppointments().add(old);
        return false;
    }

    /**
     * Imports config
     * @param configPath Path to config
     * @return Returns ConfigMap
     * @throws FileNotFoundException Throws error if file not found
     * @throws InvalidIndexException Throws error if index is invalid
     */
    protected List<ConfigMapping> importConfig(String configPath) throws FileNotFoundException, InvalidIndexException {
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
                if (br++ != cm.getIndex()) throw new InvalidIndexException();
            }

        }finally {
            if (sc != null) {
                sc.close();
            }
        }
        return map;
    }

}
