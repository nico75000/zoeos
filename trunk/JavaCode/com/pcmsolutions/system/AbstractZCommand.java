package com.pcmsolutions.system;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 13-Mar-2003
 * Time: 06:46:42
 * To change this template use Options | File Templates.
 */
public abstract class AbstractZCommand implements ZCommand {
    protected String presString;
    protected String descString;
    protected int numArgs;
    protected String[] argPresStrings;
    protected String[] argDescStrings;
    protected Class tc;
    protected Object target;
    protected int numModes = 1;

    public AbstractZCommand(Class tc) {
        this.tc = tc;
    }

    public AbstractZCommand(Class tc, String presString, String descString, String[] argPresStrings, String[] argDescStrings) {
        this.tc = tc;
        init(presString, descString, argPresStrings, argDescStrings);
    }

    protected void init(String presString, String descString, String[] argPresStrings, String[] argDescStrings) {
        this.argPresStrings = argPresStrings;
        if (this.argPresStrings == null)
            this.argPresStrings = new String[0];

        this.argDescStrings = argDescStrings;
        if (this.argDescStrings == null)
            this.argDescStrings = new String[0];

        if (this.argDescStrings.length != this.argPresStrings.length)
            throw new IllegalArgumentException();

        this.descString = descString;
        this.numArgs = this.argPresStrings.length;
        this.presString = presString;
    }

    public int getMnemonic() {
        return 0;
    }

    public boolean isSuitableAsLaunchButton() {
        return false;
    }

    /*  protected void verifyMode(int index) {
          if (index < 0 || index > numModes - 1)
              throw new IllegalArgumentException("Illegal mode for ZCommand");
      }*/

    public String getPresentationString() {
        return presString;
    }

    public String getDescriptiveString() {
        return descString;
    }

    public int getNumberOfArguments()              // arguments are all strings
    {
        return numArgs;
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        return null;
    }

    public String[] getArgumentPresentationStrings() {
        String[] out = new String[numArgs];
        System.arraycopy(argPresStrings, 0, out, 0, numArgs);
        return out;
    }

    public String[] getArgumentDescriptiveStrings() {
        String[] out = new String[numArgs];
        System.arraycopy(argDescStrings, 0, out, 0, numArgs);
        return out;
    }

    public String getSummaryString(Object[] arguments) throws IllegalArgumentException {
        return " ";
    }

    public Icon getIcon() {
        return null;
    }

    // if returns null no verification of command required
    public String getVerificationString() {
        return null;
    }

    public String getMenuPathString() {
        return "";
    }

    public boolean isSuitableForSorting() {
        return false;
    }

    public abstract void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException;  // IllegalArgumentException thrown for insufficient number of arguments

    public void setTarget(Object t) {
        if (t == null)
            throw new IllegalArgumentException("null target");

        if (!tc.isInstance(t))
            throw new IllegalArgumentException("Invalid type for ZCommand target");
        this.target = t;
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
            return (presString == null ? "" : presString);

        return getMenuPathString();
    }

    /*  public static String getComparativeDescription(){
          return "";
      }*/
}

