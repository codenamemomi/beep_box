package com.beepbox.exception;

public class DuplicateTxrefException extends RuntimeException {
    public DuplicateTxrefException(String txref) {
        super("Box already exists with reference: " + txref);
    }
}
