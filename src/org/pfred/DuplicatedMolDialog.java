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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import com.borland.jbcl.layout.XYConstraints;
import com.borland.jbcl.layout.XYLayout;

public class DuplicatedMolDialog extends JDialog implements ActionListener {

    JLabel msg = new JLabel();
    XYLayout xYLayout1 = new XYLayout();
    JButton skip = new JButton();
    JButton skipAll = new JButton();
    JButton keep = new JButton();
    JButton keepAll = new JButton();
    JButton giveNewName = new JButton();
    JButton giveNewNameAll = new JButton();
    public final static String SKIP = "Skip";
    public final static String ALWAYS_SKIP = "Always Skip";
    public final static String OVERRIDE = "Overwrite";
    public final static String ALWAYS_OVERRIDE = "Always Overwrite";
    public final static String NEW_NAME = "Give a new name";
    public final static String ALWAYS_NEW_NAME = "Always give a new name";
    private static String action = SKIP;
    private String molname = "";
    private boolean calledFromLoad;

    private DuplicatedMolDialog(Frame owner, String molname, boolean calledFromLoad) {
        super(owner, calledFromLoad ? "Loading Molecules" : "Appending Molecules", true);
        this.molname = molname;
        this.calledFromLoad = calledFromLoad;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getActionCommand() != null && evt.getActionCommand().length() != 0) {
            action = evt.getActionCommand();
            dispose();
        }
    }

    public static String showDialog(Frame frame, String molname, boolean calledFromLoad) {
        DuplicatedMolDialog dialog = new DuplicatedMolDialog(frame, molname, calledFromLoad);
        dialog.setLocation(200, 200);
        dialog.show();
        return action;
    }

    private void jbInit() throws Exception {

        this.getContentPane().setLayout(xYLayout1);
        skip.setFont(new java.awt.Font("SansSerif", 0, 11));
        skip.setActionCommand(SKIP);
        skip.setSelected(true);
        skip.setText("Skip");
        skip.addActionListener(this);

        skipAll.setFont(new java.awt.Font("SansSerif", 0, 11));
        skipAll.setActionCommand(ALWAYS_SKIP);
        skipAll.setText("Always Skip");
        skipAll.addActionListener(this);

        keep.setText("Overwrite");
        keep.setFont(new java.awt.Font("SansSerif", 0, 11));
        keep.setActionCommand(OVERRIDE);
        keep.addActionListener(this);

        keepAll.setText("Always Overwrite");
        keepAll.setFont(new java.awt.Font("SansSerif", 0, 11));
        keepAll.setActionCommand("Always Overwrite");
        keepAll.addActionListener(this);

        giveNewName.setText("Give a new name");
        giveNewName.setFont(new java.awt.Font("SansSerif", 0, 11));
        giveNewName.setActionCommand(NEW_NAME);
        giveNewName.addActionListener(this);

        giveNewNameAll.setText("Always give a new name");
        giveNewNameAll.setFont(new java.awt.Font("SansSerif", 0, 11));
        giveNewNameAll.setActionCommand(ALWAYS_NEW_NAME);
        giveNewNameAll.addActionListener(this);

        msg.setFont(new java.awt.Font("Dialog", 1, 11));
        msg.setToolTipText("");
        msg.setIcon(null);
        msg.setIconTextGap(4);
        msg.setText("Duplicated molecule " + molname + " found during " +
                (calledFromLoad ? "loading" : "appending") + ". Please choose an action.");

        xYLayout1.setWidth(600);
        xYLayout1.setHeight(96);
        this.setResizable(false);

        this.getContentPane().add(keepAll, new XYConstraints(277, 39, -1, 23));
        this.getContentPane().add(skip, new XYConstraints(10, 39, 63, 23));
        this.getContentPane().add(skipAll, new XYConstraints(82, 39, 101, 23));
        this.getContentPane().add(keep, new XYConstraints(191, 39, -1, 23));
        this.getContentPane().add(giveNewNameAll, new XYConstraints(190, 65, 170, 23));
        this.getContentPane().add(giveNewName, new XYConstraints(53, 65, 130, 23));
        this.getContentPane().add(msg, new XYConstraints(13, 8, 399, 23));
        pack();
    }
}
