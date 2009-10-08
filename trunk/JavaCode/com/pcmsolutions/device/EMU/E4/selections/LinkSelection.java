package com.pcmsolutions.device.EMU.E4.selections;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.preset.*;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 07-Aug-2003
 * Time: 19:25:16
 * To change this template use Options | File Templates.
 */
public class LinkSelection extends AbstractE4Selection {
    protected IsolatedPreset.IsolatedLink[] links;
    protected ReadablePreset.ReadableLink[] readableLinks;

    public LinkSelection(DeviceContext dc, ReadablePreset.ReadableLink[] readableLinks) {
        super(dc);
        this.readableLinks = readableLinks;
    }

    public int linkCount() {
        return readableLinks.length;
    }

    public ReadablePreset.ReadableLink[] getReadableLinks() {
        return (ReadablePreset.ReadableLink[]) Arrays.asList(readableLinks).toArray(new ReadablePreset.ReadableLink[readableLinks.length]);

    }

    public IsolatedPreset.IsolatedLink[] getIsolatedLinks() {
        if (links == null) {
            links = new IsolatedPreset.IsolatedLink[readableLinks.length];
        }
        for (int i = 0,j = readableLinks.length; i < j; i++)
            if (links[i] == null)
                links[i] = getIsolatedLink(i);

        return links;
    }

    public IsolatedPreset.IsolatedLink getIsolatedLink(int i) {
        if (links == null)
            links = new IsolatedPreset.IsolatedLink[readableLinks.length];

        if (i >= 0 && i < readableLinks.length) {
            if (links[i] == null)
                try {
                    links[i] = readableLinks[i].getIsolated();
                    return links[i];
                } catch (NoSuchPresetException e) {
                    e.printStackTrace();
                } catch (PresetEmptyException e) {
                    e.printStackTrace();
                } catch (NoSuchLinkException e) {
                    e.printStackTrace();
                }
        }
        return null;
    }
}
