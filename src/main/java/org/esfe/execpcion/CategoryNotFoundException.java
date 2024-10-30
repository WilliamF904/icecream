package org.esfe.execpcion;

public class CategoryNotFoundException  extends RuntimeException {

        public CategoryNotFoundException(String message) {
            super(message);
        }
}