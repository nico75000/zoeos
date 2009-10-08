package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.gui.table.DragAndDropTable;
import com.pcmsolutions.device.EMU.E4.selections.*;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 19-May-2003
 * Time: 19:23:57
 * To change this template use Options | File Templates.
 */
public class VoiceOverviewTableTransferHandler extends TransferHandler implements Transferable {
    public static final DataFlavor zoneFlavor = new DataFlavor(ZoneSelection.class, "ZoneSelection");
    protected static ZoneSelection zs;

    public static final DataFlavor voiceFlavor = new DataFlavor(VoiceSelection.class, "VoiceSelection");
    protected static VoiceSelection vs;

    public static final VoiceOverviewTableTransferHandler.VoiceParameterCollectionDataFlavor voiceParameterCollectionFlavor = new VoiceOverviewTableTransferHandler.VoiceParameterCollectionDataFlavor();
    protected static VoiceParameterSelectionCollection vpsc;

    protected DataFlavor activeFlavor;
    protected Object activeSelection;

    public static class VoiceParameterCollectionDataFlavor extends DataFlavor {
        protected int category;
        protected Integer[] ids;
        protected int count;

        public boolean containsId(Integer id) {
            if (id != null)
                for (int i = 0,j = ids.length; i < j; i++)
                    if (ids[i].equals(id))
                        return true;
            return false;
        }

        public boolean canApplyIdsToZones() {
            int iv;
            for (int i = 0,j = ids.length; i < j; i++) {
                iv = ids[i].intValue();
                if (!((iv >= 38 && iv <= 40) || iv == 42 || (iv >= 44 && iv <= 52)))
                    return false;
            }
            return true;
        }

        public int getCount() {
            return count;
        }

        protected void setCount(int count) {
            this.count = count;
        }

        public boolean equals(DataFlavor that) {
            return ((that instanceof VoiceOverviewTableTransferHandler.VoiceParameterCollectionDataFlavor) && ((VoiceOverviewTableTransferHandler.VoiceParameterCollectionDataFlavor) that).getCategory() == category);
        }

        public int getCategory() {
            return category;
        }

        protected void setCategory(int category) {
            this.category = category;
        }

        public Integer[] getIds() {
            return (Integer[]) ids.clone();
        }

        protected void setIds(Integer[] ids) {
            this.ids = ids;
        }

        public VoiceParameterCollectionDataFlavor() {
            this(VoiceParameterSelection.VOICE_GENERAL, new Integer[]{}, 0);
        }

        public VoiceParameterCollectionDataFlavor(int category, Integer[] ids, int count) {
            super(LinkParameterSelectionCollection.class, "VoiceParameterSelectionCollection");
            this.category = category;
            this.ids = ids;
            this.count = count;
        }
    }


    public VoiceOverviewTableTransferHandler() {
    }

    public void exportAsDrag(JComponent comp, InputEvent e, int action) {
        super.exportAsDrag(comp, e, action);
    }

    public boolean importData(JComponent comp, Transferable t) {
        return false;
    }

    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        return false;
    }

    public int getSourceActions(JComponent c) {
        if ((c instanceof VoiceOverviewTable && ((VoiceOverviewTable) c).hasValidVoiceParameterSelectionCollection()) || c instanceof VoiceOverviewTable.VoiceAndZoneSelectionProvider)
            return TransferHandler.COPY;
        return 0;
    }

    protected Transferable createTransferable(JComponent c) {
        if (c instanceof VoiceOverviewTable.VoiceAndZoneSelectionProvider) {
            VoiceOverviewTable.VoiceAndZoneSelectionProvider vzp = (VoiceOverviewTable.VoiceAndZoneSelectionProvider) c;

            switch (vzp.whatIsAvailable()) {
                case VoiceOverviewTable.VoiceAndZoneSelectionProvider.NOTHING_AVAILABLE:
                    break;
                case VoiceOverviewTable.VoiceAndZoneSelectionProvider.VOICES_AVAILABLE:
                    activeFlavor = voiceFlavor;
                    activeSelection = vs = vzp.getVoiceSelection();
                    if (c instanceof DragAndDropTable)
                        ((DragAndDropTable) c).clearSelection();
                    return this;
                case VoiceOverviewTable.VoiceAndZoneSelectionProvider.ZONES_AVAILABLE:
                    activeFlavor = zoneFlavor;
                    activeSelection = zs = vzp.getZoneSelection();
                    if (c instanceof DragAndDropTable)
                        ((DragAndDropTable) c).clearSelection();
                    return this;
            }
        } else if (c instanceof VoiceOverviewTable) {
            activeSelection = vpsc = ((VoiceOverviewTable) c).getSelection();
            activeFlavor = voiceParameterCollectionFlavor;
            voiceParameterCollectionFlavor.setCategory(vpsc.getCategory());
            voiceParameterCollectionFlavor.setIds(vpsc.getIds());
            voiceParameterCollectionFlavor.setCount(vpsc.getCount());
            if (c instanceof DragAndDropTable)
                ((DragAndDropTable) c).clearSelection();
            return this;
        }

        return null;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{activeFlavor};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(activeFlavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor))
            return activeSelection;
        return null;
    }
}
