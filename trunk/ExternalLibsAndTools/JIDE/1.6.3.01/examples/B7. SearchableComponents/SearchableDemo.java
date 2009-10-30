/*
 * @(#)SearchableDemo.java
 *
 * Copyright 2002 - 2004 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Demoed Component: {@link com.jidesoft.swing.Searchable}
 * <br>
 * Required jar files: jide-common.jar
 */
public class SearchableDemo extends JFrame {

    private static SearchableDemo _frame;
    private static JComponent _splitPane;

    public SearchableDemo(String title) throws HeadlessException {
        super(title);
    }

    public SearchableDemo() throws HeadlessException {
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

        _frame = new SearchableDemo("Demo of Searchable Components");
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        _splitPane = createDemoPanes();
        _frame.getContentPane().setLayout(new BorderLayout(10, 10));
        _frame.getContentPane().add(_splitPane, BorderLayout.CENTER);
        JLabel label = new JLabel("<HTML>" +
                "<B>Helpful hints:</B><BR>" +
                "<B>1.</B> Press any letter key to start the search<BR>" +
                "<B>2.</B> Press up/down arrow key to navigation to next or previous matching occurrence<BR>" +
                "<B>3.</B> Hold CTRL key while pressing up/down arrow key to to multiple selection<BR>" +
                "<B>4.</B> Press CTRL+A to select all matching occurrences<BR>" +
                "<B>5.</B> Use '?' to match any character or '*' to match several characters<BR>" +
                "<B>-></B> Refer to \"JIDE Components Developer Guide\" section \"Searchable Components\" for more customization options" +
                "</HTML>");
        label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        _frame.getContentPane().add(label, BorderLayout.BEFORE_FIRST_LINE);

        _frame.setBounds(10, 10, 800, 500);

        _frame.setVisible(true);

    }

    private static JComponent createDemoPanes() {
        JTree tree = new JTree();
        TreeSearchable treeSearchable = (TreeSearchable) SearchableUtils.installSearchable(tree);

        JList list = new JList(getCountryNames());
        ListSearchable listSearchable = (ListSearchable) SearchableUtils.installSearchable(list);

// listen to the searching text change
//        listSearchable.addPropertyChangeListener(new PropertyChangeListener(){
//            public void propertyChange(PropertyChangeEvent evt) {
//                System.out.println(evt.getOldValue() + " ==> " + evt.getNewValue());
//
//            }
//        });

        JComboBox comboBox = new JComboBox(getCountryNames());
        comboBox.setEditable(false); // combobox searchable only works when combobox is not editable.
        ComboBoxSearchable comboBoxSearchable = (ComboBoxSearchable) SearchableUtils.installSearchable(comboBox);

        JTable table = new JTable(new QuoteTableModel());
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        SearchableUtils.installSearchable(table);

        JideSplitPane pane = new JideSplitPane(JideSplitPane.HORIZONTAL_SPLIT);
        pane.add(createTitledPanel(new JLabel("Searchable JTree"), new JScrollPane(tree)));
        JPanel listPanel = createTitledPanel(new JLabel("Searchable JList"), new JScrollPane(list));
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(listPanel, BorderLayout.CENTER);
        panel.add(createTitledPanel(new JLabel("Searchable JComboBox"), comboBox), BorderLayout.AFTER_LAST_LINE);
        pane.add(panel);
        pane.add(createTitledPanel(new JLabel("Searchable JTable (Configured to search for \"name\" column only.)"), new JScrollPane(table)));

        return pane;
    }

