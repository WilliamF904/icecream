package org.esfe.execpcion;

public class PayNotFoundException   extends RuntimeException {
    public PayNotFoundException(String message) {
        super(message);
    }
}
