package com.pcmsolutions.smdi;

import com.pcmsolutions.gui.ProgressCallback;

/**
 * User: paulmeehan
 * Date: 27-Feb-2004
 * Time: 03:38:23
 */
public interface SMDITransferInstance {

    public int getPacketSizeInBytes();

    public int getHAID();

    public int getID();

    public int getSample();

    public ProgressCallback getProgressCallback();
}
