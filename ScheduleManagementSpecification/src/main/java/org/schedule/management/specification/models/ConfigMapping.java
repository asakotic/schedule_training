package org.schedule.management.specification.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfigMapping {
    private Integer index;
    private String userLabel;
    private String primaryLabel;

    public ConfigMapping(Integer index, String userLabel, String primaryLabel) {
        this.index = index;
        this.userLabel = userLabel;
        this.primaryLabel = primaryLabel;
    }
}
