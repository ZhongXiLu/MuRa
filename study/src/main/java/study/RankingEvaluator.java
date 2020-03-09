package study;

import core.Coefficient;
import core.RankedMutant;
import lumutator.Configuration;
import lumutator.Mutant;
import study.issue.Issue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Class that helps evaluating a ranking produces by MuRa.
 */
public class RankingEvaluator {

    /**
     * Evaluate the mutant ranking of MuRa.
     *
     * @param mutants   The list of mutants with their ranking coefficients.
     * @param bugReport The issue (i.e. bug report) on which the ranking should be evaluated on.
     */
    public static void evaluateRanking(List<Mutant> mutants, Issue bugReport) {
        Configuration config = Configuration.getInstance();

        List<Mutant> fixedMutants = new ArrayList<>();  // The mutants that are on a line that is fixed in a future bug

        // (1) Find the relevant mutants, i.e. the mutants that are on a line that is fixed in a future bug
        HashMap<String, Set<Integer>> fixedLines = bugReport.fixedLines;
        for (Mutant mutant : mutants) {
            final Path sourcePath = Paths.get(config.get("projectDir")).relativize(mutant.getOriginalFile().toPath());
            // TODO: use column instead for more precision?
            Set<Integer> lines = fixedLines.getOrDefault(sourcePath.toString(), null);
            if (lines != null && lines.contains(mutant.getLineNr())) {
                // Found a mutant on a line that was fixed in future bug fix
                fixedMutants.add(mutant);
            }
        }
        System.out.println("Found " + fixedMutants.size() + " mutants related to issue " + bugReport.id);

        // (2) Find the optimal configuration
        System.out.println("Optimal configuration for these mutants:");

        // First add all the ranking coefficient values
        HashMap<String, Double> coeffWeights = new HashMap<>();
        for (Mutant mutant : fixedMutants) {
            List<Coefficient> coeffs = ((RankedMutant) mutant).getRankCoefficients();
            for (Coefficient coeff : coeffs) {
                coeffWeights.put(coeff.getRanker(), coeff.getValue() + coeffWeights.getOrDefault(coeff.getRanker(), 0.0));
            }
        }

        // Take average of the coeff values as "optimal" weight
        for (Map.Entry<String, Double> coeff : coeffWeights.entrySet()) {
            final double optimalCoeffWeight = coeff.getValue() / coeffWeights.size();
            System.out.println("\t" + coeff.getKey() + ": " + optimalCoeffWeight);
            coeffWeights.put(coeff.getKey(), optimalCoeffWeight);
        }

        // (3) Apply ranking with the found optimal weights for the ranking methods

        // Find the highest score (or ranking); this value is needed to normalize the scores
        double highestScore = 0.0;
        for (Mutant mutant : mutants) {
            List<Coefficient> coeffs = ((RankedMutant) mutant).getRankCoefficients();
            for (Coefficient coeff : coeffs) {
                final double mutantScore = coeffWeights.get(coeff.getRanker()) * coeff.getValue();
                if (mutantScore > highestScore) {
                    highestScore = mutantScore;
                }
            }
        }

        // Rank each mutant
        mutants.sort((m1, m2) -> {
            List<Coefficient> coeffsM1 = ((RankedMutant) m1).getRankCoefficients();
            List<Coefficient> coeffsM2 = ((RankedMutant) m2).getRankCoefficients();

            double scoreM1 = 0.0;
            double scoreM2 = 0.0;
            for (Coefficient coeff : coeffsM1) {
                scoreM1 += coeffWeights.get(coeff.getRanker()) * coeff.getValue();
            }
            for (Coefficient coeff : coeffsM2) {
                scoreM2 += coeffWeights.get(coeff.getRanker()) * coeff.getValue();
            }

            if (scoreM1 < scoreM2) {
                return -1;
            } else if (scoreM1 > scoreM2) {
                return 1;
            } else {
                return 0;
            }
        });

        // (4) Evaluate the ranking
        // TODO: also take into account mutants with equal scores
        List<Integer> ranks = new ArrayList<>();
        for (int i = 0; i < mutants.size(); i++) {
            List<Coefficient> coeffs = ((RankedMutant) mutants.get(i)).getRankCoefficients();
            double score = 0.0;
            for (Coefficient coeff : coeffs) {
                score += coeffWeights.get(coeff.getRanker()) * coeff.getValue();
            }
            if (fixedMutants.contains(mutants.get(i))) {
                ranks.add(i + 1);
                System.out.println("Mutant #" + (i + 1) + ": " + score);
            }
        }

        final double avgRanking = ranks.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        System.out.println("Score of the ranking algorithm: " + (avgRanking / mutants.size()));
    }

}
