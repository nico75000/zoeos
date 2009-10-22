package com.pcmsolutions.system;

import com.pcmsolutions.gui.UserMessaging;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 13-Mar-2003
 * Time: 06:46:42
 * To change this template use Options | File Templates.
 */
public abstract class AbstractZCommand <C> implements ZCommand {
    private List<C> targets = null;

    public boolean isSortable() {
        return true;
    }

    public final boolean isTargeted() {
        return targets != null;
    }

    public abstract String getPresentationCategory();

    public int getMnemonic() {
        return 0;
    }

    public boolean isSuitableAsButton() {
        return false;
    }

    public boolean isSuitableInToolbar() {
        return true;
    }

    public boolean overrides(ZCommand cmd) {
        return false;
    }

    public abstract String getPresentationString();

    public abstract String getDescriptiveString();

    public Icon getIcon() {
        return null;
    }

    public String getMenuPathString() {
        return "";
    }

    public boolean isSuitableForSorting() {
        return false;
    }

    public final void execute() throws CommandFailedException, ZCommandTargetsNotSpecifiedException {
        List<C> targets = getTargets();
        Iterator<C> i = targets.iterator();
        int curr = 0;
        try {
            while (i.hasNext()) {
                if (!handleTarget(i.next(), targets.size(), curr++))
                    break;
            }
        } catch (Exception e) {
            throw new CommandFailedException(e.getMessage());
        }
    }

    protected abstract boolean handleTarget(final C target, final int total, final int curr) throws Exception;

    public int getMinNumTargets() {
        return 1;
    }

    public int getMaxNumTargets() {
        return Integer.MAX_VALUE;
    }

    public final List<C> getTargets() throws ZCommandTargetsNotSpecifiedException {
        if (targets == null)
            throw new ZCommandTargetsNotSpecifiedException();
        ArrayList<C> clone = new ArrayList<C>();
        clone.addAll(targets);
        return clone;
    }

    public final int numTargets() {
        return targets.size();
    }

    public final void setTargets(Object target) throws IllegalArgumentException, ZCommandTargetsNotSuitableException {
        setTargets(new Object[]{target});
    }

    public final void setTargets(Object[] targets) throws ZCommandTargetsNotSuitableException {
        this.targets = null;
        if (targets == null || targets.length == 0)
            throw new ZCommandTargetsNotSuitableException("Null targets for ZCommand");

        int st = targets.length;

        if (st < getMinNumTargets())
            throw new ZCommandTargetsNotSuitableException("Insufficient number of targets");

        if (st > getMaxNumTargets())
            throw new ZCommandTargetsNotSuitableException("Too many targets");

        ArrayList<C> targetList = new ArrayList<C>();
        for (int n = 0; n < st; n++) {
            try {
                targetList.add((C) targets[n]);
            } catch (Exception e) {
                e.printStackTrace();
                throw new ZCommandTargetsNotSuitableException("Specified target(s) is incompatible type or is null");
            }
        }
        this.targets = targetList;
        try {
            acceptTargets();
        } catch (ZCommandTargetsNotSpecifiedException e) {
            SystemErrors.internal(e);
            throw new ZCommandTargetsNotSuitableException();
        }
    }

    protected void acceptTargets() throws ZCommandTargetsNotSuitableException, ZCommandTargetsNotSpecifiedException {
    }

    protected final List<C> getTargetObjects() throws ZCommandTargetsNotSpecifiedException {
        if (targets == null)
            throw new ZCommandTargetsNotSpecifiedException();
        return targets;
    }

    public int compareTo(Object o) {
        if (o != null) {
            return toString().compareTo(o.toString());
        }
        return 0;
    }

    public String toString() {
        String mp = getMenuPathString();
        if (mp == null || mp.equals(""))
            return (getPresentationString() == null ? "" : getPresentationString());

        return getMenuPathString();
    }

    protected static void handleZCommandFailed(Exception e) {
        UserMessaging.showCommandFailed(e.getMessage());
    }
}

