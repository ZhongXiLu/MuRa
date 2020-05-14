package core.rankers.ck;

import core.Coefficient;
import core.RankedMutant;
import lumutator.Mutant;
import me.tongfei.progressbar.ProgressBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Ranks mutants based on the CK metric suite.
 */
public class CKRanker {

    public static void rankCK(List<Mutant> mutants, final String sourcePath) {

        CKCalculator ck = new CKCalculator();
        ck.calculateCKMetrics(sourcePath);

        // CBO
        int highest = 0;
        List<Integer> cbos = new ArrayList<>();
        for (Mutant mutant : ProgressBar.wrap(mutants, "Calculating CBO")) {
            final String className = mutant.getMutatedClass().split("\\$")[0];
            final int score = ck.getCbo(className);
            if (score > highest) {
                highest = score;
            }
            cbos.add(score);
        }
        for (int i = 0; i < mutants.size(); i++) {
            final Mutant mutant = mutants.get(i);
            final String className = mutant.getMutatedClass().split("\\$")[0];
            final double coeff = (double) cbos.get(i) / (double) highest;
            final String explanation = className + " has a CBO of " + ck.getCbo(className);
            ((RankedMutant) mutant).addRankCoefficient(
                    new Coefficient("CBO", coeff, explanation)
            );
        }

        // DIT
        highest = 0;
        List<Integer> dits = new ArrayList<>();
        for (Mutant mutant : ProgressBar.wrap(mutants, "Calculating DIT")) {
            final String className = mutant.getMutatedClass().split("\\$")[0];
            final int score = ck.getDit(className);
            if (score > highest) {
                highest = score;
            }
            dits.add(score);
        }
        for (int i = 0; i < mutants.size(); i++) {
            final Mutant mutant = mutants.get(i);
            final String className = mutant.getMutatedClass().split("\\$")[0];
            final double coeff = (double) dits.get(i) / (double) highest;
            final String explanation = className + " has a DIT of " + ck.getDit(className);
            ((RankedMutant) mutant).addRankCoefficient(
                    new Coefficient("DIT", coeff, explanation)
            );
        }

        // WMC
        highest = 0;
        List<Integer> wmcs = new ArrayList<>();
        for (Mutant mutant : ProgressBar.wrap(mutants, "Calculating NOM")) {
            final String className = mutant.getMutatedClass().split("\\$")[0];
            final int score = ck.getWmc(className);
            if (score > highest) {
                highest = score;
            }
            wmcs.add(score);
        }
        for (int i = 0; i < mutants.size(); i++) {
            final Mutant mutant = mutants.get(i);
            final String className = mutant.getMutatedClass().split("\\$")[0];
            final double coeff = (double) wmcs.get(i) / (double) highest;
            final String explanation = className + " has a WMC of " + ck.getWmc(className);
            ((RankedMutant) mutant).addRankCoefficient(
                    new Coefficient("WMC", coeff, explanation)
            );
        }

        // RFC
        highest = 0;
        List<Integer> rfcs = new ArrayList<>();
        for (Mutant mutant : ProgressBar.wrap(mutants, "Calculating RFC")) {
            final String className = mutant.getMutatedClass().split("\\$")[0];
            final int score = ck.getRfc(className);
            if (score > highest) {
                highest = score;
            }
            rfcs.add(score);
        }
        for (int i = 0; i < mutants.size(); i++) {
            final Mutant mutant = mutants.get(i);
            final String className = mutant.getMutatedClass().split("\\$")[0];
            final double coeff = (double) rfcs.get(i) / (double) highest;
            final String explanation = className + " has a RFC of " + ck.getRfc(className);
            ((RankedMutant) mutant).addRankCoefficient(
                    new Coefficient("RFC", coeff, explanation)
            );
        }

        // NOC
        highest = 0;
        List<Integer> nocs = new ArrayList<>();
        for (Mutant mutant : ProgressBar.wrap(mutants, "Calculating NOC")) {
            final String className = mutant.getMutatedClass().split("\\$")[0];
            final int score = ck.getNoc(className);
            if (score > highest) {
                highest = score;
            }
            nocs.add(score);
        }
        for (int i = 0; i < mutants.size(); i++) {
            final Mutant mutant = mutants.get(i);
            final String className = mutant.getMutatedClass().split("\\$")[0];
            final double coeff = (double) nocs.get(i) / (double) highest;
            final String explanation = className + " has a NOC of " + ck.getNoc(className);
            ((RankedMutant) mutant).addRankCoefficient(
                    new Coefficient("NOC", coeff, explanation)
            );
        }

        // Class complexity: all combined (each have equal weight)
        double highest_ = 0.0;
        List<Double> scores_ = new ArrayList<>();
        for (int i = 0; i < mutants.size(); i++) {
            final double score = ((double)(cbos.get(i) + dits.get(i) + wmcs.get(i) + rfcs.get(i) + nocs.get(i))) / (double) 5;
            if (score > highest_) {
                highest_ = score;
            }
            scores_.add(score);
        }
        for (int i = 0; i < mutants.size(); i++) {
            final Mutant mutant = mutants.get(i);
            final double coeff = scores_.get(i) / highest_;
            final String explanation = mutant.getMutatedClass().split("\\$")[0] + " has a class complexity of " + scores_.get(i);
            ((RankedMutant) mutant).addRankCoefficient(
                    new Coefficient("CK", coeff, explanation)
            );
        }

    }

}
