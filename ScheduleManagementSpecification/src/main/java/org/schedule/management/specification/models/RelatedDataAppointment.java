package org.schedule.management.specification.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RelatedDataAppointment {
    private String name;
    private List<String> data;

    public RelatedDataAppointment(String name, List<String> data) {
        this.name = name;
        this.data = data;
    }
}
