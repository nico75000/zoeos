
import com.jidesoft.combobox.*;
import com.jidesoft.converter.ObjectConverterManager;
import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.utils.Lm;

import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeNode;
import java.awt.*;

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

        _frame = new ComboBoxDemo("Demo of ComboBox");
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        _pane = new JPanel();
        _pane.setLayout(new JideBoxLayout(_pane, JideBoxLayout.Y_AXIS));
        _pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // create boolean combobox
        AbstractComboBox booleanComboBox = createBooleanComboBox();
        booleanComboBox.setToolTipText("ComboBox to choose boolean");
        booleanComboBox.setSelectedItem(Boolean.FALSE);
        booleanComboBox.setPrototypeDisplayValue(Boolean.FALSE);
        _pane.add(new JLabel("ListComboBox (Boolean)"));
        _pane.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
        _pane.add(booleanComboBox);
        _pane.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

        // create font name combobox
        AbstractComboBox fontNameComboBox = createFontNameComboBox();
        fontNameComboBox.setToolTipText("ComboBox to choose font name from a list");
        fontNameComboBox.setEditable(false);
        fontNameComboBox.setSelectedItem("Arial");
        _pane.add(new JLabel("ListComboBox (Font Name) (Non-editable)"));
        _pane.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
        _pane.add(fontNameComboBox);
        _pane.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

        // create font name combobox
        AbstractComboBox fontNameComboBox2 = createFontNameComboBox();
        fontNameComboBox2.setToolTipText("ComboBox to choose font name or type in font name directly");
        fontNameComboBox2.setSelectedItem("Arial");
        _pane.add(new JLabel("ListComboBox (Font Name) (Editable)"));
        _pane.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
        _pane.add(fontNameComboBox2);
        _pane.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

        // create font name combobox
        AbstractComboBox treeComboBox = new TreeComboBox() {
            protected boolean isValidSelection(TreePath path) {
                TreeNode treeNode = (TreeNode) path.getLastPathComponent();
                return treeNode.isLeaf();
            }
        };
        treeComboBox.setEditable(false);
        treeComboBox.setToolTipText("ComboBox which has a tree as selection list");
        _pane.add(new JLabel("TreeComboBox (Non-editable, only leaf node selectable)"));
        _pane.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
        _pane.add(treeComboBox);
        _pane.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

        // create font name combobox
        AbstractComboBox treeComboBox2 = new TreeComboBox();
        treeComboBox2.setToolTipText("ComboBox which has a tree as selection list");
        _pane.add(new JLabel("TreeComboBox (Editable)"));
        _pane.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
        _pane.add(treeComboBox2);
        _pane.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

        // create date combobox
        DateComboBox dateComboBox = new DateComboBox();
//        dateComboBox.setDate(Calendar.getInstance().getTime());
        dateComboBox.setToolTipText("ComboBox to chooose date");
        dateComboBox.setDate(null);

        _pane.add(new JLabel("DateComboBox"));
        _pane.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
        _pane.add(dateComboBox);
        _pane.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

        // create color combobox
        ColorComboBox colorComboBox = new ColorComboBox();
        colorComboBox.setSelectedColor(Color.GREEN);
        colorComboBox.setToolTipText("ComboBox to chooose color");
        _pane.add(new JLabel("ColorComboBox"));
        _pane.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
        _pane.add(colorComboBox);
        _pane.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

        // create file chooser combobox
        FileChooserComboBox fileComboBox = new FileChooserComboBox();
        fileComboBox.setToolTipText("ComboBox to choose a file");
        _pane.add(new JLabel("FileChooserComboBox"));
        _pane.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
        _pane.add(fileComboBox);
        _pane.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

        _pane.add(Box.createGlue(), JideBoxLayout.VARY);
        _frame.getContentPane().setLayout(new BorderLayout());
        _frame.getContentPane().add(_pane, BorderLayout.CENTER);


        _frame.pack();

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
