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
    public String getPresentationCategory();

    public int getMnemonic();

    public boolean isSuitableAsButton();

    public boolean isSuitableInToolbar();

    public boolean overrides(ZCommand cmd);

    public boolean isSortable();

    public boolean isTargeted();

    public void setTargets(Object target) throws ZCommandTargetsNotSuitableException;

    public void setTargets(Object[] targets) throws ZCommandTargetsNotSuitableException;

    public int getMinNumTargets();

    public int getMaxNumTargets();

    public String getPresentationString();

    public String getDescriptiveString();

    public Icon getIcon();

    // preceed each new path element with ";", do not place ";" after last path element
    // "" means root menu
    // null is illegal
    // legal ->     ;foo;bar;foo
    // illegal -> foo;bar;foo;
    public String getMenuPathString();

    public boolean isSuitableForSorting();

    public void execute() throws CommandFailedException, ZCommandTargetsNotSpecifiedException;  // IllegalArgumentException thrown for insufficient number of arguments
}
