/*
 * @(#)DocumentPaneDemo.java
 *
 * Copyright 2002 - 2003 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.document.DocumentComponent;
import com.jidesoft.document.DocumentPane;
import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.JideTabbedPane;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Demoed Component: {@link DocumentPane}
 * <br>
 * Required jar files: jide-common.jar, jide-components.jar
 * <br>
 * Required L&F: Jide L&F extension required
 */
public class DocumentPaneDemo extends JFrame {

    private static DocumentPaneDemo _frame;
    private static DocumentPane _documentPane;

    public DocumentPaneDemo(String title) throws HeadlessException {
        super(title);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            LookAndFeelFactory.installJideExtension();
        }
        catch (ClassNotFoundException e) {
        }
        catch (InstantiationException e) {
        }
        catch (IllegalAccessException e) {
        }
        catch (UnsupportedLookAndFeelException e) {
        }
        _frame = new DocumentPaneDemo("Demo of DocumentPane");
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        _documentPane = createDocumentTabs();
        _documentPane.setGroupsAllowed(true);
        _documentPane.setTabbedPaneCustomizer(new DocumentPane.TabbedPaneCustomizer() {
            public void customize(JideTabbedPane tabbedPane) {
                tabbedPane.setUseDefaultShowCloseButtonOnTab(false);
                tabbedPane.setShowCloseButtonOnTab(true);
            }
        });

        _frame.getContentPane().setLayout(new BorderLayout());
        _frame.getContentPane().add(_documentPane, BorderLayout.CENTER);

        _frame.setBounds(10, 10, 800, 500);

        _frame.setVisible(true);

    }

    private static DocumentPane createDocumentTabs() {
        DocumentPane panel = new DocumentPane();
        panel.openDocument(new DocumentComponent(createMultiViewDocument("EULA.htm"),
                "License", "License", JideIconsFactory.getImageIcon(JideIconsFactory.FileType.HTML)));
        panel.openDocument(new DocumentComponent(new JScrollPane(DocumentPaneDemo.createTextArea("Readme.txt")),
                "Readme", "Readme",
                JideIconsFactory.getImageIcon(JideIconsFactory.FileType.TEXT)));
        panel.openDocument(new DocumentComponent(new JScrollPane(DocumentPaneDemo.createTextArea("DocumentPaneDemo.java")),
                "DocumentPaneDemo.java", "C:\\Projects\\JideSoft\\Source Code\\DocumentPaneDemo.java",
                JideIconsFactory.getImageIcon(JideIconsFactory.FileType.JAVA)));
        return panel;
    }

    private static JComponent createMultiViewDocument(String fileName) {
        JideTabbedPane pane = new JideTabbedPane(JideTabbedPane.BOTTOM);
        pane.setBoxStyleTab(true);
        pane.addTab("Design", JideIconsFactory.getImageIcon(JideIconsFactory.View.DESIGN), new JScrollPane(createHtmlArea(fileName)));
        pane.addTab("HTML", JideIconsFactory.getImageIcon(JideIconsFactory.View.HTML), new JScrollPane(createTextArea(fileName)));
        return pane;
    }

    public static JTextArea createTextArea(String fileName) {
        JTextArea area = new JTextArea();
        Document doc = new PlainDocument();
        try {
            // try to start reading
            InputStream in = DocumentPaneDemo.class.getResourceAsStream(fileName);
            if (in != null) {
                byte[] buff = new byte[4096];
                int nch;
                while ((nch = in.read(buff, 0, buff.length)) != -1) {
                    doc.insertString(doc.getLength(), new String(buff, 0, nch), null);
                }
                area.setDocument(doc);
            }
            else {
                area.setText("Copy Readme.txt and DocumentPaneDemo.java into the class output directory");
            }
        }
        catch (FileNotFoundException e) {
        }
        catch (IOException e) {
        }
        catch (BadLocationException e) {
        }
        return area;
    }

    public static Component createHtmlArea(String fileName) {
        JEditorPane area = new JEditorPane();
        try {
            area.setPage(DocumentPaneDemo.class.getResource(fileName));
        }
        catch (IOException e) {
            area.setText("Copy EULA.html into the class output directory");
        }
        return area;
    }
}
