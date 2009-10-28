package com.pcmsolutions.device.EMU.database.events.content;

import com.pcmsolutions.system.ZDisposable;


/**
 * User: paulmeehan
 * Date: 12-Aug-2004
 * Time: 13:28:47
 */
public interface ManageableContentEventHandler <CE extends ContentEvent, RE extends ContentRequestEvent, CL extends ContentListener> extends ContentEventHandler<CE, RE>, ZDisposable {
    interface ExternalHandler <CE,CL> {
        boolean handleEvent(CE ev);

        void setEventHandler(ManageableContentEventHandler ceh);
    }

    interface RequestHandler <RE> {
        Object handleRequest(RE ev);

        void setEventHandler(ManageableContentEventHandler ceh);
    }

    public void addExternalHandler(ExternalHandler eh);

    public void removeExternalHandler(ExternalHandler eh);

    public void addRequestHandler(RequestHandler rh);

    public void removeRequestHandler(RequestHandler rh);

    public void addListener(CL el, Integer index);

    public void removeListener(CL el, Integer index);
}
