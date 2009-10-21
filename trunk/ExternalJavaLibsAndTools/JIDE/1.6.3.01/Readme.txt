JIDE Docking Framework 1.6.2, JIDE Action Framework 1.0.1, JIDE Components 1.4.1, JIDE Grids 1.4.1 and JIDE Dialogs 1.1.1
                                        ----    Last update on November 21, 2004

*******************************
    JIDE Docking Framework
*******************************
JIDE Docking Framework provides a very powerful yet easy-to-use dockable window solution as you can
find in Visual Studio .NET IDE or Eclipse IDE. Dockable window is proven to be the most effective
solution to the limited screen space problem and complex window layout. Since its first release at
the end of 2002, JIDE Docking Framework has been chosen by hundreds of companies all over the world.
It is no doubt the best dockable window solution currently available in the market.

By taking advantage of Swing pluggable look and feel technology, we made several flavors of JIDE
Docking Framework. In current release, we include Office 2003-like LookAndFeel, Visual Studio .NET-like
LookAndFeel, Eclipse-like LookAndFeel, Aqua-like LookAndFeel (Mac OS X) and the default Swing Metal
LookAndFeel. You can always find a style you like the most.

Dockable Window Features

* Drag-n-drop support
* Unlimited nested levels of dockable window
* Autohide windows
* Sliding autohide window
* Floating dockable window
* Maximize dockable window
* Notifying dockable window
* Hide central workspace area

LookAndFeel Style Features

* Office 2003 style
* Visual Studio .NET style
* Eclipse style
* Metal style
* Aqua style

Persistence Layout Features

* Load and save layout using javax pref package
* Load and save layout using file
* Load and save layout using InputStream
* List available layouts
* Instantly switching layout

*******************************
    JIDE Action Framework
*******************************
Almost all applications need toolbars and menu bar. These components are JIDE Action Framework all
about. It provides a much more sophisticated component called CommandBar to replace both JToolBar and
JMenuBar, the default Swing implantations. Most features you saw in Microsoft Office products regarding
toolbars and menu bar, you will find them in JIDE Action Framework product.

You also can choose different style in JIDE Action Framework. In currently release, we included Office
2003 style, Visual Studio .NET style, Office XP style, Eclipse style, even Aqua style on Mac OS X. The
default Metal style is in there too.

Dockable Command Bar Features

* Drag-n-drop support
* Shrinkable command bar
* Docking command bar at any sides of the main window
* Floating command bar
* Show/hide each command bar on fly
* Command bars at any child level panel

Supported Components on Command Bar

* JideButton - regular button
* JideSplitButton - combination of button and popup menu
* JideColorSplitButton - color chooser for toolbar
* JideMenu - submenu on toolbar
* Comboboxes

LookAndFeel Style Features

* Office 2003 style
* Visual Studio .NET style
* Eclipse style
* Metal style
* Aqua style

Persistence Layout Features

* Load and save layout using javax pref package
* Load and save layout using file
* Load and save layout using InputStream
* List available layouts
* Instantly switching layout
* Single layout file for both action framework and docking framework

***********************
    JIDE Components
***********************
Thousands and thousands of developers' valuable hours are wasted on building the same component which
has been built elsewhere. Why don't you focus on the most value-add part in your application and we
build those commonly used components for you? That's exact the purpose of JIDE Components. This single
product collected over ten components. We are sure you will find them very handy during the development
of your application.

UI Components

* DocumentPane - a drag-n-drop tabbed document interface, supporting split view, multiple view etc. An ideal replacement for JDesktopPane
* StatusBar - fully customizable status bar that you can use to display message, status, progress, time and other information
* JideTabbedPane - an extended version of JTabbedPane supporting shrinkable tabs, close button on tab, editable tab etc.
* JideSplitPane - an extended version of JSplitPane supporting multiple splits (JSplitPane can only have two splits)
* FloorTabbedPane - also known as outlook shortcut bar
* CollapsiblePane - also known as task bar as in Windows XP
* JideButton - an ideal replacement for toolbar button
* JideSplitButton - a composite component which is a combination of button and popup
* Searchable JList, JTree and JTable - type in any text to quickly find the row or tree node or table cell that matching the text you typed in.

