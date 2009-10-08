package com.pcmsolutions.device.EMU.E4.gui.colors;

import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 01-Jun-2003
 * Time: 18:45:18
 * To change this template use Options | File Templates.
 */
public class UIColors implements Serializable {

    public static int tableAlpha = 85;
    public static int bubbleAlpha = 150;
    //public static int tableAlpha = 65;
    public static int listAlpha = 100;
    public static int iconAlpha = 210;
    public static float tableSelectionGradientFactor = (float) 0.75;
    public static float tableDropTargetGradientFactor = (float) 2.0;

    // default root colors

/*    private final static Color grade1 = new Color(225, 225, 250);
    private final static Color grade2 = new Color(150, 150, 225);
    private final static Color grade3 = new Color(125, 100, 150);
    private final static Color grade4 = new Color(100, 100, 150);
  */
    private final static Color grade1 = new Color(160, 160, 187);
    private final static Color grade2 = new Color(210, 210, 230);
    private final static Color grade3 = new Color(178, 178, 214);
    private final static Color grade4 = new Color(142, 142, 178);

    //private final static Color defBorder = Color.gray;
    private final static Color defBorder = applyAlpha(grade1, 250);
    private static Color envelopeBG = grade1;

    // ENVELOPE
    /*
    private static Color atkColor = Color.green;
     private static Color decColor = Color.orange;
     private static Color rlsColor = Color.red;
     */

    private static Color atkColor = new Color(174, 190, 195);
    private static Color decColor = new Color(168, 158, 174);
    private static Color rlsColor = new Color(170, 120, 157);

    /*
    private static Color atkColor = grade2;
    private static Color decColor = grade1;
    private static Color rlsColor = grade3;
    */

    private static Color dropColor = UIColors.applyAlpha(Color.RED, 150);

    private static Color tableBG = Color.white;

    private static int tableRowHeight = 16;
    private static int tableBorderWidth = 11;

    // PRESET
    //private static Color defaultBG = new Color(240, 240, 255);  // light milky blue
    //private static Color defaultBG = Color.LIGHT_GRAY;
    //private static Color defaultBG = new Color(210, 210, 210);   // STD gray
    private static Color defaultBG = new Color(207, 207, 217);   // STD bluey gray
    //  private static Color defaultBG = new Color(200, 244, 200); // greeny

    private static Color defaultFG = Color.black;

    private static Color presetPendingIcon = new Color(100, 250, 100);
    private static Color presetInitializingIcon = new Color(230, 200, 50);
    private static Color presetNamedIcon = new Color(100, 250, 100);
    private static Color presetFlashIcon = new Color(250, 100, 100);
    private static Color presetInitializedIcon = new Color(100, 100, 250);

    private static Color ultraDeviceIcon = new Color(75, 75, 125);
    private static Color classicDeviceIcon = new Color(150, 150, 175);


    private static Color endBubbleColor = applyAlpha(Color.ORANGE, 100);
    private static Color beginBubbleColor = applyAlpha(Color.RED, 150);

    // VOICE OVERVIEW TABLE
    private static Color tableBorder = defBorder;

    // private static Color tableHeaderBG = new Color(249, 251, 5);
    private static Color tableHeaderBG = grade2.brighter();
    private static Color tableHeaderFG = Color.darkGray;

    static {
        System.out.println(tableHeaderBG);
    }

    private static Color tableFirstSectionBG = grade1;
    private static Color tableFirstSectionFG = Color.DARK_GRAY;
    private static Color tableSecondSectionBG = grade2;
    private static Color tableSecondSectionFG = Color.DARK_GRAY;
    private static Color tableThirdSectionBG = grade3;
    private static Color tableThirdSectionFG = Color.DARK_GRAY;

    private static Color tableFourthSectionBG = grade4;
    private static Color tableFourthSectionFG = Color.DARK_GRAY;

