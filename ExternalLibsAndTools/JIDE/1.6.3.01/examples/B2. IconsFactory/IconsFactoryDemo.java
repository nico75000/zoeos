/*
 * @(#)SampleIconsFactory.java
 *
 * Copyright 2002-2003 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.icons.IconsFactory;

import javax.swing.*;

/**
 * Demoed Component: {@link IconsFactory}
 * <br>
 * Required jar files: jide-common.jar
 * <br>
 * Required L&F: any L&F
 */
public class IconsFactoryDemo {

    public static class CollapsiblePane {
        public final static String RENAME = "icons/e_rename.png";
        public final static String MOVE = "icons/e_move.png";
        public final static String COPY = "icons/e_copy.png";
        public final static String PUBLISH = "icons/e_publish.png";
        public final static String EMAIL = "icons/e_email.png";
        public final static String DELET = "icons/e_delete.png";
        public final static String LOCALDISK = "icons/e_localdisk.png";
        public final static String PICTURES = "icons/e_pictures.png";
        public final static String COMPUTER = "icons/e_computer.png";
        public final static String NETWORK = "icons/e_network.png";
    }

    public static class OutlookShortcuts {
        public final static String TODAY = "icons/o_today.png";
        public final static String INBOX = "icons/o_inbox.png";
        public final static String CONTACTS = "icons/o_contacts.png";
        public final static String TASKS = "icons/o_tasks.png";
        public final static String CALENDAR = "icons/o_calendar.png";
        public final static String NOTES = "icons/o_notes.png";
        public final static String DELETED_ITEMS = "icons/o_deleted_items.png";

        public final static String DRAFTS = "icons/o_drafts.png";
        public final static String OUTBOX = "icons/o_outbox.png";
        public final static String SEND_ITEMS = "icons/o_send_items.png";
        public final static String JOURNAL = "icons/o_journal.png";

        public final static String COMPUTER = "icons/o_my_computer.png";
        public final static String NETWORK = "icons/o_my_network.png";
        public final static String DOCUMENTS = "icons/o_my_documents.png";
    }

    public static class PropertiesWindow {
        public final static String CATEGORIED = "icons/t_category.png";
        public final static String SORT = "icons/t_sort.png";
        public final static String DESCRIPTION = "icons/t_description.png";
    }

    public static ImageIcon getImageIcon(String name) {
        if (name != null)
            return IconsFactory.getImageIcon(IconsFactoryDemo.class, name);
        else
            return null;
    }

    public static void main(String[] argv) {
        IconsFactory.generateHTML(IconsFactoryDemo.class);
    }


}
