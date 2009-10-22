package com.pcmsolutions.device.EMU.database.events.content;

import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.callback.Callback;
import com.pcmsolutions.system.delays.DelayFactory;
import com.pcmsolutions.system.tasking.*;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * User: paulmeehan
 * Date: 12-Aug-2004
 * Time: 13:19:23
 */
class Impl_ManageableContentEventHandler <CE extends ContentEvent,RE extends ContentRequestEvent, CL extends ContentListener> implements ManageableContentEventHandler<CE, RE, CL>, ZDisposable, Serializable {
    final private int size;
    final private Vector<ExternalHandler> externalHandlers = new Vector<ExternalHandler>();
    final private Vector<RequestHandler> requestHandlers = new Vector<RequestHandler>();
    transient private Vector<List<CL>> listeners = new Vector<List<CL>>();
    transient protected ManageableTaskQ<EventTask> eventQ;

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        buildTransients();
    }

    public Impl_ManageableContentEventHandler(int size) {
        this.size = size;
        buildTransients();
    }

    abstract class EventTask implements Task {
        final private CE event;

        public EventTask(CE event) {
            this.event = event;
        }

        public final void cbCancelled() {
            if (event instanceof Callback)
                ((Callback) event).result(null, true);
        }

        public void cbFinished(Exception e) {
            if (event instanceof Callback)
                ((Callback) event).result(e, false);
        }

        public final String getName() {
            return event.toString();
        }

        public final CE getEvent() {
            return event;
        }
    }

    //  private Vector<CE> eventHistory = new Vector<CE>();
    class InternalTask extends EventTask implements Task {

        public InternalTask(CE event) {
            super(event);
        }

        public void run() throws Exception {
            synchronized (listeners) {
                final List<CL> indexListeners = listeners.get(getEvent().getIndex().intValue());
                if (indexListeners == null)
                    return;
                final Object[] la = indexListeners.toArray();

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        for (final Object o : la)
                            //SwingUtilities.invokeLater(new Runnable(){
                             //   public void run() {
                                    try {
                                        getEvent().fire((CL) o);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                           //     }
                           // });
                    }
                });
            }
        }

        public void cbFinished(Exception e) {
            //    eventHistory.add(getEvent());
        }
    }

    class ExternalTask extends EventTask implements Task {
        public ExternalTask(CE event) {
            super(event);
        }

        public void run() throws Exception {
            final Object[] eha = externalHandlers.toArray();
            for (Object o : eha)
                try {
                    if (((ExternalHandler) o).handleEvent(getEvent()))
                        break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }

    class CombinedTask extends EventTask implements Task {
        final InternalTask internalTask;
        final ExternalTask externalTask;
        final boolean externalFirst;

        public CombinedTask(CE event, boolean externalFirst) {
            super(event);
            this.externalFirst = externalFirst;
            internalTask = new InternalTask(event);
            externalTask = new ExternalTask(event);
        }

        public void run() throws Exception {
            if (externalFirst) {
                externalTask.run();
                internalTask.run();
            } else {
                internalTask.run();
                externalTask.run();
            }
        }
    }

    private void buildTransients() {
        listeners = new Vector<List<CL>>();
        listeners.setSize(size);

        eventQ = QueueFactory.createTaskQueue(this, "contentEventHandlerQ", 6, new ThreadAllower() {
            public boolean allowThread() {
                return !SwingUtilities.isEventDispatchThread();
            }

            public void zDispose() {
            }
        }, new TaskInserter<EventTask>() {

            public void zDispose() {
            }

            public void insertTask(EventTask task, List<EventTask> postedTasks, String qName) throws QueueUnavailableException {
                for (int i = postedTasks.size() - 1; i >= 0; i--) {
                    if (task.getEvent().subsumes(postedTasks.get(i).getEvent()))
                        postedTasks.remove(i--);
                    else if (!task.getEvent().independentOf(postedTasks.get(i).getEvent()))
                        break;
                }
                postedTasks.add(task);
            }
        }, new TaskFetcher<EventTask>() {
            public List<EventTask> fetch(List<EventTask> eventTasks) {
                ArrayList<EventTask> outEventTasks = new ArrayList<EventTask>();
                outEventTasks.addAll(eventTasks);
                eventTasks.clear();
                return outEventTasks;
            }

            public void zDispose() {

            }
        }, DelayFactory.createDelay(75));
        eventQ.start();
    }

    public void zDispose() {
        listeners.clear();
        externalHandlers.clear();
        eventQ.stop(true);
    }

    public void addListener(CL l, Integer index) {
        if (index.intValue() < 0)
            return;
        synchronized (listeners) {
            List<CL> indexListeners = listeners.get(index.intValue());
            if (indexListeners == null) {
                indexListeners = new ArrayList<CL>();
                listeners.set(index.intValue(), indexListeners);
            }
            indexListeners.add(l);
        }
    }

    public void removeListener(CL l, Integer index) {
        if (index.intValue() < 0)
            return;
        synchronized (listeners) {
            List<CL> indexListeners = listeners.get(index.intValue());
            if (indexListeners != null)
                indexListeners.remove(l);
        }
    }

    public void sync() {
        eventQ.waitUntilEmpty();
    }

    public void postEvent(final CE ev) {
        postEvent(ev, true);
    }

    public void postEvent(final CE ev, boolean externalFirst) {
        try {
            eventQ.postTask(new CombinedTask(ev, externalFirst));
        } catch (QueueUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void postInternalEvent(final CE ev) {
        try {
            eventQ.postTask(new InternalTask(ev));
        } catch (QueueUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void sendInternalEvent(final CE ev) throws Exception {
        //eventQ.waitUntilEmpty();
        new InternalTask(ev).run();
    }

    public void postExternalEvent(CE ev) {
        try {
            eventQ.postTask(new ExternalTask(ev));
        } catch (QueueUnavailableException e) {
            e.printStackTrace();
        }
    }

    public boolean sendRequest(RE re) {
        final Object[] rha = requestHandlers.toArray();
        for (Object o : rha)
            try {
                re.requestedData = ((RequestHandler) o).handleRequest(re);
                return re.requestedData != null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        return false;
    }

    public void addExternalHandler(ExternalHandler eh) {
        eh.setEventHandler(this);
        externalHandlers.add(eh);
    }

    public void removeExternalHandler(ExternalHandler eh) {
        externalHandlers.remove(eh);
    }

    public void addRequestHandler(ManageableContentEventHandler.RequestHandler rh) {
        rh.setEventHandler(this);
        requestHandlers.add(rh);
    }

    public void removeRequestHandler(ManageableContentEventHandler.RequestHandler rh) {
        requestHandlers.remove(rh);
    }
}
