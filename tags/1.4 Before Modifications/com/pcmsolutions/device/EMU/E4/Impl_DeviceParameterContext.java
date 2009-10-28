package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.comms.RemoteDeviceDidNotRespondException;
import com.pcmsolutions.comms.RemoteMessagingException;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.remote.Remotable;
import com.pcmsolutions.gui.ProgressSession;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.SystemErrors;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.Zoeos;

import javax.swing.*;
import java.io.Serializable;
import java.util.*;

// should be immutable after construction/initialization

class Impl_DeviceParameterContext implements DeviceParameterContext, Serializable, ZDisposable {
    private String generator = new String("Unknown");

    private Map multimodeIds = Collections.synchronizedMap(new TreeMap());        // Map of Integer id -> Integer default value
    private Map masterIds = Collections.synchronizedMap(new TreeMap());        // Map of Integer id -> Integer default value
    private Map presetIds = Collections.synchronizedMap(new TreeMap());        // Map of Integer id -> Integer default value
    private Map voiceIds = Collections.synchronizedMap(new TreeMap());         // Map of Integer id -> Integer default value
    private Map linkIds = Collections.synchronizedMap(new TreeMap());          // Map of Integer id -> Integer default value
    private Map zoneIds = Collections.synchronizedMap(new TreeMap());          // Map of Integer id -> Integer default value
    private Vector multimodePDs = new Vector();    // List of GeneralParameterDescriptor
    private Vector masterPDs = new Vector();    // List of GeneralParameterDescriptor
    private Vector presetPDs = new Vector();    // List of GeneralParameterDescriptor
    private Vector voicePDs = new Vector();     // List of GeneralParameterDescriptor
    private Vector linkPDs = new Vector();      // List of GeneralParameterDescriptor
    private Vector zonePDs = new Vector();      // List of GeneralParameterDescriptor

    private Hashtable id2pd = new Hashtable();              // id -> GeneralParameterDescriptor
    private Hashtable id2ref = new Hashtable();             // id -> refernece string
    private Hashtable ref2id = new Hashtable();             // refernece string -> id

    private Hashtable cat2mpd = new Hashtable();             // cat String -> GeneralParameterDescriptor
    private Hashtable cat2ppd = new Hashtable();             // cat String -> GeneralParameterDescriptor
    private Hashtable cat2vpd = new Hashtable();             // cat String -> GeneralParameterDescriptor
    private Hashtable cat2lpd = new Hashtable();             // cat String -> GeneralParameterDescriptor
    private Hashtable cat2zpd = new Hashtable();             // cat String -> GeneralParameterDescriptor

    private final ParameterContext multimodeContext = new Impl_ParameterContext(multimodeIds, multimodePDs, null);
    private final ParameterContext masterContext = new Impl_ParameterContext(masterIds, masterPDs, cat2mpd);
    private final ParameterContext presetContext = new Impl_ParameterContext(presetIds, presetPDs, cat2ppd);
    private final ParameterContext voiceContext = new Impl_ParameterContext(voiceIds, voicePDs, cat2vpd);
    private final ParameterContext linkContext = new Impl_ParameterContext(linkIds, linkPDs, cat2lpd);
    private final ParameterContext zoneContext = new Impl_ParameterContext(zoneIds, zonePDs, cat2zpd);

    // CORD adjustment maps
    private HashMap cordSrcValueTranslationMap;
    private HashMap cordDestValueTranslationMap;

    private DeviceContext dc;
    private double devVer;

    public Impl_DeviceParameterContext(DeviceContext dc, com.pcmsolutions.device.EMU.E4.remote.Remotable remote) throws RemoteDeviceDidNotRespondException, com.pcmsolutions.device.EMU.E4.RemoteUnreachableException, RemoteMessagingException {
        this.devVer = remote.getDeviceVersion();
        this.dc = dc;
        Zoeos z = Zoeos.getInstance();
        ProgressSession ps = z.getProgressSession(dc.makeDeviceProgressTitle("Initializing Device Parameter Context "), 6);
        try {
            generateMultiModeIds(remote);
            ps.updateStatus();
            generateMasterIds(remote);
            ps.updateStatus();
            generatePresetIds(remote);
            ps.updateStatus();
            generateLinkIds(remote);
            ps.updateStatus();
            generateZoneIds(remote);
            ps.updateStatus();
            generateVoiceIds(remote);
            ps.updateStatus();
        } finally {
            ps.end();
        }
    }

