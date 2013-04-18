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

import javax.swing.JList;
import javax.swing.JTabbedPane;
import javax.swing.JTable;


import org.pfred.pme.PMEAdaptor;
import org.pfred.table.CustomTablePanel;

public class PFREDUIManager {

    PFREDContext context;

    public PFREDUIManager(PFREDContext context) {
        this.context = context;
    }
    private JList clusterList, groupList, treeList;

    private JTabbedPane mainTabbedPane;
    private PFREDView pfred_frame;
    private PropertyActionHandler propertyActionHandler;
    private FileActionHandler fileActionHandler;
    private ViewActionHandler viewActionHandler;

    private GroupActionHandler groupActionHandler;
    private SelectionActionHandler selectionActionHandler;
    private OligoActionHandler oligoActionHandler;
    private ConfigurationActionHandler configurationActionHandler;

    private RNAActionHandler rnaActionHandler;
    private MouseAdaptor mouseAdaptor;

    private JTable cmpd_table;
    private CustomTablePanel cmpd_table_pane;
    private TargetSequencePanel target_seq_panel;
    private PMEAdaptor pmeAdaptor;

    public static String version = "20Feb2008_Release_0.1 development";

    public String getVersion() {
        return version;
    }

    public void setTreeList(JList list) {
        treeList = list;
    }

    public void setGroupList(JList list) {
        groupList = list;
    }

    public JList getClusterList() {
        return clusterList;
    }

    public JList getTreeList() {
        return treeList;
    }

    public JList getGroupList() {
        return groupList;
    }

    public void setPMEPane() {
    }

    public void setTargetSequencePanel(TargetSequencePanel panel) {
        target_seq_panel = panel;
    }

    public TargetSequencePanel getTargetSequencePanel() {
        return target_seq_panel;
    }

    public JTabbedPane getMainTabbedPane() {
        return mainTabbedPane;
    }

    public void setMainTabbedPane(JTabbedPane pane) {
        mainTabbedPane = pane;
    }

    public JTable getOligoTable() {
        return cmpd_table;
    }

    public void setOligoTable(JTable table) {
        cmpd_table = table;
    }

    public CustomTablePanel getOligoTablePane() {
        return cmpd_table_pane;
    }

    public void setOligoTablePane(CustomTablePanel pane) {
        cmpd_table_pane = pane;
    }

    public void setPFREDFrame(PFREDView frame) {
        pfred_frame = frame;
    }

    public PFREDView getPFREDFrame() {
        return pfred_frame;
    }


    public PropertyActionHandler getPropertyActionHandler() {
        if (propertyActionHandler == null) {
            propertyActionHandler = new PropertyActionHandler(context,
                    pfred_frame);
        }
        return propertyActionHandler;
    }

    public FileActionHandler getFileActionHandler() {
        if (fileActionHandler == null) {
            fileActionHandler = new FileActionHandler(context, pfred_frame);
        }
        return fileActionHandler;
    }

    public ViewActionHandler getViewActionHandler() {
        if (viewActionHandler == null) {
            viewActionHandler = new ViewActionHandler(context, pfred_frame);
        }
        return viewActionHandler;
    }

    public OligoActionHandler getOligoActionHandler() {
        if (oligoActionHandler == null) {
            oligoActionHandler = new OligoActionHandler(context, pfred_frame);
        }
        return oligoActionHandler;
    }

    public GroupActionHandler getGroupActionHandler() {
        if (groupActionHandler == null) {
            groupActionHandler = new GroupActionHandler(context, pfred_frame);
        }
        return groupActionHandler;
    }

    public SelectionActionHandler getSelectionActionHandler() {
        if (selectionActionHandler == null) {
            selectionActionHandler = new SelectionActionHandler(context,
                    pfred_frame);
        }
        return selectionActionHandler;
    }
    
    public ConfigurationActionHandler getConfigurationActionHandler() {
        if (configurationActionHandler == null) {
            configurationActionHandler = new ConfigurationActionHandler(context,
                    pfred_frame);
        }
        return configurationActionHandler;
    }

 
    public RNAActionHandler getRNAActionHandler() {
        if (rnaActionHandler == null) {
            rnaActionHandler = new RNAActionHandler(context, pfred_frame);
        }
        return rnaActionHandler;
    }

 
    public MouseAdaptor getMouseAdaptor() {
        if (mouseAdaptor == null) {
            mouseAdaptor = new MouseAdaptor(context);
        }
        return mouseAdaptor;
    }


    // close any manager or tree windows that might be open
    public void closeExtraWindows() {
        // close any tree windows that are open
    }

    public void clear() {
        closeExtraWindows();
        groupList.setValueIsAdjusting(true);
        groupList.clearSelection();
        groupList.setValueIsAdjusting(false);

    }

   
    public void setPMEAdaptor(PMEAdaptor adaptor) {
        pmeAdaptor = adaptor;
    }

    public PMEAdaptor getPMEAdaptor() {
        return pmeAdaptor;
    }

}
