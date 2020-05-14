package core.rankers.impact;

import core.Coefficient;
import core.RankedMutant;
import lumutator.Mutant;
import me.tongfei.progressbar.ProgressBar;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Ranks mutants based on the achieved line coverage.
 */
public class CoverageRanker {

    /**
     * Rank mutants based on the line coverage of the method they reside in.
     *
     * @param mutants    List of mutants that needs to be ranked.
     * @param classesDir Directory that contains all the class files.
     */
    public static void rank(List<Mutant> mutants, final String classesDir) throws IOException {
        final CoverageRunner coverageRunner = new CoverageRunner(classesDir);

        coverageRunner.runCoverage();
        Map<String, Double> classCoverages = coverageRunner.getClassLineCoverages();

        for (Mutant mutant : ProgressBar.wrap(mutants, "Calculating coverage")) {
            final String methodName = mutant.getMutatedClass() + "." + mutant.getMutatedMethod();
            final double coverage = classCoverages.getOrDefault(methodName, 0.0);
            String explanation = mutant.getMutatedMethod() + " has a line coverage of " + coverage;
            ((RankedMutant) mutant).addRankCoefficient(
                    new Coefficient("LC", coverage, explanation)
            );
        }

    }
}