    protected void generateMasterIds(Remotable remote) throws RemoteDeviceDidNotRespondException, com.pcmsolutions.device.EMU.E4.RemoteUnreachableException, RemoteMessagingException {
        // CLASSIC: 183-195 inclusive (total 13)
        // 196, 197,
        // CLASSIC 198 - 199 inclusive (total 2)
        // 200 not used
        // CLASSIC 201 - 222 inclusive (total 22)
        // 223-227 are selection ids
        // CLASSIC: 228 - 245 inclusive (total 18)
        // ULTRA ONLY:   267-270 inclusive (4)
        // CLASSIC: 271 (total 1)


        Integer id;
        // CLASSIC
        for (int n = 0; n < 13; n++) {
            id = IntPool.get(183 + n);
            this.addMasterParameterToContext(remote, id);
        }
        for (int n = 0; n < 2; n++) {
            id = IntPool.get(198 + n);
            this.addMasterParameterToContext(remote, id);
        }
        for (int n = 0; n < 22; n++) {
            id = IntPool.get(201 + n);
            this.addMasterParameterToContext(remote, id);
        }
        for (int n = 0; n < 18; n++) {
            id = IntPool.get(228 + n);
            this.addMasterParameterToContext(remote, id);
        }
        //this.addMasterParameterToContext(IntPool.get(271));

        // ULTRA ONLY
        if (remote.getDeviceVersion() >= DeviceContext.BASE_ULTRA_VERSION)
            for (int n = 0; n < 4; n++) {
                id = IntPool.get(267 + n);
                this.addMasterParameterToContext(remote, id);
            }
    }

    protected void generatePresetIds(Remotable remote) throws RemoteDeviceDidNotRespondException, com.pcmsolutions.device.EMU.E4.RemoteUnreachableException, RemoteMessagingException {
        // 0-21 inclusive(22 total)
        Integer id;
        for (int n = 0; n < 22; n++) {
            id = IntPool.get(n);
            this.addPresetParameterToContext(remote, id);
        }
    }

    protected void generateMultiModeIds(Remotable remote) throws RemoteDeviceDidNotRespondException, com.pcmsolutions.device.EMU.E4.RemoteUnreachableException, RemoteMessagingException {
        // 247-250 inclusive(4 total)
        Integer id;
        for (int n = 0; n < 4; n++) {
            id = IntPool.get(247 + n);
            this.addMultiModeParameterToContext(remote, id);
        }
    }

    protected void generateLinkIds(Remotable remote) throws RemoteDeviceDidNotRespondException, com.pcmsolutions.device.EMU.E4.RemoteUnreachableException, RemoteMessagingException {
        // 23-35 inclusive(13), 251-266 inclusive(16)  (total 29)
        Integer id;
        for (int n = 0; n < 13; n++) {
            id = IntPool.get(23 + n);
            this.addLinkParameterToContext(remote, id);
        }
        for (int n = 0; n < 16; n++) {
            id = IntPool.get(251 + n);
            this.addLinkParameterToContext(remote, id);
        }
    }

    protected void generateZoneIds(Remotable remote) throws RemoteDeviceDidNotRespondException, com.pcmsolutions.device.EMU.E4.RemoteUnreachableException, RemoteMessagingException {
        // 38-40, 42, 44-52 inclusive(13 total)
        this.addZoneParameterToContext(remote, IntPool.get(38));
        this.addZoneParameterToContext(remote, IntPool.get(39));
        this.addZoneParameterToContext(remote, IntPool.get(40));
        this.addZoneParameterToContext(remote, IntPool.get(42));
        Integer id;
        for (int n = 0; n < 9; n++) {
            id = IntPool.get(44 + n);
            this.addZoneParameterToContext(remote, id);
        }
    }

    protected void generateVoiceIds(Remotable remote) throws RemoteDeviceDidNotRespondException, com.pcmsolutions.device.EMU.E4.RemoteUnreachableException, RemoteMessagingException {
        // GENERAL  37-56       (20 total)
        // TUNING   57-67       (11 total)
        // AMP      68-81       (14 total)
        // FILTER   82-84,      (3 total)
        //                      (48 subtotal)
        // 85, 86 are reserved
        //                      (2 subtotal)
        // FILTER   87-104      (18 total)
        // LFO      105-116     (12 total)
        // AUX      117-128     (12 total)
        // CORDS    129-182     (54 total)
        //                      (96 sub total)
        //                      (146 grand total)
        Integer id;
        for (int n = 0; n < 146; n++) {
            id = IntPool.get(37 + n);
            this.addVoiceParameterToContext(remote, id);
        }
    }

