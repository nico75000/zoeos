/*
 * @(#)PropertyPaneDemo.java
 *
 * Copyright 2002 - 2003 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.combobox.ListComboBox;
import com.jidesoft.converter.ObjectConverterManager;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.grid.*;
import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.swing.JideSwingUtilities;
import com.jidesoft.swing.MultilineLabel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Demoed Component: {@link PropertyPane}, {@link PropertyTable}
 * <br>
 * Required jar files: jide-common.jar, jide-grids.jar
 * <br>
 * Required L&F: Jide L&F extension required
 */
public class TradingHierarchicalTableDemo extends JFrame {
    protected static final Color BG1 = new Color(232, 237, 230);

    private static TradingHierarchicalTableDemo _frame;
    private static HierarchicalTable _table;
    protected static TableModel _quotesTableModel;

    public TradingHierarchicalTableDemo(String title) throws HeadlessException {
        super(title);
    }

    public TradingHierarchicalTableDemo() throws HeadlessException {
        this("");
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

        ObjectConverterManager.initDefaultConverter();
        CellEditorManager.initDefaultEditor();
        CellRendererManager.initDefaultRenderer();

        _frame = new TradingHierarchicalTableDemo("TradingHierarchicalTable Sample");
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        _frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                _frame.dispose();
                System.exit(0);
            }
        });

        _table = createTable();

        _frame.getContentPane().setLayout(new BorderLayout(6, 6));
        _frame.getContentPane().add(new JScrollPane(_table), BorderLayout.CENTER);
        _frame.getContentPane().add(new MultilineLabel("Double click each row to see expanded component."), BorderLayout.BEFORE_FIRST_LINE);

        _frame.setBounds(10, 10, 600, 500);

        _frame.setVisible(true);

    }

    // create property table
    private static HierarchicalTable createTable() {
        _quotesTableModel = new QuoteTableModel();
        HierarchicalTable table = new HierarchicalTable(_quotesTableModel);
        table.setName("Quote Table");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setExpandableColumn(-1);
        table.setSingleExpansion(true);
        return table;
    }


    static String[] QUOTE_COLUMNS = new String[]{"Symbol", "Name", "Last", "Change", "Volume"};

    static Object[][] QUOTES = new Object[][]{
        new Object[]{"AA","ALCOA INC","32.88","+0.53 (1.64%)","4156200"},
        new Object[]{"AIG","AMER INTL GROUP","69.53"," -0.58 (0.83%)","4369200"},
        new Object[]{"AXP","AMER EXPRESS CO","48.90"," -0.35 (0.71%)","4103600"},
        new Object[]{"BA","BOEING CO","49.14"," -0.18 (0.36%)","3573700"},
        new Object[]{"C","CITIGROUP","44.21"," -0.89 (1.97%)","28594900"},
        new Object[]{"CAT","CATERPILLAR INC","79.40","+0.62 (0.79%)","1458200"},
        new Object[]{"DD","DU PONT CO","42.62"," -0.14 (0.33%)","1832700"},
        new Object[]{"DIS","WALT DISNEY CO","23.87"," -0.32 (1.32%)","4443600"},
        new Object[]{"GE","GENERAL ELEC CO","33.37","+0.24 (0.72%)","31429500"},
        new Object[]{"GM","GENERAL MOTORS","43.94"," -0.20 (0.45%)","3722100"},
        new Object[]{"HD","HOME DEPOT INC","34.33"," -0.18 (0.52%)","5367900"},
        new Object[]{"HON","HONEYWELL INTL","35.70","+0.23 (0.65%)","4092100"},
        new Object[]{"HPQ","HEWLETT-PACKARD","19.65"," -0.25 (1.26%)","11003000"},
        new Object[]{"IBM","INTL BUS MACHINE","84.02"," -0.11 (0.13%)","6880500"},
        new Object[]{"INTC","INTEL CORP","23.15"," -0.23 (0.98%)","95177008"},
        new Object[]{"JNJ","JOHNSON&JOHNSON","55.35"," -0.57 (1.02%)","5428000"},
        new Object[]{"JPM","JP MORGAN CHASE","36.00"," -0.45 (1.23%)","12135300"},
        new Object[]{"KO","COCA COLA CO","50.84"," -0.32 (0.63%)","4143600"},
        new Object[]{"MCD","MCDONALDS CORP","27.91","+0.12 (0.43%)","6110800"},
        new Object[]{"MMM","3M COMPANY","88.62","+0.43 (0.49%)","2073800"},
        new Object[]{"MO","ALTRIA GROUP","48.20"," -0.80 (1.63%)","6005500"},
        new Object[]{"MRK","MERCK & CO","44.71"," -0.97 (2.12%)","5472100"},
        new Object[]{"MSFT","MICROSOFT CP","27.87"," -0.26 (0.92%)","46717716"},
        new Object[]{"PFE","PFIZER INC","32.58"," -1.43 (4.20%)","28783200"},
        new Object[]{"PG","PROCTER & GAMBLE","55.01"," -0.07 (0.13%)","5538400"},
        new Object[]{"SBC","SBC COMMS","23.00"," -0.54 (2.29%)","6423400"},
        new Object[]{"UTX","UNITED TECH CP","91.00","+1.16 (1.29%)","1868600"},
        new Object[]{"VZ","VERIZON COMMS","34.81"," -0.35 (1.00%)","4182600"},
        new Object[]{"WMT","WAL-MART STORES","52.33"," -0.25 (0.48%)","6776700"},
        new Object[]{"XOM","EXXON MOBIL","45.32"," -0.14 (0.31%)","7838100"}
    };

    static class QuoteTableModel extends DefaultTableModel implements HierarchicalTableModel {

        public QuoteTableModel() {
            super(QUOTES, QUOTE_COLUMNS);
        }

        public boolean isCellEditable(int row, int column) {
            return false;
        }

        public boolean hasChildComponent(int row) {
            return true;
        }

        public boolean isHierarchical(int row) {
            return true;
        }

        public JComponent getChildComponent(int row) {
            return new HierarchicalPanel(new TradePanel(_table, row), BorderFactory.createEmptyBorder());
        }
    }

    static class TradePanel extends JPanel {
        private HierarchicalTable _table;
        private int _row;

        public TradePanel(HierarchicalTable table, int row) {
            _table = table;
            _row = row;
            initComponents();
        }

        public TradePanel() {
            initComponents();
        }

        void initComponents() {
            setLayout(new BorderLayout(4, 4));
            setBorder(BorderFactory.createEmptyBorder(6, 2, 6, 2));
            add(createDetailPanel(), BorderLayout.CENTER);
            add(createButtonPanel(), BorderLayout.AFTER_LAST_LINE);
            JideSwingUtilities.setOpaqueRecursively(this, false);
            setOpaque(true);
            setBackground(BG1);
        }

        JComponent createDetailPanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new JideBoxLayout(panel, JideBoxLayout.X_AXIS));
            panel.add(new LabelCombobox("Order Type:", 'O', new String[]{"Select one", "Buy", "Sell", "Sell short", "Buy to over"}));
            panel.add(Box.createHorizontalStrut(6), JideBoxLayout.FIX);
            panel.add(new LabelTextField("Quality:", 'Q', 8));
            panel.add(Box.createHorizontalStrut(6), JideBoxLayout.FIX);
            panel.add(new LabelTextField("Price:", 'P', 8));
            panel.add(Box.createHorizontalStrut(6), JideBoxLayout.FIX);
            panel.add(new LabelCombobox("Price Type:", 'R', new String[]{"Select one", "Limit", "Market", "Stop", "Stop limit"}));
            panel.add(Box.createHorizontalStrut(6), JideBoxLayout.FIX);
            panel.add(new LabelCombobox("Term:", 'T', new String[]{"Day", "GTC"}));
            return panel;
        }

        JComponent createButtonPanel() {
            ButtonPanel buttonPanel = new ButtonPanel();
            buttonPanel.addButton(new JButton(new AbstractAction("Trade") {
                public void actionPerformed(ActionEvent e) {
                    _table.collapseRow(_row);
                }
            }));
            buttonPanel.addButton(new JButton(new AbstractAction("Cancel") {
                public void actionPerformed(ActionEvent e) {
                    _table.collapseRow(_row);
                }
            }));
            return buttonPanel;
        }
    }

    static class LabelTextField extends JPanel {
        JTextField _textField;
        JLabel _label;

        public LabelTextField(String label, char mnemonic, int width) {
            _label = new JLabel(label);
            _textField = new JTextField(width);
            _label.setLabelFor(_textField);
            _label.setDisplayedMnemonic(mnemonic);
            setLayout(new BorderLayout(2, 2));
            add(_label, BorderLayout.BEFORE_FIRST_LINE);
            add(_textField, BorderLayout.CENTER);
        }
    }

    static class LabelCombobox extends JPanel {
        ListComboBox _comboxBox;
        JLabel _label;

        public LabelCombobox(String label, char mnemonic, Object[] values) {
            _label = new JLabel(label);
            _comboxBox = new ListComboBox(values);
            _comboxBox.setSelectedItem(values[0]);
            _label.setLabelFor(_comboxBox);
            _label.setDisplayedMnemonic(mnemonic);
            setLayout(new BorderLayout(2, 2));
            add(_label, BorderLayout.BEFORE_FIRST_LINE);
            add(_comboxBox, BorderLayout.CENTER);
        }
    }
}
