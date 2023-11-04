package org.schedule.management.specification.models;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

@Setter
@Getter
public class Room {
    private String name;
    private String capacity;
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Integer> equipment;

    public Room(String roomName, String capacity, Map<String, Integer> equipment) {
        this.name = roomName;
        this.capacity = capacity;
        this.equipment = equipment;
    }

    public boolean addEquipment(String name, int quantity){
        return false; //DODAJEMO U JSON EQUIPMENT NIZ {RACUNAR : 5} ODMAH JSON EDIT
    }
    public boolean removeEquipment(String name){
        return false;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(name, room.name);
    }
    @Override
    public String toString() {
        return "Room{" +
                "roomName='" + name + '\'' +
                ", capacity='" + capacity + '\'' +
                ", equipment=" + equipment +
                '}';
    }
}