    protected void addMasterParameterToContext(Remotable remote, Integer id) throws RemoteMessagingException, com.pcmsolutions.device.EMU.E4.RemoteUnreachableException, RemoteDeviceDidNotRespondException {
        GeneralParameterDescriptor pd = null;
        try {
            pd = ParameterTables.generateParameterDescriptor(id, DeviceParameterContext.MASTER);
        } catch (ParameterRequiresMMDException e) {
            MinMaxDefault mmd;
            mmd = remote.getParameterContext().req_prmMMD(id);
            if (id.intValue() == 187 && devVer >= DeviceContext.BASE_ULTRA_VERSION) {
                // TODO!! keep a check on the validity of this
                // adjust the default  value to 1 because of a sysex bug for ultras
                final MinMaxDefault f_mmd = mmd;
                mmd = new MinMaxDefault() {
                    public boolean equals(Object obj) {
                        if (obj instanceof MinMaxDefault) {
                            MinMaxDefault mmd = (MinMaxDefault) obj;
                            return (mmd.getID().equals(getID()) && mmd.getMin().equals(getMin()) && mmd.getMax().equals(getMax()) && mmd.getDefault().equals(getDefault()));
                        }
                        return false;
                    }

                    public Integer getID() {
                        return f_mmd.getID();
                    }

                    public Integer getMin() {
                        return f_mmd.getMin();
                    }

                    public Integer getMax() {
                        return f_mmd.getMax();
                    }

                    public Integer getDefault() {
                        return IntPool.get(1);
                    }
                };
            }
            pd = ParameterTables.generateParameterDescriptor(id, mmd, DeviceParameterContext.MASTER);
        }
        id2pd.put(id, pd);
        String rn = pd.getReferenceString();
        id2ref.put(id, rn);
        ref2id.put(rn, id);
        addMPDToCat(pd);
        masterIds.put(id, pd.getDefaultValue());
        masterPDs.add(pd);
    }

    protected void addPresetParameterToContext(Remotable remote, Integer id) throws RemoteMessagingException, com.pcmsolutions.device.EMU.E4.RemoteUnreachableException, RemoteDeviceDidNotRespondException {
        GeneralParameterDescriptor pd = null;
        try {
            pd = ParameterTables.generateParameterDescriptor(id, DeviceParameterContext.PRESET);
        } catch (ParameterRequiresMMDException e) {
            MinMaxDefault mmd;
            mmd = remote.getParameterContext().req_prmMMD(id);
            pd = ParameterTables.generateParameterDescriptor(id, mmd, DeviceParameterContext.PRESET);
        }
        id2pd.put(id, pd);
        String rn = pd.getReferenceString();
        id2ref.put(id, rn);
        ref2id.put(rn, id);
        addPPDToCat(pd);
        presetIds.put(id, pd.getDefaultValue());
        presetPDs.add(pd);
    }

