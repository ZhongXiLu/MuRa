package core.rankers.ck;

import core.Coefficient;
import core.RankedMutant;
import lumutator.Mutant;
import me.tongfei.progressbar.ProgressBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Ranks mutants based on the CK metric suite.
 */
public class CKRanker {

    public static void rankCK(List<Mutant> mutants, final String sourcePath) throws IOException {

        CKCalculator ck = new CKCalculator();
        ck.calculateCKMetrics(sourcePath);

        // CBO
        int highest = 0;
        List<Integer> cbos = new ArrayList<>();
        for (Mutant mutant : ProgressBar.wrap(mutants, "Calculating CBO")) {
            final String className = mutant.getMutatedClass();
            final int score = ck.getCbo(className);
            if (score > highest) {
                highest = score;
            }
            cbos.add(score);
        }
        for (int i = 0; i < mutants.size(); i++) {
            final Mutant mutant = mutants.get(i);
            final String className = mutant.getMutatedClass();
            final double coeff = (double) cbos.get(i) / (double) highest;
            final String explanation = mutant.getMutatedClass() + " has a CBO of " + ck.getCbo(className);
            ((RankedMutant) mutant).addRankCoefficient(
                    new Coefficient("CBO", coeff, explanation)
            );
        }

        // DIT
        highest = 0;
        List<Integer> dits = new ArrayList<>();
        for (Mutant mutant : ProgressBar.wrap(mutants, "Calculating DIT")) {
            final String className = mutant.getMutatedClass();
            final int score = ck.getDit(className);
            if (score > highest) {
                highest = score;
            }
            dits.add(score);
        }
        for (int i = 0; i < mutants.size(); i++) {
            final Mutant mutant = mutants.get(i);
            final String className = mutant.getMutatedClass();
            final double coeff = (double) dits.get(i) / (double) highest;
            final String explanation = mutant.getMutatedClass() + " has a DIT of " + ck.getDit(className);
            ((RankedMutant) mutant).addRankCoefficient(
                    new Coefficient("DIT", coeff, explanation)
            );
        }

        // WCM
        highest = 0;
        List<Integer> wcms = new ArrayList<>();
        for (Mutant mutant : ProgressBar.wrap(mutants, "Calculating NOM")) {
            final String className = mutant.getMutatedClass();
            final int score = ck.getWcm(className);
            if (score > highest) {
                highest = score;
            }
            wcms.add(score);
        }
        for (int i = 0; i < mutants.size(); i++) {
            final Mutant mutant = mutants.get(i);
            final String className = mutant.getMutatedClass();
            final double coeff = (double) wcms.get(i) / (double) highest;
            final String explanation = mutant.getMutatedClass() + " has a WCM of " + ck.getWcm(className);
            ((RankedMutant) mutant).addRankCoefficient(
                    new Coefficient("WCM", coeff, explanation)
            );
        }

        // RFC
        highest = 0;
        List<Integer> rfcs = new ArrayList<>();
        for (Mutant mutant : ProgressBar.wrap(mutants, "Calculating RFC")) {
            final String className = mutant.getMutatedClass();
            final int score = ck.getRfc(className);
            if (score > highest) {
                highest = score;
            }
            rfcs.add(score);
        }
        for (int i = 0; i < mutants.size(); i++) {
            final Mutant mutant = mutants.get(i);
            final String className = mutant.getMutatedClass();
            final double coeff = (double) rfcs.get(i) / (double) highest;
            final String explanation = mutant.getMutatedClass() + " has a RFC of " + ck.getRfc(className);
            ((RankedMutant) mutant).addRankCoefficient(
                    new Coefficient("RFC", coeff, explanation)
            );
        }

        // Class complexity: all combined (each have equal weight)
        double highest_ = 0.0;
        List<Double> scores_ = new ArrayList<>();
        for (int i = 0; i < mutants.size(); i++) {
            final double score = ((double)(cbos.get(i) + dits.get(i) + wcms.get(i) + rfcs.get(i))) / (double) 4;
            if (score > highest_) {
                highest_ = score;
            }
            scores_.add(score);
        }
        for (int i = 0; i < mutants.size(); i++) {
            final Mutant mutant = mutants.get(i);
            final double coeff = scores_.get(i) / highest_;
            final String explanation = mutant.getMutatedClass() + " has a class complexity of " + scores_.get(i);
            ((RankedMutant) mutant).addRankCoefficient(
                    new Coefficient("CC", coeff, explanation)
            );
        }

    }

}
