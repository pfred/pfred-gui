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

public class PFREDConstant {

    public static final String GROUP_PROPERTY = "PFRED_GROUP";
    public static final String PFRED_ANNOTATION = "PFRED_ANNOTATION";
    public static final String PROP_DISTRIBUTION = "DIST_";
    public static final String PFRED_HEADER_PROPERTY = "_PFRED_HEADER_";
    public static final String OLIGO_TABLE_DISP_OPT = "oligo_table_display_opts";
    public static final String REP_PREFIX = "Rep_";
    public static final String OLIGO_TABLE_OPTS = "oligo_table_opts"; // table options NOT associated with a column (for that use cmpd_table_disp_opts)
    public static final String PFRED_COLOR_SCHEME = "PFRED_COLOR_SCHEME";
    public static final String PFRED_ORIGINAL_NAME = "ORIGINAL_INPUT_NAME";
    public static int KEEP_ORIGINAL = 0;
    public static int REPLACE = 1;
    public static int MERGE = 2;
    public static final String OR = "OR";
    public static final String AND = "AND";
    public static final String BUT_NOT = "BUT NOT";
    public static final String ALL_COMPOUNDS = "All Compounds";
    public static final String SELECTED_COMPOUNDS = "Selected Compounds";
    public static final String ALL_CLUSTERS = "All Clusters";
    public static final String SELECTED_CLUSTERS = "Selected Clusters";
    public static final String INCREASING = "INCREASING";
    public static final String DECREASING = "DECREASING";
    public static final String PFRED_CONCATENATE_SEPARATOR = "; ";
    public final static String GROUP_CONFIG = "GROUP_CONFIG";
    public final static String ENSEMBL_ID_PREFIX = "ENS";
    public final static String DEFAULT_CONFIG_FILENAME = "pfred.config";
    public final static String DEFAULT_OLIGO_FILENAME = "oligo.txt";
    public final static String DEFAULT_TARGET_FILENAME = "target.txt";
    public final static String META_FILENAME = "content.txt";
    public final static String CONFIG_FILE = "CONFIG_FILE";
    public final static String OLIGO_FILE = "OLIGO_FILE";
    public final static String TARGET_FILE = "TARGET_FILE";
    public final static String PUBLIC_ENSEMBL_TRANSCRIPT_URL = "http://www.ensembl.org/Homo_sapiens/transview?transcript=";
    public final static String PUBLIC_ENSEMBL_URL = "http://www.ensembl.org/index.html";
}
