package com.pcmsolutions.system;


/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 13-Mar-2003
 * Time: 06:46:42
 * To change this template use Options | File Templates.
 */
public abstract class AbstractZMTCommand extends AbstractZCommand implements ZMTCommand {
    protected Object[] targets = null;

    public AbstractZMTCommand(Class tc) {
        super(tc);
    }

    public AbstractZMTCommand(Class tc, String presString, String descString, String[] argPresStrings, String[] argDescStrings) {
        super(tc, presString, descString, argPresStrings, argDescStrings);
    }

    public int getMinNumTargets() {
        return 1;
    }

    public int getMaxNumTargets() {
        return Integer.MAX_VALUE;
    }

    public ZMTCommand getNextMode() {
        return null;
    }

    public void setTargets(Object[] targets) throws IllegalArgumentException, ZMTCommandTargetsNotSuitableException {
        if (targets == null)
            throw new IllegalArgumentException("Null targets for ZMTCommand");

        int st = targets.length;

        if (st < getMinNumTargets())
            throw new IllegalArgumentException("Insufficient number of targets");

        if (st > getMaxNumTargets())
            throw new IllegalArgumentException("Too many targets");

        acceptTargets(targets);

        for (int n = 0; n < st; n++) {
            if (!tc.isInstance(targets[n]) || targets[n] == null)
                throw new IllegalArgumentException("Specified target(s) is incompatible type or is null");
        }
        this.targets = new Object[targets.length];
        System.arraycopy(targets, 0, this.targets, 0, targets.length);
    }

/*    public void setTarget(Object t) {
        try {
            setTargets(new Object[]{t});
        } catch (ZMTCommandTargetsNotSuitableException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
  */
    protected void acceptTargets(Object[] targets) throws ZMTCommandTargetsNotSuitableException {
    }
}

