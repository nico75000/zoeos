package com.pcmsolutions.gui;

import com.pcmsolutions.system.Zoeos;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 04-Nov-2003
 * Time: 01:24:26
 * To change this template use Options | File Templates.
 */
public class LicenseAgreementDialog extends ZDialog {
    private static String text = "ZoeOS binary end-user license agreement \nThis agreement is entered between the Licensor (Zuonics Ltd., Arklow, Ireland) and You (the User).  ZoeOS Demo (the Software) is distributed on an \"AS IS\" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. IN NO EVENT WILL LICENSOR BE LIABLE TO YOU FOR ANY INDIRECT, INCIDENTAL, SPECIAL OR CONSEQUENTIAL DAMAGES OR ANY LOSS OF REVENUE, DATA, USE, OR PROFITS.  You may not modify, decompile or disassemble the Software. You may not distribute modified or derivative copies of the Software. You may not, without Licensor's prior written consent, charge for copies or modified copies of the Software, charge for any service that uses Software, or charge for support services associated with the Software. You may not rent, lease, lend, or in any way distribute or transfer any rights in this agreement or the Software to third parties without Licensor's written approval. All rights not expressly granted to you are retained by Licensor. Each copy made of the Software must be unmodified in its contents and reproduce all copyright and other proprietary rights notices on or in the Software.  THIS IS PRE-RELEASE SOFTWARE and as such it should be used for evaluation and testing only, not day-to-day use. Data formats and functionality might change without notice of any kind.   This software is Copyright (c) 2003 Zuonics Ltd..";

    public LicenseAgreementDialog(Frame owner) throws HeadlessException {
        super(owner, Zoeos.versionStr + " License Agreement", true);
        JPanel mp = new JPanel(new BorderLayout());
        JTextPane tp = new JTextPane();
        tp.setEditable(false);
        tp.setText(text);
        JScrollPane sp = new JScrollPane(tp);
        JButton acceptButton = new JButton(new AbstractAction("I ACCEPT") {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JButton declineButton = new JButton(new AbstractAction("DECLINE") {
            public void actionPerformed(ActionEvent e) {
                System.exit(-1);
            }
        });
        JPanel bp = new JPanel();
        bp.add(acceptButton);
        bp.add(declineButton);

        mp.add(bp, BorderLayout.SOUTH);
        mp.add(sp, BorderLayout.CENTER);
        this.setContentPane(mp);
        this.setResizable(false);
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        pack();
        this.setSize(300, 300);
    }
}
