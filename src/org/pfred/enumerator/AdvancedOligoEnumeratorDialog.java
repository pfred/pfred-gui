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

import com.pfizer.rtc.util.BrowserControl;
import com.pfizer.rtc.util.SpringUtilities;
import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
// import javax.xml.rpc.ServiceException;
import org.pfred.FileActionHandler;
import org.pfred.PFREDConstant;
import org.pfred.PFREDContext;
import org.pfred.RNAActionHandler;
import org.pfred.rest.RestServiceClient;
import org.pfred.icon.IconLoader;
import java.io.IOException;
import org.pfred.model.Oligo;

public class AdvancedOligoEnumeratorDialog extends JDialog implements ActionListener {

    JFrame parent = null;
    JPanel stepTwoPanel = new JPanel();
    JPanel stepTwoParamPanel = new JPanel();
    JPanel stepOnePanel = new JPanel();
    JPanel progressPanel = new JPanel();
    JTextField jtf_oligo_len = new JTextField("14", 5);
    JTextField jtf_offTargetMismatch = new JTextField("2", 5);
    JCheckBox jcb_runOffTargetSearch = new JCheckBox();
    JCheckBox jcb_runEfficacyModel = new JCheckBox();
    JCheckBox jcb_human = null;
    JCheckBox jcb_rat = null;
    JCheckBox jcb_mouse = null;
    JCheckBox jcb_dog = null;
    JCheckBox jcb_chimp = null;
    JCheckBox jcb_monkey = null;
    JComboBox jcb_specifiedSpecies = new JComboBox(new String[]{"human", "rat", "mouse", "dog", "chimp", "macaque"});
    // JComboBox jcb_specifiedSpecies = new JComboBox(new String[]{"human", "rat", "mouse", "dog", "chimp"});
    JComboBox jcb_databases = new JComboBox(new String[]{"Human transcriptome", "Small test database"});
    JComboBox jcb_design = new JComboBox(new String[]{"siRNA design", "AntiSense design"});
    JTextField jtf_gene = new JTextField("ENSG00000165175", 40);
    JLabel l_oligo_length = null;
    JLabel l_offTargetSearch = null;
    JLabel l_efficacyModel = null;
    JLabel l_missMatches = null;
    int currentStep = 1;
    private final static String RETRIEVE_ORTHO_SEQ = "Retrieve Ortholog Sequence";
    private final static String ENUMERATE_AND_SEARCH = "Enumerate & Annonate";
    JButton bttn_OK = new JButton(RETRIEVE_ORTHO_SEQ);
    JButton bttn_Cancel = new JButton("Stop");
    JButton bttn_Restart = new JButton("Start Over");
    JCheckBox jcb_suppress = new JCheckBox();
    boolean canceled = true;
    PFREDContext context;
    TargetTablePanel targetPanel;
    JProgressBar progress;
    JPanel speciesPanel;
    String seqAnnotation = null;
    String ensembl_gene_id = null;
    String species = null;
    String finalOligoSummary = null;
    String primary_target_id = null;
    String secondary_target_ids = null;
    String secondary_target_seqs = null;
    GetSequenceThread getSequenceThread = null;
    EnumerateOligoThread enumerateOligoThread = null;
    HashMap seqCache = new HashMap();
    public static final String ENSEMBL_GENE_ID = "ENSEMBL_GENE_ID";
    public static final String SPECIES = "SPECIES";
    public static final String REQUESTEDSPECIES = "REQUESTEDSPECIES";
    public static final String PRIMARY_TARGET_ID = "PRIMARY_TARGET_ID";
    public static final String SECONDARY_TARGET_IDs = "SECONDARY_TARGET_IDs";
    public static final String OLLIGO_LENGTH = "OligoLength";
    public static final String ON_TARGET_MISMATCH = "OnTargetMismatch";
    public static final String OFF_TARGET_MISMATCH = "OffTargetMismatch";
    public static final String RUN_OFF_TARGET_SEARCH = "RunOffTargetSearch";
    public static final String SUPPRESS = "Suppress";
    public static final String USER_SPECIFIED_TRANSCRIPTS = "UserSpecifiedTranscripts";
    public final static String CHOOSE_EFFICACY_MODEL = "ChooseEfficacyModel";
    public static final String TRANSCRIPT_IDS = "Transcript_IDs";
    public static final String PRIMARY_ID = "PrimaryID";
    public String errMsg = "";
    public boolean isSuccessful;
    private static String runName;
    private boolean serverRunDirExist = false;
    //

