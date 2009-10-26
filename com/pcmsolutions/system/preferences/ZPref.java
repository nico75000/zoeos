package com.pcmsolutions.system.preferences;

import com.pcmsolutions.system.ZDisposable;

import javax.swing.event.ChangeListener;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 01-Dec-2003
 * Time: 05:30:10
 * To change this template use Options | File Templates.
 */
public interface ZPref extends ZDisposable{
    public void addChangeListener(ChangeListener cl);
    public void removeChangeListener(ChangeListener cl);
    public void putDefault();
    public void putValueString(String strVal);
    public String getValueString();
    public Object getValueObject();
    public String getDefaultString();
    public Preferences getPrefs();
    public String getCategory();
    public String getPresentationName();
    public String getDescription();
}
