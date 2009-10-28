package com.pcmsolutions.device.EMU.E4.gui.colors;


/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 15-Apr-2003
 * Time: 23:00:43
 * To change this template use Options | File Templates.
 */
public class ColorFactory {
    /*   private static ColorFactory INSTANCE = new ColorFactory();

       public static ColorFactory getInstance() {
           return INSTANCE;
       }

       public static Color applyAlpha(Color c, int alpha) {
           return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
       }

       public Color getPopupBGColor() {
           return UIColors.getDevicePopupBG();
       }

       public Color getBGColor() {
           return UIColors.getDeviceBG();
       }

       public Color getFGColor() {
           return UIColors.getDeviceFG();
       }

       public PresetColorContext getPresetContext() {
           return presetColorContext;
       }

       public SampleColorContext getSampleContext() {
           return sampleColorContext;
       }

       public MultiModeColorContext getMultiModeContext() {
           return multiModeColorContext;
       }


       public MasterColorContext getMasterContext() {
           return masterColorContext;
       }

       private static final MultiModeColorContext multiModeColorContext = new MultiModeColorContext() {
           // POPUP
           public Color getPopupBGColor() {
               return UIColors.getMultimodePopupBG();
           }

           public Color getPopupFGColor() {
               return UIColors.getMultimodePopupFG();
           }

           // BACKGROUND
           public Color getBGColor() {
               return UIColors.getMultimodeBG();
           }

           public Color getFGColor() {
               return UIColors.getMultimodeFG();
           }

           // SELECTION
           public Color getSelectionFGColor() {
               return UIColors.getMultimodeSelectionFG();
           }

           public Color getSelectionBGColor() {
               return UIColors.getMultimodeSelectionBG();
           }

           public Color getTitleBGColor() {
               return UIColors.getMultimodeHeaderBG();
           }

           public Color getTitleFGColor() {
               return UIColors.getMultimodeHeaderFG();
           }
       };
       private static final MasterColorContext masterColorContext = new MasterColorContext() {
           // POPUP
           public Color getPopupBGColor() {
               return UIColors.getMasterPopupBG();
           }

           public Color getPopupFGColor() {
               return UIColors.getMasterPopupFG();
           }

           // BACKGROUND
           public Color getBGColor() {
               return UIColors.getMasterBG();
           }

           public Color getFGColor() {
               return UIColors.getMasterFG();
           }

           // SELECTION
           public Color getSelectionFGColor() {
               return UIColors.getMasterSelectionFG();
           }

           public Color getSelectionBGColor() {
               return UIColors.getMasterSelectionBG();
           }

           public Color getTitleBGColor() {
               return UIColors.getMasterTitleBG();
           }

           public Color getTitleFGColor() {
               return UIColors.getMasterTitleFG();
           }
       };
       private static final PresetColorContext presetColorContext = new PresetColorContext() {
           private final VoiceOverviewTableColorContext voiceOverviewTableColorContext = new VoiceOverviewTableColorContext() {
               public Color getVoiceBG() {
                   return UIColors.getVoiceOverViewTableVoiceBG();
               }

               public Color getVoiceFG() {
                   return UIColors.getVoiceOverViewTableVoiceFG();
               }

               public Color getVoiceSelectionBG() {
                   return UIColors.getVoiceOverViewTableVoiceSelectionBG();
               }

               public Color getVoiceSelectionFG() {
                   return UIColors.getVoiceOverViewTableVoiceSelectionFG();
               }

               public Color getZoneBG() {
                   return UIColors.getVoiceOverViewTableZoneBG();
               }

               public Color getZoneFG() {
                   return UIColors.getVoiceOverViewTableZoneFG();
               }

               public Color getZoneSelectionBG() {
                   return UIColors.getVoiceOverViewTableZoneSelectionBG();
               }

               public Color getZoneSelectionFG() {
                   return UIColors.getVoiceOverViewTableZoneSelectionFG();
               }

               public Color getHeaderBG() {
                   return UIColors.getVoiceOverViewTableHeaderBG();
               }

               public Color getHeaderFG() {
                   return UIColors.getVoiceOverViewTableHeaderFG();
               }

               public Color getRowHeaderSectionBG() {
                   return UIColors.getVoiceOverViewTableRowHeaderSectionBG();
               }

               public Color getRowHeaderSectionFG() {
                   return UIColors.getVoiceOverViewTableRowHeaderSectionFG();
               }

               public Color getRowHeaderSectionZoneBG() {
                   return UIColors.getVoiceOverViewTableRowHeaderSectionZoneBG();
               }

               public Color getRowHeaderSectionZoneFG() {
                   return UIColors.getVoiceOverViewTableRowHeaderSectionZoneFG();
               }

               public Color getMainSectionBG() {
                   return UIColors.getVoiceOverViewTableMainSectionBG();
               }

               public Color getMainSectionFG() {
                   return UIColors.getVoiceOverViewTableMainSectionFG();
               }

               public Color getKeyWinSectionBG() {
                   return UIColors.getVoiceOverViewTableKeyWinSectionBG();
               }

               public Color getKeyWinSectionFG() {
                   return UIColors.getVoiceOverViewTableKeyWinSectionFG();
               }

               public Color getVelWinSectionBG() {
                   return UIColors.getVoiceOverViewTableVelWinSectionBG();
               }

               public Color getVelWinSectionFG() {
                   return UIColors.getVoiceOverViewTableVelWinSectionFG();
               }

               public Color getRTWinSectionBG() {
                   return UIColors.getVoiceOverViewTableRTWinSectionBG();
               }

               public Color getRTWinSectionFG() {
                   return UIColors.getVoiceOverViewTableVelWinSectionFG();
               }

               public Color getCustomSectionBG() {
                   return UIColors.getVoiceOverViewTableCustomSectionBG();
               }

               public Color getCustomSectionFG() {
                   return UIColors.getVoiceOverViewTableCustomSectionFG();
               }

           };
           private final LinkTableColorContext linkTableColorContext = new LinkTableColorContext() {
               public Color getBG() {
                   return null;
               }

               public Color getFG() {
                   return null;
               }

               public Color getSelectionBG() {
                   return null;
               }

               public Color getSelectionFG() {
                   return null;
               }

               public Color getHeaderBG() {
                   return null;
               }

               public Color getHeaderFG() {
                   return null;
               }

               public Color getMainSectionBG() {
                   return null;
               }

               public Color getMainSectionFG() {
                   return null;
               }

               public Color getKeyWinSectionBG() {
                   return null;
               }

               public Color getKeyWinSectionFG() {
                   return null;
               }

               public Color getVelWinSectionBG() {
                   return null;
               }

               public Color getVelWinSectionFG() {
                   return null;
               }

               public Color getMidiFiltersSectionBG() {
                   return null;
               }

               public Color getMidiFiltersSectionFG() {
                   return null;
               }

           };

           // POPUP
           public Color getPopupBGColor() {
               return UIColors.getPresetPopupBG();
           }

           public Color getPopupFGColor() {
               return UIColors.getPresetPopupFG();
           }

           // BACKGROUND
           public Color getBGColor() {
               return UIColors.getDefaultBG();
           }

           public Color getFGColor() {
               return UIColors.getDefaultFG();
           }

           // SELECTION
           public Color getSelectionFGColor() {
               return UIColors.getPresetSelectionFG();
           }

           public Color getSelectionBGColor() {
               return UIColors.getPresetSelectionBG();
           }

           public Color getTitleBGColor() {
               return UIColors.getPresetTitleBG();
           }

           public Color getTitleFGColor() {
               return UIColors.getPresetTitleFG();
           }

           public Color getPendingPresetIconColor() {
               return UIColors.getPresetPendingIcon();
           }

           public Color getNamedPresetIconColor() {
               return UIColors.getPresetNamedIcon();
           }

           public Color getFlashPresetIconColor() {
               return UIColors.getPresetFlashIcon();
           }

           public Color getInitializedPresetIconColor() {
               return UIColors.getPresetInitializedIcon();
           }

           public LinkTableColorContext getLinkTableContext() {
               return linkTableColorContext;
           }

           public VoiceOverviewTableColorContext getVoiceOverviewTableContext() {
               return voiceOverviewTableColorContext;
           }
       };

       private static final SampleColorContext sampleColorContext = new SampleColorContext() {
           // POPUP
           public Color getPopupBGColor() {
               return UIColors.getSamplePopupBG();
           }

           public Color getPopupFGColor() {
               return UIColors.getSamplePopupFG();
           }

           // BACKGROUND
           public Color getBGColor() {
               return UIColors.getSampleBG();
           }

           public Color getFGColor() {
               return UIColors.getSampleFG();
           }

           // SELECTION
           public Color getSelectionFGColor() {
               return UIColors.getSampleSelectionFG();
           }

           public Color getSelectionBGColor() {
               return UIColors.getSampleSelectionBG();
           }

           public Color getTitleBGColor() {
               return UIColors.getSampleTitleBG();
           }

           public Color getTitleFGColor() {
               return UIColors.getSampleTitleFG();
           }
       };
       */
}


