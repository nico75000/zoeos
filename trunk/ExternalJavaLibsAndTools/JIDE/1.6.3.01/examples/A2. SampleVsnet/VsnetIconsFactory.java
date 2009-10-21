/*
 * @(#)VsnetIconsFactory.java
 *
 * Copyright 2002 JIDE Software Inc. All rights reserved.
 */
import com.jidesoft.icons.IconsFactory;

import javax.swing.*;

/**
 * A helper class to contain icons from Visual Studio .NET
 * Those icons are copyrighted by Microsoft.
 */
public class VsnetIconsFactory {

    public static class Standard {
        public final static String ADD_NEW_ITEMS = "vsnet/standard/add_new_items.gif";
        public final static String OPEN = "vsnet/standard/open.gif";
        public final static String SAVE = "vsnet/standard/save.gif";
        public final static String SAVE_ALL = "vsnet/standard/save_all.gif";
        // ----

        public final static String CUT = "vsnet/standard/cut.gif";
        public final static String COPY = "vsnet/standard/copy.gif";
        public final static String PASTE = "vsnet/standard/paste.gif";
        // ----

        public final static String UNDO = "vsnet/standard/undo.gif";
        public final static String REDO = "vsnet/standard/redo.gif";
        public final static String NAVIGATE_BACKWARD = "vsnet/standard/navigate_backward.gif";
        public final static String NAVIGATE_FORWARD = "vsnet/standard/navigate_forward.gif";
        // ----

        public final static String START = "vsnet/standard/start.gif";
        // ----

        public final static String FIND_IN_FILES = "vsnet/standard/find_in_files.gif";
        // ----

        public final static String SOLUTION = "vsnet/toolwindows/solution_explorer.gif";
        public final static String PROPERTY = "vsnet/toolwindows/property.gif";
        public final static String TOOLBOX = "vsnet/toolwindows/toolbox.gif";

        // ---- // ----
        public final static String CLASSVIEW = "vsnet/toolwindows/class_view.gif";
        public final static String SERVER = "vsnet/toolwindows/server_explorer.gif";
        public final static String RESOURCEVIEW = "vsnet/toolwindows/resource_view.gif";

        // ---- // ----
        public final static String MACRO = "vsnet/toolwindows/macro_explorer.gif";
        public final static String OBJECT = "vsnet/toolwindows/object_browser.gif";
        public final static String DOCUMENTOUTLINE = "vsnet/toolwindows/document_outline.gif";

        // ---- // ----
        public final static String TASKLIST = "vsnet/toolwindows/tasklist.gif";
        public final static String COMMAND = "vsnet/toolwindows/command.gif";
        public final static String OUTPUT = "vsnet/toolwindows/output.gif";

        // ---- // ----
        public final static String FINDRESULT1 = "vsnet/toolwindows/find_result_1.gif";
        public final static String FINDRESULT2 = "vsnet/toolwindows/find_result_2.gif";
        public final static String FINDSYMBOL = "vsnet/toolwindows/find_symbol_result.gif";

        // ---- // ----
        public final static String FAVORITES = "vsnet/toolwindows/favorites.gif";
    }

    public static class Build {
        public final static String BUILD_FILE = "vsnet/build/build_file.gif";
        public final static String BUILD_SOLUTION = "vsnet/build/build_solution.gif";
        public final static String CANCEL = "vsnet/build/cancel.gif";
    }

    public static class Layout {
        // ----

        public final static String ALIGN_TO_GRID = "vsnet/layout/align_to_grid.gif";
        // ----

        public final static String ALIGN_LEFTS = "vsnet/layout/align_lefts.gif";
        public final static String ALIGN_CENTERS = "vsnet/layout/align_centers.gif";
        public final static String ALIGN_RIGHTS = "vsnet/layout/align_rights.gif";
        // ----

        public final static String ALIGN_TOPS = "vsnet/layout/align_tops.gif";
        public final static String ALIGN_MIDDLES = "vsnet/layout/align_middles.gif";
        public final static String ALIGN_BOTTOMS = "vsnet/layout/align_bottoms.gif";
        // ----

        public final static String MAKE_SAME_WIDTH = "vsnet/layout/make_same_width.gif";
        public final static String SIZE_TO_GRID = "vsnet/layout/size_to_grid.gif";
        public final static String MAKE_SAME_HEIGHT = "vsnet/layout/make_same_height.gif";
        public final static String MAKE_SAME_SIZE = "vsnet/layout/make_same_size.gif";
        // ----

        public final static String MAKE_HORI_SPACING_EQUAL = "vsnet/layout/make_hori_spacing_equal.gif";
        public final static String INC_HORI_SPACING = "vsnet/layout/inc_hori_spacing.gif";
        public final static String DEC_HORI_SPACING = "vsnet/layout/dec_hori_spacing.gif";
        public final static String REMOVE_HORI_SPACING = "vsnet/layout/remove_hori_spacing.gif";
        // ----

        public final static String MAKE_VERT_SPACING_EQUAL = "vsnet/layout/make_vert_spacing_equal.gif";
        public final static String INC_VERT_SPACING = "vsnet/layout/inc_vert_spacing.gif";
        public final static String DEC_VERT_SPACING = "vsnet/layout/dec_vert_spacing.gif";
        public final static String REMOVE_VERT_SPACING = "vsnet/layout/remove_vert_spacing.gif";
        // ----

        public final static String CENTER_HORI = "vsnet/layout/center_hori.gif";
        public final static String CENTER_VERT = "vsnet/layout/center_vert.gif";
        // ----

        public final static String BRING_TO_FRONT = "vsnet/layout/bring_to_front.gif";
        public final static String SEND_TO_BACK = "vsnet/layout/send_to_back.gif";
    }

    public static class Formatting {
        // ----

        public final static String BOLD = "vsnet/formatting/bold.gif";
        public final static String ITALIC = "vsnet/formatting/italic.gif";
        public final static String UNDERLINE = "vsnet/formatting/underline.gif";
        // ----

        public final static String FOREGROUND = "vsnet/formatting/foreground.gif";
        public final static String BACKGROUND = "vsnet/formatting/background.gif";
        // ----

        public final static String ALIGN_LEFT = "vsnet/formatting/align-left.gif";
        public final static String ALIGN_CENTER = "vsnet/formatting/align-center.gif";
        public final static String ALIGN_RIGHT = "vsnet/formatting/align-right.gif";
        public final static String JUSTIFY = "vsnet/formatting/justify.gif";
        // ----

        public final static String NUMBERING = "vsnet/formatting/numbering.gif";
        public final static String BULLETS = "vsnet/formatting/bullets.gif";
        // ----

        public final static String DECREASE_INDENT = "vsnet/formatting/decrease-indent.gif";
        public final static String INCREASE_INDENT = "vsnet/formatting/increase-indent.gif";
    }

    public static ImageIcon getImageIcon(String name) {
        if (name != null)
            return IconsFactory.getImageIcon(VsnetIconsFactory.class, name);
        else
            return null;
    }

    public static void main(String[] argv) {
        IconsFactory.generateHTML(VsnetIconsFactory.class);
    }


}
