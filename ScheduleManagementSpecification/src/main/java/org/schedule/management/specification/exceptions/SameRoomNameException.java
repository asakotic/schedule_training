package org.schedule.management.specification.exceptions;

public class SameRoomNameException extends Exception{
    public SameRoomNameException(String errorMessage){
        super(errorMessage);
    }
}
