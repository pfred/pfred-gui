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

import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.ArrayList;
import org.pfred.model.Oligo;
import com.pfizer.rtc.notation.model.Nucleotide;
import com.pfizer.rtc.notation.model.RNAPolymerNode;
import com.pfizer.rtc.notation.model.PolymerNode;
import com.pfizer.rtc.notation.tools.NucleotideSequenceParser;
import com.pfizer.rtc.notation.tools.SimpleNotationParser;

public class OligoHelper {

    //jdh
    public static String smiles_largest_fragment(String smi) {
        //cheap way to desalt
        StringTokenizer st = new StringTokenizer(smi, ".");
        String s, ret = "";
        while (st.hasMoreTokens()) {
            s = st.nextToken();
            if (s.length() > ret.length()) {
                ret = s;
            }
        }
        //if(!smi.equals(ret))
        //    System.err.println(smi+" desalts to \n"+ret);
        return ret;
    }

    public static String dna2sense_rna(String seq) {
        seq = T2U(seq);
        return seq;
    }

    public static String dna2antisense_rna(String seq) {
        seq = rc(seq);
        seq = T2U(seq);
        return seq;
    }

    public static String antisense_rna2dna(String seq) {
        seq = rc(seq, false);
        seq = U2T(seq);
        return seq;
    }

    public static String sense_rna2antisense_rna(String seq) {
        seq = rc(seq, true);
        return seq;
    }

    public static String sense_rna2dna(String seq) {
        seq = U2T(seq);
        return seq;
    }

    public static String T2U(String seq) {
        StringBuffer sb = new StringBuffer(seq);
        int size = seq.length();
        for (int i = 0; i < size; i++) {
            char c = seq.charAt(i);
            if (c != 'T' && c != 't') {
                continue;
            }
            if (c == 'T') {
                c = 'U';
            } else if (c == 't') {
                c = 'u';
            }
            sb.setCharAt(i, c);
        }
        return sb.toString();
    }

    public static String U2T(String seq) {
        StringBuffer sb = new StringBuffer(seq);
        int size = seq.length();
        for (int i = 0; i < size; i++) {
            char c = seq.charAt(i);
            if (c != 'U' && c != 'u') {
                continue;
            }
            if (c == 'U') {
                c = 'T';
            } else if (c == 'u') {
                c = 't';
            }
            sb.setCharAt(i, c);
        }
        return sb.toString();
    }

    public static String rc(String seq) {
        return rc(seq, false);//default to dna
    }

    public static String rc(String seq, boolean isRNA) {
        StringBuffer sb = new StringBuffer(seq);

        int size = seq.length();
        for (int i = 0; i < size; i++) {
            char c = seq.charAt(i);
            switch (c) {
                case 'A':
                    c = isRNA ? 'U' : 'T';
                    break;
                case 'T':
                    c = 'A';
                    break;
                case 'G':
                    c = 'C';
                    break;
                case 'C':
                    c = 'G';
                    break;
                case 'a':
                    c = isRNA ? 'u' : 't';
                    break;
                case 't':
                    c = 'a';
                    break;
                case 'g':
                    c = 'c';
                    break;
                case 'c':
                    c = 'g';
                    break;
                case 'U':
                    c = 'A';
                    break;
                case 'u':
                    c = 'a';
                    break;
            }
            sb.setCharAt(i, c);
        }
        sb.reverse();
        return sb.toString();
    }

    public static boolean isValidSequence(String oligo) {
        int size = oligo.length();
        oligo = oligo.toUpperCase();
        for (int i = 0; i < size; i++) {
            int c = oligo.charAt(i);
            if (c != 'A' && c != 'T' && c != 'G' && c != 'C' && c != 'U' && c != 'N') {
                return false;
            }
        }
        return true;
    }

