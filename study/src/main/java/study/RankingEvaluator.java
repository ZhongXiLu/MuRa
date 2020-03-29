package study;

import core.Coefficient;
import core.RankedMutant;
import lumutator.Configuration;
import lumutator.Mutant;
import study.issue.Issue;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Class that helps evaluating a ranking produces by MuRa.
 */
public class RankingEvaluator {

    /**
     * Get all the mutants that are related to the bug report.
     * I.e. the line on which the fault was injected corresponds a fixed line in the bug report.
     *
     * @param mutants   List of all the mutants.
     * @param bugReport The bug report.
     * @return All the mutants that are related to the bug report.
     */
    public static List<Mutant> getMutantsRelatedToBugReport(List<Mutant> mutants, Issue bugReport) {
        List<Mutant> fixedMutants = new ArrayList<>();  // The mutants that are on a line that is fixed in a future bug

        Configuration config = Configuration.getInstance();
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

        return fixedMutants;
    }

    /**
     * Evaluate the mutant ranking of MuRa.
     *
     * @param mutants   The list of mutants with their ranking coefficients.
     * @param bugReport The issue (i.e. bug report) on which the ranking should be evaluated on.
     * @param logFile   Path to file where the log should be written to.
     */
    public static void evaluateRanking(List<Mutant> mutants, Issue bugReport, String logFile) throws IOException {
        FileWriter fileWriter = new FileWriter(logFile, true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("\nIssue " + bugReport.id);
        printWriter.println("\nCommit fix: " + bugReport.commitFix);
        printWriter.println("\nCommit before fix: " + bugReport.commitBeforeFix);

        // (1) Find the relevant mutants, i.e. the mutants that are on a line that is fixed in a future bug
        List<Mutant> fixedMutants = getMutantsRelatedToBugReport(mutants, bugReport);
        printWriter.println("Found " + fixedMutants.size() + " mutant(s) related to issue " + bugReport.id);

        // (2) Find the optimal configuration
        printWriter.println("Optimal configuration for these mutants:");

        // First add all the ranking coefficient values
        HashMap<String, Double> coeffWeights = new HashMap<>();
        for (Mutant mutant : fixedMutants) {
            List<Coefficient> coeffs = ((RankedMutant) mutant).getRankCoefficients();
            for (Coefficient coeff : coeffs) {
                coeffWeights.put(coeff.getRanker(), coeff.getValue() + (double) coeffWeights.getOrDefault(coeff.getRanker(), 0.0));
            }
        }

        // Take average of the coeff values as "optimal" weight
        for (Map.Entry<String, Double> coeff : coeffWeights.entrySet()) {
            final double optimalCoeffWeight = coeff.getValue() / (double) fixedMutants.size();
            printWriter.println("\t" + coeff.getKey() + ": " + optimalCoeffWeight);
            coeffWeights.put(coeff.getKey(), optimalCoeffWeight);
        }

        // (3) Apply ranking with the found optimal weights for the ranking methods

        // Find the highest score (or ranking); this value is needed to normalize the scores
        double highestScore = 0.0;
        for (Mutant mutant : mutants) {
            final double mutantScore = getScore(coeffWeights, mutant);
            if (mutantScore > highestScore) {
                highestScore = mutantScore;
            }
        }

        // Rank each mutant
        mutants.sort(Comparator.comparingDouble(m -> getScore(coeffWeights, m)));
        Collections.reverse(mutants);

        // (4) Evaluate the ranking
        List<Double> ranks = new ArrayList<>();
        for (int i = 0; i < mutants.size(); i++) {
            if (fixedMutants.contains(mutants.get(i))) {
                final double score = getScore(coeffWeights, mutants.get(i));

                // Calculate how many other mutants have same score/rank
                int mutantsWithSameScore = 0;
                int mutantsWithHigherScore = i; // at i-th position = i mutants with higher score
                for (int j = i - 1; true; j--) {   // mutants placed lower than the mutant
                    if (j >= 0) {
                        final double otherScore = getScore(coeffWeights, mutants.get(j));
                        if (Double.compare(score, otherScore) == 0) {
                            mutantsWithSameScore++;
                            mutantsWithHigherScore--;
                            continue;
                        }
                    }
                    break;
                }
                for (int j = i + 1; true; j++) {   // mutants placed higher than the mutant
                    if (j < mutants.size()) {
                        final double otherScore = getScore(coeffWeights, mutants.get(j));
                        if (Double.compare(score, otherScore) == 0) {
                            mutantsWithSameScore++;
                            continue;
                        }
                    }
                    break;
                }

                ranks.add(((double) mutantsWithHigherScore + (mutantsWithSameScore / 2.0)));
                printWriter.println("Mutant @" + (i + 1) + ": " + (score / highestScore));
            }
        }

        final double avgRanking = ranks.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        printWriter.println("Score of the ranking algorithm: " + (avgRanking / mutants.size()));

        printWriter.close();
    }

    /**
     * Get the final score of a mutant taking into account some weights.
     *
     * @param coeffWeights The weights for each ranking method.
     * @param mutant       The mutant of which the score needs to be calculated.
     * @return The final score of the mutant (not normalized yet).
     */
    private static double getScore(HashMap<String, Double> coeffWeights, Mutant mutant) {
        List<Coefficient> coeffs = ((RankedMutant) mutant).getRankCoefficients();
        double score = 0.0;
        for (Coefficient coeff : coeffs) {
            score += (coeffWeights.get(coeff.getRanker()) * (double) coeff.getValue());
        }
        return score;
    }

}
