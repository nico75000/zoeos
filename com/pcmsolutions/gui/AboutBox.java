package com.pcmsolutions.gui;

import com.jidesoft.dialog.BannerPanel;
import com.pcmsolutions.system.Zoeos;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 23-Sep-2003
 * Time: 05:24:57
 * To change this template use Options | File Templates.
 */
public class AboutBox extends JDialog {
    public AboutBox(Frame owner) {
        super(owner, "About", true);
        JLabel lbl = new JLabel();

        Border b1 = new BevelBorder(BevelBorder.LOWERED);
        Border b2 = new EmptyBorder(5, 5, 5, 5);
        lbl.setBorder(new CompoundBorder(b1, b2));

        String message = Zoeos.aboutMessage;
        JTextArea txt = new JTextArea(message);
        txt.setBorder(new EmptyBorder(5, 10, 5, 10));
        //txt.setFont(new Font("Helvetica", Font.BOLD, 12));
        txt.setFont(getFont().deriveFont(Font.BOLD));
        txt.setEditable(false);
        txt.setBackground(getBackground());

        message = "JVM version " +
                System.getProperty("java.version") + "\n" +
                " by " + System.getProperty("java.vendor");
        txt = new JTextArea(message);
        txt.setBorder(new EmptyBorder(5, 10, 5, 10));
        //txt.setFont(new Font("Arial", Font.PLAIN, 12));
        txt.setEditable(false);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setBackground(getBackground());

        BannerPanel bp = new BannerPanel("About ZoeOS", "", new ImageIcon(ZoeosFrame.getInstance().getIconImage()));
        getContentPane().add(bp, BorderLayout.NORTH);
        getContentPane().add(txt, BorderLayout.CENTER);

        final JButton btOK = new JButton("OK");
        ActionListener lst = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
        btOK.addActionListener(lst);

        WindowListener wl = new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                btOK.requestFocus();
            }
        };
        addWindowListener(wl);

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }
}