Utility Classes

* IconFactory - simplify the usage of icon across your application
* SystemInfo - a utility class to tell your the current system information such as OS name and version, JDK version requirement etc.
* A fast gradient paint method on JideSwingUtilities. By leveraging DirectDraw, the fast gradient paint is 2 to 40 times faster than normal GradientPaint.

*********************
    JIDE Grids
*********************
Believe it or not, JTable is probably one of the most used Swing components in most Swing applications.
To unleash the power of JTable, we introduced JIDE Grids - a collection of all JTable related components.

In current release, in addition to the foundation work we laid out for JTable, we also provided several
very useful component table related components such as PropertyTable - a two-column JTable which can be
used in many applications to show object properties, SortableTable - a multiple-column sortable table,
FilterableTable - a table can accept filters and HierarchicalTable - allows nesting components inside table.

UI Components

* PropertyGrid - a two-column JTable used to display properties of any object with nested structure
* SortableTable - supporting sort by multiple columns
* FilterableTableModel - supporting adding filters on each column
* HierarchicalTable - nesting any components as children component of a table row
* ColorComboBox and ColorChooserPanel - a set of color chooser components from the choosing panel, to combobox, to cell editor, supporting customized color palette.
* DateComboBox and DateChooserPanel - a set of data/month chooser components from the choosing panel, to combobox, to cell editor, supporting internationalization and localization (i18n and l10n)
* AbstractComboBox - supporting any component as popup panel

Utility Classes

* Centralized cell editor and renderer mechanism - customization of cell editor and renderer in one place and use it across the application.
* Centralized object converter mechanism - customization of converting from string to any object or vice verse
* Centralized object comparator mechanism - customization of object comparison and used by SortableTable during sorting
* TableUtils class - a utility class collecting some useful functions for JTable

*********************
    JIDE Dialogs
*********************

Dialogs are used everywhere. JIDE Dialogs is such a product which makes the creation of dialogs easier.

In current release, in addition to a standard dialog template, it also includes several pre-built dialogs
such as wizard, options dialog, tips of the day dialog etc. Don't you need any of those?

UI Components
* Wizard component - supporting different styles such as Microsoft Wizard 97 standard, Java L&F standard
and Mac OS X standard (on Mac OS X only)
* Build-in wizard page templates such as welcome page and completion page
* MultiplePageDialog - can be used as options dialog or user preference dialog, supporting different
style such as IntelliJ option dialog style, VSNET style, IE style, Mozilla Firebird style etc
* Tips of the Day Dialog
* ButtonPanel - layout buttons in different order and using different gap based OS convention
* Pre-built panels such as BannerPanel
* AbstractPage - lazy loading panel with page event (open, closing, closed etc)

************************
   Directory Structure
************************
Readme.txt		        this file
doc\			        documents directory
    *.pdf               developer guides
javadoc\		        javadoc of all public classes
lib\                    jar files directory
    jide-common.jar     the common jar used by all JIDE products
    jide-action.jar     the jar for JIDE Action Framework
    jide-dock.jar       the jar for JIDE Docking Framework
    jide-components.jar the jar for JIDE Components
    jide-grids.jar      the jar for JIDE Grids
    jide-dialogs.jar    the jar for JIDE Dialogs
examples\               examples directory
src\                    source code directory (if you purchased source code license)
    src.zip             source code zip file
license\
    EULA.htm	        end user license agreement

************************
   System Requirements
*************************

1. Any Java-enabled OS
2. j2sdk 1.4.0 and above from Sun. You can download jdk1.4.1 from
http://java.sun.com/j2se/1.4.1/download.html

**********************
   JIDE Software
**********************

JIDE Software is a private held software company that focusing on providing rich client solutions
and services using Java technology. Founded in 2002, JIDE Software has developed five products and
over 30 professional Swing components that cover almost every aspect of Java/Swing development.

With more than 200 customers in 30 countries worldwide, JIDE Software is the clear market leader.
Many companies are depending on technologies JIDE provides to build their applications or frameworks,
deliver their customers with a polished user interface, and simplify their in-house development.

For further information, please contact support@jidesoft.com or visit our website
at http://www.jidesoft.com
