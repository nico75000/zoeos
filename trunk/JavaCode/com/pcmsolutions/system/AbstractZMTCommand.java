package com.pcmsolutions.system;


/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 13-Mar-2003
 * Time: 06:46:42
 * To change this template use Options | File Templates.
 */
public abstract class AbstractZMTCommand<C> extends AbstractZCommand<C> implements ZMTCommand {

    public ZMTCommand getNextMode() {
        return null;
    }
}

