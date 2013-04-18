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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;
import com.pfizer.rtc.util.FileUtil;

public class Preferences {

    protected static Preferences instance;
    protected static File propFile;
    protected Properties properties;

    protected Preferences(Properties properties) {
        this.properties = properties;
    }

    public static Preferences getInstance() {
        if (instance == null) {
            Properties props = new Properties();
            try {

                File file = getPreferencesFile();
                if (file.exists()) {
                    FileInputStream fis = new FileInputStream(file);
                    props.load(fis);
                    fis.close();
                }
                instance = new Preferences(props);
            } catch (Exception e) {
                e.printStackTrace();
                instance = new Preferences(props);
            }
        }

        return instance;
    }

    public static File getPreferencesFile() {
        if (propFile == null) {
            String userHome = System.getProperty("user.home");
            propFile = new File(userHome, PreferenceFileName);
        }

        return propFile;
    }

    public void purgeProferenceFile(File f) {
        f.delete();
    }

    public String getProperty(String name) {
        return properties.getProperty(name);
    }

    public void setProperty(String name, String value) {
        properties.setProperty(name, value);
    }

    public void save() {
        try {
            purgeProferenceFile(getPreferencesFile());
            FileOutputStream fos = new FileOutputStream(getPreferencesFile());
            properties.store(fos, PreferencesHeader);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // application specific stuff constants...
    protected static String PreferenceFileName = ".pfred";  // props file
    protected static String PreferencesHeader = "PFRED";    // header in props file
    protected static int MaxMRUFiles = 10;               // # of most recently used files saved...

    // application specific methods...
    public String getFileToOpen() {
        String prop = getProperty("pgvlexport");
        return prop;
    }

    public void removeFileToOpen() {
        removeProperty("pgvlexport");
    }

    // get most recently used files
    public String[] getMRUFiles() {
        String prop = getProperty("pfred.mruFiles");
        //prop.replaceAll("\\:", ":");
        //perl.substitute("s/\\\\/\\//g", prop);
        prop = FileUtil.backSlash2forwardSlash(prop);

        if (prop == null || prop.length() < 1) {
            return new String[0];
        }

        "blah".split("bloo");
        StringTokenizer st = new StringTokenizer(prop, "|");
        String[] ret = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++) {
            ret[i] = st.nextToken();
        }
        return ret;
    }

    // add a file to the most recently used files list...
    public void addMRUFile(String fileName) {
        if (fileName == null) {
            return;
        }

        fileName = FileUtil.backSlash2forwardSlash(fileName);

        String[] mru = getMRUFiles();

        // check to see if this already contains this filename
        int skip;
        for (skip = 0; skip < mru.length; skip++) {
            if (mru[skip].equals(fileName)) {
                break;
            }
        }
        int length = mru.length;
        if (skip == mru.length) {
            // if the array is too large then skip the last...
            if (mru.length >= MaxMRUFiles) {
                length = mru.length - 1;
            }
        }

        // ok now make a string starting with THIS filename
        // and then containing all the old filenames...skipping
        // over the LAST (if list too long) or over our OWN
        // filename (if its in the list).
        StringBuffer sb = new StringBuffer();
        sb.append(fileName);
        sb.append("|");
        for (int i = 0; i < length; i++) {
            if (i != skip) {
                sb.append(mru[i]);
                if (i != length - 1) {
                    sb.append("|");
                }
            }
        }
        String value = sb.toString();

        setProperty("pfred.mruFiles", value);

        // we save right here so the effect is immediate...
        // but this still does not really fix issues with
        // multiple instances...
        // IN FACT: this code doesn't play well with others...
        // the correct code would first read in the mru files list
        // from the disk before adding incase another instance
        // has opened a file and changed the list since this
        // instance last read it...
        save();
    }

    public void removeProperty(String name) {
        properties.remove(name);
    }


    public boolean getShowToolbar() {
        String s = properties.getProperty("pcat.showToolbar");
        if (s == null) {
            return true;
        }
        s = s.trim();
        if (s.equals("") || s.equals("true")) {
            return true;
        }
        return false;
    }

    public void setShowToolbar(boolean showToolbar) {
        properties.setProperty("pcat.showToolbar", showToolbar ? "true" : "false");
        save();
    }
}
