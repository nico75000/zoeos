JIDE Docking Framework 1.4.0, JIDE Components 1.3.1, JIDE Grids 1.2.0 and JIDE Dialogs 1.0.0
                                                ----  Released on January 5, 2003

***********************
    JIDE Components
***********************
Thousands and thousands of developers' valuable hours are wasted on building the
same component which has been built elsewhere. Why don't you focus on the most
value-add part in your application and we build those components for you? JIDE
Components is such a Java UI components library.

*******************************
    JIDE Docking Framework
*******************************
Swing or .NET? This is a decision that many IT developers need to make. Both have
pros and cons, depending on which one you care the most. If you happened to choose
Java and Swing, as we did, you probably found there aren't many ready-to-use
component in Swing as .NET has. An obviously missing one is dockable windows as
you see in Visual Studio .NET. However everything changed after JIDE releases JIDE
Docking Framework. Now, you can get the same features of dockable windows, but,
using purely Swing.
By taking advantage of Swing pluggable look and feel technology, we plan to make
several flavors of JIDE Docking Framework. So far we have released Visual
Studio .NET LookAndFeel, Eclipse LookAndFeel, Aqua LookAndFeel and default Swing Metal 
LookAndFeel. However you can also create your own version look and feel.

*********************
    JIDE Grids
*********************
Believe it or not, JTable is probably one of the most used Swing components in most
Swing application.
Many people complain about the design of JTable. However in our opinion, every design
has its pros and cons. So is JTable. People have so many various kinds of requirements,
so it’s really hard to design such a complex component as JTable which can satisfy
everybody’s need. However JTable does leave many extension points so that you can enhance
it to meet your needs. So as long as we keep improving it, it will get better and better.
JIDE Grids is one step toward this goal. All components in JIDE Grids will be fully
compatible with JTable. It will not only make your migration to easier but also make it
possible to leverage future improvement to JTable done but other people.

*********************
    JIDE Dialogs
*********************

JIDE Dialogs is a product to make your creation of dialogs in Swing easier. As the name
indicates, this product will focus on all kinds of dialogs. It will introduce a simple
yet useful base dialog class - StandardDialog. On top of this class, several pre-built
dialogs are built – such as wizard dialog, multiple-page dialog which can be used display
user options and preference, and tip of today dialog.

**************
   Features
**************
* Provide your end-users powerful docking window functionality within minutes
* Uses the latest in user-interface design concepts, originated in Visual Studio
.NET
* Dock dockable frames on all sides of a central client area in the main window
* Dock several dockable frames in one area, displayed with tabs
* Place dockable frames in auto-hide mode for optimization of screen real estate
* Float dockable frames above the main window
* Dock dockable frames in any combination within floating frames
* Full support of a complex hierarchy of dockable frames in any area
* Show and hide dockable frames
* Load and save dockable window layout data directly to data files, or persist
layout data in some other media, such as a database
* Instantly switch between window layouts - useful for IDEs when state switching
occurs between Code and Runtime modes
* Provide drag-n-drop tabbed document interface as you see in Visual Studio .NET
etc.
* Provide full customizable status bar that you can use to display status,
progress, time and other information at the bottom of your application.
* Extended version TabbedPane and SplitPane. We used these two components
extensively in our other products.
* FloorTabbedPane, also named as Outlook Bar.
* CollapsiblePane, also named as Task Bar which you can find in Windows XP.
* PropertyTable includes PropertyGrid
* ColorComboBox and ColorChooserPanel
* DateComboBox and DateChooserPanel
* SortableTable
* Wizard component which support both Microsoft Wizard 97 standard and Java L&F standard
* MultiplePageDialog which can be used as options dialog or user preference dialog.
* Tips of the Day Dialog
* ButtonPanel which can layout buttons in different order based OS convention.
* Pre-built panels such as BannerPanel
* AbstractPage - lazy loading panel with page event (open, closing, closed etc) support
* Additional LayoutManager classes that enrich Swing's LayoutManager family.
* IconsFactory
* Useful utility classes ...

************************
   Directory Structure
************************
Readme.txt		        this file
doc\			        documents directory
    *.pdf               developer guides
javadoc\		        javadoc of all public classes
lib\                    jar files directory
    jide-common.jar     the common jar used by all JIDE products
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

JIDE Software, Inc. is a leading-edge provider of Swing components for Java developers.
JIDE means Java IDE (Integrated Development Environment). As the name indicated, JIDE
Software's development components focus on IDE or IDE-like applications for software
developers. All our products are in pure Java and Swing, to allow the most compatibilities
with industrial standards.

For further information, please contact support@jidesoft.com or visit our website
at http://www.jidesoft.com
