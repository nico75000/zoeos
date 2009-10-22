package com.pcmsolutions.system;

/**
 * User: paulmeehan
 * Date: 27-Apr-2004
 * Time: 06:21:46
 */
public class AuditioningDisabledException extends AuditioningException{
    public AuditioningDisabledException() {
        super("Auditioning disabled");
    }
}
