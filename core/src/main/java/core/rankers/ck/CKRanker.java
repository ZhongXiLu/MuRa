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
        List<Integer> scores = new ArrayList<>();
        for (Mutant mutant : ProgressBar.wrap(mutants, "Calculating CBO")) {
            final String className = mutant.getMutatedClass();
            final int score = ck.getCbo(className);
            if (score > highest) {
                highest = score;
            }
            scores.add(score);
        }
        for (int i = 0; i < mutants.size(); i++) {
            final Mutant mutant = mutants.get(i);
            final String className = mutant.getMutatedClass();
            final double coeff = (double) scores.get(i) / (double) highest;
            final String explanation = mutant.getMutatedMethod() + " has a CBO of " + ck.getCbo(className);
            ((RankedMutant) mutant).addRankCoefficient(
                    new Coefficient("CBO", coeff, explanation)
            );
        }

        // DIT
        highest = 0;
        scores = new ArrayList<>();
        for (Mutant mutant : ProgressBar.wrap(mutants, "Calculating DIT")) {
            final String className = mutant.getMutatedClass();
            final int score = ck.getDit(className);
            if (score > highest) {
                highest = score;
            }
            scores.add(score);
        }
        for (int i = 0; i < mutants.size(); i++) {
            final Mutant mutant = mutants.get(i);
            final String className = mutant.getMutatedClass();
            final double coeff = (double) scores.get(i) / (double) highest;
            final String explanation = mutant.getMutatedMethod() + " has a DIT of " + ck.getDit(className);
            ((RankedMutant) mutant).addRankCoefficient(
                    new Coefficient("DIT", coeff, explanation)
            );
        }

        // WCM
        highest = 0;
        scores = new ArrayList<>();
        for (Mutant mutant : ProgressBar.wrap(mutants, "Calculating NOM")) {
            final String className = mutant.getMutatedClass();
            final int score = ck.getWcm(className);
            if (score > highest) {
                highest = score;
            }
            scores.add(score);
        }
        for (int i = 0; i < mutants.size(); i++) {
            final Mutant mutant = mutants.get(i);
            final String className = mutant.getMutatedClass();
            final double coeff = (double) scores.get(i) / (double) highest;
            final String explanation = mutant.getMutatedMethod() + " has a WCM of " + ck.getWcm(className);
            ((RankedMutant) mutant).addRankCoefficient(
                    new Coefficient("WCM", coeff, explanation)
            );
        }

        // RFC
        highest = 0;
        scores = new ArrayList<>();
        for (Mutant mutant : ProgressBar.wrap(mutants, "Calculating RFC")) {
            final String className = mutant.getMutatedClass();
            final int score = ck.getRfc(className);
            if (score > highest) {
                highest = score;
            }
            scores.add(score);
        }
        for (int i = 0; i < mutants.size(); i++) {
            final Mutant mutant = mutants.get(i);
            final String className = mutant.getMutatedClass();
            final double coeff = (double) scores.get(i) / (double) highest;
            final String explanation = mutant.getMutatedMethod() + " has a RFC of " + ck.getRfc(className);
            ((RankedMutant) mutant).addRankCoefficient(
                    new Coefficient("RFC", coeff, explanation)
            );
        }

    }

}
