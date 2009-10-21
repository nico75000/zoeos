
import com.jidesoft.combobox.*;
import com.jidesoft.converter.ObjectConverterManager;
import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.util.Locale;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * Demoed Component: {@link ListComboBox}, {@link FileChooserComboBox}, {@link ColorComboBox}, {@link DateComboBox}
 * <br>
 * Required jar files: jide-common.jar, jide-grids.jar
 * <br>
 * Required L&F: Jide L&F extension required
 */
public class ComboBoxDemo extends JFrame {

    private static ComboBoxDemo _frame;
    private static JPanel _pane;

    public ComboBoxDemo(String title) throws HeadlessException {
        super(title);
    }

    public static void main(String[] args) {
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

        _frame = new ComboBoxDemo("Demo of ComboBox");
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        _pane = new JPanel();
        _pane.setLayout(new JideBoxLayout(_pane, JideBoxLayout.Y_AXIS));
        _pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // create boolean combobox
        AbstractComboBox booleanComboBox = createBooleanComboBox();
        booleanComboBox.setSelectedItem(Boolean.FALSE);
        _pane.add(new JLabel("ListComboBox (Boolean)"));
        _pane.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
        _pane.add(booleanComboBox);
        _pane.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

        // create font name combobox
        AbstractComboBox fontNameComboBox = createFontNameComboBox();
        fontNameComboBox.setSelectedItem("Arial");
        _pane.add(new JLabel("ListComboBox (Font Name)"));
        _pane.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
        _pane.add(fontNameComboBox);
        _pane.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

        // create date combobox
        DateComboBox dateComboBox = new DateComboBox();
        dateComboBox.setDate(Calendar.getInstance().getTime());
        _pane.add(new JLabel("DateComboBox"));
        _pane.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
        _pane.add(dateComboBox);
        _pane.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

        // create color combobox
        ColorComboBox colorComboBox = new ColorComboBox();
        colorComboBox.setSelectedColor(Color.GREEN);
        _pane.add(new JLabel("ColorComboBox"));
        _pane.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
        _pane.add(colorComboBox);
        _pane.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

        // create file chooser combobox
        FileChooserComboBox fileComboBox = new FileChooserComboBox();
        _pane.add(new JLabel("FileChooserComboBox"));
        _pane.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
        _pane.add(fileComboBox);
        _pane.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

        _pane.add(Box.createGlue(), JideBoxLayout.VARY);
        _frame.getContentPane().setLayout(new BorderLayout());
        _frame.getContentPane().add(_pane, BorderLayout.CENTER);


        _frame.setBounds(10, 10, 200, 300);

        _frame.setVisible(true);

    }

    private static ListComboBox createBooleanComboBox() {
        ListComboBox booleanComboBox = new ListComboBox(ListComboBox.BOOLEAN_ARRAY, Boolean.class);
        return booleanComboBox;
    }

    private static ListComboBox createFontNameComboBox() {
        String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        ListComboBox fontNameComboBox = new ListComboBox(fontNames, String.class);
        return fontNameComboBox;
    }
}
