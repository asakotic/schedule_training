import org.schedule.management.specification.exceptions.*;
import org.schedule.management.specification.models.Appointment;
import org.schedule.management.specification.models.Manager;
import org.schedule.management.specification.models.Room;
import org.schedule.management.specification.models.ScheduleSpecification;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
public class TestMain {
    public static void main(String[] args) throws IOException, InvalidDateFormatException, NotWorkingTimeException {

        try {
            Class.forName("org.schedule.management.implementationtwo.ScheduleImpl");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        ScheduleSpecification ss = Manager.getScheduleSpecification();

        String configPath = "";
        String filePath = "";
        String metaDataPath = "";

        Scanner reader = new Scanner(System.in);  // Reading from System.in
        System.out.println("Please enter path of your MetaData file: ");
        metaDataPath = reader.nextLine();
        ss.importMeta(metaDataPath);

        System.out.println("Which type of file do you have: \n1. JSON \n2. CSV \nEnter number: ");
        int fileType = reader.nextInt();

        if(fileType == 2){
            System.out.println("You picked CSV, please enter path of your config file: ");
            reader.nextLine();
            configPath = reader.nextLine();
            System.out.println("Please enter path of your CSV file: ");
            filePath = reader.nextLine();
            try {
                ss.importDataCSV(filePath, configPath);
            } catch (InvalidIndexException | CSVDateNullException | InvalidDateFormatException | NotWorkingTimeException e) {
                throw new RuntimeException(e);
            }
        }else{
            System.out.println("You picked JSON, please enter path of your JSON file: ");
            reader.nextLine();
            filePath = reader.nextLine();
            ss.importDataJSON(filePath);
        }

        List<Appointment> appointmentList = new ArrayList<>(ss.getAppointments());

        while(true){
            //1. Export(csv, json, pdf, konzola) //2. Exit //3. Search //4. Add Room//5. Add App//6. Delete app//7. Res app
            printCommands();

            System.out.print("Enter your command: ");
            String command = reader.nextLine();

            switch (command){
                case "1":
                    System.out.println("In which file type do you want to export table?\n" +
                            "1. JSON\n2. CSV\n3. PDF\n4. CONSOLE WITH FILTERS\n5. CONSOLE WITHOUT FILTERS");
                    command = reader.nextLine();

                    switch (command) {
                        case "1" -> ss.exportDataJSON("1.json", ss.getAppointments()); // 2. impl 1. impl
                        case "2" -> {
                            try {
                                ss.exportDataCSV("1.csv", configPath, ss.getAppointments());
                            } catch (InvalidIndexException e) {
                                throw new RuntimeException(e);
                            }
                        } //2. impl 1. impl
                        case "3" -> ss.exportDataPDF("1.pdf", ss.getAppointments()); //2. impl 1. impl
                        case "4" -> ss.exportDataConsole(ss.getAppointments());
                        case "5" -> ss.exportDataConsole(ss.getAppointments());
                        default -> {
                        }
                    }
                    break;
                case "2":
                    while(true){
                        System.out.println("Please enter filter type from list below");
                        System.out.println("1. Filter by room");
                        System.out.println("2. Filter by capacity");
                        System.out.println("3. Filter by date");
                        System.out.println("4. Filter by related data");
                        System.out.println("5. Filter by equipment");
                        System.out.println("6. Reset filter");
                        System.out.println("7. Exit");

                        System.out.print("Your command: ");
                        String inputCommand = reader.nextLine();
                        boolean breakB = false;

                        switch (inputCommand){
                            case "1":
                                Set<Room> rooms = new HashSet<>();

                                while(true){
                                    System.out.println("Please select room from list below. Only write room name!");
                                    for(Room r : ss.getMetaData().getRooms()){
                                        System.out.println("Room name: " + r.getName() + ", capacity: " + r.getCapacity());
                                    }
                                    System.out.print("Enter room, write exit to stop adding rooms: ");
                                    String room = reader.nextLine();

                                    if(room.equalsIgnoreCase("exit"))
                                        break;

                                    Room send = null;

                                    for(Room r : ss.getMetaData().getRooms()){
                                        if(r.getName().equalsIgnoreCase(room)) {
                                            send = r;
                                            break;
                                        }
                                    }
                                    if(send == null){
                                        System.out.println("You did not enter valid room!");
                                        continue;
                                    }

                                    rooms.add(send);
                                }

                                appointmentList = ss.filterByRoom(appointmentList, rooms);
                                break;
                            case "2":
                                System.out.print("Enter capacity number: ");
                                String capacity = reader.nextLine();

                                System.out.print("Do you want more or less then your number (Greater or Less)? ");
                                String greater = reader.nextLine();

                                if(greater.equalsIgnoreCase("Greater"))
                                    appointmentList = ss.filterCapacity(appointmentList, true, Integer.parseInt(capacity));
                                else
                                    appointmentList = ss.filterCapacity(appointmentList, false, Integer.parseInt(capacity));

                                break;
                            case "3":
                                System.out.print("Enter date from (YYYY-MM-DD): ");
                                String d1 = reader.nextLine();

                                LocalDate localDateFrom = LocalDate.parse(d1);

                                System.out.print("Enter date to (YYYY-MM-DD): ");
                                String d2 = reader.nextLine();

                                LocalDate localDateTo = LocalDate.parse(d2);

                                appointmentList = ss.filterDate(appointmentList, localDateFrom, localDateTo);

                                break;
                            case "4":
                                while(true){
                                    System.out.println("Please enter related data from list below");
                                    int i = 0;
                                    for(String s : ss.getListRelatedData()){
                                        System.out.println(i++ + ". " + s);
                                    }
                                    System.out.print("Enter command number, or exit to end: ");
                                    String input = reader.nextLine();

                                    if(input.equalsIgnoreCase("exit"))
                                        break;

                                    System.out.print("Enter value for " + ss.getListRelatedData().toArray()[Integer.parseInt(input)] +": ");
                                    String input2 = reader.nextLine();

                                    appointmentList = ss.filterRelatedData(appointmentList, (String) ss.getListRelatedData().toArray()[Integer.parseInt(input)],
                                            input2);

                                }

                                break;
                            case "5":
                                System.out.print("Please enter equipment name: ");
                                String equipmentName = reader.nextLine();
                                System.out.print("Please enter quantity, type 0 if you want rooms without that specific equipment: ");
                                String equipmentQuantity = reader.nextLine();
                                appointmentList = ss.filterEquipment(appointmentList, equipmentName, Integer.parseInt(equipmentQuantity));
                                break;
                            case "6":
                                appointmentList = ss.resetFilter();
                                break;
                            case "7":
                                breakB = true;
                                break;

                        }

                        if(breakB) break;
                    }
                    break;
                case "3":
                    appointmentList = ss.searchByAvailableAppointments();
                    while(true) {
                        System.out.println("Please enter filter type from list below");
                        System.out.println("1. Filter by room");
                        System.out.println("2. Filter by capacity");
                        System.out.println("3. Filter by date");
                        System.out.println("4. Filter by related data");
                        System.out.println("5. Filter by equipment");
                        System.out.println("6. Reset filter");
                        System.out.println("7. Exit");

                        System.out.print("Your command: ");
                        String inputCommand = reader.nextLine();
                        boolean breakB = false;

                        switch (inputCommand) {
                            case "1":
                                Set<Room> rooms = new HashSet<>();

                                while (true) {
                                    System.out.println("Please select room from list below. Only write room name!");
                                    for (Room r : ss.getMetaData().getRooms()) {
                                        System.out.println("Room name: " + r.getName() + ", capacity: " + r.getCapacity());
                                    }
                                    System.out.print("Enter room, write exit to stop adding rooms: ");
                                    String room = reader.nextLine();

                                    if (room.equalsIgnoreCase("exit"))
                                        break;

                                    Room send = null;

                                    for (Room r : ss.getMetaData().getRooms()) {
                                        if (r.getName().equalsIgnoreCase(room)) {
                                            send = r;
                                            break;
                                        }
                                    }
                                    if (send == null) {
                                        System.out.println("You did not enter valid room!");
                                        continue;
                                    }

                                    rooms.add(send);
                                }

                                appointmentList = ss.filterByRoom(appointmentList, rooms);

                                break;
                            case "2":
                                System.out.print("Enter capacity number: ");
                                String capacity = reader.nextLine();

                                System.out.print("Do you want more or less then your number (Greater or Less)? ");
                                String greater = reader.nextLine();

                                if (greater.equalsIgnoreCase("Greater"))
                                    appointmentList = ss.filterCapacity(appointmentList, true, Integer.parseInt(capacity));
                                else
                                    appointmentList = ss.filterCapacity(appointmentList, false, Integer.parseInt(capacity));
                                break;
                            case "3":
                                System.out.print("Enter date from (YYYY-MM-DD): ");
                                String d1 = reader.nextLine();

                                LocalDate localDateFrom = LocalDate.parse(d1);

                                System.out.print("Enter date to (YYYY-MM-DD): ");
                                String d2 = reader.nextLine();

                                LocalDate localDateTo = LocalDate.parse(d2);

                                appointmentList = ss.filterDate(appointmentList, localDateFrom, localDateTo);

                                break;
                            case "4":
                                while (true) {
                                    System.out.println("Please enter related data from list below");
                                    int i = 0;
                                    for (String s : ss.getListRelatedData()) {
                                        System.out.println(i++ + ". " + s);
                                    }
                                    System.out.print("Enter command number, or exit to end: ");
                                    String input = reader.nextLine();

                                    if (input.equalsIgnoreCase("exit"))
                                        break;

                                    System.out.print("Enter value for " + ss.getListRelatedData().toArray()[Integer.parseInt(input)] + ": ");
                                    String input2 = reader.nextLine();

                                    appointmentList = ss.checkRelatedDataAvailable(appointmentList, (String) ss.getListRelatedData().toArray()[Integer.parseInt(input)],
                                            input2);
                                }
                                break;
                            case "5":
                                System.out.print("Please enter equipment name: ");
                                String equipmentName = reader.nextLine();
                                System.out.print("Please enter quantity, type 0 if you want rooms without that specific equipment: ");
                                String equipmentQuantity = reader.nextLine();
                                appointmentList = ss.filterEquipment(appointmentList, equipmentName, Integer.parseInt(equipmentQuantity));
                                break;
                            case "6":
                                appointmentList = ss.resetFilter();
                                break;
                            case "7":
                                breakB = true;
                                break;

                        }

                        if (breakB) break;
                    }
                    break;
                case "4":
                    System.out.print("Please enter room name: ");
                    String roomName = reader.nextLine();
                    System.out.print("Please enter room capacity: ");
                    int capacity = reader.nextInt();
                    Map<String, Integer> equipment = new HashMap<>();

                    System.out.print("Please enter new equipment name, or type exit for end: ");
                    reader.nextLine();
                    String equipmentName = reader.nextLine();

                    while(!equipmentName.equalsIgnoreCase("exit")){
                        System.out.print("Please enter equipment amount: ");
                        int equipmentAmount = reader.nextInt();

                        if(equipment.containsKey(equipmentName)){
                            System.out.print("You already entered that equipment! Do you want to change amount? Current amount is " + equipment.get(equipmentName) + ". ");
                            reader.nextLine();
                            String info = reader.nextLine();
                            if(info.equalsIgnoreCase("Yes")){
                                System.out.print("Enter new amount: ");
                                int a = reader.nextInt();
                                equipment.put(equipmentName, a);
                            }
                        }else{
                            equipment.put(equipmentName, equipmentAmount);
                        }
                        System.out.print("Please enter new equipment name, or type exit for end: ");
                        reader.nextLine();
                        equipmentName = reader.nextLine();
                    }

                    try {
                        if(!ss.addRoom(roomName, String.valueOf(capacity), equipment)) System.out.println("This room already exists!");
                        else System.out.println("You added new room!");
                    } catch (SameRoomNameException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "5":
                    System.out.println("Please select room from list below. Only write room name!");
                    for(Room r : ss.getMetaData().getRooms()){
                        System.out.println("Room name: " + r.getName() + ", capacity: " + r.getCapacity());
                    }
                    System.out.print("Enter room: ");
                    String room = reader.nextLine();

                    Room send = null;

                    for(Room r : ss.getMetaData().getRooms()){
                        if(r.getName().equalsIgnoreCase(room)) {
                            send = r;
                            break;
                        }
                    }
                    if(send == null){
                        System.out.println("You did not enter valid room!");
                        break;
                    }
                    System.out.print("Enter date from (YYYY-MM-DDTHH:MM): ");
                    String d1 = reader.nextLine();

                    LocalDateTime localDateTimeFrom = LocalDateTime.parse(d1);

                    System.out.print("Enter date to (YYYY-MM-DDTHH:MM): ");
                    String d2 = reader.nextLine();

                    LocalDateTime localDateTimeTo = LocalDateTime.parse(d2);

                    if(!localDateTimeFrom.toLocalDate().equals(localDateTimeTo.toLocalDate())){
                        System.out.println("You can only add one appointment at same date!");
                        break;
                    }

                    Map<String,String> related = new HashMap<>();

                    for(String relatedData: ss.getListRelatedData()){
                        System.out.print("Do you want to add " + relatedData+"? ");
                        String answer = reader.nextLine();
                        if(answer.equalsIgnoreCase("Yes")){
                            System.out.print("Type value for " + relatedData +": ");
                            answer = reader.nextLine();
                            related.put(relatedData, answer);
                        }
                    }

                    if(ss.addAppointment(new Appointment(localDateTimeFrom.getDayOfWeek(),
                            send, related, localDateTimeFrom, localDateTimeTo)))
                        System.out.println("You added new appointment!");
                    else System.out.println("This appointment already exists!");
                    break;
                case "6":
                    System.out.println("Please select room from list below. Only write room name!");
                    for(Room r : ss.getMetaData().getRooms()){
                        System.out.println("Room name: " + r.getName() + ", capacity: " + r.getCapacity());
                    }
                    System.out.print("Enter room: ");
                    room = reader.nextLine();

                    send = null;

                    for(Room r : ss.getMetaData().getRooms()){
                        if(r.getName().equalsIgnoreCase(room)) {
                            send = r;
                            break;
                        }
                    }
                    if(send == null){
                        System.out.println("You did not enter valid room!");
                        break;
                    }
                    System.out.print("Enter date from (YYYY-MM-DDTHH:MM): ");
                    d1 = reader.nextLine();

                    localDateTimeFrom = LocalDateTime.parse(d1);

                    System.out.print("Enter date to (YYYY-MM-DDTHH:MM): ");
                    d2 = reader.nextLine();

                    localDateTimeTo = LocalDateTime.parse(d2);

                    if(!localDateTimeFrom.isBefore(localDateTimeTo))
                        throw new InvalidDateFormatException();

                    related = new HashMap<>();

                    for(String relatedData: ss.getListRelatedData()){
                        System.out.print("Do you want to add " + relatedData+"? ");
                        String answer = reader.nextLine();
                        if(answer.equalsIgnoreCase("Yes")){
                            System.out.print("Type value for " + relatedData +": ");
                            answer = reader.nextLine();
                            related.put(relatedData, answer);
                        }
                    }

                    if(!ss.addAppointments(send, localDateTimeFrom, localDateTimeTo, related)){
                        System.out.println("This appointments are not available!");
                    }

                    break;
                case "7":

                    System.out.println("ID / RoomName / relatedData / dateFrom / DateTo");
                    for(Appointment a : ss.getAppointments()){
                        System.out.println(ss.getAppointments().indexOf(a) + " / " + a.getRoom().getName()
                        + " / " + a.getRelatedData() + " / " + a.getDateFrom() + " / " + a.getDateTo());
                    }
                    System.out.print("Choose appointment ID: ");
                    String input = reader.nextLine();

                    Appointment old = ss.getAppointments().get(Integer.parseInt(input));
                    Appointment pom = old.copy();

                    System.out.print("Do you want to change room? ");
                    input = reader.nextLine();


                    if(input.equalsIgnoreCase("Yes")){
                        System.out.println("Please select room from list below. Only write room name!");
                        for(Room r : ss.getMetaData().getRooms()){
                            System.out.println("Room name: " + r.getName() + ", capacity: " + r.getCapacity());
                        }
                        System.out.print("Enter room: ");
                        input = reader.nextLine();

                        send = null;
                        for(Room r : ss.getMetaData().getRooms()){
                            if(r.getName().equalsIgnoreCase(input)) {
                                send = r;
                                break;
                            }
                        }
                        if(send == null){
                            System.out.println("You did not enter valid room!");
                            break;
                        }

                        pom.setRoom(send);
                    }

                    System.out.print("Enter date from (YYYY-MM-DDTHH:MM): ");
                    input = reader.nextLine();

                    LocalDateTime localFrom = LocalDateTime.parse(input);

                    System.out.print("Enter date to (YYYY-MM-DDTHH:MM): ");
                    input = reader.nextLine();

                    LocalDateTime localTo = LocalDateTime.parse(input);

                    pom.setDay(localFrom.getDayOfWeek());
                    pom.setDateFrom(localFrom);
                    pom.setDateTo(localTo);

                    if(ss.rescheduleAppointment(pom, old)){
                        System.out.println("You rescheduled appointment!");
                        System.out.println(ss.getAppointments());
                    }else{
                        System.out.println("That appointment is already taken :(");
                    }

                    break;
                case "8":
                    System.out.println("ID / RoomName / relatedData / dateFrom / DateTo");
                    for(Appointment a : ss.getAppointments()){
                        System.out.println(ss.getAppointments().indexOf(a) + " / " + a.getRoom().getName()
                                + " / " + a.getRelatedData() + " / " + a.getDateFrom() + " / " + a.getDateTo());
                    }
                    System.out.print("Choose appointment ID: ");
                    input = reader.nextLine();

                    ss.getAppointments().remove(ss.getAppointments().get(Integer.parseInt(input)));

                    break;
                case "9":
                    return;
            }
        }
    }
    public static void printCommands(){
        System.out.println("Please enter one of the following commands:");
        System.out.println("1. Export (CSV, JSON, PDF, CONSOLE)");
        System.out.println("2. Search (find taken)");
        System.out.println("3. Search (find available)");
        System.out.println("4. Add room");
        System.out.println("5. Add appointment");
        System.out.println("6. Add appointments");
        System.out.println("7. Reschedule appointment");
        System.out.println("8. Delete appointment");
        System.out.println("9. Exit :(\n");
    }


}
