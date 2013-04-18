/*
 *  PFRED: A computational tool for siRNA and antisense design
 *  Copyright (C) 2011 Pfizer, Inc.
 *
 *  This file is part of the PFRED software.
 *
 *  PFRED is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.pfred;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.PrintJob;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.util.ResourceBundle;
import org.pfred.group.GroupCellRenderer;
import org.pfred.icon.IconLoader;
import org.pfred.pme.PMEAdaptor;
import org.pfred.table.OligoTableMouseAdaptor;
import org.pfred.table.CustomTablePanel;
import org.pfred.pme.MoleculeEditor;
import com.pfizer.rtc.notation.editor.editor.MacroMoleculeViewer;
import org.pfred.axis.client.PFREDAxisClientConfiguration;

/**
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Pfizer Re   Technology Center</p>
 * @author $Author: xih $
 * @version $Revision: 1.5 $   $Date: 2008/07/10 15:08:09 $
 */
public class PFREDView
        extends JFrame
        implements ActionListener, ChangeListener {

    public String frame_name = "";
    private JSplitPane splitPane_1, splitPane_1L;
    private JList groupList;
    private JTabbedPane detailTabbedPane;
    private JTabbedPane tabbedPane;
    private JPanel pmePanel;
    private TargetSequencePanel targetPane;
    private FileActionHandler fileActionHandler;
    private ViewActionHandler viewActionHandler;
    private PropertyActionHandler propertyActionHandler;
    private RNAActionHandler rnaActionHandler;
    private SelectionActionHandler selectionActionHandler;
    private ConfigurationActionHandler configurationActionHandler;
    private Preferences preferences;
    private JMenu mruFilesMenu;
    private JMenu appMenu;
    private JToolBar toolbar;
    private JCheckBoxMenuItem showToolbar;
    private ArrayList cs_menuItems = new ArrayList();
    private PFREDContext context;
    private int splitStat = 0;
    private static MoleculeEditor editor = null;

    public PFREDView(PFREDContext context) {
        frame_name = getFrameName();
        startup(context);
        buildUI();
        init();
        pack();
    }

    private String getFrameName() {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("org.pfred.PFREDApp");
        String title = null;
        title = resourceBundle.getString("Application.name") + " (" + resourceBundle.getString("Application.id") + " v" + resourceBundle.getString("Application.version") + ")";
        return title;
    }
       
    /**
     * create the binding of ctrl-V to the frame
     */
    private void attachKeyStrokeHandler() {
        //  Handle escape key to close the dialog

        KeyStroke ctrlV_KeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_V,
                KeyEvent.CTRL_MASK, false);
        Action ctrlv_Action = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {

                PFREDView.this.fileActionHandler.importPropertiesFromClipboard();
            }
        };

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                ctrlV_KeyStroke, "ctrl-v");
        getRootPane().getActionMap().put("ctrl-v", ctrlv_Action);

    }

    private void startup(PFREDContext context) {

        if (context == null) {
            context = new PFREDContext();
        }
        this.context = context;


        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent wev) {
                //System.err.println("before windowClosing (wev)");
                PFREDView.this.windowClosing(wev);
                //System.err.println("after windowClosing (wev)");
            }
        });

        // we must do this very early in startup so that other modules
        // that persist values with preferences will get valid data
        // during startup below...
        preferences = Preferences.getInstance();

        context.getUIManager().setPFREDFrame(this);
        fileActionHandler = context.getUIManager().getFileActionHandler();
        viewActionHandler = context.getUIManager().getViewActionHandler();
        propertyActionHandler = context.getUIManager().getPropertyActionHandler();

        rnaActionHandler = context.getUIManager().getRNAActionHandler();
        selectionActionHandler = context.getUIManager().getSelectionActionHandler();
        
        configurationActionHandler = context.getUIManager().getConfigurationActionHandler();

        //keyStroke binding
        attachKeyStrokeHandler();
    }

    public void removeNotify() {
        super.removeNotify();
        preferences.save();
    }

    public void addMRUFile(String fileName) {
        Preferences.getInstance().addMRUFile(fileName);
        rebuildMRUFilesMenu();
    }

    public PFREDView(Component callback_component) {
        this(null, callback_component);
    }

    public PFREDView(PFREDContext context, Component callback_component) {

        startup(context);
        buildUI();
        init();
        setLauncherCallbackGUIComponent(callback_component);
        pack();

    }

    public void setLauncherCallbackGUIComponent(Component callback_component) {
        toolbar.add(callback_component);
    }

    public void windowClosing(WindowEvent wev) {
        // confirm save, if cancel then don't quit
        if (!fileActionHandler.confirmSave(
                "You will lose any unsaved data if you quit.")) {
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            return;
        }

        //remove server run directory
        if (rnaActionHandler.isAdvancedOligoEnumeratorDialogInitialized()) {
            rnaActionHandler.getAdvancedOligoEnumerationDialog().cleanServerRunDir();
        }
        
        // just assume we are talking THIS window unless told otherwise...
        Window w = this;

        if (wev != null) {
            if (wev.getWindow() != null) {
                w = wev.getWindow();
            }
        }
        w.setVisible(false);
        w.dispose();

        System.exit(0);
    }

    public void showToolBar(boolean show) {
        if (!show) {
            getContentPane().remove(toolbar);
        } else {
            getContentPane().add(toolbar, BorderLayout.NORTH);
        }
        Preferences.getInstance().setShowToolbar(show);
        getContentPane().validate();
    }

    public void buildUI() {
        setTitle(frame_name + ": " + "Untitled");
        this.setIconImage(IconLoader.getIcon("PFRED").getImage());

        this.addMouseListener(context.getUIManager().getMouseAdaptor());

        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        BorderLayout content_layout = new BorderLayout();
        getContentPane().setLayout(content_layout);

        //*******layout the menus
        JMenuBar menubar = new JMenuBar();
        setJMenuBar(menubar);
        buildMenu(menubar);

        //*******layout the toolbars
        toolbar = new JToolBar();
        toolbar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        toolbar.setRollover(true);
        toolbar.setFloatable(true);

        boolean showFlag = Preferences.getInstance().getShowToolbar();
        if (showFlag) {
            getContentPane().add(toolbar, BorderLayout.NORTH);
        }
        buildToolBar(toolbar);

        //*********layout the panels
        splitPane_1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane_1.setOneTouchExpandable(true);
        splitPane_1L = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane_1L.setOneTouchExpandable(true);


        getContentPane().add(splitPane_1, BorderLayout.CENTER);

        splitPane_1.setDividerLocation(0.85);
        splitPane_1L.setDividerLocation(0.8);


        tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(this);

        tabbedPane.setName("mainTabbedPane");
        context.getUIManager().setMainTabbedPane(tabbedPane);
        tabbedPane.addMouseListener(context.getUIManager().getMouseAdaptor());

        //add cmpd table
        CustomTablePanel oligo_table_pane = new CustomTablePanel(context, this,
                context.getDataStore().getOligoTableModel(),
                context.getDataStore().getOligoListSelectionModel());
        oligo_table_pane.setStatusLabelPrefix("oligo: ");
        oligo_table_pane.changeRowHeight(50);

        JMenuItem customItem = new JMenuItem("Replace...");
        customItem.setName("replacePropValue");
        customItem.addActionListener(propertyActionHandler);
        oligo_table_pane.addCustomHeaderPopupMenuItems(customItem);
        oligo_table_pane.addHeaderPopupMenuSeparator();
        customItem = new JMenuItem("Set as Comopound Id");
        customItem.setName("setPropAsCompoundId");
        customItem.addActionListener(propertyActionHandler);
        oligo_table_pane.addCustomHeaderPopupMenuItems(customItem);

        customItem = new JMenuItem("Split Column...");
        customItem.setName("columnSplit");
        customItem.addActionListener(propertyActionHandler);
        oligo_table_pane.addCustomHeaderPopupMenuItems(customItem);

        tabbedPane.add("Oligo Table", oligo_table_pane);
        oligo_table_pane.addMouseListener(new OligoTableMouseAdaptor(context, oligo_table_pane));

        context.getUIManager().setOligoTable(oligo_table_pane.getTable());
        context.getUIManager().setOligoTablePane(oligo_table_pane);

        groupList = new JList(context.getDataStore().getGroupListModel());
        groupList.setCellRenderer(new GroupCellRenderer());
        groupList.setName("groupList");
        context.getUIManager().setGroupList(groupList);
        groupList.addMouseListener(context.getUIManager().getMouseAdaptor());
        ToolTipManager.sharedInstance().registerComponent(groupList);
        groupList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane groupScroll = new JScrollPane(groupList);
        groupScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        groupScroll.setMinimumSize(new Dimension(50, 50));
        groupScroll.setPreferredSize(new Dimension(50, 50));
        JPanel groupPanel = new JPanel(new BorderLayout());
        JPanel groupLabelPanel = new JPanel();
        JLabel groupLabel = new JLabel("  Groups  ");
        groupLabel.setName("groupListLabel");
        groupLabel.addMouseListener(context.getUIManager().getMouseAdaptor());
        groupLabelPanel.add(groupLabel);

        groupPanel.add(groupLabelPanel, BorderLayout.NORTH);
        groupPanel.add(groupScroll, BorderLayout.CENTER);


        pmePanel = new JPanel(); //PME panel to view detail oligo structure
        pmePanel.setLayout(new BorderLayout());
        MacroMoleculeViewer mmv = new MacroMoleculeViewer(false);

        PMEAdaptor pmeAdaptor = new PMEAdaptor(context, mmv); //register with list Listener;
        context.getUIManager().setPMEAdaptor(pmeAdaptor);

        pmePanel.add(mmv, BorderLayout.CENTER);
        pmePanel.setPreferredSize(new Dimension(140, 130));


        targetPane = new TargetSequencePanel(context.getDataStore().getTargetListModel());

        detailTabbedPane = new JTabbedPane();

        detailTabbedPane.addTab("Oligo Detail", pmePanel);
        detailTabbedPane.addTab("Target Transcript", targetPane);


        splitPane_1L.setBottomComponent(detailTabbedPane);
        splitPane_1L.setTopComponent(tabbedPane);
        splitPane_1.setLeftComponent(splitPane_1L);
        splitPane_1.setRightComponent(groupPanel); //for 1R we already set the divider location

        splitPane_1.setResizeWeight(0.9);
        splitPane_1L.setResizeWeight(0.8);
    }

    private void buildToolBar(JToolBar toolbar) {

        JButton butt = IconLoader.getButton("Open");
        butt.setName("open");
        butt.setToolTipText("Open PFRED Files");
        butt.addActionListener(fileActionHandler);
        toolbar.add(butt);

        butt = IconLoader.getButton("OpenCSV");
        butt.setName("openCSV");
        butt.setToolTipText("Open csv and tab delimited files");
        butt.addActionListener(fileActionHandler);
        toolbar.add(butt);

        butt = IconLoader.getButton("Save");
        butt.setName("save");
        butt.setToolTipText("Save current file");
        butt.addActionListener(fileActionHandler);
        toolbar.add(butt);


        JToolBar.Separator js = new JToolBar.Separator();
        js.setOrientation(JSeparator.VERTICAL);
        js.setBorder(BorderFactory.createEtchedBorder());
        js.setSeparatorSize(new Dimension(2, 32));
        js.setPreferredSize(new Dimension(2, 32));
        toolbar.add(js);


        URL url = null;
        toolbar.setFloatable(true);

        butt = null;
        butt = IconLoader.getButton("ID2oligo");

        butt.setName("runAdvancedOligoEnumerator");
        butt.setToolTipText("Retrieve target seq, Enumerate oligo & Annotate ");
        butt.addActionListener(rnaActionHandler);
        toolbar.add(butt);

        butt = IconLoader.getButton("Seq2oligo");
        butt.setName("openSeq");
        butt.setToolTipText("Enumerate Oligo from Target Sequence");
        butt.addActionListener(context.getUIManager().getFileActionHandler());
        toolbar.add(butt);

        js = new JToolBar.Separator();
        js.setOrientation(JSeparator.VERTICAL);
        js.setBorder(BorderFactory.createEtchedBorder());
        js.setSeparatorSize(new Dimension(2, 32));
        js.setPreferredSize(new Dimension(2, 32));
        toolbar.add(js);

        JToolBar.Separator js2 = new JToolBar.Separator();
        js2.setOrientation(JSeparator.VERTICAL);
        js2.setBorder(BorderFactory.createEtchedBorder());
        js2.setSeparatorSize(new Dimension(2, 32));
        js2.setPreferredSize(new Dimension(2, 32));

        toolbar.add(js2);

        butt = IconLoader.getButton("Screenshot");
        butt.setName("screenshot");
        butt.addActionListener(viewActionHandler);
        toolbar.add(butt);

        butt = IconLoader.getButton("Formula");
        butt.setName("newPropertyFromExp");
        butt.addActionListener(propertyActionHandler);
        toolbar.add(butt);

        butt = IconLoader.getButton("Search");
        butt.setName("search");
        butt.setToolTipText("Property search");
        butt.addActionListener(selectionActionHandler);
        toolbar.add(butt);

        butt = IconLoader.getButton("PViz");
        butt.setName("launchPViz");
        butt.setToolTipText("View data in PViz");
        butt.addActionListener(this);
        butt.setVisible(false);
        toolbar.add(butt);

        JToolBar.Separator js3 = new JToolBar.Separator();
        js3.setOrientation(JSeparator.VERTICAL);
        js3.setBorder(BorderFactory.createEtchedBorder());
        js3.setSeparatorSize(new Dimension(2, 32));
        js3.setPreferredSize(new Dimension(2, 32));

        toolbar.add(js3);

        butt = IconLoader.getButton("AddMolToGroup");
        butt.setName("addMolToGroup");
        butt.setToolTipText("Add oligo to selected group");
        butt.addActionListener(context.getUIManager().getGroupActionHandler());
        toolbar.add(butt);
    }

    private void rebuildMRUFilesMenu() {
        if (mruFilesMenu == null) {
            mruFilesMenu = new JMenu("Recent Files");
        }

        mruFilesMenu.removeAll();
        String[] mruFiles = preferences.getMRUFiles();
        if (mruFiles.length < 1) {
            mruFilesMenu.setEnabled(false);
        } else {
            mruFilesMenu.setEnabled(true);
            for (int i = 0; i < mruFiles.length; i++) {
                StringBuffer sb = new StringBuffer();
                sb.append(i + 1);
                sb.append(" ");
                sb.append(mruFiles[i]);
                JMenuItem mi = new JMenuItem(sb.toString());
                mi.addActionListener(fileActionHandler);
                mi.setName("open:" + mruFiles[i]);
                mruFilesMenu.add(mi);
            }
        }
    }

    private void buildMenu(JMenuBar menubar) {
        JMenu menu = new JMenu("File");
        menubar.add(menu);
        JMenuItem mi;
        menu.add(mi = new JMenuItem("New..."));
        mi.setName("new");
        mi.addActionListener(fileActionHandler);

        menu.addSeparator();
        menu.add(mi = new JMenuItem("Open PFRED (.fred) File ..."));
        mi.setName("open");
        mi.addActionListener(fileActionHandler);

//        menu.add(mi = new JMenuItem("Open Delimited Text ..."));
//        mi.setName("openCSV");
//        mi.addActionListener(fileActionHandler);

        menu.add(mi = new JMenuItem("Open Delimited Text with Oligo Sequence ..."));
        mi.setName("openOligoCSV");
        mi.addActionListener(fileActionHandler);

        menu.addSeparator();

        menu.add(mi = new JMenuItem("Enumerate Oligo from Target Sequence ...."));
        mi.setName("openSeq");
        mi.addActionListener(fileActionHandler);

        menu.add(mi = new JMenuItem("Enumerate Oligo from Ensembl Gene ID ...."));
        mi.setName("runAdvancedOligoEnumerator");
        mi.addActionListener(rnaActionHandler);

        menu.add(mi = new JMenuItem("Save"));
        mi.setName("save");
        mi.addActionListener(fileActionHandler);
        menu.add(mi = new JMenuItem("Save As..."));
        mi.setName("saveAs");
        mi.addActionListener(fileActionHandler);
        menu.add(mi = new JMenuItem("Save As ID List"));
        mi.setName("export selection");
        mi.addActionListener(selectionActionHandler);

        menu.addSeparator();

        menu.add(mi = new JMenuItem("Import Oligo Properties..."));
        mi.setName("importProperties");
        mi.addActionListener(fileActionHandler);

        JMenu export_menu = new JMenu("Export Properties");
        menu.add(export_menu);

        export_menu.add(mi = new JMenuItem("Oligo Properties..."));
        mi.setName("exportCompoundProperties");
        mi.addActionListener(propertyActionHandler);

        rebuildMRUFilesMenu();
        menu.addSeparator();
        menu.add(mruFilesMenu);
        menu.addSeparator();

        menu.add(mi = new JMenuItem("Exit"));
        mi.setName("exit");
        mi.addActionListener(this);


        // ********************** for select menu ************************
        menu = new JMenu("Select");
        menubar.add(menu);


        menu.add(mi = new JMenuItem("Search..."));
        mi.setName("search");
        mi.addActionListener(selectionActionHandler);


        menu.add(mi = new JMenuItem("Select Oligo IDs from Clipboard"));
        mi.setName("selectFromClipboard");
        mi.addActionListener(selectionActionHandler);


        menu.add(mi = new JMenuItem("Copy Selected Oligo IDs to Clipboard"));
        mi.setName("copyToClipboard");
        mi.addActionListener(selectionActionHandler);

        menu.addSeparator();

        menu.add(mi = new JMenuItem("Select by Group..."));
        mi.setName("selectByGroup");
        mi.addActionListener(selectionActionHandler);
        mi.setVisible(false);

        menu.add(mi = new JMenuItem("Select All"));
        mi.setName("selectAll");
        mi.addActionListener(selectionActionHandler);

        menu.add(mi = new JMenuItem("Inverse Selection"));
        mi.setName("inverseSelection");
        mi.addActionListener(selectionActionHandler);

        // ************************* View menu *********************
        menu = new JMenu("View");
        menubar.add(menu);

        boolean showFlag = Preferences.getInstance().getShowToolbar();
        showToolbar = new JCheckBoxMenuItem("Show Toolbar", showFlag);
        menu.add(showToolbar);
        showToolbar.setName("showToolBar");
        showToolbar.addActionListener(this);

        JMenu propMenu = new JMenu("Display Properties");
        menu.add(propMenu);

        propMenu.add(mi = new JMenuItem("in Oligo Table"));
        mi.setName("cmpdTablePropertyDisplayOption");
        mi.addActionListener(viewActionHandler);


        JMenu sortMenu = new JMenu("Sort ");
        menu.add(sortMenu);
        sortMenu.add(mi = new JMenuItem("by Properties..."));
        mi.setName("sortCompounds");
        mi.addActionListener(viewActionHandler);

        menu.addSeparator();

        menu.add(mi = new JMenuItem("Screen Shot"));
        mi.setName("screenshot");
        mi.addActionListener(viewActionHandler);

        // ********************** for Property menu ************************
        menu = new JMenu("Properties");
        menubar.add(menu);


        menu.addSeparator();

        menu.add(mi = new JMenuItem("Create Empty Property ..."));
        mi.setName("newProperty");
        mi.addActionListener(propertyActionHandler);

        menu.add(mi = new JMenuItem("Delete Property ..."));
        mi.setName("deleteProperties");
        mi.addActionListener(propertyActionHandler);

        menu.add(mi = new JMenuItem("Assign Row Number ..."));
        mi.setName("assignRowNumber");
        mi.addActionListener(propertyActionHandler);


        menu.addSeparator();
        menu.add(mi = new JMenuItem("Apply Formula ..."));
        mi.setName("newPropertyFromExp");
        mi.addActionListener(context.getUIManager().getPropertyActionHandler());

        menu.addSeparator();
        menu.add(mi = new JMenuItem("Apply Sequence Modifications ..."));
        mi.setName("applySequenceModifications");
        mi.addActionListener(context.getUIManager().getPropertyActionHandler());

        // ********************** for analysis menu ************************
        menu = new JMenu("Analysis");
        menubar.add(menu);

        JMenu submenu = new JMenu("Enumerate Oligo");
        menu.add(submenu);

        submenu.add(mi = new JMenuItem("from Ensembl ID"));
        mi.setName("runAdvancedOligoEnumerator");
        mi.addActionListener(rnaActionHandler);

        submenu.add(mi = new JMenuItem("from fasta sequence"));
        mi.setName("openSeq");
        mi.addActionListener(fileActionHandler);

        submenu = new JMenu("Oligo Selector");
        menu.add(submenu);
        submenu.add(mi = new JMenuItem("for siRNA"));
        mi.setName("oligoSelector");
        mi.addActionListener(rnaActionHandler);

        submenu.add(mi = new JMenuItem("for AntiSense"));
        mi.setName("antisenseOligoSelector");
        mi.addActionListener(rnaActionHandler);
        menu.addSeparator();


        submenu = new JMenu("Misc");
        menu.add(submenu);

        submenu.add(mi = new JMenuItem("Subsetting by Leader Algorithm"));
        mi.setName("leaderSubsetting");
        mi.addActionListener(rnaActionHandler);

        submenu.add(mi = new JMenuItem("Create Oligo Activity BarChart..."));
        mi.setName("createOligoBarChart");
        mi.setToolTipText("Bar chart showing oligo structure along with data");
        mi.addActionListener(rnaActionHandler);
        
        // ********************** for Configuration menu ************************
        menu = new JMenu("Config");
        menubar.add(menu);

        menu.add(mi = new JMenuItem("Configure Service..."));
        mi.setName("configureService");
        mi.setToolTipText("Change PFRED service URL");
        mi.addActionListener(configurationActionHandler);
        
    }

    public void init() {
        context.getUIManager().clear();
        context.getDataStore().clear();

        tabbedPane.setSelectedIndex(0);

        setTitle(frame_name + ": " + "Untitled");
        fileActionHandler.m_file = null;

        initPME();
    }

    public int getSelectedTab() {
        return tabbedPane.getSelectedIndex();
    }

    public void setSelectedTab(int index) {
        if (index < 0 || index >= tabbedPane.getComponentCount()) {
            return;
        }

        tabbedPane.setSelectedIndex(index);
    }

    // close any manager or tree windows that might be open
    public void closeExtraWindows() {
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src instanceof Component) {
            String name = ((Component) src).getName();
            if (name.equals("exit")) {
                windowClosing(null);
            } else if (name.equals("showToolBar")) {
                showToolBar(showToolbar.isSelected());
            } else if (name.equals("printAll")) {
                Properties props = new Properties();
                PrintJob prjob = Toolkit.getDefaultToolkit().getPrintJob(this,
                        "table", props);
                if (prjob != null) {
                    Graphics g = prjob.getGraphics();
                    prjob.end();
                }
            }


        }

    }

    public void stateChanged(ChangeEvent evt) {
    }

    public void saveSplitStat() {
        if (splitPane_1.getDividerLocation()
                == splitPane_1.getMinimumDividerLocation()) {
            splitStat = splitStat | 1;
        }

    }

    public void collapseSplitPanes() {
        //save the current split

        //collapse
        if (splitPane_1.getDividerLocation()
                != splitPane_1.getWidth()) {
            splitPane_1.setDividerLocation(1.0);

        }

        //debug
        int loc = splitPane_1.getDividerLocation();
        System.err.println(loc);



    }

    public void uncollapseSplitPanes() {
        splitPane_1.setDividerLocation(splitPane_1.getLastDividerLocation());
    }

    public static void main(String[] args) {

        try {
            com.jgoodies.looks.LookUtils.setLookAndTheme(
                    new com.jgoodies.looks.plastic.Plastic3DLookAndFeel(),
                    new com.jgoodies.looks.plastic.theme.DesertBluer());
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        
        
        int a = 0;
        String molFile = null;
       


        while (args.length > a) {
            if ((args[a].equals("-f") || args[a].equals("-open")) && (args.length > a + 1)) {
                a++;
                molFile = args[a];
            } else {
                System.err.println("Usage:");
            }
            System.err.println("pfred [-f pfredFile]");
            System.err.println("-f    molecular file in .fred format");

            a++;
        }

        PFREDContext context = new PFREDContext();
        final PFREDView f = new PFREDView(context);       
        f.setSize(950, 700);
        f.setVisible(true);
        
        //set default service endpoint
        String url = context.getDefaultServiceEndpoint();
        PFREDAxisClientConfiguration.getInstance().setWSEndPoint(url);

        if (molFile != null) {
            LoadPFREDFileRunner runner = new LoadPFREDFileRunner(context, molFile);
            EventQueue.invokeLater(runner);
        }

    }

    public void setSelectedColorScheme(String scheme) {
        if (scheme.equalsIgnoreCase("mono")) {
            ((JRadioButtonMenuItem) cs_menuItems.get(0)).setSelected(true);
        } else if (scheme.equalsIgnoreCase("cpk")) {
            ((JRadioButtonMenuItem) cs_menuItems.get(1)).setSelected(true);
        } else if (scheme.equalsIgnoreCase("atomset")) {
            ((JRadioButtonMenuItem) cs_menuItems.get(2)).setSelected(true);
        }
    }

    public JMenu getAppMenu() {
        return appMenu;
    }

    protected void debugSplitPane(JSplitPane pane, String name) {
        System.out.println("SplitPane: " + name
                + " min: " + pane.getMinimumDividerLocation()
                + " max: " + pane.getMaximumDividerLocation()
                + " loc: " + pane.getDividerLocation());
    }

    public String splitPaneOptionsToString() {
        int loc_1 = splitPane_1.getDividerLocation();

        StringBuffer sb = new StringBuffer();
        sb.append(loc_1);

        return sb.toString();
    }

    public void splitPaneOptionsFromString(String optString) {
        int loc_1 = -1;

        try {
            StringTokenizer st = new StringTokenizer(optString, " ");
            if (st.hasMoreTokens()) {
                loc_1 = Integer.parseInt(st.nextToken());
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if (loc_1 >= 0) {
            splitPane_1.setDividerLocation(loc_1);
        }

    }

    private void initPME() {
        try {
            editor = new MoleculeEditor();
        } catch (Exception ex) {
            editor = null;
        }
    }

    public static MoleculeEditor invokePME(MacroMoleculeViewer viewer, Object propertyDisplayTarget) {

        if (null != editor) {

            if (!editor.isVisible()) {
                editor.setVisible(true);
            }
            editor.setViewer(viewer);
            editor.synchronizeZoom();
            editor.setNotation(viewer.getNotation());
            editor.setPropertyDisplayTarget(propertyDisplayTarget);
            editor.requestFocus();
        }

        return editor;
    }
}
