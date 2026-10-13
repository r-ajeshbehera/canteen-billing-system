package com.rajesh.canteen.exception;

public class InvalidQuantityException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidQuantityException(String message) {
        super(message);
    }
}