    // private static Color tableFifthSectionBG = Color.gray.brighter();
    private static Color tableFifthSectionBG = Color.gray.brighter();
    private static Color tableFifthSectionFG = Color.DARK_GRAY;

    //private static Color tableRowHeaderBG = new Color(250, 214, 138);
    private static Color tableRowHeaderBG = new Color(214, 214, 200);
    // private static Color tableRowHeaderBG = new Color(110, 110, 135);
    private static Color tableRowHeaderFG = Color.DARK_GRAY;

    private static Color voiceOverViewTableRowHeaderSectionVoiceBG = tableRowHeaderBG;
    private static Color voiceOverViewTableRowHeaderSectionVoiceFG = Color.DARK_GRAY;

    private static Color voiceOverViewTableRowHeaderSectionZoneBG = Color.LIGHT_GRAY;
    private static Color voiceOverViewTableRowHeaderSectionZoneFG = Color.DARK_GRAY;

    private static Color utilityButtonBG = tableFirstSectionBG;
    private static Color refreshButtonBG = tableThirdSectionBG;

    private static double fuzzyAlphaSkew = 1.15;

    public static int getTableRowHeight() {
        return tableRowHeight;
    }

    public static void setTableRowHeight(int tableRowHeight) {
        UIColors.tableRowHeight = tableRowHeight;
    }

    public static int getFuzzyAlpha(int iteration, int max, boolean fadingIn) {
        int rand = (int) (Math.random() * (255 / max) / 2);
        int a = (fadingIn ? (int) (255 - iteration * (255 / max) * fuzzyAlphaSkew - rand) : (int) (iteration * (255 / max) * fuzzyAlphaSkew + rand));
        if (a < 0)
            a = 0;
        if (a > 255)
            a = 255;
        return a;
    }

    public static Border makeFuzzyBorder(Color bdrColor, int borderWidth) {
        Border b = new LineBorder(bdrColor, 1, true);
        for (int i = 0; i < borderWidth - 1; i++)
            b = new CompoundBorder(new LineBorder(UIColors.applyAlpha(bdrColor, 255 - i * (255 / borderWidth)), 1, true), b);

        return b;
    }

    public static Color getBeginBubbleColor() {
        return beginBubbleColor;
    }

    public static void setBeginBubbleColor(Color beginBubbleColor) {
        UIColors.beginBubbleColor = beginBubbleColor;
    }

    public static Color getEndBubbleColor() {
        return endBubbleColor;
    }

    public static void setEndBubbleColor(Color endBubbleColor) {
        UIColors.endBubbleColor = endBubbleColor;
    }

    public static Color getAtkColor() {
        return atkColor;
    }

    public static void setAtkColor(Color atkColor) {
        UIColors.atkColor = atkColor;
    }

    public static Color getDecColor() {
        return decColor;
    }

    public static void setDecColor(Color decColor) {
        UIColors.decColor = decColor;
    }

    public static Color getRlsColor() {
        return rlsColor;
    }

    public static void setRlsColor(Color rlsColor) {
        UIColors.rlsColor = rlsColor;
    }

    public static Color getTableBG() {
        return tableBG;
    }

    public static void setTableBG(Color tableBG) {
        UIColors.tableBG = tableBG;
    }

    public static Color applyAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    public static Color getTableRowHeaderBG() {
        return tableRowHeaderBG;
    }

    public static void setTableRowHeaderBG(Color tableRowHeaderBG) {
        UIColors.tableRowHeaderBG = tableRowHeaderBG;
    }

    public static Color getTableRowHeaderFG() {
        return tableRowHeaderFG;
    }

    public static void setTableRowHeaderFG(Color tableRowHeaderFG) {
        UIColors.tableRowHeaderFG = tableRowHeaderFG;
    }

    public static Color getEnvelopeBG() {
        return envelopeBG;
    }

    public static void setEnvelopeBG(Color envelopeBG) {
        UIColors.envelopeBG = envelopeBG;
    }

