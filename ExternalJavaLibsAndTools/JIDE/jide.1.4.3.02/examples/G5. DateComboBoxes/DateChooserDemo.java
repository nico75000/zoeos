/*
 * @(#)DateChooserDemo.java
 *
 * Copyright 2002 - 2003 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.combobox.DateChooserPanel;
import com.jidesoft.combobox.DateComboBox;
import com.jidesoft.combobox.DefaultDateModel;
import com.jidesoft.combobox.DateFilter;
import com.jidesoft.converter.ObjectConverterManager;
import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;
import java.util.prefs.Preferences;

/**
 * Demoed Component: {@link DateComboBox}, {@link DateChooserPanel}
 * <br>
 * Required jar files: jide-common.jar, jide-grids.jar
 * <br>
 * Required L&F: Jide L&F extension required
 */
public class DateChooserDemo extends JFrame {

    private static DateChooserDemo _frame;
    private static DateComboBox _dateComboBox;
    private static JPanel _pane;
    private SimpleDateFormat _dateFormat;
    private JLabel _restart;
    private static Locale _locale;
    private static Locale[] LOCALES = Locale.getAvailableLocales();

    public DateChooserDemo(String title) throws HeadlessException {
        super(title);
    }

    public static void main(String[] args) {
        Preferences prefs = Preferences.userRoot();
        String locale = prefs.get("date_locale", "en_US");
        for (int i = 0; i < LOCALES.length; i++) {
            Locale l = LOCALES[i];
            if (l.toString().equals(locale)) {
                _locale = l;
                Locale.setDefault(_locale);
            }
        }

        ObjectConverterManager.initDefaultConverter();

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

        _frame = new DateChooserDemo("Demo of DateComboBox");
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        _pane = new JPanel(new BorderLayout());
        _pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        _dateComboBox = createDateComboBox();
        _pane.add(_dateComboBox, BorderLayout.BEFORE_FIRST_LINE);


        _frame.getContentPane().setLayout(new BorderLayout());
        _frame.getContentPane().add(_pane, BorderLayout.CENTER);
        _frame.getContentPane().add(_frame.createOptionsPanel(), BorderLayout.AFTER_LINE_ENDS);

        _frame.setBounds(10, 10, 500, 400);

        _frame.setVisible(true);

    }

    private static DateComboBox createDateComboBox() {
        DefaultDateModel model = new DefaultDateModel();
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.YEAR, 2010);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
        model.setMaxDate(calendar);