    protected void addVoiceParameterToContext(Remotable remote, Integer id) throws RemoteMessagingException, com.pcmsolutions.device.EMU.E4.RemoteUnreachableException, RemoteDeviceDidNotRespondException {
        GeneralParameterDescriptor pd = null;
        try {
            pd = ParameterTables.generateParameterDescriptor(id, DeviceParameterContext.VOICE);
        } catch (ParameterRequiresMMDException e) {
            MinMaxDefault mmd;
            mmd = remote.getParameterContext().req_prmMMD(id);
            if (id.intValue() == 38 && mmd.getMin().intValue() == 0) {
                // adjust the min value to -1 so we it will accept sample number of -1 for multisample voice
                final MinMaxDefault f_mmd = mmd;
                mmd = new MinMaxDefault() {
                    public boolean equals(Object obj) {
                        if (obj instanceof MinMaxDefault) {
                            MinMaxDefault mmd = (MinMaxDefault) obj;
                            return (mmd.getID().equals(getID()) && mmd.getMin().equals(getMin()) && mmd.getMax().equals(getMax()) && mmd.getDefault().equals(getDefault()));
                        }
                        return false;
                    }

                    public Integer getID() {
                        return f_mmd.getID();
                    }

                    public Integer getMin() {
                        return IntPool.get(-1);
                    }

                    public Integer getMax() {
                        return f_mmd.getMax();
                    }

                    public Integer getDefault() {
                        return f_mmd.getDefault();
                    }
                };
            }
            pd = ParameterTables.generateParameterDescriptor(id, mmd, DeviceParameterContext.VOICE);
        }

        int idv = id.intValue();
        if (idv >= 129 && idv <= 182) {
            if ((idv - 129) % 3 == 0) {
                // we've got a cord src parameter
                if (cordSrcValueTranslationMap == null) {
                    cordSrcValueTranslationMap = new HashMap();
                    buildCordValueTranslationMap(pd, cordSrcValueTranslationMap);
                }
                pd = new CordParameterDescriptor(true, pd);

            } else if ((idv - 129) % 3 == 1) {
                // we've got a cord dest parameter
                if (cordDestValueTranslationMap == null) {
                    cordDestValueTranslationMap = new HashMap();
                    buildCordValueTranslationMap(pd, cordDestValueTranslationMap);
                }
                pd = new CordParameterDescriptor(false, pd);
            }
        }

        id2pd.put(id, pd);
        String rn = pd.getReferenceString();
        id2ref.put(id, rn);
        ref2id.put(rn, id);
        addVPDToCat(pd);
        voiceIds.put(id, pd.getDefaultValue());
        voicePDs.add(pd);
    }

    private void addVPDToCat(GeneralParameterDescriptor pd) {
        ArrayList m = (ArrayList) cat2vpd.get(pd.getCategory());
        if (m == null) {
            m = new ArrayList();
            cat2vpd.put(pd.getCategory(), m);
        }
        m.add(pd);
    }

    private void addPPDToCat(GeneralParameterDescriptor pd) {
        ArrayList m = (ArrayList) cat2ppd.get(pd.getCategory());
        if (m == null) {
            m = new ArrayList();
            cat2ppd.put(pd.getCategory(), m);
        }
        m.add(pd);
    }

    private void addMPDToCat(GeneralParameterDescriptor pd) {
        ArrayList m = (ArrayList) cat2mpd.get(pd.getCategory());
        if (m == null) {
            m = new ArrayList();
            cat2mpd.put(pd.getCategory(), m);
        }
        m.add(pd);
    }

    private void addLPDToCat(GeneralParameterDescriptor pd) {
        ArrayList m = (ArrayList) cat2lpd.get(pd.getCategory());
        if (m == null) {
            m = new ArrayList();
            cat2lpd.put(pd.getCategory(), m);
        }
        m.add(pd);
    }

    private void addZPDToCat(GeneralParameterDescriptor pd) {
        ArrayList m = (ArrayList) cat2zpd.get(pd.getCategory());
        if (m == null) {
            m = new ArrayList();
            cat2zpd.put(pd.getCategory(), m);
        }
        m.add(pd);
    }

    private void buildCordValueTranslationMap(GeneralParameterDescriptor pd, HashMap tMap) {

        int min = pd.getMinValue().intValue();
        int max = pd.getMaxValue().intValue();
        Integer lastVal = IntPool.get(0);
        Object o;
        Integer io;
        for (int i = min, j = max; i <= j; i++) {
            io = IntPool.get(i);
            try {
                o = pd.getStringForValue(io);
                if (o == null)
                    tMap.put(io, lastVal);
                else {
                    tMap.put(io, io);
                    lastVal = io;
                }
            } catch (ParameterValueOutOfRangeException e) {
                tMap.put(io, lastVal);  // should never happen
                SystemErrors.internal(e);
            }
        }
    }

    protected class CordParameterDescriptor implements GeneralParameterDescriptor {
        protected GeneralParameterDescriptor pd;
        protected boolean srcCord;

        public CordParameterDescriptor(boolean srcCord, GeneralParameterDescriptor pd) {
            this.pd = pd;
            this.srcCord = srcCord;
        }

        public String toString() {
            return pd.toString();
        }

        public MinMaxDefault getMMD() {
            return pd.getMMD();
        }

        public Integer getId() {
            return pd.getId();
        }

