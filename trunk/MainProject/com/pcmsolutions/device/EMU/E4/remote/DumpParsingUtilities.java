package com.pcmsolutions.device.EMU.E4.remote;

import com.pcmsolutions.device.EMU.E4.parameter.ParameterContext;

import java.io.ByteArrayInputStream;
import java.util.Set;

/**
 * User: paulmeehan
 * Date: 25-Aug-2004
 * Time: 12:02:25
 */
public class DumpParsingUtilities {
    public static Integer[] parseDumpStream(ByteArrayInputStream dis, ParameterContext pc) {
        Set setIds = pc.getIds();
        return parseDumpStream(dis, pc, setIds.size());
    }

    public static Integer[] parseDumpStream(ByteArrayInputStream dis, ParameterContext pc, int num) {
        Set setIds = pc.getIds();
        if (num > setIds.size())
            throw new IllegalArgumentException("too many ids for parameter context");
        int nBytes = num * 2;
        Integer[] arrIds = (Integer[]) setIds.toArray(new Integer[setIds.size()]);

        Integer[] idVals = new Integer[nBytes];
        byte[] dumpField = new byte[nBytes];
        dis.read(dumpField, 0, nBytes);

        for (int n = 0; n < nBytes; n += 2) {
            idVals[n] = arrIds[n / 2];
            idVals[n + 1] = com.pcmsolutions.device.EMU.E4.remote.SysexHelper.DataIn(dumpField, n);
        }
        return idVals;
    }

    public static Integer[] parseDumpStream(ByteArrayInputStream dis, Integer[] arrIds) {
        int nBytes = arrIds.length * 2;

        Integer[] idVals = new Integer[nBytes];
        byte[] dumpField = new byte[nBytes];
        dis.read(dumpField, 0, nBytes);

        for (int n = 0; n < nBytes; n += 2) {
            idVals[n] = arrIds[n / 2];
            if (idVals[n].intValue() == 61) // delay
                idVals[n + 1] = com.pcmsolutions.device.EMU.E4.remote.SysexHelper.UnsignedDataIn(dumpField, n);
            else
                idVals[n + 1] = com.pcmsolutions.device.EMU.E4.remote.SysexHelper.DataIn(dumpField, n);
        }
        return idVals;
    }
}
