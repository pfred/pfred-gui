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
import java.io.File;

public class LoadPFREDFileRunner
        implements Runnable {

    PFREDContext context;
    String filename;
    String templateFilename;
    String outMolFilename;

    public LoadPFREDFileRunner(PFREDContext context, String filename) {
        this(context, filename, null, null);
    }

    public LoadPFREDFileRunner(PFREDContext context, String filename, String templateFilename, String outMolFilename) {
        this.context = context;
        this.filename = filename;
        this.templateFilename = templateFilename;
        this.outMolFilename = outMolFilename;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used to
     * create a thread, starting the thread causes the object's <code>run</code>
     * method to be called in that separately executing thread.
     *
     * @todo Implement this java.lang.Runnable method
     */
    public void run() {
        try {
            FileActionHandler fah = context.getUIManager().getFileActionHandler();

            fah.loadFile(new File(filename));
            //this part is not yet implemented in PFRED
            //if (templateFilename != null && outMolFilename != null)
            //  fah.applyTemplateWriteOutputAndQuit(templateFilename,outMolFilename);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
