import org.schedule.management.implementationone.ScheduleImpl;
import org.schedule.management.specification.models.ScheduleSpecification;

import java.io.IOException;

public class TestMain {
    public static void main(String[] args) throws IOException {
        ScheduleSpecification ss = new ScheduleImpl();
        ss.importMeta();
        ss.importDataCSV("C:\\Users\\jovvu\\IdeaProjects\\schedule-management-component-implementation\\ScheduleManagementSpecification\\src\\main\\resources\\schedule02.csv",
                "C:\\Users\\jovvu\\IdeaProjects\\schedule-management-component-implementation\\ScheduleManagementSpecification\\src\\main\\resources\\config.txt");
        System.out.println(ss.getAppointments());
    }
}
