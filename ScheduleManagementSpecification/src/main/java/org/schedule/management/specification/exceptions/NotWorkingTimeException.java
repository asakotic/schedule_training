package org.schedule.management.specification.exceptions;

public class NotWorkingTimeException extends Exception{
    public NotWorkingTimeException(){
        super("Date is not in working time!");
    }
}