    private static JPanel createTitledPanel(JComponent titleComponent, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(2, 2));
        panel.add(titleComponent, BorderLayout.BEFORE_FIRST_LINE);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private static String[] getCountryNames() {
        return new String[]{
            "Andorra",
            "United Arab Emirates",
            "Afghanistan",
            "Antigua And Barbuda",
            "Anguilla",
            "Albania",
            "Armenia",
            "Netherlands Antilles",
            "Angola",
            "Antarctica",
            "Argentina",
            "American Samoa",
            "Austria",
            "Australia",
            "Aruba",
            "Azerbaijan",
            "Bosnia And Herzegovina",
            "Barbados",
            "Bangladesh",
            "Belgium",
            "Burkina Faso",
            "Bulgaria",
            "Bahrain",
            "Burundi",
            "Benin",
            "Bermuda",
            "Brunei Darussalam",
            "Bolivia",
            "Brazil",
            "Bahamas",
            "Bhutan",
            "Bouvet Island",
            "Botswana",
            "Belarus",
            "Belize",
            "Canada",
            "Cocos (Keeling) Islands",
            "Congo, The Democratic Republic Of The",
            "Central African Republic",
            "Congo",
            "Switzerland",
            "Côte D'Ivoire",
            "Cook Islands",
            "Chile",
            "Cameroon",
            "China",
            "Colombia",
            "Costa Rica",
            "Cuba",
            "Cape Verde",
            "Christmas Island",
            "Cyprus",
            "Czech Republic",
            "Germany",
            "Djibouti",
            "Denmark",
            "Dominica",
            "Dominican Republic",
            "Algeria",
            "Ecuador",
            "Estonia",
            "Egypt",
            "Western Sarara",
            "Eritrea",
            "Spain",
            "Ethiopia",
            "Finland",
            "Fiji",
            "Falkland Islands (Malvinas)",
            "Micronesia, Federated States Of",
            "Faroe Islands",
            "France",
            "Gabon",
            "United Kingdom",
            "Grenada",
            "Georgia",
            "French Guiana",
            "Ghana",
            "Gibraltar",
            "Greenland",
            "Gambia",
            "Guinea",
            "Guadeloupe",
            "Equatorial Guinea",
            "Greece",
            "South Georgia And The South Sandwich Islands",
            "Guatemala",
            "Guam",
            "Guinea-bissau",
            "Guyana",
            "Hong Kong",
            "Heard Island And Mcdonald Islands",
            "Honduras",
            "Croatia",
            "Haiti",
            "Hungary",
            "Indonesia",
            "Ireland",
            "Israel",
            "India",
            "British Indian Ocean Territory",
            "Iraq",
            "Iran, Islamic Republic Of",
            "Iceland",
            "Italy",
            "Jamaica",
            "Jordan",
            "Japan",
            "Kenya",
            "Kyrgyzstan",
            "Cambodia",
            "Kiribati",
            "Comoros",
            "Saint Kitts And Nevis",
            "Korea, Democratic People'S Republic Of",
            "Korea, Republic Of",
            "Kuwait",
            "Cayman Islands",
            "Kazakhstan",
            "Lao People'S Democratic Republic",
            "Lebanon",
            "Saint Lucia",
            "Liechtenstein",
            "Sri Lanka",
            "Liberia",
            "Lesotho",
            "Lithuania",
            "Luxembourg",
            "Latvia",
            "Libyan Arab Jamabiriya",
            "Morocco",
            "Monaco",
            "Moldova, Republic Of",
            "Madagascar",
            "Marshall Islands",
            "Macedonia, The Former Yugoslav Repu8lic Of",
            "Mali",
            "Myanmar",
            "Mongolia",
            "Macau",
            "Northern Mariana Islands",
            "Martinique",
            "Mauritania",
            "Montserrat",
            "Malta",
            "Mauritius",
            "Maldives",
            "Malawi",
            "Mexico",
            "Malaysia",
            "Mozambique",
            "Namibia",
            "New Caledonia",
            "Niger",
            "Norfolk Island",
            "Nigeria",
            "Nicaragua",
            "Netherlands",
            "Norway",
            "Nepal",
            "Niue",
            "New Zealand",
            "Oman",
            "Panama",
            "Peru",
            "French Polynesia",
            "Papua New Guinea",
            "Philippines",
            "Pakistan",
            "Poland",
            "Saint Pierre And Miquelon",
            "Pitcairn",
            "Puerto Rico",
            "Portugal",
            "Palau",
            "Paraguay",
            "Qatar",
            "Réunion",
            "Romania",
            "Russian Federation",
            "Rwanda",
            "Saudi Arabia",
            "Solomon Islands",
            "Seychelles",
            "Sudan",
            "Sweden",
            "Singapore",
            "Saint Helena",
            "Slovenia",
            "Svalbard And Jan Mayen",
            "Slovakia",
            "Sierra Leone",
            "San Marino",
            "Senegal",
            "Somalia",
            "Suriname",
            "Sao Tome And Principe",
            "El Salvador",
            "Syrian Arab Republic",
            "Swaziland",
            "Turks And Caicos Islands",
            "Chad",
            "French Southern Territories",
            "Togo",
            "Thailand",
            "Tajikistan",
            "Tokelau",
            "Turkmenistan",
            "Tunisia",
            "Tonga",
            "East Timor",
            "Turkey",
            "Trinidad And Tobago",
            "Tuvalu",
            "Taiwan, Province Of China",
            "Tanzania, United Republic Of",
            "Ukraine",
            "Uganda",
            "United States Minor Outlying Islands",
            "United States",
            "Uruguay",
            "Uzbekistan",
            "Venezuela",
            "Virgin Islands, British",
            "Virgin Islands, U.S.",
            "Viet Nam",
            "Vanuatu",
            "Wallis And Futuna",
            "Samoa",
            "Yemen",
            "Mayotte",
            "Yugoslavia",
            "South Africa",
            "Zambia",
            "Zimbabwe"
        };
    }

