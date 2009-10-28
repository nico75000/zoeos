package com.pcmsolutions.device.EMU.database;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.database.events.content.ContentListener;
import com.pcmsolutions.system.tasking.Ticket;

import java.util.Map;
import java.util.SortedSet;
import java.util.List;


/**
 * User: paulmeehan
 * Date: 12-Aug-2004
 * Time: 15:56:42
 */
public interface Context <CI extends ContextElement, CX extends Context, CL extends ContentListener, IC> {

    public void addContentListener(CL l, Integer[] indexes) throws DeviceException;

    public void removeContentListener(CL l, Integer[] indexes) ;

    public void addContextListener(ContextListener l);

    public void removeContextListener(ContextListener l);

    public List<CI> getContextElements() throws DeviceException;

    public boolean isReallyEmpty(Integer index) throws DeviceException, ContentUnavailableException;

    public Integer firstEmpty() throws DeviceException;

    public SortedSet<Integer> findEmpties(Integer reqd, Integer beginIndex, Integer maxIndex) throws DeviceException;

    public SortedSet<Integer> getIndexesInContext() throws DeviceException;

    public SortedSet getDatabaseIndexes() throws DeviceException;

    public Map<Integer, String> getContextNamesMap() throws DeviceException;

    public List<ContextLocation> getContextIndexNamesInRange(Integer lowIndex, Integer highIndex) throws DeviceException;

    public boolean containsIndex(Integer index) throws DeviceException;

    public boolean readsIndex(Integer index) throws DeviceException;

    public Ticket copy(Integer srcIndex, Integer destIndex, String name) ;

    public Ticket copy(Integer srcIndex, Integer destIndex);

    public IC getIsolated (Integer index, Object flags) throws DeviceException, ContentUnavailableException, EmptyException;

    public Ticket release() ;

    public Ticket refresh(Integer index);

    public boolean isEmpty(Integer index) throws DeviceException;

    public boolean isPending(Integer index) throws DeviceException;

    public boolean isInitializing(Integer index) throws DeviceException;

    public Ticket assertNamed(Integer index, boolean refreshIfEmpty) ;

    public Ticket assertInitialized(Integer index, boolean refreshIfEmpty);

    public Ticket refreshIfRemoteNameMismatch(Integer index) ;

    // returns true if was originally empty but refreshed succesfully, false if not empty
    public Ticket refreshIfEmpty(Integer index) ;

    //public void newContent(IC isoContent, Integer index, String name);

    public boolean isInitialized(Integer index) throws DeviceException;

    public Ticket uninitialize(Integer index) ;

    public Ticket erase(Integer index) ;

    public Ticket setName(Integer index, String name) ;

    public String getString(Integer index) throws DeviceException;

    public String getName(Integer index) throws DeviceException, ContentUnavailableException, EmptyException;

    public int size() throws DeviceException;

    public int databaseSize() throws DeviceException;

    public CX newContext(String name, Integer[] indexes) throws DeviceException;

    public int numEmpties(Integer[] indexes) throws DeviceException;
}
