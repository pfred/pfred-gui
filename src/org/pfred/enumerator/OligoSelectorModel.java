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

import java.util.ArrayList;


public class OligoSelectorModel {

    private boolean isAntisense;
    private boolean checkSpliceVariants;
    private int hitSpliceVariantsNumMismatches;
    private boolean checkOrthologs;
    private ArrayList<String> speciesNames;
    private ArrayList<Boolean> speciesSelected;
    private ArrayList<Integer> speciesNumMismatches;
    private boolean avoid5UTR;
    private boolean avoid3UTR;
    private boolean avoidExonIntronJunction;
    private boolean avoidSNPs;
    private boolean avoidOffTargetHits;
    private int offTargetHitNumMismatches;
    private boolean efficacy;
    private float efficacyScore;
    private boolean polyUAGC; //siRNA
    private boolean polyA; //antisense
    private boolean reynoldRules; //siRNA
    private int reynoldRulesScore; //siRNA
    private int topNCount;
    private int minimalDistance;

    public OligoSelectorModel(boolean isAntisense) {

        this.isAntisense = isAntisense;

        checkSpliceVariants = true;
        hitSpliceVariantsNumMismatches = 0;
        checkOrthologs = true;
        //todo

        avoid5UTR = true;
        avoid3UTR = true;
        avoidExonIntronJunction = true;
        avoidSNPs = true;

        avoidOffTargetHits = true;
        offTargetHitNumMismatches = 0;

        efficacy = true;

        if (isAntisense) {
            efficacyScore = 0f;
            polyA = true;
            topNCount = 5;
            minimalDistance = 14;
        } else {
            efficacyScore = 0.7f;
            polyUAGC = true;
            reynoldRules = false;
            reynoldRulesScore = 5;
            topNCount = 5;
            minimalDistance = 19;
        }

    }

    /**
     * @return the isAntisense
     */
    public boolean isIsAntisense() {
        return isAntisense;
    }

    /**
     * @param isAntisense the isAntisense to set
     */
    public void setIsAntisense(boolean isAntisense) {
        this.isAntisense = isAntisense;
    }

    /**
     * @return the checkSpliceVariants
     */
    public boolean isCheckSpliceVariants() {
        return checkSpliceVariants;
    }

    /**
     * @param checkSpliceVariants the checkSpliceVariants to set
     */
    public void setCheckSpliceVariants(boolean checkSpliceVariants) {
        this.checkSpliceVariants = checkSpliceVariants;
    }

    /**
     * @return the hitSpliceVariantsNumMismatches
     */
    public int getHitSpliceVariantsNumMismatches() {
        return hitSpliceVariantsNumMismatches;
    }

    /**
     * @param hitSpliceVariantsNumMismatches the hitSpliceVariantsNumMismatches to set
     */
    public void setHitSpliceVariantsNumMismatches(int hitSpliceVariantsNumMismatches) {
        this.hitSpliceVariantsNumMismatches = hitSpliceVariantsNumMismatches;
    }

    /**
     * @return the checkOrthologs
     */
    public boolean isCheckOrthologs() {
        return checkOrthologs;
    }

    /**
     * @param checkOrthologs the checkOrthologs to set
     */
    public void setCheckOrthologs(boolean checkOrthologs) {
        this.checkOrthologs = checkOrthologs;
    }

    /**
     * @return the speciesNames
     */
    public ArrayList<String> getSpeciesNames() {
        return speciesNames;
    }

    /**
     * @param speciesNames the speciesNames to set
     */
    public void setSpeciesNames(ArrayList<String> speciesNames) {
        this.speciesNames = speciesNames;
    }

    /**
     * @return the speciesSelected
     */
    public ArrayList<Boolean> getSpeciesSelected() {
        return speciesSelected;
    }

    /**
     * @param speciesSelected the speciesSelected to set
     */
    public void setSpeciesSelected(ArrayList<Boolean> speciesSelected) {
        this.speciesSelected = speciesSelected;
    }

    /**
     * @return the speciesNumMismatches
     */
    public ArrayList<Integer> getSpeciesNumMismatches() {
        return speciesNumMismatches;
    }

    /**
     * @param speciesNumMismatches the speciesNumMismatches to set
     */
    public void setSpeciesNumMismatches(ArrayList<Integer> speciesNumMismatches) {
        this.speciesNumMismatches = speciesNumMismatches;
    }

