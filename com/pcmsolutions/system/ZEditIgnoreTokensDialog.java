package com.pcmsolutions.system;

import com.pcmsolutions.comms.MidiSystemFacade;
import com.pcmsolutions.gui.ZDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 03-Sep-2003
 * Time: 18:41:44
 * To change this template use Options | File Templates.
 */
public class ZEditIgnoreTokensDialog extends ZDialog implements MidiSystemFacade.MidiSystemListener {
    protected JList list;
    private static int defHeight = 300;

    public ZEditIgnoreTokensDialog(Frame owner, boolean modal) throws HeadlessException {
        super(owner, "Port Ignore Tokens", modal);
        this.getContentPane().setLayout(new BorderLayout());

        list = new JList();

        JScrollPane sp = new JScrollPane(list);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        getContentPane().add(sp, BorderLayout.CENTER);

        JPanel p = new JPanel();
        JButton ok = new JButton(new AbstractAction("OK") {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        JButton add = new JButton(new AbstractAction("Add") {
            public void actionPerformed(ActionEvent e) {
                final String s = JOptionPane.showInputDialog("New token: ");
                if (s != null && !s.equals(""))
                    MidiSystemFacade.getInstance().addIgnoreToken(s);
            }
        });

        JButton remove = new JButton(new AbstractAction("Remove") {
            public void actionPerformed(ActionEvent e) {
                final Object[] sv = list.getSelectedValues();
                for (int i = 0; i < sv.length; i++)
                    MidiSystemFacade.getInstance().removeIgnoreToken(sv[i]);
            }
        });

        JButton removeAll = new JButton(new AbstractAction("Remove All") {
            public void actionPerformed(ActionEvent e) {
                MidiSystemFacade.getInstance().clearIgnoreTokens();
            }
        });
        p.add(ok);
        p.add(add);
        p.add(remove);
        p.add(removeAll);
        getContentPane().add(p, BorderLayout.SOUTH);

        loadList();
        MidiSystemFacade.getInstance().addMidiSystemListener(this);
        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
    }

    public void refresh() {
        loadList();
    }

    private void adjustSize() {
        pack();
        Dimension d = getSize();
        if (d.getHeight() > defHeight)
            setSize(new Dimension((int) d.getWidth(), defHeight));
    }


    protected void loadList() {
        list.setListData(MidiSystemFacade.getInstance().getIgnoreTokens());
        adjustSize();
    }

    public void midiSystemChanged(MidiSystemFacade msf) {
        loadList();
    }

}