    public static Color getDropColor() {
        return dropColor;
    }

    public static void setDropColor(Color dropColor) {
        UIColors.dropColor = dropColor;
    }

    public static Color getDefaultBG() {
        return defaultBG;
    }

    public static void setDefaultBG(Color defaultBG) {
        UIColors.defaultBG = defaultBG;
    }

    public static Color getDefaultFG() {
        return defaultFG;
    }

    public static void setDefaultFG(Color defaultFG) {
        UIColors.defaultFG = defaultFG;
    }

    public static Color getPresetFlashIcon() {
        return presetFlashIcon;
    }

    public static void setPresetFlashIcon(Color presetFlashIcon) {
        UIColors.presetFlashIcon = presetFlashIcon;
    }

    public static Color getPresetInitializedIcon() {
        return presetInitializedIcon;
    }

    public static void setPresetInitializedIcon(Color presetInitializedIcon) {
        UIColors.presetInitializedIcon = presetInitializedIcon;
    }

    public static Color getPresetInitializingIcon() {
        return presetInitializingIcon;
    }

    public static void setPresetInitializingIcon(Color presetInitializingIcon) {
        UIColors.presetInitializingIcon = presetInitializingIcon;
    }

    public static Color getPresetNamedIcon() {
        return presetNamedIcon;
    }

    public static void setPresetNamedIcon(Color presetNamedIcon) {
        UIColors.presetNamedIcon = presetNamedIcon;
    }

    public static Color getPresetPendingIcon() {
        return presetPendingIcon;
    }

    public static void setPresetPendingIcon(Color presetPendingIcon) {
        UIColors.presetPendingIcon = presetPendingIcon;
    }

    public static Color getUltraDeviceIcon() {
        return ultraDeviceIcon;
    }

    public static void setUltraDeviceIcon(Color ultraDeviceIcon) {
        UIColors.ultraDeviceIcon = ultraDeviceIcon;
    }

    public static Color getClassicDeviceIcon() {
        return classicDeviceIcon;
    }

    public static void setClassicDeviceIcon(Color classicDeviceIcon) {
        UIColors.classicDeviceIcon = classicDeviceIcon;
    }

    public static Color getTableFifthSectionBG() {
        return tableFifthSectionBG;
    }

    public static void setTableFifthSectionBG(Color tableFifthSectionBG) {
        UIColors.tableFifthSectionBG = tableFifthSectionBG;
    }

    public static Color getTableFifthSectionFG() {
        return tableFifthSectionFG;
    }

    public static void setTableFifthSectionFG(Color tableFifthSectionFG) {
        UIColors.tableFifthSectionFG = tableFifthSectionFG;
    }

    public static Color getVoiceOverViewTableRowHeaderSectionVoiceFG() {
        return voiceOverViewTableRowHeaderSectionVoiceFG;
    }

    public static void setVoiceOverViewTableRowHeaderSectionVoiceFG(Color voiceOverViewTableRowHeaderSectionVoiceFG) {
        UIColors.voiceOverViewTableRowHeaderSectionVoiceFG = voiceOverViewTableRowHeaderSectionVoiceFG;
    }

    public static Color getVoiceOverViewTableRowHeaderSectionVoiceBG() {
        return voiceOverViewTableRowHeaderSectionVoiceBG;
    }

    public static void setVoiceOverViewTableRowHeaderSectionVoiceBG(Color voiceOverViewTableRowHeaderSectionVoiceBG) {
        UIColors.voiceOverViewTableRowHeaderSectionVoiceBG = voiceOverViewTableRowHeaderSectionVoiceBG;
    }

    public static Color getVoiceOverViewTableRowHeaderSectionZoneBG() {
        return voiceOverViewTableRowHeaderSectionZoneBG;
    }

