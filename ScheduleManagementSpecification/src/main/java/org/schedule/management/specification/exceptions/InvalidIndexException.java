package org.schedule.management.specification.exceptions;

public class InvalidIndexException extends Exception{
    public InvalidIndexException(){
        super("Missing index!");
    }
}