    public static String[] oligo2Txt(ArrayList oligos, String[] propNames, HashMap<String, String> perOperandNames) {
        int size = oligos.size();
        String[] stx = new String[size + 1];
        StringBuffer header = new StringBuffer();
        header.append(Oligo.NAME_PROP);
        header.append("\t");
        header.append(Oligo.ANTISENSE_OLIGO_PROP);
        header.append("\t");
        header.append(Oligo.SENSE_OLIGO_PROP);
        header.append("\t");
        header.append(Oligo.DNA_OLIGO_PROP);
        header.append("\t");
        header.append(Oligo.TARGET_NAME_PROP);
        header.append("\t");
        header.append(Oligo.START_POS_PROP);
        header.append("\t");
        header.append(Oligo.END_POS_PROP);
        if (propNames != null) {
            for (int j = 0; j < propNames.length; j++) {
                header.append("\t");
                header.append(propNames[j]);
            }
        }

        String[] perOperandPropNames = null;
        String[] perOperandMappedPropNames = null;
        if (perOperandNames != null) {
            perOperandPropNames = new String[perOperandNames.size()];
            perOperandMappedPropNames = new String[perOperandNames.size()];

            Iterator name_iter = perOperandNames.keySet().iterator();
            int name_idx = -1;
            while (name_iter.hasNext()) {
                name_idx++;
                String key = (String) name_iter.next();
                perOperandPropNames[name_idx] = key;
                perOperandMappedPropNames[name_idx] = (String) perOperandNames.get(key);
            }

            for (int i = 0; i < perOperandPropNames.length; i++) {
                header.append("\t");
                header.append(perOperandPropNames[i]);
            }
        }


        stx[0] = header.toString();
        for (int i = 1; i < size + 1; i++) {
            StringBuffer buffer = new StringBuffer();
            Oligo o = (Oligo) oligos.get(i - 1);
            buffer.append(o.getName());
            buffer.append("\t");
            buffer.append(o.getSingleLetterSeq(Oligo.TYPE_PARENT_ANTISENSE_OLIGO));
            buffer.append("\t");
            buffer.append(o.getSingleLetterSeq(Oligo.TYPE_PARENT_SENSE_OLIGO));
            buffer.append("\t");
            buffer.append(o.getSingleLetterSeq(Oligo.TYPE_PARENT_DNA_OLIGO));
            buffer.append("\t");
            buffer.append(o.getTargetName());
            buffer.append("\t");
            buffer.append(o.getStart());
            buffer.append("\t");
            buffer.append(o.getEnd());
            if (propNames != null) {

                for (int j = 0; j < propNames.length; j++) {
                    buffer.append("\t");
                    String v = (String) o.getProperty(propNames[j]);
                    if (v == null) {
                        v = "";
                    }
                    buffer.append(v);
                }
            }

            if (perOperandMappedPropNames != null) {
                for (int j = 0; j < perOperandMappedPropNames.length; j++) {
                    buffer.append("\t");
                    Object obj = o.getProperty(perOperandMappedPropNames[j]);
                    String v = "";
                    if (obj != null) {
                        v = obj.toString();
                    }

                    buffer.append(v);
                }
            }
            stx[i] = buffer.toString();
        }

        return stx;
    }

    public static ArrayList searchOligos(String propName, String comparator,
            String query, ArrayList mols) {

        ArrayList found = new ArrayList();

        String tmp = null;
        float num_tmp = 0;
        float num_query = 0;
        float num_bQuery = 0;

        if (comparator.indexOf("matches") < 0) //do this outside of for loop to speed things up
        {
            num_query = Float.parseFloat(query); //query is guaranteed to be a numeric number. validated before passed in.
        }
        for (int i = 0; i < mols.size(); i++) {
            Oligo mol = (Oligo) mols.get(i);
            tmp = (String) mol.getProperty(propName);
            if (tmp == null) {
                System.out.println("DEBUG:: No property " + propName +
                        " info avaiable for compound" + mol.getName());
                continue;
            }

            tmp = tmp.toLowerCase();

            if (propName.equalsIgnoreCase("name") && mol.getName().toLowerCase().indexOf(query.toLowerCase()) >= 0) {
                found.add(mol);
            } else if (comparator.equalsIgnoreCase("exact matches")) {
                // this was old code
                //match the string here
                //if (tmp.indexOf(query)>=0)

                // replaced with simple comparison
                // keep in mind tho that all the params have
                // been LOWER CASED so as long as by "match"
                // you mean case-insensitive match then we are set
                if (tmp.equals(query)) {
                    found.add(mol);
                }
            } else if (comparator.equalsIgnoreCase("matches")) {
                // this was old code
                //match the string here
                //if (tmp.indexOf(query)>=0)

                // replaced with simple comparison
                // keep in mind tho that all the params have
                // been LOWER CASED so as long as by "match"
                // you mean case-insensitive match then we are set
                if (tmp.indexOf(query) >= 0) {
                    found.add(mol);
                }
            } else if (comparator.equalsIgnoreCase(">")) {
                try {
                    num_tmp = Float.parseFloat(tmp);

                    if (num_tmp > num_query) {
                        found.add(mol);
                    }
                } catch (Exception ex) {
                    System.out.println("DEBUG:: Malformatted property in the input: " +
                            num_tmp + " " + mol.getName());
                    continue;
                }
            } else if (comparator.equalsIgnoreCase("<")) {
                try {
                    num_tmp = Float.parseFloat(tmp);

                    if (num_tmp < num_query) {
                        found.add(mol);
                    }
                } catch (Exception ex) {
                    System.out.println("DEBUG:: Malformatted property in the input: " +
                            num_tmp + " " + mol.getName());
                    continue;
                }
            } else if (comparator.equalsIgnoreCase("between")) {
                try {
                    num_tmp = Float.parseFloat(tmp);

                    if (num_tmp >= num_query && num_tmp <= num_bQuery) {
                        found.add(mol);
                    }
                } catch (Exception ex) {
                    System.out.println("DEBUG:: Malformatted property in the input: " +
                            num_tmp + " " + mol.getName());
                    continue;
                }
            } else if (comparator.equalsIgnoreCase("=")) {
                try {
                    num_tmp = Float.parseFloat(tmp);

                    if (num_tmp == num_query) {
                        found.add(mol);
                    }
                } catch (Exception ex) {
                    System.out.println("DEBUG:: Malformatted property in the input: " +
                            num_tmp + " " + mol.getName());
                    continue;
                }
            }
        }
        if (found.size() == 0) {
            return null;
        } else {
            return found;
        }

    }