    /**
     * @return the avoid5UTR
     */
    public boolean isAvoid5UTR() {
        return avoid5UTR;
    }

    /**
     * @param avoid5UTR the avoid5UTR to set
     */
    public void setAvoid5UTR(boolean avoid5UTR) {
        this.avoid5UTR = avoid5UTR;
    }

    /**
     * @return the avoid3UTR
     */
    public boolean isAvoid3UTR() {
        return avoid3UTR;
    }

    /**
     * @param avoid3UTR the avoid3UTR to set
     */
    public void setAvoid3UTR(boolean avoid3UTR) {
        this.avoid3UTR = avoid3UTR;
    }

    /**
     * @return the avoidExonIntronJunction
     */
    public boolean isAvoidExonIntronJunction() {
        return avoidExonIntronJunction;
    }

    /**
     * @param avoidExonIntronJunction the avoidExonIntronJunction to set
     */
    public void setAvoidExonIntronJunction(boolean avoidExonIntronJunction) {
        this.avoidExonIntronJunction = avoidExonIntronJunction;
    }

    /**
     * @return the avoidSNPs
     */
    public boolean isAvoidSNPs() {
        return avoidSNPs;
    }

    /**
     * @param avoidSNPs the avoidSNPs to set
     */
    public void setAvoidSNPs(boolean avoidSNPs) {
        this.avoidSNPs = avoidSNPs;
    }

    /**
     * @return the avoidOffTargetHits
     */
    public boolean isAvoidOffTargetHits() {
        return avoidOffTargetHits;
    }

    /**
     * @param avoidOffTargetHits the avoidOffTargetHits to set
     */
    public void setAvoidOffTargetHits(boolean avoidOffTargetHits) {
        this.avoidOffTargetHits = avoidOffTargetHits;
    }

    /**
     * @return the offTargetHitNumMismatches
     */
    public int getOffTargetHitNumMismatches() {
        return offTargetHitNumMismatches;
    }

    /**
     * @param offTargetHitNumMismatches the offTargetHitNumMismatches to set
     */
    public void setOffTargetHitNumMismatches(int offTargetHitNumMismatches) {
        this.offTargetHitNumMismatches = offTargetHitNumMismatches;
    }

    /**
     * @return the efficacy
     */
    public boolean isEfficacy() {
        return efficacy;
    }

    /**
     * @param efficacy the efficacy to set
     */
    public void setEfficacy(boolean efficacy) {
        this.efficacy = efficacy;
    }

    /**
     * @return the efficacyScore
     */
    public float getEfficacyScore() {
        return efficacyScore;
    }

    /**
     * @param efficacyScore the efficacyScore to set
     */
    public void setEfficacyScore(float efficacyScore) {
        this.efficacyScore = efficacyScore;
    }

    /**
     * @return the polyUAGC
     */
    public boolean isPolyUAGC() {
        return polyUAGC;
    }

    /**
     * @param polyUAGC the polyUAGC to set
     */
    public void setPolyUAGC(boolean polyUAGC) {
        this.polyUAGC = polyUAGC;
    }

    /**
     * @return the polyA
     */
    public boolean isPolyA() {
        return polyA;
    }

    /**
     * @param polyA the polyA to set
     */
    public void setPolyA(boolean polyA) {
        this.polyA = polyA;
    }

    /**
     * @return the reynoldRules
     */
    public boolean isReynoldRules() {
        return reynoldRules;
    }

    /**
     * @param reynoldRules the reynoldRules to set
     */
    public void setReynoldRules(boolean reynoldRules) {
        this.reynoldRules = reynoldRules;
    }

    /**
     * @return the reynoldRulesScore
     */
    public int getReynoldRulesScore() {
        return reynoldRulesScore;
    }

    /**
     * @param reynoldRulesScore the reynoldRulesScore to set
     */
    public void setReynoldRulesScore(int reynoldRulesScore) {
        this.reynoldRulesScore = reynoldRulesScore;
    }

    public void setTopNCount(int topNCount) {
        this.topNCount = topNCount;
    }

    public int getTopNCount() {
        return topNCount;
    }

    public void setMinimalDistance(int minimalDistance) {
        this.minimalDistance = minimalDistance;
    }

    public int getMinimalDistance() {
        return minimalDistance;
    }
}
