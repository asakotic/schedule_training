package org.schedule.management.specification.exceptions;

public class CSVDateNullException extends Exception{
    public CSVDateNullException(){
        super("Missing date value in CSV table!");
    }
}