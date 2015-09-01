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
package org.pfred.enumerator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.Popup;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.pfred.PFREDContext;
import org.pfred.RNAActionHandler;
import org.pfred.group.GroupInfo;
import org.pfred.group.NewGroupDialog;
import org.pfred.group.OligoSelectorGroupInfo;
import org.pfred.model.CustomListSelectionModel;
import org.pfred.model.Oligo;
import com.pfizer.rtc.util.SpringUtilities;

public class OligoSelector extends JDialog implements ActionListener, ComponentListener, ChangeListener {

    Frame owner;
    JCheckBox jcb_avoid5UTR = new JCheckBox();
    JCheckBox jcb_avoid3UTR = new JCheckBox();
    JCheckBox jcb_efficacy = new JCheckBox();
    JCheckBox jcb_polyUAGC = new JCheckBox();
    JCheckBox jcb_polyA = new JCheckBox();
    JCheckBox jcb_reynoldRules = new JCheckBox();
    JTextField jtf_topN = new JTextField("5", 10);
    JTextField jtf_minimalDistance = new JTextField("19", 10);
    JComboBox jcb_offTargetNumMismatches = new JComboBox(new String[]{"0", "1", "2", "3"});
    JComboBox jcb_hitSpliceVariantsNumMismatches = new JComboBox(new String[]{"0", "1", "2", "3"});
    JButton bttn_OK = new JButton("Select");
    JButton bttn_Cancel = new JButton("Cancel");
    JButton bttn_CreateGroup = new JButton("Create Group");
    JLabel l_checkSpliceVariants = createLabel("  ", JLabel.TRAILING);
    JLabel l_checkOrthologs = createLabel("  ", JLabel.TRAILING);
    JLabel l_avoid5UTR = createLabel("  ", JLabel.TRAILING);
    JLabel l_avoid3UTR = createLabel("  ", JLabel.TRAILING);
    JLabel l_avoidExonJunction = createLabel("  ", JLabel.TRAILING);
    JLabel l_avoidSNPs = createLabel("  ", JLabel.TRAILING);
    JLabel l_avoidOffTargetHits = createLabel("  ", JLabel.TRAILING);
    JLabel l_efficacy = createLabel(" ", JLabel.TRAILING);
    JLabel l_avoidPolyUAGC = createLabel("  ", JLabel.TRAILING);
    JLabel l_avoidPolyA = createLabel("  ", JLabel.TRAILING);
    JLabel l_reynoldRules = createLabel(" ", JLabel.TRAILING);
    JPanel filterPane = new JPanel();
    JButton bttn_customizeOrthologs;
    JSpinner js_efficacy = null;
    JSpinner js_reynoldRules = null;
    ArrayList species_checkboxes = new ArrayList();
    ArrayList species_mismatches_comboboxes = new ArrayList();
    ImageIcon up_arrow = null;
    ImageIcon down_arrow = null;
    int finalCount = 0;
    boolean isCanceled = true;
    String ENST_PREFIX = "ENST";
    String ENS_PREFIX = "ENS";
    String MISMATCHES_SUFFIX = "_Mismatches";
    String ORTHOLOG_PREFIX = "orthologs_";
//    String[] ENSEMBL_SPECIES_PREFIX = new String[]{"ENST", "ENSMUST", "ENSRNOT", "ENSCAFT", "ENSPTRT", "ENSMMUT"};
//    String[] ENSEMBL_SPECIES_NAMES = new String[]{"human", "mouse", "rat", "dog", "chimp", "rhesus macaque"};
    String[] ENSEMBL_SPECIES_PREFIX = new String[]{"ENST", "ENSMUST", "ENSRNOT", "ENSCAFT", "ENSPTRT"};
    String[] ENSEMBL_SPECIES_NAMES = new String[]{"human", "mouse", "rat", "dog", "chimp"};
    JLabel filterCount;
    Color headerColor = BorderFactory.createTitledBorder("").getTitleColor();
    private Font defaultFont = new JLabel().getFont();
    private Font boldFont = new Font(defaultFont.getName(), Font.BOLD, defaultFont.getSize());
    private ArrayList oligos;
    private ArrayList selected;
    JPanel container_pane = new JPanel();
    JPanel popup_pane = new JPanel();
    JPanel selectorPane = new JPanel();
    private PFREDContext context = null;
    private boolean isAntiSenseDesign = false;
    Popup popup = null;
    JWindow window = null;
    OligoSelectorModel oligoSelectorModel;

    public OligoSelector(JFrame owner, PFREDContext context, ArrayList oligos, boolean isAntiSenseDesign) {
        super(owner);
        this.owner = owner;
        this.context = context;
        this.oligos = oligos;
        this.isAntiSenseDesign = isAntiSenseDesign;

        oligoSelectorModel = new OligoSelectorModel(isAntiSenseDesign);

        initialize();
    }

    public OligoSelector(JFrame owner, PFREDContext context, OligoSelectorGroupInfo oligoSelectorGroupInfo) {
        super(owner);
        this.owner = owner;
        this.context = context;

        oligos = context.getDataStore().getOligoListModel().getAllData();
        oligoSelectorModel = oligoSelectorGroupInfo.getOligoSelectorModel();
        isAntiSenseDesign = oligoSelectorModel.isIsAntisense();
        initialize();
    }