        public boolean isValidValue(Integer value) {
            return pd.isValidValue(value);
        }

        public String getPresentationString() {
            return pd.getPresentationString();
        }

        public String getReferenceString() {
            return pd.getReferenceString();
        }

        public String getUnits() {
            return pd.getUnits();
        }

        public Integer getMinValue() {
            return pd.getMinValue();
        }

        public Integer getMaxValue() {
            return pd.getMaxValue();
        }

        public Integer getDefaultValue() {
            return pd.getDefaultValue();
        }

        public Integer constrainValue(Integer value) {
            return pd.constrainValue(value);
        }

        public String getStringForValue(Integer value) throws ParameterValueOutOfRangeException {
            return pd.getStringForValue(value);
        }

        // returns units appended to string value
        public String getUnitlessStringForValue(Integer value) throws ParameterValueOutOfRangeException {
            return pd.getUnitlessStringForValue(value);
        }

        public Integer getValueForString(String valueString) throws ParameterValueOutOfRangeException {
            return pd.getValueForString(valueString);
        }

        public Integer getValueForUnitlessString(String valueExString) throws ParameterValueOutOfRangeException {
            return pd.getValueForUnitlessString(valueExString);
        }

        public Integer getNextValue(Integer v) // usually +1 ( cords are different because they have a discontinuous space )
        {
            Integer nv = IntPool.get(v.intValue() + 1);
            try {
                if (pd.getUnitlessStringForValue(nv) == null)
                    return getNextValue(nv);
                else
                    return nv;
            } catch (ParameterValueOutOfRangeException e) {
            }
            return null;
        }

        public Integer getPreviousValue(Integer v) // usually -1 ( cords are different because they have a discontinuous space )
        {
            Integer pv = IntPool.get(v.intValue() - 1);
            try {
                if (pd.getUnitlessStringForValue(pv) == null)
                    return getPreviousValue(pv);
                else
                    return pv;
            } catch (ParameterValueOutOfRangeException e) {
            }
            return null;
        }

        public int getHierarchicalPosition() {
            return pd.getHierarchicalPosition();
        }

        public String getCategory() {
            return pd.getCategory();
        }

        public List<String> getStringList() {
            if (getUnits() == null)
                return getUnitlessStringList();

            List<String> l = pd.getStringList();
            ArrayList<String> nl = new ArrayList<String>();
            String s;

            for (int i = 0, j = l.size(); i < j; i++) {
                s = l.get(i);
                if (srcCord && devVer < DeviceContext.BASE_ULTRA_VERSION && (i >= 40 && i <= 43))
                    continue;
                if (s != null)
                    nl.add(s + getUnits());
            }
            return nl;
        }

        public List<String> getUnitlessStringList() {
            List<String> l = pd.getUnitlessStringList();
            ArrayList<String> nl = new ArrayList<String>();
            String s;
            for (int i = 0, j = l.size(); i < j; i++) {
                s = l.get(i);
                if (srcCord && devVer < DeviceContext.BASE_ULTRA_VERSION && (i >= 40 && i <= 43))
                    continue;
                if (s != null)
                    nl.add(s);
            }
            return nl;
        }

        public String getTipForValue(Integer value) throws ParameterValueOutOfRangeException {
            return pd.getTipForValue(value);
        }

        public boolean shouldUseSpinner() {
            return pd.shouldUseSpinner();
        }

        public Icon getIcon() {
            return pd.getIcon();
        }

        public boolean equals(Object obj) {
            return pd.equals(obj);
        }

        public String getToolTipText() {
            return pd.getToolTipText();
        }
    }

    protected void addLinkParameterToContext(Remotable remote, Integer id) throws RemoteMessagingException, com.pcmsolutions.device.EMU.E4.RemoteUnreachableException, RemoteDeviceDidNotRespondException {
        GeneralParameterDescriptor pd = null;
        try {
            pd = ParameterTables.generateParameterDescriptor(id, DeviceParameterContext.LINK);
        } catch (ParameterRequiresMMDException e) {
            MinMaxDefault mmd;
            mmd = remote.getParameterContext().req_prmMMD(id);
            pd = ParameterTables.generateParameterDescriptor(id, mmd, DeviceParameterContext.LINK);
        }
        id2pd.put(id, pd);
        String rn = pd.getReferenceString();
        id2ref.put(id, rn);
        ref2id.put(rn, id);
        addLPDToCat(pd);
        linkIds.put(id, pd.getDefaultValue());
        linkPDs.add(pd);
    }

