package com.pcmsolutions.gui;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 19-Nov-2003
 * Time: 13:07:46
 * To change this template use Options | File Templates.
 */
public class UserMessaging {

    public static final int YES_SELECTOR = 0;
    public static final int NO_SELECTOR = 1;
    public static final int ASK_SELECTOR = 2;

    public static final String ERROR_TITLE = "Error";
    public static final String INFO_TITLE = "Information";
    public static final String YES_NO_TITLE = "Question";
    public static final String OPERATION_FAILED_TITLE = "Operation Failed";
    public static final String OPERATION_CANCELLED_TITLE = "Operation Cancelled";

    // return CLOSED_OPTION or >=0
    public static int askOptions(String title, String msg, Object[] options) {
        return askOptions(title, msg, options, options[0]);
    }

    // return CLOSED_OPTION or >=0
    public static int askOptions(String title, String msg, Object[] options, Object defOpt) {
        return JOptionPane.showOptionDialog(ZoeosFrame.getInstance(), msg, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, defOpt);
    }

    public static void showError(String title, String msg) {
        JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), msg, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void showError(String msg) {
        JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), msg, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
    }

    public static void showOperationCancelled(String msg) {
        JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), msg, OPERATION_CANCELLED_TITLE, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showOperationFailed(String msg) {
        JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), msg, OPERATION_FAILED_TITLE, JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(String title, String msg) {
        JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showInfo(String msg) {
        JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), msg, INFO_TITLE, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showCommandFailed(String msg) {
        JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), msg, "Command Failed", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean askYesNo(String question, String title) {
        if (JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), question, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 0)
            return true;
        return false;
    }

    public static int askYesNoYesAll(String question, String title) {
        return JOptionPane.showOptionDialog(ZoeosFrame.getInstance(), question, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Yes", "No", "Yes to All"}, "Yes");
   }

    public static int askYesNoYesAllNoAll(String question, String title) {
        return JOptionPane.showOptionDialog(ZoeosFrame.getInstance(), question, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Yes", "No", "Yes to All", "No to All"}, "Yes");
   }

    public static boolean askYesNo(String question) {
        if (JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), question, YES_NO_TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 0)
            return true;
        return false;
    }
}
