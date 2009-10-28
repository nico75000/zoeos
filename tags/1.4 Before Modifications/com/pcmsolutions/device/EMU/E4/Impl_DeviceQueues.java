package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.tasking.ManageableTicketedQ;
import com.pcmsolutions.system.tasking.QueueFactory;

/**
 * User: paulmeehan
 * Date: 07-Aug-2004
 * Time: 11:09:23
 */
class Impl_DeviceQueues implements DeviceQueues, ZDisposable {
    DeviceContext device;
    private final ManageableTicketedQ generalQ;
    private final ManageableTicketedQ refreshQ;
    private final ManageableTicketedQ zCommandQ;
    private final ManageableTicketedQ parameterQ;
    private final ManageableTicketedQ auditionQ;
    private final ManageableTicketedQ ddQ;
    private final ManageableTicketedQ externalizationQ;
    private final ManageableTicketedQ presetContextQ;

    public Impl_DeviceQueues(DeviceContext dc) {
        this.device = dc;
        generalQ = QueueFactory.createTicketedQueue(device, "generalQ", 6);
        refreshQ = QueueFactory.createTicketedQueue(device, "refreshQ", 6);
        zCommandQ = QueueFactory.createTicketedQueue(device, "zcommandQ", 6);
        parameterQ = QueueFactory.createTicketedQueue(device, "parameterQ", 6);
        auditionQ = QueueFactory.createTicketedQueue(device, "auditionQ", 6);
        ddQ = QueueFactory.createTicketedQueue(device, "ddQ", 6);
        externalizationQ = QueueFactory.createTicketedQueue(device, "externalizationQ", 6);
        presetContextQ = QueueFactory.createTicketedQueue(device, "presetContextQ", 6);
    }

    void start() {
        generalQ.start();
        refreshQ.start();
        zCommandQ.start();
        parameterQ.start();
        auditionQ.start();
        ddQ.start();
        externalizationQ.start();
        presetContextQ.start();
    }

    void stop(boolean flush) {
        generalQ.stop(flush);
        refreshQ.stop(flush);
        zCommandQ.stop(flush);
        parameterQ.stop(flush);
        auditionQ.stop(flush);
        ddQ.stop(flush);
        externalizationQ.stop(flush);
        presetContextQ.stop(flush);
    }

    void pause() {
        generalQ.pause();
        refreshQ.pause();
        zCommandQ.pause();
        parameterQ.pause();
        auditionQ.pause();
        ddQ.pause();
        externalizationQ.pause();
        presetContextQ.pause();
    }

    void resume() {
        generalQ.resume();
        refreshQ.resume();
        zCommandQ.resume();
        parameterQ.resume();
        auditionQ.resume();
        ddQ.resume();
        externalizationQ.resume();
        presetContextQ.resume();
    }

    public DeviceContext getDevice() {
        return device;
    }

    public ManageableTicketedQ generalQ() {
        return generalQ;
    }

    public ManageableTicketedQ refreshQ() {
        return refreshQ;
    }

    public ManageableTicketedQ zCommandQ() {
        return zCommandQ;
    }

    public ManageableTicketedQ parameterQ() {
        return parameterQ;
    }

    public ManageableTicketedQ auditionQ() {
        return auditionQ;
    }

    public ManageableTicketedQ ddQ() {
        return ddQ;
    }

    public ManageableTicketedQ externalizationQ() {
        return externalizationQ;
    }

    public ManageableTicketedQ presetContextQ() {
        return presetContextQ;
    }

    public void zDispose() {
    }
}