    public static void setVoiceOverViewTableRowHeaderSectionZoneBG(Color voiceOverViewTableRowHeaderSectionZoneBG) {
        UIColors.voiceOverViewTableRowHeaderSectionZoneBG = voiceOverViewTableRowHeaderSectionZoneBG;
    }

    public static Color getVoiceOverViewTableRowHeaderSectionZoneFG() {
        return voiceOverViewTableRowHeaderSectionZoneFG;
    }

    public static void setVoiceOverViewTableRowHeaderSectionZoneFG(Color voiceOverViewTableRowHeaderSectionZoneFG) {
        UIColors.voiceOverViewTableRowHeaderSectionZoneFG = voiceOverViewTableRowHeaderSectionZoneFG;
    }

    public static Color getRefreshButtonBG() {
        return refreshButtonBG;
    }

    public static void setRefreshButtonBG(Color refreshButtonBG) {
        UIColors.refreshButtonBG = refreshButtonBG;
    }

    public static Color getUtilityButtonBG() {
        return utilityButtonBG;
    }

    public static void setUtilityButtonBG(Color utilityButtonBG) {
        UIColors.utilityButtonBG = utilityButtonBG;
    }

    public static Color getTableHeaderBG() {
        return tableHeaderBG;
    }

    public static void setTableHeaderBG(Color tableHeaderBG) {
        UIColors.tableHeaderBG = tableHeaderBG;
    }

    public static Color getTableHeaderFG() {
        return tableHeaderFG;
    }

    public static void setTableHeaderFG(Color tableHeaderFG) {
        UIColors.tableHeaderFG = tableHeaderFG;
    }

    public static Color getTableSecondSectionBG() {
        return tableSecondSectionBG;
    }

    public static void setTableSecondSectionBG(Color tableSecondSectionBG) {
        UIColors.tableSecondSectionBG = tableSecondSectionBG;
    }

    public static Color getTableSecondSectionFG() {
        return tableSecondSectionFG;
    }

    public static void setTableSecondSectionFG(Color tableSecondSectionFG) {
        UIColors.tableSecondSectionFG = tableSecondSectionFG;
    }

    public static Color getTableFirstSectionBG() {
        return tableFirstSectionBG;
    }

    public static void setTableFirstSectionBG(Color tableFirstSectionBG) {
        UIColors.tableFirstSectionBG = tableFirstSectionBG;
    }

    public static Color getTableFirstSectionFG() {
        return tableFirstSectionFG;
    }

    public static void setTableFirstSectionFG(Color tableFirstSectionFG) {
        UIColors.tableFirstSectionFG = tableFirstSectionFG;
    }

    public static Color getTableFourthSectionBG() {
        return tableFourthSectionBG;
    }

    public static void setTableFourthSectionBG(Color tableFourthSectionBG) {
        UIColors.tableFourthSectionBG = tableFourthSectionBG;
    }

    public static Color getTableFourthSectionFG() {
        return tableFourthSectionFG;
    }

    public static void setTableFourthSectionFG(Color tableFourthSectionFG) {
        UIColors.tableFourthSectionFG = tableFourthSectionFG;
    }

    public static Color getTableThirdSectionBG() {
        return tableThirdSectionBG;
    }

    public static void setTableThirdSectionBG(Color tableThirdSectionBG) {
        UIColors.tableThirdSectionBG = tableThirdSectionBG;
    }

    public static Color getTableThirdSectionFG() {
        return tableThirdSectionFG;
    }

    public static void setTableThirdSectionFG(Color tableThirdSectionFG) {
        UIColors.tableThirdSectionFG = tableThirdSectionFG;
    }

    public static Color getTableBorder() {
        return tableBorder;
    }

    public static void setTableBorder(Color tableBorder) {
        UIColors.tableBorder = tableBorder;
    }

    public static int getTableBorderWidth() {
        return tableBorderWidth;
    }

    public static void setTableBorderWidth(int tableBorderWidth) {
        UIColors.tableBorderWidth = tableBorderWidth;
    }
}


