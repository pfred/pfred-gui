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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import javax.swing.JOptionPane;
import org.pfred.rest.RestServiceClient;


public class ConfigurationActionHandler implements ActionListener {

    PFREDContext context;
    JFrame parent;

    public ConfigurationActionHandler(PFREDContext context, JFrame parent) {
        this.context = context;
        this.parent = parent;
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src instanceof Component) {
            String name = ((Component) src).getName();

            if (name.equals("configureService")) {
                String current = RestServiceClient.getEndPoint();
                if (null == current || current.length() ==0 ){
                    current = context.getDefaultServiceEndpoint();
                }
                String result = JOptionPane.showInputDialog(parent, "***  Please enter the full URL to PFRED service in the text box below  ***", current);
                if (null != result && result.length() >0 && result.startsWith("http")) {
                    RestServiceClient.setEndPoint(result);
                }
            }
        }
    }
}
