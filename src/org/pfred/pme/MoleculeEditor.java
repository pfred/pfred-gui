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


package org.pfred.pme;

import java.awt.BorderLayout;
import java.io.IOException;

import org.jdom.JDOMException;

import com.pfizer.rtc.notation.MonomerException;
import com.pfizer.rtc.notation.NotationException;
import com.pfizer.rtc.notation.editor.editor.MacroMoleculeViewer;
import com.pfizer.rtc.notation.editor.editor.MacromoleculeEditor;
import com.pfizer.rtc.notation.editor.utility.IconGenerator;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JButton;
import javax.swing.JPanel;

public class MoleculeEditor extends javax.swing.JFrame {
    

    /** Creates new MoleculeEditor 
     * @throws JDOMException 
     * @throws IOException 
     * @throws MonomerException 
     * @throws NotationException */
    public MoleculeEditor() throws MonomerException, IOException, JDOMException, NotationException {
        customInit();
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Custom Initialization
     * - Creates new PME and adds a Close button on the frame
     * @throws com.pfizer.rtc.notation.MonomerException
     * @throws java.io.IOException
     * @throws org.jdom.JDOMException
     */
    private void customInit() throws MonomerException, IOException, JDOMException, NotationException {
        editor = new MacromoleculeEditor();        
//        viewer = new MacroMoleculeViewer(true);
        initComponents();
        setTitle("Pfizer Macromolecule Editor - Plugin");
        setPMEIcon();
        add(BorderLayout.NORTH, editor.getMenuBar());
        add(BorderLayout.CENTER, editor.getContentComponent());
        editor.getContentComponent().repaint();        
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {                
                formWindowClosing(null);
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
              
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);      

        add(BorderLayout.SOUTH, buttonPanel);
        pack();
        setLocationRelativeTo(null);
    }
    
    /**
     * Before the window closes, set the notation in the main frame
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if (null != viewer) {
            String oldNotation = viewer.getNotation();
            String newNotation = editor.getNotation();
            viewer.setNotation(newNotation);
            pcs.firePropertyChange(NOTATION_PROPERTY, oldNotation, newNotation);
        }

        this.setVisible(false);
    }//GEN-LAST:event_formWindowClosing
   
    
    public String getNotation() {
        return editor.getNotation();
    }

    
    public void setNotation(String notation) {
        editor.setNotation(notation);
    }

    public void synchronizeZoom() {
        editor.synchronizeZoom();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    
    public MacroMoleculeViewer getViewer() {
        return viewer;
    }

    
    public void setViewer(MacroMoleculeViewer viewer) {
        this.viewer = viewer;
    }
    
    public void setPMEIcon() {
        setIconImage(IconGenerator.getImage(IconGenerator.PME_APP_ICON_RESOURCE_URL));
    }

       /**
     * @return the propertyDisplayTarget
     */
    public Object getPropertyDisplayTarget() {
        return propertyDisplayTarget;
    }

    /**
     * @param propertyDisplayTarget the propertyDisplayTarget to set
     */
    public void setPropertyDisplayTarget(Object propertyDisplayTarget) {
        this.propertyDisplayTarget = propertyDisplayTarget;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    // ---- PME Related ----
    private MacromoleculeEditor editor;
    private MacroMoleculeViewer viewer;
    
    public static final String NOTATION_PROPERTY = "notation";
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private Object propertyDisplayTarget;

 
}
