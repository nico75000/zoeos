package com.pcmsolutions.device.EMU.E4.selections;

import com.pcmsolutions.device.EMU.E4.preset.PresetContextMacros;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterException;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.system.IntPool;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 13-Aug-2003
 * Time: 13:43:12
 * To change this template use Options | File Templates.
 */
public class CordParameterSelection extends VoiceParameterSelection {
    protected int highCord = Integer.MIN_VALUE;
    protected int lowCord = Integer.MAX_VALUE;
    protected int[] selCols;
    protected int[] selRows;
    //protected Integer[] normalIds;
    HashMap row2NormalRow = new HashMap();

    public CordParameterSelection(ReadablePreset.ReadableVoice voice, Integer[] ids) throws ParameterException, PresetException, EmptyException {
        super(voice, ids, VoiceParameterSelection.VOICE_CORDS);
        determineSelectionConfiguration();
    }

    public CordParameterSelection(VoiceParameterSelection vps) {
        super(vps);
        determineSelectionConfiguration();
    }

    protected void determineSelectionConfiguration() {
        int cord;
        int col;
        ArrayList selRows = new ArrayList();
        ArrayList selCols = new ArrayList();

        row2NormalRow.clear();
        int nextNormalRow = 0;
        for (int i = 0,j = ids.length; i < j; i++) {
            cord = (ids[i].intValue() - 129) / 3;
            col = (ids[i].intValue() - 129) % 3;

            if (!selRows.contains(IntPool.get(cord))) {
                selRows.add(IntPool.get(cord));
                row2NormalRow.put(IntPool.get(cord), IntPool.get(nextNormalRow++));
            }

            if (!selCols.contains(IntPool.get(col)))
                selCols.add(IntPool.get(col));

            if (cord > highCord)
                highCord = cord;

            if (cord < lowCord)
                lowCord = cord;
        }
        this.selRows = new int[selRows.size()];
        this.selCols = new int[selCols.size()];

        for (int i = 0,j = selRows.size(); i < j; i++)
            this.selRows[i] = ((Integer) selRows.get(i)).intValue();

        for (int i = 0,j = selCols.size(); i < j; i++)
            this.selCols[i] = ((Integer) selCols.get(i)).intValue();

    }

    public void render(ContextEditablePreset.EditableVoice[] voices, int startCord, boolean normalized) {
        Integer[] finalIds;
        if (normalized)
            finalIds = getIdsNormalized(startCord);
        else
            finalIds = getIdsPositioned(startCord);

        /* for (int v = 0,x = voices.length; v < x; v++)
             for (int i = 0, n = finalIds.length; i < n; i++) {
                 if (finalIds[i] != null) // might have been positioned beyond legal range in getIdsPositioned
                     try {
                         voices[v].setVoicesParam(finalIds[i], vals[i]);
                     } catch (IllegalParameterIdException e) {
                         e.printStackTrace();
                     } catch (ParameterValueOutOfRangeException e) {
                         e.printStackTrace();
                     } catch (DeviceException e) {
                         e.printStackTrace();
                     } catch (EmptyException e) {
                         e.printStackTrace();
                     } catch (NoSuchVoiceException e) {
                         e.printStackTrace();
                     }
             }
             */
        try {
            PresetContextMacros.setContextVoicesParam(voices, finalIds, vals);
        }catch (PresetException e) {
            e.printStackTrace();
        }
    }

    public int getHighCord() {
        return highCord;
    }

    public int getLowCord() {
        return lowCord;
    }

    public Integer[] getIdsNormalized(int cord) {
        Integer[] outIds = new Integer[ids.length];

        int row;
        int normalRow;
        int newId;
        for (int i = 0,j = ids.length; i < j; i++) {
            row = (ids[i].intValue() - 129) / 3;
            normalRow = ((Integer) row2NormalRow.get(IntPool.get(row))).intValue();
            newId = 129 + (ids[i].intValue() % 3) + (normalRow * 3) + cord * 3;
            if (newId > 182)
                outIds[i] = null;
            else
                outIds[i] = IntPool.get(newId);
        }

        return outIds;
    }

    public Integer[] getIdsPositioned(int cord) {
        Integer[] outIds = new Integer[ids.length];

        for (int i = 0,j = ids.length; i < j; i++) {
            int newId = ids[i].intValue() - (lowCord * 3) + cord * 3;
            if (newId > 182)
                outIds[i] = null;
            else
                outIds[i] = IntPool.get(newId);
        }

        return outIds;
    }

    public int[] getSelCols() {
        return (int[]) selCols.clone();
    }

    public int[] getSelRows() {
        return (int[]) selRows.clone();
    }
}