    protected void addMultiModeParameterToContext(Remotable remote, Integer id) throws RemoteMessagingException, com.pcmsolutions.device.EMU.E4.RemoteUnreachableException, RemoteDeviceDidNotRespondException {
        GeneralParameterDescriptor pd = null;
        try {
            pd = ParameterTables.generateParameterDescriptor(id, DeviceParameterContext.MULTIMODE);
        } catch (ParameterRequiresMMDException e) {
            MinMaxDefault mmd;
            mmd = remote.getParameterContext().req_prmMMD(id);
            pd = ParameterTables.generateParameterDescriptor(id, mmd, DeviceParameterContext.MULTIMODE);
        }
        id2pd.put(id, pd);
        String rn = pd.getReferenceString();
        id2ref.put(id, rn);
        ref2id.put(rn, id);
        //addPDToCat(pd);
        multimodeIds.put(id, pd.getDefaultValue());
        multimodePDs.add(pd);
    }

    protected void addZoneParameterToContext(Remotable remote, Integer id) throws RemoteMessagingException, com.pcmsolutions.device.EMU.E4.RemoteUnreachableException, RemoteDeviceDidNotRespondException {
        GeneralParameterDescriptor pd = null;
        try {
            pd = ParameterTables.generateParameterDescriptor(id, DeviceParameterContext.ZONE);
        } catch (ParameterRequiresMMDException e) {
            MinMaxDefault mmd;
            mmd = remote.getParameterContext().req_prmMMD(id);
            pd = ParameterTables.generateParameterDescriptor(id, mmd, DeviceParameterContext.ZONE);
        }
        id2pd.put(id, pd);
        String rn = pd.getReferenceString();
        id2ref.put(id, rn);
        ref2id.put(rn, id);
        addZPDToCat(pd);
        zoneIds.put(id, pd.getDefaultValue());
        zonePDs.add(pd);
    }

    public String getGenerator() {
        return generator;
    }

    public DeviceContext getDeviceContext() {
        return dc;
    }

    public boolean paramExists(Integer id) {
        return ParameterTables.paramExists(id);
    }

    public boolean paramExists(String refName) {
        return ParameterTables.paramExists(refName);
    }

    public GeneralParameterDescriptor getParameterDescriptor(String ref) throws IllegalParameterReferenceException {
        GeneralParameterDescriptor pd = null;

        if (ref2id.containsKey(ref))
            pd = (GeneralParameterDescriptor) id2pd.get(ref2id.get(ref));

        if (pd == null)
            throw new IllegalParameterReferenceException();

        return pd;
    }

    public GeneralParameterDescriptor getParameterDescriptor(Integer id) throws IllegalParameterIdException {
        GeneralParameterDescriptor pd = (GeneralParameterDescriptor) id2pd.get(id);
        if (pd == null)
            throw new IllegalParameterIdException();

        return pd;
    }

    public String getRefName(Integer id) throws IllegalParameterIdException {
        return getParameterDescriptor(id).getReferenceString();
    }

    public Integer getId(String refName) throws IllegalParameterReferenceException {
        return getParameterDescriptor(refName).getId();
    }

    public Integer getNearestCordSrcValue(Integer value) {
        return (Integer) cordSrcValueTranslationMap.get(value);
    }

    public Integer getNearestCordDestValue(Integer value) {
        return (Integer) cordDestValueTranslationMap.get(value);
    }

    public Integer discontinuousOffset(final GeneralParameterDescriptor pd, final Integer value, final int offset, final boolean constrain) {
        Integer next = value;
        Integer curr = value;
        int absOffset = Math.abs(offset);
        if (offset < 0)
            while (absOffset-- > 0) {
                next = pd.getPreviousValue(next);
                if (next == null)
                    return (constrain ? curr : null);
            }
        else
            while (absOffset-- > 0) {
                next = pd.getNextValue(next);
                if (next == null)
                    return (constrain ? curr : null);
            }
        return next;
    }

    // EOS 3.2 amd 3.00 (<EOS 4.0?) seem to only have 28 link words and no word for number of voices in dump
    public boolean isWeirdPresetDumping() {
        if (devVer < 4.0)
            return true;

        return false;
    }

