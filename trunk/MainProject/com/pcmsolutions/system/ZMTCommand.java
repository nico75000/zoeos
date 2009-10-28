package com.pcmsolutions.system;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 25-Apr-2003
 * Time: 03:18:21
 * To change this template use Options | File Templates.
 */

// Multi-target command

public interface ZMTCommand extends ZCommand {

    public ZMTCommand getNextMode();   
}
