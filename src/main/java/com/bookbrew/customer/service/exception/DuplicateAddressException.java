package com.bookbrew.customer.service.exception;

public class DuplicateAddressException extends RuntimeException {

    public DuplicateAddressException(String message) {
        super(message);
    }

}
