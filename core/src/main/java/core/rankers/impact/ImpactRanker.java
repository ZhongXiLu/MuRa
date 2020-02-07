package core.rankers.impact;

import core.Coefficient;
import core.RankedMutant;
import lumutator.Configuration;
import lumutator.Mutant;
import me.tongfei.progressbar.ProgressBar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Ranks mutants based on their impact.
 */
public class ImpactRanker {

    /**
     * Name of the ranking method.
     */
    final static String rankingMethod = "Impact";

    /**
     * Rank mutants based on their impact.
     *
     * @param mutants    List of mutants that needs to be ranked.
     * @param classesDir Directory that contains all the class files.
     */
    public static void rank(List<Mutant> mutants, final String classesDir) throws IOException {
        Configuration config = Configuration.getInstance();
        final CoverageRunner coverageRunner = new CoverageRunner(classesDir);

        // Calculate the instruction coverage on the original version of the program
        Map<String, Integer> originalCoverage = coverageRunner.runCoverage();

        // List containing the raw impact score for each mutant
        // The n-th element of this list corresponds to the n-th element of the mutant list
        List<Integer> mutantImpactScores = new ArrayList<>();

        // Calculate the coverage for each mutant
        String currentTempFile = "";    // Store current class, so we dont need to make a copy for each mutant
        for (Mutant mutant : ProgressBar.wrap(mutants, "Calculating impact")) {
            // Most of it copied from: https://github.com/ZhongXiLu/LuMutator/blob/master/core/src/main/java/lumutator/tracer/Tracer.java
            final String classFilesDir = config.get("classFiles") + "/" + mutant.getMutatedClass().replace(".", "/");
            final String newClassFile = mutant.getClassFile().getCanonicalPath();
            final String oldClassFile = classFilesDir + ".class";
            final String oldClassTempFile = classFilesDir + ".tmp";

            // Create copy of original .class file if necessary
            if (currentTempFile.isEmpty()) {
                // Start
                Files.move(Paths.get(oldClassFile), Paths.get(oldClassTempFile), StandardCopyOption.REPLACE_EXISTING);
                currentTempFile = oldClassTempFile;
            } else if (!currentTempFile.equals(oldClassTempFile)) {
                // New class
                Files.move(Paths.get(currentTempFile), Paths.get(currentTempFile.replace(".tmp", ".class")), StandardCopyOption.REPLACE_EXISTING);
                Files.move(Paths.get(oldClassFile), Paths.get(oldClassTempFile), StandardCopyOption.REPLACE_EXISTING);
                currentTempFile = oldClassTempFile;
            } // else: Mutants still in same class, no need to make copy of original

            // Copy the mutant .class file
            Files.copy(Paths.get(newClassFile), Paths.get(oldClassFile), StandardCopyOption.REPLACE_EXISTING);

            // Calculate the instruction coverage for this mutant
            Map<String, Integer> mutantCoverage = coverageRunner.runCoverage();
            if (mutantCoverage != null) {
                int classesWithDifferentCoverage = 0;

                // Compare the coverages of the original version with the mutant to calculate the raw impact score
                for (Map.Entry<String, Integer> coverage : originalCoverage.entrySet()) {
                    // Check if the coverage is different for this class
                    //System.out.println(String.format("%s: %d <=> %d", coverage.getKey(), coverage.getValue(), mutantCoverage.getOrDefault(coverage.getKey(), 0)));
                    if (!mutantCoverage.getOrDefault(coverage.getKey(), 0).equals(coverage.getValue())) {
                        classesWithDifferentCoverage++;
                    }
                }
                mutantImpactScores.add(classesWithDifferentCoverage);
            } else {
                mutantImpactScores.add(-1); // couldn't calculate coverage
            }
        }

        // Restore copy of class of last mutant
        Files.move(Paths.get(currentTempFile), Paths.get(currentTempFile.replace(".tmp", ".class")), StandardCopyOption.REPLACE_EXISTING);

        // First iteration to get the highest impact score
        int highestImpactScore = 0;
        for (Integer impactScore : mutantImpactScores) {
            if (impactScore > highestImpactScore) {
                highestImpactScore = impactScore;
            }
        }

        for (int i = 0; i < mutants.size(); i++) {
            final int impactScore = mutantImpactScores.get(i);
            double coeff = (double) impactScore / (double) highestImpactScore;
            String explanation = "mutant has impact on " + impactScore + " different class(es)";
            if (impactScore == -1) {
                coeff = 1.0 / (double) highestImpactScore;
                explanation = "could not calculate impact of this mutant due to timeout";
            }
            ((RankedMutant) mutants.get(i)).addRankCoefficient(
                    new Coefficient(rankingMethod, coeff, explanation)
            );
        }
    }

}