    public ParameterContext getLinkContext() {
        return linkContext;
    }

    public ParameterContext getMasterContext() {
        return masterContext;
    }

    public ParameterContext getPresetContext() {
        return presetContext;
    }

    public ParameterContext getVoiceContext() {
        return voiceContext;
    }

    public ParameterContext getZoneContext() {
        return zoneContext;
    }

    public void zDispose() {
        multimodeIds.clear();
        masterIds.clear();
        presetIds.clear();
        voiceIds.clear();
        linkIds.clear();
        zoneIds.clear();
        multimodePDs.clear();
        masterPDs.clear();
        presetPDs.clear();
        voicePDs.clear();
        linkPDs.clear();
        zonePDs.clear();
        id2pd.clear();
        id2ref.clear();
        ref2id.clear();

        cat2ppd.clear();
        cat2mpd.clear();
        cat2vpd.clear();
        cat2lpd.clear();
        cat2zpd.clear();

        dc = null;
    }

    public class Impl_ParameterContext implements ParameterContext, Serializable {
        private Map ids;
        private Vector pds;
        private Map cat2pd;

        private Impl_ParameterContext(Map ids, Vector pds, Map cat2pd) {
            this.ids = ids;
            this.pds = pds;
            this.cat2pd = cat2pd;
        }

        public String getGenerator() {
            return generator;
        }

        public Map getIdsAndDefaultsAsMap() {
            return new TreeMap(ids);
        }

        public Set<Integer> getIds()               // returns Set of Integer ids
        {
            return new TreeSet<Integer>(ids.keySet());
        }

        public int size() {
            return ids.size();
        }

        public GeneralParameterDescriptor getParameterDescriptor(Integer id) throws IllegalParameterIdException {
            GeneralParameterDescriptor pd = Impl_DeviceParameterContext.this.getParameterDescriptor(id);
            if (pd instanceof FilterParameterDescriptor)
                return ((FilterParameterDescriptor) pd).duplicate();

            return pd;
        }

        public GeneralParameterDescriptor getParameterDescriptor(String refName) throws IllegalParameterReferenceException {
            GeneralParameterDescriptor pd = Impl_DeviceParameterContext.this.getParameterDescriptor(refName);
            if (pd instanceof FilterParameterDescriptor)
                return ((FilterParameterDescriptor) pd).duplicate();

            return pd;
        }

        public List getAllParameterDescriptors() {
            return (List) pds.clone();
        }

        public String getRefName(Integer id) throws IllegalParameterIdException {
            return Impl_DeviceParameterContext.this.getRefName(id);
        }

        public Integer getId(String refName) throws IllegalParameterReferenceException {
            return Impl_DeviceParameterContext.this.getId(refName);
        }

        public boolean paramExists(Integer id) {
            return ids.containsKey(id);
        }

        public List getCategories()            // returns List of category strings ( cat;(1..n)subcat;)
        {
            int size = pds.size();
            String cat;
            ArrayList cats = new ArrayList();
            for (int n = 0; n < size; n++) {
                cat = ((GeneralParameterDescriptor) pds.get(n)).getCategory();
                if (cat != null && !cats.contains(cat))
                    cats.add(cat);
            }
            return cats;
        }

        public List getPDsForCategory(String cs) // returns ordered list of ParameterDescriptors
        {
            if (cat2pd != null) {
                List l = (List) cat2pd.get(cs);
                return (l == null ? new ArrayList() : l);
            } else {
                int size = pds.size();
                String cat;
                ArrayList catPDs = new ArrayList();
                GeneralParameterDescriptor pd;
                for (int n = 0; n < size; n++) {
                    pd = (GeneralParameterDescriptor) pds.get(n);
                    cat = pd.getCategory();
                    if (!cat.equals(cs))
                        continue;
                    catPDs.add(pd);
                }
                return catPDs;
            }
        }

        public List getIdsForCategory(String cs) // returns ordered list of Parameter Ids
        {
            List pds = getPDsForCategory(cs);
            ArrayList catIds = new ArrayList();

            for (int i = 0, n = pds.size(); i < n; i++)
                catIds.add(((GeneralParameterDescriptor) pds.get(i)).getId());

            return catIds;
        }

        public boolean paramExists(String refName) {
            return ParameterTables.paramExists(refName);
        }
    }
}