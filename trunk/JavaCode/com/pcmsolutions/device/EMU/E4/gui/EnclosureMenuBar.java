package com.pcmsolutions.device.EMU.E4.gui;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.gui.FuzzyLineBorder;
import com.pcmsolutions.gui.JMenuSeparatorComponent;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;

/**
 * User: paulmeehan
 * Date: 02-May-2004
 * Time: 08:49:04
 */
public class EnclosureMenuBar implements ZDisposable {
    private static final int borderWidth = 3;

    private HashMap staticContexts = new HashMap();
    private HashMap<String, Component[]> dynamicContexts = new HashMap<String, Component[]>();
    private JMenuBar jMenuBar = new JMenuBar() {
        {
            setBorder(new FuzzyLineBorder(UIColors.getTableFirstSectionHeaderBG(), borderWidth, true, true));
            setMaximumSize(new Dimension(-1, 30));
            setPreferredSize(new Dimension(-1, 30));
            //setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            //setLayout(new BorderLayout(0,0));
        }

        public Color getBackground() {
            return UIColors.getDefaultBG();
        }

        public Color getForeground() {
            return UIColors.getDefaultFG();
        }
    };
    /*private JToolBar jToolBar = new JToolBar() {
        {
            setBorder(new FuzzyLineBorder(UIColors.getTableFirstSectionBG(), borderWidth, true, true));
            //  setFloatable(true);
        }

        public Color getBackground() {
            return UIColors.getDefaultBG();
        }

        public Color getForeground() {
            return UIColors.getDefaultFG();
        }
    }; */
    
    public JMenuBar getjMenuBar() {
        return jMenuBar;
    }

    /*  public JToolBar getjToolBar() {
          return jToolBar;
      }*/

    public void addStaticMenuContext(Component[] components, String contextName) {
        if (components.length == 0)
            return;
        Component[] comps = new Component[components.length + 1];
        System.arraycopy(components, 0, comps, 0, components.length);
        comps[components.length] = new JMenuSeparatorComponent();
        staticContexts.put(contextName, comps);
        addComponents(comps);
    }

    public void addStaticMenuContext(Component component, String contextName) {
        addStaticMenuContext(new Component[]{component}, contextName);
    }

    public void addDynamicMenuContext(Component[] components, String contextName) {
        if (components.length == 0) {
            dynamicContexts.put(contextName, components);
        } else {
            Component[] comps = new Component[components.length + 1];
            System.arraycopy(components, 0, comps, 0, components.length);
            comps[components.length] = new JMenuSeparatorComponent();
            dynamicContexts.put(contextName, comps);
            addComponents(comps);
        }
    }

    public void changeDynamicMenuContext(Component[] components, String contextName) {
        if (dynamicContexts.containsKey(contextName)) {
            replaceComponents(dynamicContexts.get(contextName), components);
            dynamicContexts.put(contextName, components);
        }
    }

    public void addDynamicMenuContext(Component component, String contextName) {
        addDynamicMenuContext(new Component[]{component, new JMenuSeparatorComponent()}, contextName);
    }

    public void removeAllDynamicMenuContexts() {
        for (Iterator i = dynamicContexts.keySet().iterator(); i.hasNext();)
            removeDynamicMenuContext(i.next().toString());
    }

    public void removeDynamicMenuContext(String contextName) {
        Component[] comps = (Component[]) dynamicContexts.get(contextName);
        if (comps != null)
            removeComponents(comps);
    }
    /*
    void updateComponents(Component[] comps) {
        for (int i = 0; i < comps.length; i++)
            jToolBar.add(comps[i]);
        jToolBar.revalidate();
        jToolBar.repaint();
    }

    void removeComponents(Component[] comps) {
        for (int i = 0; i < comps.length; i++)
            jToolBar.remove(comps[i]);
        jToolBar.revalidate();
        jToolBar.repaint();
    } */

    void addComponents(Component[] comps) {
        if (comps.length == 0)
            return;
        for (int i = 0; i < comps.length; i++)
            jMenuBar.add(comps[i]);
        jMenuBar.revalidate();
        jMenuBar.repaint();
    }

    void replaceComponents(Component[] comps1, Component[] comps2) {
        if (comps1.length == 0) {
            for (int j = 0; j < comps2.length; j++)
                jMenuBar.add(comps2[j]);
        } else {
            int i = jMenuBar.getComponentIndex(comps1[0]);
            if (i >= 0) {
                for (int j = 0; j < comps1.length; j++)
                    jMenuBar.remove(comps1[j]);

                for (int j = 0; j < comps2.length; j++)
                    jMenuBar.add(comps2[j], j + i);
            }
        }
        jMenuBar.revalidate();
        jMenuBar.repaint();
    }

    void removeComponents(Component[] comps) {
        boolean removed = false;
        for (int i = 0; i < comps.length; i++)
            if (jMenuBar.getComponentIndex(comps[i]) >= 0) {
                jMenuBar.remove(comps[i]);
                removed = true;
            }
        if (removed) {
            jMenuBar.revalidate();
            jMenuBar.repaint();
        }
    }

    public void zDispose() {
        dynamicContexts = null;
        staticContexts = null;
        jMenuBar = null;
    }
}
