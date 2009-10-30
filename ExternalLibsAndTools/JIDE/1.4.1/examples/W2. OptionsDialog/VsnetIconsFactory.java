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

    public static class ClassElement {
        public final static String CLASS = "vsnet/msdev_class_class.gif";
        public final static String FIELD = "vsnet/msdev_class_field.gif";
        public final static String FIELD_PROTECTED = "vsnet/msdev_class_field_protected.gif";
        public final static String FIELD_PRIVATE = "vsnet/msdev_class_field_private.gif";
        public final static String METHOD = "vsnet/msdev_class_method.gif";
        public final static String METHOD_PROTECTED = "vsnet/msdev_class_method_protected.gif";
        public final static String METHOD_PRIVATE = "vsnet/msdev_class_method_private.gif";
        public final static String CONSTANT = "vsnet/msdev_class_const.gif";
        public final static String MAP = "vsnet/msdev_class_map.gif";
        public final static String GLOBAL = "vsnet/msdev_class_global.gif";
    }

    public static class Cursor {
        public final static String HSPLIT = "vsnet/cursor_h_split.gif";
        public final static String VSPLIT = "vsnet/cursor_v_split.gif";

        public final static String DROP = "vsnet/cursor_drag.gif";
        public final static String NODROP = "vsnet/cursor_drag_stop.gif";
    }

    public static class ToolWindow {
        public final static String BLANK = "vsnet/toolwindow_blank.gif";
        public final static String OUTPUT = "vsnet/toolwindow_output.gif";
        public final static String FINDRESULT1 = "vsnet/toolwindow_find_result_1.gif";
        public final static String FINDRESULT2 = "vsnet/toolwindow_find_result_2.gif";
        public final static String FINDSYMBOL = "vsnet/toolwindow_find_symbol_result.gif";
        public final static String COMMAND = "vsnet/toolwindow_command.gif";
        public final static String PROPERTY = "vsnet/toolwindow_property.gif";
        public final static String CLASSVIEW = "vsnet/toolwindow_class_view.gif";
        public final static String RESOURCEVIEW = "vsnet/toolwindow_resource_view.gif";
        public final static String SOLUTION = "vsnet/toolwindow_solution_explorer.gif";
        public final static String SERVER = "vsnet/toolwindow_server_explorer.gif";
        public final static String TOOLBOX = "vsnet/toolwindow_toolbox.gif";
        public final static String MACRO = "vsnet/toolwindow_macro_explorer.gif";
        public final static String OBJECT = "vsnet/toolwindow_object_browser.gif";
        public final static String DOCUMENTOUTLINE = "vsnet/toolwindow_document_outline.gif";
        public final static String TASKLIST = "vsnet/toolwindow_tasklist.gif";
        public final static String FAVORITES = "vsnet/toolwindow_favorites.gif";
    }

    public static class FileMenu {
        public final static String NEW_PROJECT = "vsnet/msdev_new_project.gif";
        public final static String NEW_FILE = "vsnet/msdev_new_file.gif";
        public final static String NEW_SOLUTION = "vsnet/msdev_new_solution.gif";
        public final static String OPEN_PROJECT = "vsnet/msdev_open_project.gif";
        public final static String OPEN_PROJECT_FROM_WEB = "vsnet/msdev_open_project_from_web.gif";
        public final static String OPEN_FILE = "vsnet/msdev_open_file.gif";
        public final static String OPEN_FILE_FROM_WEB = "vsnet/msdev_open_file_from_web.gif";
        public final static String PAGE_SETUP = "vsnet/msdev_page_setup.gif";
        public final static String PRINT = "vsnet/msdev_print.gif";
        public final static String SAVE = "vsnet/msdev_save.gif";
        public final static String SAVE_ALL = "vsnet/msdev_saveall.gif";
    }

    public static class EditMenu {
        public final static String CUT = "vsnet/msdev_cut.gif";
        public final static String COPY = "vsnet/msdev_copy.gif";
        public final static String PASTE = "vsnet/msdev_paste.gif";
        public final static String DELETE = "vsnet/msdev_delete.gif";
        public final static String FIND = "vsnet/msdev_find.gif";
        public final static String REPLACE = "vsnet/msdev_replace.gif";
        public final static String FIND_IN_FILES = "vsnet/msdev_find_in_files.gif";
        public final static String REPLACE_IN_FILES = "vsnet/msdev_replace_in_files.gif";
        public final static String FIND_SYMBOLS = "vsnet/msdev_find_symbol.gif";
    }

    public static class WindowMenu {
        public final static String NEW = "vsnet/msdev_windows_new.gif";
        public final static String CLOSE_ALL = "vsnet/msdev_windows_close_all.gif";
        public final static String NEW_HORIZONTAL_TAB = "vsnet/msdev_windows_new_horizontal_tab_group.gif";
        public final static String NEW_VERTICAL_TAB = "vsnet/msdev_windows_new_vertical_tab_group.gif";
        public final static String SPLIT = "vsnet/msdev_windows_split.gif";
    }

    public static class HelpMenu {
        public final static String DYNAMIC_HELP = "vsnet/msdev_help_dynamic_help.gif";
        public final static String CONTENTS = "vsnet/msdev_help_contents.gif";
        public final static String INDEX = "vsnet/msdev_help_index.gif";
        public final static String SEARCH = "vsnet/msdev_help_search.gif";
        public final static String INDEX_RESULTS = "vsnet/msdev_help_index_results.gif";
        public final static String SEARCH_RESULTS = "vsnet/msdev_help_search_results.gif";
        public final static String PREVIOUS_TOPIC = "vsnet/msdev_help_prev_topic.gif";
        public final static String NEXT_TOPIC = "vsnet/msdev_help_next_topic.gif";
        public final static String SYNC_CONTENTS = "vsnet/msdev_help_sync_contents.gif";
        public final static String SHOW_STARTPAGE = "vsnet/msdev_help_start_page.gif";
        public final static String TECHNICAL_SUPPORT = "vsnet/msdev_help_technical_support.gif";
    }

    public static class FileElement {
        public final static String SOLUTION = "vsnet/msdev_solution.gif";
        public final static String PROJECT = "vsnet/msdev_project.gif";
        public final static String FOLDER = "vsnet/msdev_folder.gif";
        public final static String FOLDER_CLOSED = "vsnet/msdev_folder_close.gif";
        public final static String BUILDABLE = "vsnet/msdev_buildable_file.gif";
        public final static String NONBUILDABLE = "vsnet/msdev_non_buildable_file.gif";
    }

    public static class FileView {
        public final static String DESIGN = "vsnet/view_design.gif";
        public final static String HTML = "vsnet/view_html.gif";
    }

    public final static String TAIL = "vsnet/tail.gif";

    public final static String MENU_CHECKBOX = "vsnet/msdev_menu_checkbox.gif";

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
