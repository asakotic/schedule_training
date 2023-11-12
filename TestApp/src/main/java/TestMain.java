import org.schedule.management.implementationtwo.ScheduleImpl;
import org.schedule.management.specification.models.Appointment;
import org.schedule.management.specification.models.ScheduleSpecification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
                            ss.exportDataConsole(appointmentList);
                            break;
                    }

                    break;
                case "7":
                    return;
            }

            break;


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
