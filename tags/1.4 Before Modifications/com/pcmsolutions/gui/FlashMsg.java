package com.pcmsolutions.gui;

import com.pcmsolutions.system.Zoeos;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 30-Apr-2003
 * Time: 03:14:29
 * To change this template use Options | File Templates.
 */
public class FlashMsg extends ZWindow {

    public static Color colorWarning = Color.orange;
    public static Color colorError = new Color(250, 150, 150);
    public static Color colorInfo = new Color(175, 175, 250);

    private static long defFlashInterval = 200;
    private static long defDisplayTime = 1000;

    private String message;
    private long flashInterval;
    private Color color;
    //private Color fgColor;
    private long displayTime;
    private JLabel j;
    private volatile boolean terminate = false;
    private Thread flashThread = null;

    public static volatile boolean globalDisable = false;

    static final Font msgFont = new Font("Arial", Font.PLAIN, 16);

    public FlashMsg(Frame owner, Component centreAboutComponent, long displayTime, long flashInterval, Color color, String message) throws HeadlessException {
        super(owner, centreAboutComponent);
        this.color = color;
        this.displayTime = displayTime;
        this.flashInterval = flashInterval;
        this.message = message;
        begin();
    }

    public FlashMsg(Frame owner, long displayTime, long flashInterval, Color color, String message) throws HeadlessException {
        this(owner, owner, displayTime, flashInterval, color, message);
    }

    public FlashMsg(Frame owner, Component centreAboutComponent, Color color, String message) throws HeadlessException {
        super(owner, centreAboutComponent);
        this.color = color;
        this.displayTime = defDisplayTime;
        this.flashInterval = defFlashInterval;
        this.message = message;
        begin();
    }

    public FlashMsg(Frame owner, Color color, String message) throws HeadlessException {
        this(owner, owner, color, message);
    }

    public FlashMsg(Frame owner, Color color, String message, long displayTime) throws HeadlessException {
        this(owner, owner, displayTime, defFlashInterval, color, message);
    }
    /*public FlashMsg(Frame owner, long displayTime, Color color, long flashInterval, String message) throws HeadlessException {
        super(owner);
        this.color = color;
        this.displayTime = displayTime;
        this.flashInterval = flashInterval;
        this.message = message;
        begin();
    } */

    private void begin() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                j = new JLabel(message);
                //j.setFont(new Font("MONOSPACED", Font.BOLD, 18));
                j.setFont(msgFont);
                //j.setOpaque(false);
                j.setForeground(color);
                j.setAlignmentX(Component.CENTER_ALIGNMENT);
                getContentPane().setBackground(color.brighter());
                getContentPane().setLayout(new BorderLayout());
                getContentPane().add(j, BorderLayout.CENTER);
                pack();
                if (!globalDisable)
                    show();
                flashThread = new FlashThread();
                flashThread.start();
            }
        });
    }

    private class FlashThread extends Thread {
        public void run() {
            long beginTime = Zoeos.getZoeosTime();
            while (Zoeos.getZoeosTime() < beginTime + displayTime && terminate == false) {
                try {
                    Thread.sleep(flashInterval);
                } catch (InterruptedException e) {
                }
                switchState();
            }
            FlashMsg.this.dispose();
        }
    }

    public void terminate() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                terminate = true;
                try {
                    flashThread.interrupt();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void switchState() {
        // try {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (globalDisable) {
                    FlashMsg.this.setVisible(false);
                    return;
                }
                Color fgLabel = j.getForeground();
                j.setForeground(FlashMsg.this.getContentPane().getBackground());
                FlashMsg.this.getContentPane().setBackground(fgLabel);
            }
        });
        //   } catch (InterruptedException e) {
        // } catch (InvocationTargetException e) {
        //  }
    }
}