    public AdvancedOligoEnumeratorDialog(JFrame parent, PFREDContext context) throws Exception {
        super(parent, "Advanced Oligo Enumerator", true);
        this.parent = parent;
        this.context = context;

        runName = getUUID();

        initGUI();
        this.setMinimumSize(new Dimension(500, 40));
        pack();

        this.showAntiSenseSpecificParams(false);
        this.setLocationRelativeTo(parent);
    }

    public void update() {
    }

    public void dispose() {
        super.dispose();
        //        cleanDir();
        stopThreads();
    }

    public void cleanServerRunDir() {
        if (serverRunDirExist) {
            try{
                String success = RestServiceClient.runScriptUtilitiesService("CleanRun", runName, null, null, null);
            } catch (Exception ex){
                Logger.getLogger(AdvancedOligoEnumeratorDialog.class.getName()).log(Level.SEVERE, "Error executing CleanRun Service", ex);
            }
            // Logger.getLogger(AdvancedOligoEnumeratorDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isAntiSenseDesign() {
        if (jcb_design.getSelectedItem().equals("AntiSense design")) {
            return true;
        }
        return false;
    }

    private void showOffTargetSpecificParams(boolean show) {
        jtf_offTargetMismatch.setVisible(show);
        l_missMatches.setVisible(show);

        if (show) {
            if (isAntiSenseDesign()) {
                jtf_offTargetMismatch.setText("1");
            } else {
                jtf_offTargetMismatch.setText("2");
            }
        }
    }

    private void showAntiSenseSpecificParams(boolean show) {
        jtf_oligo_len.setVisible(show);
        l_oligo_length.setVisible(show);
        if (isAntiSenseDesign()) {
            jtf_offTargetMismatch.setText("1");
        } else {
            jtf_offTargetMismatch.setText("2");
        }
    }

    public void actionPerformed(ActionEvent evt) {
        Object src = evt.getSource();
        if (src == bttn_Cancel) {
            stopThreads();
        } else if (src == bttn_Restart) {
            restart();
        } else if (src == bttn_OK) {
            stopThreads();//kill existing thread jobs;
            if (currentStep == 1) {

                if (getEnsemblGeneID().trim().length() == 0 && getSpecies().trim().length() == 0) {
                    JOptionPane.showMessageDialog(parent, "Some parameters are missing!");
                    return;
                }

                getSequenceThread = new GetSequenceThread();
                getSequenceThread.start();

            } else if (currentStep == 2) {

                enumerateOligoThread = new EnumerateOligoThread();
                enumerateOligoThread.start();
            }
        } else if (src instanceof JButton && ((JButton) src).getName().equals("launchEnsembl")) {
            BrowserControl.displayURL(PFREDConstant.PUBLIC_ENSEMBL_URL);
        } else if (src == jcb_design) {
            if (!isAntiSenseDesign()) {
                showAntiSenseSpecificParams(false);
            } else {
                showAntiSenseSpecificParams(true);
            }
        } else if (src == jcb_runOffTargetSearch) {

            showOffTargetSpecificParams(jcb_runOffTargetSearch.isSelected());

        }
    }

    public String getEnsemblGeneID() {
        ensembl_gene_id = jtf_gene.getText().trim();
        return ensembl_gene_id;
    }

    public String getSpecifiedSpecies() {
        return (String) jcb_specifiedSpecies.getSelectedItem();
    }

    public String getSpecies() {
        StringBuffer buffer = new StringBuffer();
        if (jcb_human.isSelected()) {
            if (buffer.length() != 0) {
                buffer.append(",");
            }
            buffer.append("human");
        }
        if (jcb_rat.isSelected()) {
            if (buffer.length() != 0) {
                buffer.append(",");
            }
            buffer.append("rat");
        }
        if (jcb_mouse.isSelected()) {
            if (buffer.length() != 0) {
                buffer.append(",");
            }
            buffer.append("mouse");
        }
        if (jcb_dog.isSelected()) {
            if (buffer.length() != 0) {
                buffer.append(",");
            }
            buffer.append("dog");
        }
        if (jcb_chimp.isSelected()) {
            if (buffer.length() != 0) {
                buffer.append(",");
            }
            buffer.append("chimp");
        }
        if (jcb_monkey.isSelected()) {
            if (buffer.length() != 0) {
                buffer.append(",");
            }
            buffer.append("macaque");
        }

        species = buffer.toString();
        return species;
    }

    public String getPrimaryTranscriptID() {
        primary_target_id = targetPanel.getTargetTableModel().getPrimaryTargetID();
        return primary_target_id;
    }

    public String getPrimaryTranscriptSeq() {
        String seq_id = getPrimaryTranscriptID();
        return (String) seqCache.get(seq_id);
    }

    public String getSecondaryTranscriptIDs() {
        secondary_target_ids = targetPanel.getTargetTableModel().getSecondaryTargetIDs(",");
        return secondary_target_ids;
    }

    public String getUserSpecifiedTranscriptsAsText() {
        return targetPanel.getTargetTableModel().getUserSpecifiedTranscriptsAsText();
    }

    public int getOligoLength() {
        int oligo_length = 14;
        try {
            oligo_length = Integer.parseInt(jtf_oligo_len.getText().trim());
        } catch (Exception ex) {
        }
        return oligo_length;
    }

    public int getMissMatches() {
        int oligo_length = 1;
        try {
            oligo_length = Integer.parseInt(jtf_offTargetMismatch.getText().trim());
        } catch (Exception ex) {
        }
        return oligo_length;
    }

    public String getSummaryCSV() {

        return finalOligoSummary;
    }

    private void clearCache() {
        seqCache.clear();
    }

    private void cacheSequences(String fasta) {
        String[] lines = fasta.split("\n");
        String seq = "";
        String name = "";
        int count = 0;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith(">")) {
                if (count != 0) {
                    seqCache.put(name, seq);
                }
                name = lines[i].substring(1);
                seq = "";
                count++;
            } else {
                seq = seq + lines[i];
            }
        }
        //cache the last sequence
        if (name != null && seq != null && name.length() != 0 && seq.length() != 0) {
            seqCache.put(name, seq);
        }
    }

    private void postProcessStepTwo() {
        ///get the result
        String result = getSummaryCSV();
        if (result == null) {
            return;
        }
        String[] lines = result.split("\n");
        FileActionHandler fah = context.getUIManager().getFileActionHandler();
        //do some checking for data mapping with default
        fah.loadOligoCSV(lines, 1, fixHeaderLine(lines[0], ","), ",", "parent_dna_oligo",
                         Oligo.TYPE_PARENT_DNA_OLIGO,
                         "name", "start", "end", null);

        dispose();
        fah.setTargetSeq(getPrimaryTranscriptID(), getPrimaryTranscriptSeq());

        RNAActionHandler rah = context.getUIManager().getRNAActionHandler();
        if (!isAntiSenseDesign()) {
            rah.oligoSelector(false);
        } else {
            rah.oligoSelector(true);
        }
    }

    private String fixHeaderLine(String header, String delim) {
        String[] fields = header.split(delim);
        StringBuffer newheader = new StringBuffer();

        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                newheader.append(delim);
            }
            if (fields[i].equals(Oligo.DNA_OLIGO_PROP)) {
                newheader.append(Oligo.PARENT_DNA_OLIGO_PROP);
            } else if (fields[i].equals(Oligo.ANTISENSE_OLIGO_PROP)) {
                newheader.append(Oligo.PARENT_ANTISENSE_OLIGO_PROP);
            } else if (fields[i].equals(Oligo.SENSE_OLIGO_PROP)) {
                newheader.append(Oligo.PARENT_SENSE_OLIGO_PROP);
            } else {
                newheader.append(fields[i]);
            }
        }
        return newheader.toString();
    }

    private void initGUI() throws Exception {

        //get the species
        jcb_human = new JCheckBox("human");
        jcb_human.setName("human");
        jcb_rat = new JCheckBox("rat");
        jcb_rat.setName("rat");
        jcb_mouse = new JCheckBox("mouse");
        jcb_mouse.setName("mouse");
        jcb_dog = new JCheckBox("dog");
        jcb_dog.setName("dog");
        jcb_chimp = new JCheckBox("chimp");
        jcb_chimp.setName("chimp");
        jcb_monkey = new JCheckBox("macaque");
        jcb_monkey.setName("monkey");

        //layout the panels
        speciesPanel = new JPanel();
        speciesPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        speciesPanel.add(jcb_human);
        speciesPanel.add(jcb_rat);
        jcb_rat.setSelected(true);
        speciesPanel.add(jcb_mouse);
        speciesPanel.add(jcb_dog);
        speciesPanel.add(jcb_chimp);
        speciesPanel.add(jcb_monkey);

        stepOnePanel.setBorder(BorderFactory.createTitledBorder("Step 1: Retrieve target and ortholog transcripts "));
        stepOnePanel.setLayout(new BorderLayout());
        stepTwoPanel.setBorder(BorderFactory.createTitledBorder("Step 2: Enumerate oligo and annotate "));
        stepTwoPanel.setLayout(new BorderLayout());
        this.setLayout(new BorderLayout());
        this.add(stepOnePanel, BorderLayout.NORTH);
        stepTwoPanel.setVisible(false);
        this.add(stepTwoPanel, BorderLayout.CENTER);
        this.add(progressPanel, BorderLayout.SOUTH);

        stepTwoParamPanel.setLayout(new SpringLayout());
        stepTwoParamPanel.add(new JLabel("Design type:", JLabel.TRAILING));
        stepTwoParamPanel.add(jcb_design);
        jcb_design.addActionListener(this);

        l_oligo_length = new JLabel("Oligo Length:", JLabel.TRAILING);
        stepTwoParamPanel.add(l_oligo_length);
        stepTwoParamPanel.add(jtf_oligo_len);
        l_offTargetSearch = new JLabel("Run Off-target Search:", JLabel.TRAILING);
        jcb_runOffTargetSearch.setSelected(true);
        jcb_runOffTargetSearch.addActionListener(this);
        stepTwoParamPanel.add(l_offTargetSearch);
        stepTwoParamPanel.add(jcb_runOffTargetSearch);

        l_missMatches = new JLabel("# miss-matches:", JLabel.TRAILING);
        stepTwoParamPanel.add(l_missMatches);
        stepTwoParamPanel.add(jtf_offTargetMismatch);

        l_efficacyModel = new JLabel("Run Efficacy Model:", JLabel.TRAILING);
        jcb_runEfficacyModel.setSelected(true);
        stepTwoParamPanel.add(l_efficacyModel);
        stepTwoParamPanel.add(jcb_runEfficacyModel);

        SpringUtilities.makeCompactGrid(stepTwoParamPanel, //parent
                                        5, 2, //4x2 grid
                                        3, 3, //initX, initY
                                        3, 3); //xPad, yPad

        initButtonPanel();
        initStepOnePanel();//by default have the step one panel there
    }

    private void restart() {
        currentStep = 1;
        bttn_Restart.setEnabled(false);
        stopThreads();
        stepTwoPanel.setVisible(false);
        bttn_OK.setText(ENUMERATE_AND_SEARCH);
        pack();
    }

    private void initStepOnePanel() {
        stepOnePanel.setLayout(new BorderLayout());
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new SpringLayout());
        stepOnePanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.add(new JLabel("Ensembl ID:", JLabel.TRAILING));
        centerPanel.add(jtf_gene);
        centerPanel.add(jcb_specifiedSpecies);

        JButton button = IconLoader.getButton("ensembl");
        Insets ins = button.getMargin();
        ins.left = 6;
        ins.right = 6;
        button.setMargin(ins);
        button.setName("launchEnsembl");
        button.addActionListener(this);
        button.setToolTipText("Don't have ensembl id for your gene? Look it up from ensembl website.");
        centerPanel.add(button);
        centerPanel.add(new JLabel("Ortholog species of interest:", JLabel.TRAILING));
        centerPanel.add(speciesPanel);
        centerPanel.add(new JPanel());
        centerPanel.add(new JPanel());
        SpringUtilities.makeCompactGrid(centerPanel, //parent
                                        2, 4,
                                        3, 3, //initX, initY
                                        3, 3); //xPad, yPad
    }

    private void enablePanel(Container c, boolean enabled) {
        c.setEnabled(false);
    }

    private void initStepTwoPanel() {
        if (seqAnnotation == null) {
            //Todo: get some error message
            return;
        }
        stepTwoPanel.setVisible(true);
        if (bttn_OK.getText().equals(RETRIEVE_ORTHO_SEQ)) {
            bttn_OK.setText(ENUMERATE_AND_SEARCH);
        }

        String[] annotation = seqAnnotation.split("\n");
        try {
            targetPanel = new TargetTablePanel(runName, annotation, ",", 0, 2);
            targetPanel.setBorder(BorderFactory.createEtchedBorder());
            stepTwoPanel.removeAll();
            stepTwoPanel.add(targetPanel, BorderLayout.CENTER);
            stepTwoPanel.add(stepTwoParamPanel, BorderLayout.SOUTH);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * make button panel
     *
     * @return JPanel
     */
    private void initButtonPanel() {
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.LINE_AXIS));
        progressPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        progressPanel.add(Box.createRigidArea(new Dimension(5, 40)));
        progressPanel.add(bttn_Cancel);
        bttn_Cancel.addActionListener(this);
        bttn_Cancel.setEnabled(false);//not enable initially
        progressPanel.add(bttn_Cancel);

        progressPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        bttn_Restart.addActionListener(this);
        bttn_Restart.setEnabled(false);
        progressPanel.add(bttn_Restart);

        progressPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        progressPanel.add(Box.createHorizontalGlue());

        progress = new JProgressBar();
        progress.setIndeterminate(true);
        progress.setStringPainted(true);
        progress.setVisible(false);
        progress.setAlignmentY(Component.CENTER_ALIGNMENT);
        progressPanel.add(progress);
        progressPanel.add(Box.createHorizontalGlue());

        progressPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        bttn_OK.addActionListener(this);
        progressPanel.add(bttn_OK);
    }

    private void startProgress(String text) {
        bttn_OK.setEnabled(false);
        bttn_Cancel.setEnabled(true);
        progress.setString(text);
        progress.setVisible(true);
        progressPanel.updateUI();
        pack();
    }

    private void stopProgress() {
        bttn_OK.setEnabled(true);
        bttn_Cancel.setEnabled(false);

        progress.setVisible(false);
        progressPanel.updateUI();
    }

    private void stopThreads() {
        if (getSequenceThread != null) {
            getSequenceThread.cancel();
            getSequenceThread = null;
        }

        if (enumerateOligoThread != null) {
            enumerateOligoThread.cancel();
            enumerateOligoThread = null;
        }
    }

    public class EnumerateOligoThread extends SwingWorker {

        String finalResults = "";
        String seqs = "";
        boolean cancelled = false;
        int retries = 100;

        public Object construct() {
            String primaryTranscriptID = getPrimaryTranscriptID();
            String secondaryTranscriptIDs = getSecondaryTranscriptIDs();

            int oligo_len = 19;
            boolean isAntiSenseDesign = isAntiSenseDesign();
            if (isAntiSenseDesign) {
                oligo_len = getOligoLength();
            }

            try {
                String[] results = null;
                String aux = "";
                cancelled = false;
                startProgress("   Enumerate and annotate oligos --- may take 30 to 60 minutes   ");

                while ((results == null || aux.isEmpty() || aux == null) && retries > 0){
                    results = RestServiceClient.runEnumerateUtilitiesService("enumerate", runName, secondaryTranscriptIDs,
                                                                                primaryTranscriptID, "" + oligo_len);
                    aux = results[0];
                    // System.out.println(aux);
                    if(results == null || aux.isEmpty() || aux == null){
                        System.out.println("OUCH");
                        System.out.println("Received null results from server, retrying..." + retries);
                        retries = retries - 1;
                    }
                }
                if (results != null && results.length == 2) {

                    if (results[0] != null) {
                        finalResults = results[0];
                        System.out.println("------");
                        // System.out.println(finalResults);
                        System.out.println("DONE");
                    }

                    if (results[1] != null) {
                        seqs = results[1];
                        System.out.println("------");
                        // System.out.println(seqs);
                        System.out.println("DONE");
                    }
                    serverRunDirExist = true;
                }else{
                    cancelled = true;
                    System.out.println("Maximum connection retries exceeded, aborting...");
                    return "Enumeration Failed";
                }
            } catch (Exception e) {
                cancelled = true;
                System.out.println(e);
                return "Enumeration Failed";
            }

            clearCache();
            cacheSequences(seqs);

            if (jcb_runOffTargetSearch.isSelected()) {
                try {
                    finalResults = "";
                    startProgress("   Running Off Target Search  ");
                    String species = targetPanel.getTargetTableModel().getListOfSpecies(",");
                    String ids = targetPanel.getTargetTableModel().getListOfTranscripts(",");
                    System.out.println("species=" + species);
                    System.out.println("ids=" + ids);
                    if (isAntiSenseDesign()) {
                        while (finalResults.isEmpty() && retries > 0){
                            finalResults = RestServiceClient.runOffTargetSearchService("ASO", species, runName, ids, "" + getMissMatches());
                            System.out.println("------");
                            if (finalResults.isEmpty()){
                                System.out.println("Received empty string from server, retrying..." + retries);
                                retries = retries - 1;
                            }
                        }
                        if (finalResults.isEmpty()){
                            cancelled = true;
                            System.out.println("Maximum connection retries exceeded");
                        }
                        // System.out.println(finalResults);
                        System.out.println("DONE");
                    } else {
                        while (finalResults.isEmpty() && retries > 0){
                            finalResults = RestServiceClient.runOffTargetSearchService("siRNA", species, runName, ids, "" + getMissMatches());
                            System.out.println("------");
                            if (finalResults.isEmpty()){
                                System.out.println("Received empty string from server, retrying..." + retries);
                                retries = retries - 1;
                            }
                        }
                        if (finalResults.isEmpty()){
                            cancelled = true;
                            System.out.println("Maximum connection retries exceeded");
                            return "Off Target Search Failed";
                        }
                        // System.out.println(finalResults);
                        System.out.println("DONE");
                    }
                    serverRunDirExist = true;
                } catch (Exception e) {
                    cancelled = true;
                    System.out.println(e);
                    return "Off Target Search Failed";
                }
            }

            if (jcb_runEfficacyModel.isSelected()) {
                try {
                    startProgress("   Running activity model  ");
                    // String primarySeq = getPrimaryTranscriptSeq();
                    String primaryID = getPrimaryTranscriptID();
                    if (isAntiSenseDesign()) {
                        finalResults = RestServiceClient.runActivityModelService("ASO", runName, primaryID, "" + oligo_len);
                        System.out.println("------");
                        System.out.println("DONE");
                        // System.out.println(finalResults);
                    } else {
                        finalResults = RestServiceClient.runActivityModelService("siRNA", runName, primaryID, null);
                        System.out.println("------");
                        System.out.println("DONE");
                        // System.out.println(finalResults);
                    }
                    serverRunDirExist = true;
                } catch (Exception e) {
                    cancelled = true;
                    System.out.println(e);
                    return "Activity Model Failed";
                }
            }

            startProgress("                  Loading results                    ");

            return "All Done";
        }

        public void cancel() {
            interrupt();
            cancelled = true;
        }

        public void finished() {
            if (cancelled) {
                stopProgress();
            }

            String message = (String) get();
            if (message.equals("All Done")) {
                finalOligoSummary = finalResults;
                postProcessStepTwo();
                stopProgress();
            } else {
                JOptionPane.showMessageDialog(parent, message);
            }
        }
    }

    public class GetSequenceThread extends SwingWorker {

        String result = null;
        boolean cancelled = false;
        int retries = 20;

        public Object construct() {

            String enseblID = getEnsemblGeneID();
            String requestedSpecies = getSpecies();
            String species = getSpecifiedSpecies();

            try {
                cancelled = false;
                startProgress("   Retrieving transcripts and orthologs   ");

                System.out.println("runName=" + runName);

                while (result == null && retries > 0){
                    result = RestServiceClient.runScriptUtilitiesService("Orthologs", runName, enseblID, requestedSpecies, species);
                    if(result == null){
                        System.out.println("Connection refused, retrying..." + retries);
                        retries = retries - 1;
                    }
                }
                if(result == null){
                    System.out.println("Connection refused, maximum attempts reached, aborting...");
                    cancelled = true;
                    stopProgress();
                    return "Interrupted";
                }

                System.out.println(result);
                System.out.println("DONE");
                serverRunDirExist = true;

            } catch (Exception e) {
                System.out.println(e);
                stopProgress();
                return "Interrupted";  // SwingWorker.get() returns this
            }
            return "All Done";         // or this
        }

        public void cancel() {
            interrupt();
            cancelled = true;
        }

        public void finished() {
            stopProgress();

            if (cancelled) {
                return;
            }

            clearCache();
            seqAnnotation = result;
            cacheSequences(seqAnnotation);

            initStepTwoPanel();

            currentStep = 2;
            bttn_Restart.setEnabled(true);

            enablePanel(stepOnePanel, false);
            pack();
        }
    }

    public static String getUUID() {
        UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();

        return randomUUIDString;
    }
}