    public static void copyOligo(Oligo from, Oligo to) {

        to.setSeq(from.getSingleLetterSeq(Oligo.TYPE_DNA_OLIGO), Oligo.TYPE_DNA_OLIGO);
        to.setProperties(from.getProperties());
        to.setColor(from.getColor());
        to.setStart(from.getStart());
        to.setEnd(from.getEnd());
        to.setTargetName(from.getTargetName());
        to.setNotation(from.getNotation());
    }

    // strip any properties from mol that would mess us up
    public static void stripImportedOligo(Oligo mol) {
        String s = (String) mol.getProperty(PFREDConstant.GROUP_PROPERTY);
        if (s != null) {
            mol.removeProperty(PFREDConstant.GROUP_PROPERTY);
        }

        s = (String) mol.getProperty(PFREDConstant.PFRED_COLOR_SCHEME);
        if (s != null) {
            mol.removeProperty(PFREDConstant.PFRED_COLOR_SCHEME);
        }

    }

    /**
     * move over all properties from input_oligo to original_oligo
     * @param original_oligo
     * @param input_oligo
     */
    public static void mergeOligo(Oligo original_oligo, Oligo input_oligo) {
        ArrayList propNames = input_oligo.propertyNames();
        int size = propNames.size();
        for (int i = 0; i < size; i++) {
            String propName = (String) propNames.get(i);
            if (propName.equalsIgnoreCase("name") ||
                    propName.equalsIgnoreCase(Oligo.PARENT_ANTISENSE_OLIGO_PROP) ||
                    propName.equalsIgnoreCase(Oligo.PARENT_DNA_OLIGO_PROP) ||
                    propName.equalsIgnoreCase(Oligo.PARENT_SENSE_OLIGO_PROP)) {
                continue;
            }
            original_oligo.setProperty(propName, input_oligo.getProperty(propName));//overwrite
        }
    }

    public static String getModifiedSequence(RNAPolymerNode node) throws Exception {
        if (node == null) {
            return null;
        }

        //seq=PMEAdaptor.getNucleotideSequenceFromNotation(notation);
        List<Nucleotide> bases = SimpleNotationParser.getNucleotideList(node.getLabel());

        int baseCount = bases.size();

        //get dna or rna flap
        if (bases == null || baseCount == 0) {
            //System.err.println("unable to convert oligo_notation to single string for "+ node.getLabel());
            return null;
        }

        StringBuffer result = new StringBuffer();


        for (int j = 0; j < baseCount; j++) {
            Nucleotide n = bases.get(j);
            if (j != 0) {
                result.append(".");
            }
            result.append(n.getSymbol());

        }
        return result.toString();


    }

    public static String getSingleLetterSequence(PolymerNode node) throws Exception {
        if (node == null) {
            return null;
        }

        //seq=PMEAdaptor.getNucleotideSequenceFromNotation(notation);
        List<Nucleotide> bases = SimpleNotationParser.getNucleotideList(node.getLabel());

        int baseCount = bases.size();

        //get dna or rna flap
        if (bases == null || baseCount == 0) {
            //System.err.println("unable to convert oligo_notation to single string for "+ node.getLabel());
            return null;
        }

        StringBuffer result = new StringBuffer();


        for (int j = 0; j < baseCount; j++) {
            Nucleotide n = bases.get(j);

            String b = n.getNaturalAnalog();
            if (n.getSugarMonomer() != null && n.getSugarMonomer().getAlternateId() != null &&
                    n.getSugarMonomer().getAlternateId().equals("dR")) {
                b = b.toLowerCase();
            }
            result.append(b);

        }
        return result.toString();


    }

