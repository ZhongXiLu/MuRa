package core.rankers.impact;

import lumutator.Mutant;

import java.io.IOException;
import java.util.List;

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
        final CoverageRunner coverageRunner = new CoverageRunner();
        coverageRunner.runCoverage(classesDir);
    }

}