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

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import org.pfred.OligoHelper;

public class EnterTranscriptsDialog
    extends JDialog implements ActionListener {
  private JDialog parent=null;
  private JTextArea jta_seq=null;
  private JComboBox jcb_species=null;
  private JButton bttn_OK=null;
  private JButton bttn_Cancel=null;
  private boolean canceled=true;
/*  private String[] species;
  private String[] names=null;
  private String[] seqs=null;
 */
  private ArrayList transcripts=new ArrayList();

  public static String [] species_list=new String[]{"Homo_sapiens", "Macaca_mulatta", "Mus_musculus",
                                                    "Rattus_norvegicus","Canis_familiaris","Pan_troglodytes", "Other"};

  public EnterTranscriptsDialog(JDialog parent) {
    super(parent,"Enter User-Specified Transcripts", true);
    this.parent=parent;
    initGUI();
    pack();
    this.setLocationRelativeTo(parent);
  }

  public void initGUI(){
    setLayout(new BorderLayout());
    JPanel northPane=new JPanel();
    northPane.setBorder(BorderFactory.createTitledBorder("Select Species: "));
    northPane.setLayout(new FlowLayout(FlowLayout.LEADING));
    JLabel label=new JLabel("Species: ");
    northPane.add(label);

    jcb_species=new JComboBox(species_list);
    northPane.add(jcb_species);
    this.add(northPane, BorderLayout.NORTH);

    JPanel centerPane =new JPanel();
    centerPane.setLayout(new BorderLayout());
    centerPane.setBorder(BorderFactory.createTitledBorder("Enter FASTA sequence: "));
    jta_seq=new JTextArea(6,50);
    JScrollPane jsp_seq=new JScrollPane(jta_seq);
    centerPane.add(jsp_seq);
    this.add(centerPane, BorderLayout.CENTER);

    JPanel bttn_pane=new JPanel();
    bttn_OK=new JButton("OK");
    bttn_OK.addActionListener(this);
    bttn_pane.add(bttn_OK);
    bttn_Cancel=new JButton("Cancel");
    bttn_Cancel.addActionListener(this);
    bttn_pane.add(bttn_Cancel);
    this.add(bttn_pane, BorderLayout.SOUTH);
  }

  public void actionPerformed(ActionEvent evt) {
    if (evt.getSource() == bttn_OK) {
      String species_selected=(String)jcb_species.getSelectedItem();
      if (species_selected.trim().length()==0){
        JOptionPane.showMessageDialog(parent, "Please select the species for the sequences");
      }
      //now get the sequences
      String text=jta_seq.getText();
      String[] lines=text.split("\n");
      String name=null;
      StringBuffer buffer=new StringBuffer();
      for (int i=0; i<lines.length; i++){
        String line=lines[i];
        if (line.startsWith(">")){
          if (name!=null && buffer!=null ){
            String seq=buffer.toString();
            if (OligoHelper.isValidSequence(seq)){
              Transcript t = new Transcript();
              t.setName(name);
              t.setSequence(seq);
              t.setSpecies(species_selected);
              t.setProperty(Transcript.SOURCE_PROP, Transcript.TRANSCRIPT_SRC_USER_SPECIFIED);
              t.setAsPrimaryTranscript(false);
              t.setAsSecondaryTranscript(true);
              transcripts.add(t);
            }else{
              //notify user
              JOptionPane.showMessageDialog(parent, "Some sequences have invalid characters!", "Error", JOptionPane.ERROR_MESSAGE );
              return;
            }
          }
          name=line.substring(1);
          buffer=new StringBuffer();
        }else{
          buffer.append(line);
        }
      }

      //add the last sequence
      if (name != null && buffer != null) {
        String seq = buffer.toString();
        if (OligoHelper.isValidSequence(seq)) {
          Transcript t = new Transcript();
          t.setName(name);
          t.setSequence(seq);
          t.setSpecies(species_selected);
          t.setProperty(Transcript.SOURCE_PROP,
                        Transcript.TRANSCRIPT_SRC_USER_SPECIFIED);
          t.setAsPrimaryTranscript(false);
              t.setAsSecondaryTranscript(true);
          transcripts.add(t);
        }
        else {
          //notify user
          JOptionPane.showMessageDialog(parent,
                                        "Some sequences have invalid characters!",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
          return;
        }
      }


      /////////

      canceled = false;
      dispose();
    }
    else if (evt.getSource() == bttn_Cancel) {
      dispose();
    }

  }
/*
  public String[] getSpecies(){
    return species;
  }

  public String[] getNames(){
    return names;
  }

  public String[] getSequences(){
    return seqs;
  }
 */
 public ArrayList getTranscripts(){
   return transcripts;
 }

  public boolean isCanceled() {
    return canceled;
  }

}