    public static BitSet getModifiedBaseOrSugarPositions(PolymerNode node) throws Exception {
        BitSet bits = new BitSet();
        if (node == null) {
            return bits;
        }

        //seq=PMEAdaptor.getNucleotideSequenceFromNotation(notation);
        List<Nucleotide> bases = SimpleNotationParser.getNucleotideList(node.getLabel(), false);

        int baseCount = bases.size();

        //get dna or rna flap
        if (bases == null || baseCount == 0) {
            //System.err.println("unable to convert oligo_notation to single string for "+ node.getLabel());
            return bits;
        }



        for (int j = 0; j < baseCount; j++) {
            Nucleotide n = bases.get(j);

            if ((n.getBaseMonomer() != null && n.getBaseMonomer().isModified()) || (n.getSugarMonomer() != null && n.getSugarMonomer().isModified())) {
                bits.set(j);
            }
        }
        return bits;
    }

    public static BitSet getModifiedPosphatePositions(PolymerNode node) throws Exception {
        BitSet bits = new BitSet();
        if (node == null) {
            return bits;
        }

        //seq=PMEAdaptor.getNucleotideSequenceFromNotation(notation);

        List<Nucleotide> bases = SimpleNotationParser.getNucleotideList(node.getLabel(), false);

        int baseCount = bases.size();

        //get dna or rna flap
        if (bases == null || baseCount == 0) {
            //System.err.println("unable to convert oligo_notation to single string for "+ node.getLabel());
            return bits;
        }

        for (int j = 0; j < baseCount; j++) {
            Nucleotide n = bases.get(j);

            if (n.getPhosphateMonomer() != null && n.getPhosphateMonomer().isModified()) {
                bits.set(j);
            }
        }
        return bits;
    }

    public static int getLength(PolymerNode node) throws Exception {
        if (node == null) {
            return 0;
        }

        //seq=PMEAdaptor.getNucleotideSequenceFromNotation(notation);
        List<Nucleotide> bases = SimpleNotationParser.getNucleotideList(node.getLabel(), false);
        if (bases == null) {
            return 0;
        }
        return bases.size();

    }

    public static String getRNANotationFromRNAPolymerNode(RNAPolymerNode node, String annotation) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("RNA1{");
        buffer.append(node.getLabel());
        buffer.append("}$$$RNA1{");
        buffer.append(annotation);
        buffer.append("}$");
        return buffer.toString();
    }

    public static String getRNANotationFromSequence(String sense, String antisense) {
        String notation = null;
        try {
            notation = NucleotideSequenceParser.getSirnaNotation(sense, antisense);
        } catch (Exception ex) {
            System.err.println("unable to parse seq");
            return null;
        }
        return notation;
        /*StringBuffer buffer=new StringBuffer();
        buffer.append("RNA1{");

        String notation=null;
        try {
        notation=NucleotideSequenceParser.getNotation(seq);
        }catch (Exception ex){
        System.err.println("unable to parse seq");
        return null;
        }
        buffer.append(notation);
        buffer.append("}$$$RNA1{");
        String annotation="ss";
        if (antisense) annotation="as";
        buffer.append(annotation);
        buffer.append("}$");
        return buffer.toString();
         */

    }

    /***
     * Sequence could be single letter or dds (dot delimited sequence format)
     * @param seq
     * @return

    public static int getSequenceLength(String seq){
    if (seq.indexOf(".")>0){
    //this is dds format
    return seq.split(".").length;
    }
    return seq.length();
    }
     */
    /***
     * Sequence could be single letter or dds (dot delimited sequence format)
     * @param seq
     * @return
    
    public static String[] getNucleotodeFromSequence(String seq){
    if (seq.indexOf(".")>0){
    //this is dds format
    return seq.split(".");
    }

    }*/
    public static boolean isOligoSeqProperty(String propName) {
        if (propName != null && propName.toUpperCase().endsWith("_OLIGO")) {
            return true;
        }
        return false;
    }

    public static boolean isRNANotation(String seq) {
        if (seq.indexOf("$") >= 0) {
            return true;
        }
        return false;
    }
}



