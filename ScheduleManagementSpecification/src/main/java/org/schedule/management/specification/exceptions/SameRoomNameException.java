package org.schedule.management.specification.exceptions;

public class SameRoomNameException extends Exception{
    public SameRoomNameException(){
        super("Rooms can not have same names");
    }
}
