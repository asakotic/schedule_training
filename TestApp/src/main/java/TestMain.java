import org.schedule.management.implementationtwo.ScheduleImpl;
import org.schedule.management.specification.models.Appointment;
import org.schedule.management.specification.models.Room;
import org.schedule.management.specification.models.ScheduleSpecification;

import java.io.IOException;
import java.sql.SQLOutput;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

public class TestMain {
    public static void main(String[] args) throws IOException {
        ScheduleSpecification ss = new ScheduleImpl();

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
            ss.importDataCSV(filePath, configPath);
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
                            "1. JSON\n2. CSV\n3. PDF\n4. CONSOLE");
                    command = reader.nextLine();

                    switch (command){
                        case "1":
                            ss.exportDataJSON("1.json", appointmentList); // 2. impl 1. impl
                            break;
                        case "2":
                            ss.exportDataCSV("1.csv", configPath, appointmentList); //2. impl 1. impl
                            break;
                        case "3":
                            ss.exportDataPDF("1.pdf", appointmentList); //2. impl 1. impl
                            break;
                        case "4":
                            ss.exportDataConsole(ss.getAppointments());
                            break;
                    }

                    break;
                case "3":
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

                    if(!ss.addRoom(roomName, String.valueOf(capacity), equipment)) System.out.println("This room already exists!");
                    else System.out.println("You added new room!");
                    break;
                case "4":
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
                case "5":

                    System.out.println("ID / RoomName / relatedData / dateFrom / DateTo");
                    for(Appointment a : ss.getAppointments()){ //TODO: add filters
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
                case "6":
                    System.out.println("ID / RoomName / relatedData / dateFrom / DateTo");
                    for(Appointment a : ss.getAppointments()){ //TODO: add filters
                        System.out.println(ss.getAppointments().indexOf(a) + " / " + a.getRoom().getName()
                                + " / " + a.getRelatedData() + " / " + a.getDateFrom() + " / " + a.getDateTo());
                    }
                    System.out.print("Choose appointment ID: ");
                    input = reader.nextLine();

                    ss.getAppointments().remove(ss.getAppointments().get(Integer.parseInt(input)));

                    break;
                case "7":
                    return;
            }
        }
    }
    public static void printCommands(){
        System.out.println("Please enter one of the following commands:");
        System.out.println("1. Export (CSV, JSON, PDF, CONSOLE)");
        System.out.println("2. Search (find available and taken appointments)");
        System.out.println("3. Add room");
        System.out.println("4. Add appointment");
        System.out.println("5. Reschedule appointment");
        System.out.println("6. Delete appointment");
        System.out.println("7. Exit :(\n");
    }


}