    private void initialize() {

        setModal(true);
        setTitle("Oligo selector");
        initGUI();
        pack();

        this.setLocationRelativeTo(owner);
        this.addComponentListener(this);
    }

    private JLabel createHeaderLabel(String text) {
        JLabel l = new JLabel(text, JLabel.LEADING);
        l.setFont(boldFont);
        l.setForeground(headerColor);
        return l;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text, JLabel.LEADING);
        return l;
    }

    private JLabel createLabel(String text, int leading) {
        JLabel l = new JLabel(text, leading);
        return l;
    }

    private void addFilterPanel(JComponent c, int leading) {
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(leading));
        p.add(c);
        filterPane.add(p);
    }

    private String speciesName2speciesPrefix(String name) {
        for (int i = 0; i < ENSEMBL_SPECIES_NAMES.length; i++) {
            if (name.equalsIgnoreCase(ENSEMBL_SPECIES_NAMES[i])) {
                return ENSEMBL_SPECIES_PREFIX[i];
            }
        }
        return null;
    }

    /**
     * get the species name from the first oligo
     * @return
     */
    private ArrayList getAvailableSpeciesNames() {
        ArrayList availableSpecies = new ArrayList();

        if (oligos.size() <= 0) {
            return availableSpecies;
        }

        Oligo o = (Oligo) oligos.get(0);
        ArrayList names = o.propertyNames();
        HashSet uniqueNames = new HashSet();
        int size = names.size();

        for (int i = 0; i < size; i++) {
            String propName = (String) names.get(i);
            for (int j = 1; j < ENSEMBL_SPECIES_PREFIX.length; j++) { //skip human
                if (propName.startsWith(ENSEMBL_SPECIES_PREFIX[j])) {
                    if (!uniqueNames.contains(ENSEMBL_SPECIES_NAMES[j])) {
                        availableSpecies.add(ENSEMBL_SPECIES_NAMES[j]);
                        uniqueNames.add(ENSEMBL_SPECIES_NAMES[j]);
                    }
                    break;
                }
            }
        }
        return availableSpecies;
    }

    private String[] getSelectedSpeciesNames() {
        int size = species_checkboxes.size();
        ArrayList<String> selected = new ArrayList();
        for (int i = 0; i < size; i++) {
            JCheckBox jcb_species = (JCheckBox) species_checkboxes.get(i);

            if (jcb_species.isSelected()) {
                String name = jcb_species.getName();
                name = name.substring(ORTHOLOG_PREFIX.length());
                selected.add(name);
            }
        }
        size = selected.size();
        String[] result = new String[size];
        for (int i = 0; i < size; i++) {
            result[i] = selected.get(i);
        }
        return result;
    }

    private int[] getSelectedSpeciesMismatches() {
        int size = species_checkboxes.size();
        ArrayList<String> selected = new ArrayList();
        for (int i = 0; i < size; i++) {
            JCheckBox jcb_species = (JCheckBox) species_checkboxes.get(i);
            JComboBox cb_species_mismatch = (JComboBox) species_mismatches_comboboxes.get(i);
            if (jcb_species.isSelected()) {
                String value = (String) cb_species_mismatch.getSelectedItem();
                selected.add(value);
            }
        }
        size = selected.size();
        int[] result = new int[size];
        for (int i = 0; i < size; i++) {
            result[i] = Integer.parseInt(selected.get(i));
        }
        return result;
    }

    private void initGUI() {
        this.getContentPane().setLayout(new BorderLayout());
        JPanel mainPane = new JPanel();
        mainPane.setLayout(new BorderLayout());
        this.getContentPane().add(mainPane, BorderLayout.CENTER);

        JPanel bttnPane = new JPanel();
        bttn_CreateGroup.addActionListener(this);
        bttnPane.add(bttn_CreateGroup);
        bttn_OK.addActionListener(this);
        bttnPane.add(bttn_OK);
        bttn_Cancel.addActionListener(this);
        bttnPane.add(bttn_Cancel);

        this.getContentPane().add(bttnPane, BorderLayout.SOUTH);

        JPanel topPane = new JPanel();
        topPane.setBorder(BorderFactory.createTitledBorder("Filters"));
        topPane.setLayout(new BorderLayout());

        JLabel label;
        filterPane.setLayout(new SpringLayout());
        topPane.add(filterPane, BorderLayout.CENTER);

        filterPane.add(new JLabel("  "));
        filterPane.add(createHeaderLabel("Design Criteria"));
        filterPane.add(createHeaderLabel("# Passed Each Criteria"));

        jcb_avoid5UTR.setSelected(oligoSelectorModel.isAvoid5UTR());
        addFilterPanel(jcb_avoid5UTR, FlowLayout.CENTER);
        jcb_avoid5UTR.addActionListener(this);
        label = createLabel("Avoid 5'-UTR of primary target");
        addFilterPanel(label, FlowLayout.LEADING);
        addFilterPanel(l_avoid5UTR, FlowLayout.CENTER);

        jcb_avoid3UTR.setSelected(oligoSelectorModel.isAvoid3UTR());
        addFilterPanel(jcb_avoid3UTR, FlowLayout.CENTER);
        jcb_avoid3UTR.addActionListener(this);
        label = createLabel("Avoid 3'-UTR of primary target");
        addFilterPanel(label, FlowLayout.LEADING);
        addFilterPanel(l_avoid3UTR, FlowLayout.CENTER);

        if (!isAntiSenseDesign) {
            jcb_efficacy.addActionListener(this);
            jcb_efficacy.setSelected(oligoSelectorModel.isEfficacy());
            addFilterPanel(jcb_efficacy, FlowLayout.CENTER);
            JPanel efficacyPane = new JPanel();
            efficacyPane.setLayout(new FlowLayout(FlowLayout.LEADING));
            SpinnerNumberModel spinModel = null;
            double dMin = 0;
            double dMax = 1.4;
            double dDiff = dMax - dMin;
            spinModel = new SpinnerNumberModel(0.70, dMin, dMax, 0.05);
            js_efficacy = new JSpinner(spinModel);
            js_efficacy.setValue(oligoSelectorModel.getEfficacyScore());
            js_efficacy.addChangeListener(this);

            label = createLabel("Predicted efficacy score greater than:");
            efficacyPane.add(label);
            efficacyPane.add(js_efficacy);
            filterPane.add(efficacyPane);
            addFilterPanel(l_efficacy, FlowLayout.CENTER);

            //polyUAGC
            jcb_polyUAGC.setSelected(oligoSelectorModel.isPolyUAGC());
            addFilterPanel(jcb_polyUAGC, FlowLayout.CENTER);
            jcb_polyUAGC.addActionListener(this);
            label = createLabel("Avoid polyA,U,G,C (e.g. UUUUU, AAAAA).");
            addFilterPanel(label, FlowLayout.LEADING);
            addFilterPanel(l_avoidPolyUAGC, FlowLayout.CENTER);

            //efficacy spinner
            jcb_reynoldRules.addActionListener(this);
            jcb_reynoldRules.setSelected(oligoSelectorModel.isReynoldRules());
            addFilterPanel(jcb_reynoldRules, FlowLayout.CENTER);
            JPanel reynoldPane = new JPanel();
            reynoldPane.setLayout(new FlowLayout(FlowLayout.LEADING));
            spinModel = null;

            spinModel = new SpinnerNumberModel(5, 0, 10, 1);
            js_reynoldRules = new JSpinner(spinModel);
            js_reynoldRules.addChangeListener(this);

            label = createLabel("Reynold score greater than:");
            reynoldPane.add(label);
            reynoldPane.add(js_reynoldRules);
            filterPane.add(reynoldPane);
            addFilterPanel(l_reynoldRules, FlowLayout.CENTER);

            SpringUtilities.makeCompactGrid(filterPane, //parent
                    6, 3, //2x2 grid
                    3, 3, //initX, initY
                    3, 0); //xPad, yPad

            ///<--------- siRNA specifici ends
        } else {
            /// AntiSense specific design
            jcb_efficacy.addActionListener(this);
            jcb_efficacy.setSelected(oligoSelectorModel.isEfficacy());
            addFilterPanel(jcb_efficacy, FlowLayout.CENTER);
            JPanel efficacyPane = new JPanel();
            efficacyPane.setLayout(new FlowLayout(FlowLayout.LEADING));
            SpinnerNumberModel spinModel = null;
            double dMin = -20;
            double dMax = 20;
            double dDiff = dMax - dMin;
            spinModel = new SpinnerNumberModel(0, dMin, dMax, 5);
            js_efficacy = new JSpinner(spinModel);
            js_efficacy.setValue(oligoSelectorModel.getEfficacyScore());
            js_efficacy.addChangeListener(this);

            label = createLabel("Predicted antisense efficacy score greater than: ");
            efficacyPane.add(label);
            efficacyPane.add(js_efficacy);
            filterPane.add(efficacyPane);
            addFilterPanel(l_efficacy, FlowLayout.CENTER);

            //polyA
            jcb_polyA.setSelected(oligoSelectorModel.isPolyA());
            addFilterPanel(jcb_polyA, FlowLayout.CENTER);
            jcb_polyA.addActionListener(this);
            label = createLabel("Avoid polyA (e.g. AAAAA or more As): ");
            addFilterPanel(label, FlowLayout.LEADING);
            addFilterPanel(l_avoidPolyA, FlowLayout.CENTER);

            SpringUtilities.makeCompactGrid(filterPane, //parent
                    5, 3, //2x2 grid
                    3, 3, //initX, initY
                    3, 0); //xPad, yPad
        }


        filterCount = new JLabel("<html>Total oligos: <b>xxx</b>        Passed all criteria: <b>xxx</b></html>", JLabel.TRAILING);
        topPane.add(filterCount, BorderLayout.SOUTH);
        mainPane.add(topPane, BorderLayout.CENTER);


        selectorPane.setBorder(BorderFactory.createTitledBorder("TopN Selection"));
        mainPane.add(selectorPane, BorderLayout.SOUTH);

        selectorPane.setLayout(new SpringLayout());

        label = createLabel("Select topN oligos ranked by predicted efficacy:", JLabel.TRAILING);
        selectorPane.add(label);
        jtf_topN.setText("" + oligoSelectorModel.getTopNCount());
        selectorPane.add(jtf_topN);
        if (isAntiSenseDesign) {
            // jtf_minimalDistance.setText("14");
        }

        jtf_minimalDistance.setText("" + oligoSelectorModel.getMinimalDistance());
        label = createLabel("Minimal distance from each other", JLabel.TRAILING);
        selectorPane.add(label);
        selectorPane.add(jtf_minimalDistance);

        SpringUtilities.makeCompactGrid(selectorPane, //parent
                2, 2, //2x2 grid
                3, 3, //initX, initY
                3, 0); //xPad, yPad

        initPopupPane();

        initStat();
    }

    private void initPopupPane() {
        window = new JWindow(this);

        window.getContentPane().add(popup_pane);
        popup_pane.setBorder(BorderFactory.createLineBorder(Color.gray));

        ArrayList<String> availableSpecies = this.getAvailableSpeciesNames();
        int size = availableSpecies.size();
        popup_pane.setLayout(new SpringLayout());
        popup_pane.add(new JLabel(""));
        popup_pane.add(createHeaderLabel("Species to Hit   "));
        popup_pane.add(createHeaderLabel("Mismatch threshold"));

        for (int i = 0; i < size; i++) {
            String species = availableSpecies.get(i);
            String species_prefix = this.speciesName2speciesPrefix(species);
            JCheckBox jcb_orthologs = new JCheckBox();
            jcb_orthologs.setName(ORTHOLOG_PREFIX + species_prefix);
            jcb_orthologs.setSelected(true);
            species_checkboxes.add(jcb_orthologs);

            jcb_orthologs.addActionListener(this);
            popup_pane.add(jcb_orthologs);
            popup_pane.add(new JLabel(species));
            JComboBox cb_ortholog_mismatches = new JComboBox(new String[]{"0", "1", "2", "3"});
            cb_ortholog_mismatches.setName(ORTHOLOG_PREFIX + species_prefix);
            cb_ortholog_mismatches.addActionListener(this);
            species_mismatches_comboboxes.add(cb_ortholog_mismatches);
            popup_pane.add(cb_ortholog_mismatches);
        }

        SpringUtilities.makeCompactGrid(popup_pane, //parent
                size + 1, 3, //2x2 grid
                3, 3, //initX, initY
                3, 0); //xPad, yPad
        window.pack();
    }

    public ArrayList filterByProperty(ArrayList<Oligo> oligos, String propName, double min, double max) {
        int size = oligos.size();
        ArrayList<Oligo> keep = new ArrayList();
        for (int i = 0; i < size; i++) {
            Oligo o = oligos.get(i);
            String val = (String) o.getProperty(propName);
            if (val == null || val.length() == 0) {
                keep.add(o);
                continue;
            }
            try {
                double v = Double.parseDouble(val);
                if (v >= min && v <= max) {
                    keep.add(o);
                }
            } catch (Exception ex) {
            }
        }
        return keep;
    }

    public ArrayList filterBySpliceVariants(ArrayList<Oligo> oligos, int maxOfMismatches) {
        int size = oligos.size();
        ArrayList<Oligo> keep = new ArrayList();

        //sort oligos by start
        for (int i = 0; i < size; i++) {
            Oligo o = oligos.get(i);
            ArrayList<String> propNames = o.propertyNames();
            int count = propNames.size();
            boolean passed = false;
            loop:
            for (int j = 0; j < count; j++) {
                String propName = propNames.get(j);
                if (propName.startsWith(ENST_PREFIX) && propName.endsWith(MISMATCHES_SUFFIX)) {
                    //this is mismatch count of an oligo against human transcript
                    String v = (String) o.getProperty(propName);
                    String[] fields = v.trim().split(" ");
                    passed = false;
                    for (int k = 0; k < fields.length; k++) {
                        int mismatchCount = -1;
                        try {
                            mismatchCount = Integer.parseInt(fields[k].trim());
                        } catch (Exception ex) {
                            ;
                        }
                        if (mismatchCount <= maxOfMismatches) {
                            //on target hit
                            passed = true;
                            break;
                        }
                    }
                    if (!passed) {
                        //found a mismatch
                        break;
                    }

                }
            }
            if (passed) {
                keep.add(o);
            }
        }

        return keep;
    }

    public ArrayList filterByOrthologs(ArrayList<Oligo> oligos, String[] species_prefixes, int[] mismatches) {
        for (int i = 0; i < species_prefixes.length; i++) {
            oligos = filterByOrthologs(oligos, species_prefixes[i], mismatches[i]);
        }
        return oligos;
    }

    public ArrayList filterByOrthologs(ArrayList<Oligo> oligos, String species_prefix, int maxOfMismatches) {
        int size = oligos.size();
        ArrayList<Oligo> keep = new ArrayList();

        //sort oligos by start
        for (int i = 0; i < size; i++) {
            Oligo o = oligos.get(i);
            ArrayList<String> propNames = o.propertyNames();
            int count = propNames.size();
            boolean passed = false;
            loop:
            for (int j = 0; j < count; j++) {
                String propName = propNames.get(j);
                if (propName.startsWith(species_prefix) && propName.endsWith(MISMATCHES_SUFFIX)) {
                    //this is mismatch count of an oligo against human transcript
                    String v = (String) o.getProperty(propName);
                    String[] fields = v.trim().split(" ");
                    passed = false;
                    for (int k = 0; k < fields.length; k++) {
                        int mismatchCount = -1;
                        try {
                            mismatchCount = Integer.parseInt(fields[k].trim());
                        } catch (Exception ex) {
                        }
                        if (mismatchCount <= maxOfMismatches) {
                            //on target hit
                            passed = true;
                            break;
                        }
                    }
                    if (!passed) {
                        //found a mismatch
                        break;
                    }

                }
            }
            if (passed) {
                keep.add(o);
            }
        }

        return keep;
    }

    public ArrayList filterByTranscriptLocation(ArrayList<Oligo> oligos,
            boolean avoid3UTR, boolean avoid5UTR) {
        int size = oligos.size();
        ArrayList<Oligo> keep = new ArrayList();
        //sort oligos by start
        for (int i = 0; i < size; i++) {
            Oligo o = oligos.get(i);

            String loc = (String) o.getProperty("transcriptLocation");
            if (loc == null || loc.length() == 0) {
                keep.add(o);//keep it if no location info
                continue;
            }
            try {
                boolean pass = true;
                if (avoid3UTR && loc.equalsIgnoreCase("3UTR")) {//filter ones hit only 3UTR
                    pass = false;
                }
                if (avoid5UTR && loc.equalsIgnoreCase("5UTR")) {//filter ones hit only 5UTR
                    pass = false;
                }
                if (pass) {
                    keep.add(o);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                keep.add(o);//skip if the value is not numeric

            }
        }
        return keep;
    }

    public ArrayList filterBySNP(ArrayList<Oligo> oligos) {
        int size = oligos.size();
        ArrayList<Oligo> keep = new ArrayList();
        boolean passed = true;
        //sort oligos by start
        for (int i = 0; i < size; i++) {
            Oligo o = oligos.get(i);
            passed = true;
            ArrayList<String> propNames = o.propertyNames();
            int count = propNames.size();
            for (int j = 0; j < count; j++) {
                String propName = propNames.get(j);
                if (propName.startsWith("ENST") && propName.endsWith("_snp")) {
                    String v = (String) o.getProperty(propName);

                    if (v == null || v.length() == 0) {
                        continue;
                    }

                    if (v.trim().length() > 10) {
                        passed = false;
                        break;
                    }

                }
            }
            if (passed) {
                keep.add(o);
            }
        }
        return keep;
    }

    /**
     * this currently check for exon boundaries for all on targets
     * we might need to check it to looking only at the primary targets
     * @param oligos
     * @return
     */
    public ArrayList filterByExonJunction(ArrayList<Oligo> oligos) {
        int size = oligos.size();
        ArrayList<Oligo> keep = new ArrayList();

        //sort oligos by start
        for (int i = 0; i < size; i++) {
            Oligo o = oligos.get(i);
            boolean passed = true;
            ArrayList<String> propNames = o.propertyNames();
            int count = propNames.size();
            for (int j = 0; j < count; j++) {
                String propName = propNames.get(j);
                if (propName.startsWith("ENS") && propName.endsWith("exon_boundary")) {
                    String v = (String) o.getProperty(propName);

                    if (v == null || v.length() == 0) {
                        continue;
                    }
                    try {
                        if (v.trim().length() > 0) {
                            passed = false;
                            break;
                        }
                    } catch (Exception ex) {
                        continue;
                    }
                }
            }
            if (passed) {
                keep.add(o);
            }
        }
        return keep;
    }

    public ArrayList filterByPolyA(ArrayList<Oligo> oligos) {
        int size = oligos.size();
        ArrayList<Oligo> keep = new ArrayList();

        //sort oligos by start
        for (int i = 0; i < size; i++) {
            Oligo o = oligos.get(i);
            boolean passed = true;
            String sense_seq = o.getSingleLetterSeq(Oligo.TYPE_PARENT_SENSE_OLIGO);
            if (sense_seq != null && sense_seq.indexOf("AAAAA") >= 0) {
                passed = false;
            }
            if (passed) {
                keep.add(o);
            }
        }

        return keep;
    }

    public ArrayList filterByPolyUAGC(ArrayList<Oligo> oligos) {
        int size = oligos.size();
        ArrayList<Oligo> keep = new ArrayList();

        //sort oligos by start
        for (int i = 0; i < size; i++) {
            Oligo o = oligos.get(i);
            boolean passed = true;
            ArrayList<String> propNames = o.propertyNames();
            int count = propNames.size();
            for (int j = 0; j < count; j++) {
                String propName = propNames.get(j);
                if (propName.startsWith("UUUUU") || propName.endsWith("AAAAA") || propName.startsWith("GGGGG") || propName.endsWith("CCCCC")) { //we could done this once for all but it is ok it is still fast enough
                    String v = (String) o.getProperty(propName);

                    if (v == null || v.length() == 0) {
                        continue;
                    }
                    try {
                        int matchCount = Integer.parseInt(v.trim());
                        if (matchCount > 0) {
                            passed = false;
                            break;
                        }
                    } catch (Exception ex) {
                        continue;
                    }
                }
            }
            if (passed) {
                keep.add(o);
            }
        }

        return keep;
    }

    public ArrayList pickTopNOligo(ArrayList<Oligo> oligos, int topN, int minimalDistance, String propName) throws Exception {
        RNAActionHandler rah = context.getUIManager().getRNAActionHandler();
        return rah.leaderSubsetting(oligos, propName, false, topN, minimalDistance);
    }

    public ArrayList getSelected() {
        return selected;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public boolean isAvoid3UTR() {
        return jcb_avoid3UTR.isSelected();
    }

    public boolean isAvoid5UTR() {
        return jcb_avoid5UTR.isSelected();
    }
    /*
    public boolean isAvoidExonJunction() {
    return jcb_avoidExonJunction.isSelected();
    }

    public boolean checkSpliceVariants() {
    return jcb_checkSpliceVariants.isSelected();
    }

    public boolean checkOrthologs() {
    return jcb_checkOrthologs.isSelected();
    }

    public boolean checkAvoidOffTargetHits() {
    return jcb_avoidOffTargetHits.isSelected();
    }
     */

    public int getOffTargetNumMismatches() {
        int i = 0;
        try {
            i = Integer.parseInt((String) jcb_offTargetNumMismatches.getSelectedItem());

        } catch (Exception ex) {
            return -1;
        }
        return i;
    }

    public int getHitSpliceVariantsNumMismatches() {
        int i = 0;
        try {
            i = Integer.parseInt((String) jcb_hitSpliceVariantsNumMismatches.getSelectedItem());

        } catch (Exception ex) {
            return -1;
        }
        return i;
    }

    public int getTopN() {
        int i = 0;
        try {
            i = Integer.parseInt(jtf_topN.getText());

        } catch (Exception ex) {
            return 0;
        }
        return i;
    }

    public int getMinimalDistance() {
        int i = 0;
        try {
            i = Integer.parseInt(jtf_minimalDistance.getText());

        } catch (Exception ex) {
            return 0;
        }
        return i;
    }

    public float getEfficacyThreshold() {
        Object v = js_efficacy.getValue();
        if (v.getClass().toString().toUpperCase().contains("DOUBLE")) {
            float cutoff = ((Double) v).floatValue();
            return cutoff;
        }
        float cutoff = ((Float) v).floatValue();
        return cutoff;
    }

    public int getReynoldRulesThreshold() {
        Object v = js_reynoldRules.getValue();
        int cutoff = ((Integer) v).intValue();
        return cutoff;
    }

    /* public boolean isAvoidSNPs() {
    return jcb_avoidSNPs.isSelected();
    }
     */
    public boolean isUseEfficacyCutoff() {
        return jcb_efficacy.isSelected();
    }

    public boolean isPolyUAGC() {
        return jcb_polyUAGC.isSelected();
    }

    public boolean isPolyA() {
        return jcb_polyA.isSelected();
    }

    public boolean isCheckReynoldRules() {
        return jcb_reynoldRules.isSelected();
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        com.jgoodies.looks.LookUtils.setLookAndTheme(
                new com.jgoodies.looks.plastic.Plastic3DLookAndFeel(),
                new com.jgoodies.looks.plastic.theme.DesertBluer());

        OligoSelector d = new OligoSelector(null, null, null, true);
        d.setVisible(true);
    }

    private void makeSelection() {
        CustomListSelectionModel sel_model = context.getDataStore().getOligoListSelectionModel();
        sel_model.setValueIsAdjusting(true);
        sel_model.selectData(selected, false);
        sel_model.setValueIsAdjusting(false);
    }

    public void actionPerformed(ActionEvent evt) {

        Object src = evt.getSource();
        JComponent c = (JComponent) src;
        String name = c.getName();
        if (src == bttn_Cancel) {
            dispose();
        } else if (src == bttn_OK) {
            //run through all filters
            updateGlobalStat();

            //for the remaining pick topN of SVM prediction
            try {
                String score_propname = "SVMpred";
                if (isAntiSenseDesign) {
                    score_propname = "score";
                }
                selected = pickTopNOligo(selected, getTopN(), getMinimalDistance(), score_propname);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(owner, "Unable to select: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }

            isCanceled = false;

            makeSelection();

            dispose();

        } else if (src == bttn_CreateGroup) {
            //run through all filters
            updateGlobalStat();

            //for the remaining pick topN of SVM prediction
            try {
                String score_propname = "SVMpred";
                if (isAntiSenseDesign) {
                    score_propname = "score";
                }
                selected = pickTopNOligo(selected, getTopN(), getMinimalDistance(), score_propname);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(owner, "Unable to select: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }

            if (selected == null) {
                JOptionPane.showMessageDialog(owner, "No oligos selected", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            makeSelection();

            NewGroupDialog newGroupDialog = new NewGroupDialog(context, owner, oligoSelectorModel);
            GroupInfo opt = newGroupDialog.getGroupInfo();

            if (opt != null) {
                isCanceled = false;
                dispose();
            }
        } else if (src == jcb_avoid3UTR
                || src == jcb_avoid5UTR
                || src == jcb_efficacy
                || src == jcb_polyUAGC
                || src == jcb_polyA
                || src == jcb_reynoldRules) {
            updateGlobalStat();
        } else if (src == jcb_offTargetNumMismatches || src == jcb_hitSpliceVariantsNumMismatches) {
            initStat();
        } else if (name != null && name.startsWith(ORTHOLOG_PREFIX)) {
            initStat();
        } else if (src == bttn_customizeOrthologs) {
            if (window.isVisible()) {
                window.setVisible(false);
                bttn_customizeOrthologs.setIcon(down_arrow);
                return;
            }
            window.setVisible(true);
            bttn_customizeOrthologs.setIcon(up_arrow);
            int x = (int) bttn_customizeOrthologs.getLocationOnScreen().getX();
            int y = (int) bttn_customizeOrthologs.getLocationOnScreen().getY() + bttn_customizeOrthologs.getHeight();
            window.setLocation(new Point(x, y));
        }
    }

    private ArrayList filterByDesignCriteria(ArrayList oligos) {
        selected = oligos;
        if (isAvoid3UTR() || isAvoid5UTR()) {
            selected = filterByTranscriptLocation(oligos,
                    isAvoid3UTR(), isAvoid5UTR());
        }
        /*
        if (isAvoidExonJunction()) {
        selected = filterByExonJunction(selected);
        }

        if (checkSpliceVariants()) {
        selected = filterBySpliceVariants(selected, getHitSpliceVariantsNumMismatches());
        }
        if (checkOrthologs()) {
        String[] selected_species_prefixes = this.getSelectedSpeciesNames();
        int[] selected_species_mismatches = this.getSelectedSpeciesMismatches();
        selected = filterByOrthologs(selected, selected_species_prefixes, selected_species_mismatches);
        }
        if (checkAvoidOffTargetHits()) {
        int offTargetMismatches = getOffTargetNumMismatches();
        if (offTargetMismatches >= 0) {
        selected = filterByProperty(selected, "num_of_off_targets_with_0_mismatches", 0, 0);
        }
        if (offTargetMismatches >= 1) {
        selected = filterByProperty(selected, "num_of_off_targets_with_1_mismatches", 0, 0);
        }
        if (offTargetMismatches >= 2) {
        selected = filterByProperty(selected, "num_of_off_targets_with_2_mismatches", 0, 0);
        }
        if (offTargetMismatches >= 3) {
        selected = filterByProperty(selected, "num_of_off_targets_with_3_mismatches", 0, 0);
        }
        }
        if (isAvoidSNPs()) {
        //avoid SNPs for humans
        selected = filterBySNP(selected);
        }
         */
        if (!isAntiSenseDesign) {

            if (isUseEfficacyCutoff()) {
                //effiacy
                float efficacy_min = this.getEfficacyThreshold();
                selected = filterByProperty(selected, "SVMpred", efficacy_min, 100);
            }

            if (isPolyUAGC()) {
                selected = filterByPolyUAGC(selected);
            }

            if (isCheckReynoldRules()) {
                int reynoldRules_min = this.getReynoldRulesThreshold();
                selected = filterByProperty(selected, "ReynoldsScore", (float) reynoldRules_min, 100);
            }
        } else {
            if (isUseEfficacyCutoff()) {
                //effiacy
                float efficacy_min = this.getEfficacyThreshold();
                selected = filterByProperty(selected, "score", efficacy_min, 100);
            }

            if (isPolyA()) {
                selected = filterByPolyA(selected);
            }
        }
        return selected;
    }

    private void initStat() {
        if (oligos == null) {
            return;
        }

        int count = 0;
        ArrayList selected = selected = filterBySpliceVariants(oligos, getHitSpliceVariantsNumMismatches());
        if (selected != null) {
            count = selected.size();
        }
        l_checkSpliceVariants.setText("" + count);

        String[] selected_species_prefixes = this.getSelectedSpeciesNames();
        int[] selected_species_mismatches = this.getSelectedSpeciesMismatches();
        selected = filterByOrthologs(oligos, selected_species_prefixes, selected_species_mismatches);
        if (selected != null) {
            count = selected.size();
            l_checkOrthologs.setText("" + count);
        } else {
            l_checkOrthologs.setText("N/A");
        }


        selected = filterByTranscriptLocation(oligos, false, true);
        if (selected != null) {
            count = selected.size();
            l_avoid5UTR.setText("" + count);
        } else {
            l_avoid5UTR.setText("N/A");
        }

        count = -1;
        selected = filterByTranscriptLocation(oligos, true, false);
        if (selected != null) {
            count = selected.size();
            l_avoid3UTR.setText("" + count);
        } else {
            l_avoid3UTR.setText("N/A");
        }

        selected = filterByExonJunction(oligos);
        if (selected != null) {
            count = selected.size();
            l_avoidExonJunction.setText("" + count);
        } else {
            l_avoidExonJunction.setText("N/A");
        }

        selected = filterBySNP(oligos);
        if (selected != null) {
            count = selected.size();
            l_avoidSNPs.setText("" + count);
        } else {
            l_avoidSNPs.setText("N/A");
        }

        //off target
        int offTargetMismatches = getOffTargetNumMismatches();
        selected = oligos;
        if (offTargetMismatches >= 0) {
            selected = filterByProperty(selected, "num_of_off_targets_with_0_mismatches", 0, 0);
        }
        if (offTargetMismatches >= 1) {
            selected = filterByProperty(selected, "num_of_off_targets_with_1_mismatches", 0, 0);
        }
        if (offTargetMismatches >= 2) {
            selected = filterByProperty(selected, "num_of_off_targets_with_2_mismatches", 0, 0);
        }
        if (offTargetMismatches >= 3) {
            selected = filterByProperty(selected, "num_of_off_targets_with_3_mismatches", 0, 0);
        }

        if (selected != null) {
            count = selected.size();
            l_avoidOffTargetHits.setText("" + count);
        } else {
            l_avoidOffTargetHits.setText("N/A");
        }

        //effiacy

        if (!isAntiSenseDesign) {
            float efficacy_min = this.getEfficacyThreshold();

            selected = filterByProperty(oligos, "SVMpred", efficacy_min, 100);
            if (selected != null) {
                count = selected.size();
                l_efficacy.setText("" + count);
            } else {
                l_efficacy.setText("N/A");
            }

            //check polyUAGC
            selected = filterByPolyUAGC(oligos);
            if (selected != null) {
                count = selected.size();
                l_avoidPolyUAGC.setText("" + count);
            } else {
                l_avoidPolyUAGC.setText("N/A");
            }

            //check reynold rules
            int reynoldRules_min = this.getReynoldRulesThreshold();
            selected = filterByProperty(oligos, "ReynoldsScore", (float) reynoldRules_min, 100);
            if (selected != null) {
                count = selected.size();
                l_reynoldRules.setText("" + count);
            } else {
                l_reynoldRules.setText("N/A");
            }
        } else {
            float efficacy_min = this.getEfficacyThreshold();

            selected = filterByProperty(oligos, "score", efficacy_min, 100);
            if (selected != null) {
                count = selected.size();
                l_efficacy.setText("" + count);
            } else {
                l_efficacy.setText("N/A");
            }

            //check polyA
            selected = filterByPolyA(oligos);
            if (selected != null) {
                count = selected.size();
                l_avoidPolyA.setText("" + count);
            } else {
                l_avoidPolyA.setText("N/A");
            }
        }

        updateGlobalStat();
    }

    private void updateGlobalStat() {
        selected = this.filterByDesignCriteria(oligos);
        String text = "<html>Total oligos: <b>" + oligos.size() + "</b>    Passed all filters: <b>"
                + selected.size() + "</b></html>";
        filterCount.setText(text);
    }

    public static JButton createButton(String text, int margin) {
        JButton button = new JButton(text);
        Insets ins = button.getMargin();
        ins.left = margin;
        ins.right = margin;
        button.setMargin(ins);
        button.setFocusPainted(false);
        return button;
    }

    public class OligoComparator implements Comparator {

        String propName = "";

        public OligoComparator(String propName) {
            OligoComparator.this.propName = propName;
        }

        public int compare(Object o1, Object o2) {
            Oligo oligo1 = (Oligo) o1;
            Oligo oligo2 = (Oligo) o2;

            String v1 = (String) oligo1.getProperty(propName);
            String v2 = (String) oligo2.getProperty(propName);

            double d1 = 0;
            try {
                d1 = Double.parseDouble(v1);
            } catch (Exception ex) {
                return -1;
            }

            double d2 = 0;
            try {
                d2 = Double.parseDouble(v2);
            } catch (Exception ex) {
                return 1;
            }

            if (d1 == d2) {
                return 0;
            }
            if (d1 < d2) {
                return -1;
            }

            return 1;
        }
    }

    public void componentHidden(ComponentEvent e) {
        // TODO Auto-generated method stub
    }

    public void componentMoved(ComponentEvent e) {
        if (window.isVisible()) {
            int x = (int) bttn_customizeOrthologs.getLocationOnScreen().getX();
            int y = (int) bttn_customizeOrthologs.getLocationOnScreen().getY() + bttn_customizeOrthologs.getHeight();
            window.setLocation(new Point(x, y));
        }
    }

    public void componentResized(ComponentEvent e) {
        // TODO Auto-generated method stub
    }

    public void componentShown(ComponentEvent e) {
        // TODO Auto-generated method stub
    }

    public void stateChanged(ChangeEvent e) {
        Object src = e.getSource();
        if (src == js_efficacy || src == js_reynoldRules) {
            initStat();
        }

    }
}
