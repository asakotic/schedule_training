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
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Integer> equipment;

    /**
     * Creates new instance of Room
     * @param roomName Room name
     * @param capacity Room capacity
     * @param equipment Room equipment
     */
    public Room(String roomName, String capacity, Map<String, Integer> equipment) {
        this.name = roomName;
        this.capacity = capacity;
        this.equipment = equipment;
    }

    /**
     * Compares two rooms
     * @param o Compare with Room o
     * @return Return true if Rooms have same name
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(name, room.name);
    }

    /**
     * New string value of room
     * @return Returns String value of room
     */
    @Override
    public String toString() {
        return "Room{" +
                "roomName='" + name + '\'' +
                ", capacity='" + capacity + '\'' +
                ", equipment=" + equipment +
                '}';
    }
}