        calendar.set(Calendar.YEAR, 1980);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMinimum(Calendar.DAY_OF_YEAR));
        model.setMinDate(calendar);

        DateComboBox dateComboBox = new DateComboBox(model);
        Date currentDate = new Date();
        dateComboBox.setDate(currentDate);

        dateComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED) {
                    _dateComboBox.setSelectedItem(ie.getItem());
                }
            }
        });
        return dateComboBox;
    }

    private JPanel createOptionsPanel() {
        JLabel header = new JLabel("Options");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 13));
        header.setOpaque(true);
        header.setBackground(new Color(0, 0, 128));
        header.setForeground(Color.WHITE);
        header.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.white, Color.lightGray, Color.lightGray, Color.gray),
                        BorderFactory.createEmptyBorder(0, 5, 0, 0)));
        header.setPreferredSize(new Dimension(100, 30));

        JPanel panel = new JPanel();
        panel.setLayout(new JideBoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] formatters = new String[]{
            "(Default)",
            "MMM dd, yyyy",
            "MM/dd/yy",
            "yyyy.MM.dd",
            "EEE M/dd/yyyy",
            "EEE, MMM d, ''yy",
            "yyyyy.MMMMM.dd GGG",
            "EEE, d MMM yyyy",
            "yyMMdd"
        };

        JComboBox comboBox = new JComboBox(formatters);
        comboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof String) {
                    if ((e.getItem()).equals("(Default)")) {
                        _dateFormat = null;
                        _dateComboBox.setFormat(null);
                    }
                    else {
                        SimpleDateFormat dateFormat = new SimpleDateFormat((String) e.getItem());
                        _dateFormat = dateFormat;
                        _dateComboBox.setFormat(_dateFormat);
                    }
                }
            }
        });

        final DateComboBox minDateComboBox = new DateComboBox();
        minDateComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    _dateComboBox.getDateModel().setMinDate(minDateComboBox.getCalendar());
                }
            }
        });
        minDateComboBox.setCalendar(_dateComboBox.getDateModel().getMinDate());

        final DateComboBox maxDateComboBox = new DateComboBox();
        maxDateComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    _dateComboBox.getDateModel().setMaxDate(maxDateComboBox.getCalendar());
                }
            }
        });
        maxDateComboBox.setCalendar(_dateComboBox.getDateModel().getMaxDate());

        final JComboBox dateValidatorComboBox = new JComboBox(new String[] {
            "<None>",
            "This week",
            "This month",
            "Later this month",
            "Weekday only",
            "Weekend only"
        });
        dateValidatorComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if(_dateComboBox.getDateModel() instanceof DefaultDateModel) {
                        DateFilter dateFilter = null;
                        switch (dateValidatorComboBox.getSelectedIndex()) {
                            case 0:
                                dateFilter = null;
                                break;
                            case 1:
                                dateFilter = DefaultDateModel.THIS_WEEK;
                                break;
                            case 2:
                                dateFilter = DefaultDateModel.THIS_MONTH_ONLY;
                                break;
                            case 3:
                                dateFilter = DefaultDateModel.LATER_THIS_MONTH;
                                break;
                            case 4:
                                dateFilter = DefaultDateModel.WEEKDAY_ONLY;
                                break;
                            case 5:
                                dateFilter = DefaultDateModel.WEEKEND_ONLY;
                                break;

                        }
                        ((DefaultDateModel) _dateComboBox.getDateModel()).clearDateFilters();
                        if(dateFilter != null) {
                            ((DefaultDateModel) _dateComboBox.getDateModel()).addDateFilter(dateFilter);
                        }
                    }
                }
            }
        });

        JComboBox locale = new JComboBox(LOCALES);
        locale.setSelectedItem(_locale);
        locale.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof Locale) {
                    Preferences prefs = Preferences.userRoot();
                    prefs.put("date_locale", e.getItem().toString());
                    _restart.setText("<HTML><FONT COLOR=RED>Restart is needed</FONT></HTML>");
                }
            }
        });

        panel.add(new JLabel("Set DateFormat"));
        panel.add(Box.createVerticalStrut(6), JideBoxLayout.FIX);
        panel.add(comboBox);
        panel.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);
        panel.add(new JLabel("Set MinDate"));
        panel.add(Box.createVerticalStrut(6), JideBoxLayout.FIX);
        panel.add(minDateComboBox);
        panel.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);
        panel.add(new JLabel("Set MaxDate"));
        panel.add(Box.createVerticalStrut(6), JideBoxLayout.FIX);
        panel.add(maxDateComboBox);
        panel.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);
        panel.add(new JLabel("Example Date Filters"));
        panel.add(Box.createVerticalStrut(6), JideBoxLayout.FIX);
        panel.add(dateValidatorComboBox);
        panel.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);
        final JCheckBox todayCheckBox = (JCheckBox) panel.add(new JCheckBox("Show Today Button"));
        todayCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                _dateComboBox.setShowTodayButton(todayCheckBox.isSelected());
            }
        });
        todayCheckBox.setSelected(_dateComboBox.isShowTodayButton());
        panel.add(Box.createVerticalStrut(6), JideBoxLayout.FIX);
        final JCheckBox noneCheckBox = (JCheckBox) panel.add(new JCheckBox("Show None Button"));
        noneCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                _dateComboBox.setShowNoneButton(noneCheckBox.isSelected());
            }
        });
        noneCheckBox.setSelected(_dateComboBox.isShowNoneButton());
        panel.add(Box.createVerticalStrut(6), JideBoxLayout.FIX);
        panel.add(new JLabel("Set Locale"));
        panel.add(Box.createVerticalStrut(6), JideBoxLayout.FIX);
        panel.add(locale);
        _restart = new JLabel(" ");
        panel.add(Box.createVerticalStrut(6), JideBoxLayout.FIX);
        panel.add(_restart);
        panel.add(Box.createGlue(), JideBoxLayout.VARY);

        JPanel optionsPanel = new JPanel(new BorderLayout());

        optionsPanel.add(header, BorderLayout.BEFORE_FIRST_LINE);
        optionsPanel.add(panel, BorderLayout.CENTER);

        return optionsPanel;
    }
}
