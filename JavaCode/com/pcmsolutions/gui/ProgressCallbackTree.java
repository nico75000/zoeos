package com.pcmsolutions.gui;

import com.pcmsolutions.system.StringFinalizer;

import java.util.Vector;

/**
 * User: paulmeehan
 * Date: 29-Feb-2004
 * Time: 10:11:04
 */
public class ProgressCallbackTree implements ProgressCallback, LabellingParent, StringFinalizer {
    static final Vector<ProgressCallback> active = new Vector<ProgressCallback>();

    static final int MAX = 1000;
    static final String SEP = "[ ";
    ProgressSession sess;
    String label = "";
    boolean done = false;
    boolean childLabellingEnabled = true;

    public ProgressCallbackTree(ProgressSession sess, String label) {
        this.sess = sess;
        this.label = label;
        sess.updateTitle(label);
        active.add(this);
    }

    public static boolean hasActiveTasks(){
        return active.size() > 0;
    }

    public ProgressCallbackTree() {
        this(ZoeosFrame.getInstance().getProgressSession("", MAX, true), "");
    }

    public ProgressCallbackTree(String label, boolean childLabellingEnabled) {
        this(ZoeosFrame.getInstance().getProgressSession(label, MAX, true), label);
        this.childLabellingEnabled = childLabellingEnabled;
    }

    // fraction 0..1
    // 1 = signifies hasCompleted
    public void updateProgress(double p) {
        if (p >= 1) {
            sess.end();
            synchronized (this) {
                done = true;
                active.remove(this);
                notifyAll();
            }

        } else
            sess.updateStatus((int) (MAX * p));
    }

    public synchronized boolean isCancelled() {
        return sess.isCancelled();
    }

    public synchronized boolean blockWhileActive() {
        while (isActive())
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        return isCancelled();
    }

    public ProgressCallback[] splitTask(int count, final boolean labellingEnabled) {
        ProgressCallback[] kids = new ProgressCallback[count];
        double div = 1.0D / kids.length;
        for (int i = 0; i < kids.length; i++) {
            double l = i * div;
            double h = (i == kids.length - 1 ? 1 : l + div);
            kids[i] = new Impl_ProgressCallback(this, l, h, labellingEnabled);
        }
        return kids;
    }

    public synchronized boolean isActive() {
        return !(done || isCancelled());
    }

    public void updateLabel(String label) {
        this.label = label;
        sess.updateTitle(finalizeString(this.label));
    }

    public void updateChildLabel(String label) {
        if (childLabellingEnabled)
            sess.updateTitle(finalizeString(this.label + SEP + label));
    }

    public String finalizeString(String s) {
        return s;
    }

}
class Impl_ProgressCallback implements ProgressCallback, LabellingParent {
    String label = "";
    ProgressCallback parent;
    double low;
    double high;
    boolean done = false;
    boolean labellingEnabled;

    public Impl_ProgressCallback(ProgressCallback parent, double low, double high, boolean labellingEnabled) {
        this.parent = parent;
        if (low == high)
            throw new IllegalArgumentException();
        this.low = low;
        this.high = high;
        this.labellingEnabled = labellingEnabled;
    }

    public ProgressCallback[] splitTask(int count, final boolean enableLabelling) {
        ProgressCallback[] kids = new ProgressCallback[count];
        double div = (high - low) / kids.length;
        for (int i = 0; i < kids.length; i++) {
            double l = low + i * div;
            double h = (i == kids.length - 1 ? high : l + div);
            kids[i] = new Impl_ProgressCallback(this, l, h, enableLabelling);
        }
        return kids;
    }

    public synchronized boolean isActive() {
        return !(done || isCancelled());
    }

    public synchronized boolean blockWhileActive() {
        while (isActive())
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        return isCancelled();
    }

    public void updateChildLabel(String label) {
        if (labellingEnabled && parent instanceof LabellingParent)
            ((LabellingParent) parent).updateChildLabel(this.label + ProgressCallbackTree.SEP + label);
    }

    public void updateLabel(String label) {
        this.label = label;
        if (labellingEnabled && parent instanceof LabellingParent)
            ((LabellingParent) parent).updateChildLabel(this.label);
    }

    // fraction 0..1
    // 1 = signifies hasCompleted
    public void updateProgress(double p) {
        if (p >= 1) {
            parent.updateProgress(high);
            synchronized (this) {
                done = true;
                notifyAll();
            }
        }
        parent.updateProgress(low + (high - low) * p);
    }

    public synchronized boolean isCancelled() {
        return parent.isCancelled();
    }
}

interface LabellingParent {
    public void updateChildLabel(String label);
}