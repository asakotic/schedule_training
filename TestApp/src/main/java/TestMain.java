import org.schedule.management.implementationtwo.ScheduleImpl;
import org.schedule.management.specification.models.ScheduleSpecification;

import java.io.IOException;

public class TestMain {
    public static void main(String[] args) throws IOException {
        ScheduleSpecification ss = new ScheduleImpl();
        ss.importMeta();
        ss.importDataCSV("D:\\Education\\Racunarski Fakultet\\Treci semestar\\schedule-management-component-implementation\\ScheduleManagementSpecification\\src\\main\\resources\\schedule01.csv",
              "D:\\Education\\Racunarski Fakultet\\Treci semestar\\schedule-management-component-implementation\\ScheduleManagementSpecification\\src\\main\\resources\\config.txt");
//        System.out.println(ss.getAppointments());
//
//        ss.exportDataCSV("1.json","C:\\Users\\jovvu\\IdeaProjects\\schedule-management-component-implementation\\ScheduleManagementSpecification\\src\\main\\resources\\config.txt");
        ss.exportDataPDF("file.pdf");
    }
}
