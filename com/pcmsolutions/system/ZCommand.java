package com.pcmsolutions.system;

import javax.swing.*;


/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 12-Mar-2003
 * Time: 00:37:57
 * To change this template use Options | File Templates.
 */
public interface ZCommand extends Comparable {

    public int getMnemonic();

    public boolean isSuitableAsLaunchButton();

    public void setTarget(Object t) throws IllegalArgumentException;

    public String getPresentationString();

    public String getDescriptiveString();

    public int getNumberOfArguments();              // arguments are all strings

    public String[] getArgumentPresentationStrings();

    public String[] getArgumentDescriptiveStrings();

    // currently supporting JCombo and JTextField
    public JComponent getComponentForArgument(int index) throws IllegalArgumentException;  // exception for index out of range

    public String getSummaryString(Object[] arguments) throws IllegalArgumentException;

    public Icon getIcon();

    // if returns null no verification of command required
    public String getVerificationString();

    // preceed each new path element with ";", do not place ";" after last path element
    // "" means root menu
    // null is illegal
    // legal ->     ;foo;bar;foo
    // illegal -> foo;bar;foo;
    public String getMenuPathString();

    public boolean isSuitableForSorting();

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException;  // IllegalArgumentException thrown for insufficient number of arguments

}