    static String[] QUOTE_COLUMNS = new String[]{"Symbol", "Name", "Last", "Change", "Volume"};

    static Object[][] QUOTES = new Object[][]{
        new Object[]{"AA", "ALCOA INC", "32.88", "+0.53 (1.64%)", "4156200"},
        new Object[]{"AIG", "AMER INTL GROUP", "69.53", " -0.58 (0.83%)", "4369200"},
        new Object[]{"AXP", "AMER EXPRESS CO", "48.90", " -0.35 (0.71%)", "4103600"},
        new Object[]{"BA", "BOEING CO", "49.14", " -0.18 (0.36%)", "3573700"},
        new Object[]{"C", "CITIGROUP", "44.21", " -0.89 (1.97%)", "28594900"},
        new Object[]{"CAT", "CATERPILLAR INC", "79.40", "+0.62 (0.79%)", "1458200"},
        new Object[]{"DD", "DU PONT CO", "42.62", " -0.14 (0.33%)", "1832700"},
        new Object[]{"DIS", "WALT DISNEY CO", "23.87", " -0.32 (1.32%)", "4443600"},
        new Object[]{"GE", "GENERAL ELEC CO", "33.37", "+0.24 (0.72%)", "31429500"},
        new Object[]{"GM", "GENERAL MOTORS", "43.94", " -0.20 (0.45%)", "3722100"},
        new Object[]{"HD", "HOME DEPOT INC", "34.33", " -0.18 (0.52%)", "5367900"},
        new Object[]{"HON", "HONEYWELL INTL", "35.70", "+0.23 (0.65%)", "4092100"},
        new Object[]{"HPQ", "HEWLETT-PACKARD", "19.65", " -0.25 (1.26%)", "11003000"},
        new Object[]{"IBM", "INTL BUS MACHINE", "84.02", " -0.11 (0.13%)", "6880500"},
        new Object[]{"INTC", "INTEL CORP", "23.15", " -0.23 (0.98%)", "95177008"},
        new Object[]{"JNJ", "JOHNSON&JOHNSON", "55.35", " -0.57 (1.02%)", "5428000"},
        new Object[]{"JPM", "JP MORGAN CHASE", "36.00", " -0.45 (1.23%)", "12135300"},
        new Object[]{"KO", "COCA COLA CO", "50.84", " -0.32 (0.63%)", "4143600"},
        new Object[]{"MCD", "MCDONALDS CORP", "27.91", "+0.12 (0.43%)", "6110800"},
        new Object[]{"MMM", "3M COMPANY", "88.62", "+0.43 (0.49%)", "2073800"},
        new Object[]{"MO", "ALTRIA GROUP", "48.20", " -0.80 (1.63%)", "6005500"},
        new Object[]{"MRK", "MERCK & CO", "44.71", " -0.97 (2.12%)", "5472100"},
        new Object[]{"MSFT", "MICROSOFT CP", "27.87", " -0.26 (0.92%)", "46717716"},
        new Object[]{"PFE", "PFIZER INC", "32.58", " -1.43 (4.20%)", "28783200"},
        new Object[]{"PG", "PROCTER & GAMBLE", "55.01", " -0.07 (0.13%)", "5538400"},
        new Object[]{"SBC", "SBC COMMS", "23.00", " -0.54 (2.29%)", "6423400"},
        new Object[]{"UTX", "UNITED TECH CP", "91.00", "+1.16 (1.29%)", "1868600"},
        new Object[]{"VZ", "VERIZON COMMS", "34.81", " -0.35 (1.00%)", "4182600"},
        new Object[]{"WMT", "WAL-MART STORES", "52.33", " -0.25 (0.48%)", "6776700"},
        new Object[]{"XOM", "EXXON MOBIL", "45.32", " -0.14 (0.31%)", "7838100"}
    };

    static class QuoteTableModel extends DefaultTableModel {
        public QuoteTableModel() {
            super(QUOTES, QUOTE_COLUMNS);
        }

        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }
}
