package com.beepbox.exception;

public class BoxNotFoundException extends RuntimeException {
    public BoxNotFoundException(String txref) {
        super("Box not found with reference: " + txref);
    }
}
