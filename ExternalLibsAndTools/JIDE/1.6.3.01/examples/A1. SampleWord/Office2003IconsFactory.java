/*
 * @(#)Office2003IconsFactory.JAVA
 *
 * Copyright 2002 - 2004 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.icons.IconsFactory;

import javax.swing.*;

/**
 */
public class Office2003IconsFactory {
    public static class Standard {
        public final static String NEW = "office2003/standard/new.gif";
        public final static String OPEN = "office2003/standard/open.gif";
        public final static String SAVE = "office2003/standard/save.gif";
        public final static String PERMISSION = "office2003/standard/permission.gif";
        public final static String EMAIL = "office2003/standard/e-mail.gif";
        // ----
        public final static String PRINT = "office2003/standard/print.gif";
        public final static String PRINT_PREVIEW = "office2003/standard/print-preview.gif";
        // ----
        public final static String SPELLING_GRAMMAR = "office2003/standard/spelling-grammar.gif";
        public final static String RESEARCH = "office2003/standard/research.gif";
        // ----
        public final static String CUT = "office2003/standard/cut.gif";
        public final static String COPY = "office2003/standard/copy.gif";
        public final static String PASTE = "office2003/standard/paste.gif";
        public final static String FORMAT_PAINTER = "office2003/standard/format-painter.gif";
        // ----
        public final static String UNDO = "office2003/standard/undo.gif";
        public final static String REDO = "office2003/standard/redo.gif";
        // ----
        public final static String INSERT_HYPERLINK = "office2003/standard/insert-hyperlink.gif";
        public final static String TABLES_BORDERS = "office2003/standard/tables-borders.gif";
        public final static String INSERT_TABLE = "office2003/standard/insert-table.gif";
        public final static String INSERT_EXCEL = "office2003/standard/insert-excel.gif";
        public final static String COLUMNS = "office2003/standard/columns.gif";
        public final static String DRAWING = "office2003/standard/drawing.gif";
        // ----
        public final static String DOCUMENT_MAP = "office2003/standard/document-map.gif";
        public final static String SHOW_HIDE_SYMBOL = "office2003/standard/show-hide-symbol.gif";
        public final static String HELP = "office2003/standard/help.gif";
        // ----
    }

    public static class Formatting {
        public final static String STYLE_FORMATTING = "office2003/formatting/style-formatting.gif";
        public final static String FORMAT_FONT = "office2003/formatting/format-font.gif";
        // ----

        public final static String BOLD = "office2003/formatting/bold.gif";
        public final static String ITALIC = "office2003/formatting/italic.gif";
        // ----

        public final static String ALIGN_LEFT = "office2003/formatting/align-left.gif";
        public final static String ALIGN_CENTER = "office2003/formatting/align-center.gif";
        public final static String ALIGN_RIGHT = "office2003/formatting/align-right.gif";
        public final static String JUSTIFY = "office2003/formatting/justify.gif";
        public final static String DISTRIBUTED = "office2003/formatting/distributed.gif";
        public final static String LINE_SPACING = "office2003/formatting/line-spacing.gif";
        // ----

        public final static String NUMBERING = "office2003/formatting/numbering.gif";
        public final static String BULLETS = "office2003/formatting/bullets.gif";
        public final static String DECREASE_INDENT = "office2003/formatting/decrease-indent.gif";
        public final static String INCREASE_INDENT = "office2003/formatting/increase-indent.gif";
        // ----

        public final static String HIGHLIGHT = "office2003/formatting/highlight.gif";
        public final static String OUTSIDE_BORDER = "office2003/formatting/outside-border.gif";
        public final static String FONT_COLOR = "office2003/formatting/font-color.gif";
    }

    public static class Drawing {
        public final static String SELECT_OBJECT = "office2003/drawing/select-object.gif";
        // ----

        public final static String LINE = "office2003/drawing/line.gif";
        public final static String ARROW = "office2003/drawing/arrow.gif";
        public final static String RECTANGLE = "office2003/drawing/rectangle.gif";
        public final static String OVAL = "office2003/drawing/oval.gif";
        public final static String TEXTBOX = "office2003/drawing/textbox.gif";
        public final static String VERTICAL_TEXTBOX = "office2003/drawing/vertical-textbox.gif";
        public final static String INSERT_WORDART = "office2003/drawing/insert-wordart.gif";
        public final static String INSERT_DIAGRAM_ORGCHART = "office2003/drawing/insert-diagram-orgchart.gif";
        public final static String INSERT_CLIPART = "office2003/drawing/insert-clipart.gif";
        public final static String INSERT_PICTURE = "office2003/drawing/insert-picture.gif";
        // ----

        public final static String FILL_COLOR = "office2003/drawing/fill-color.gif";
        public final static String LINE_COLOR = "office2003/drawing/line-color.gif";
        public final static String FONT_COLOR = "office2003/drawing/font-color.gif";
        public final static String LINE_STYLE = "office2003/drawing/line-style.gif";
        public final static String DASH_STYLE = "office2003/drawing/dash-style.gif";
        public final static String ARROW_STYLE = "office2003/drawing/arrow-style.gif";
        public final static String SHADOW_STYLE = "office2003/drawing/shadow-style.gif";
        public final static String THREED_STYLE = "office2003/drawing/3d-style.gif";
    }

    public static class Status {
        public final static String ERROR = "office2003/status/error.gif";
    }
    
    public static ImageIcon getImageIcon(String name) {
        if (name != null)
            return IconsFactory.getImageIcon(Office2003IconsFactory.class, name);
        else
            return null;
    }

    public static void main(String[] argv) {
        IconsFactory.generateHTML(Office2003IconsFactory.class);
    }
}
