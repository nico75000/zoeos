package com.pcmsolutions.system;

/**
 * User: paulmeehan
 * Date: 19-Feb-2004
 * Time: 11:31:01
 */
public interface Linkable {
    public void linkTo(Object o) throws InvalidLinkException;

    class InvalidLinkException extends Exception {
        public InvalidLinkException(String message) {
            super(message);
        }
    }
}
