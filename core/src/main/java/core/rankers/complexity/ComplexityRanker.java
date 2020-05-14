package core.rankers.complexity;

import core.Coefficient;
import core.RankedMutant;
import lumutator.Mutant;
import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Ranks mutants based on their complexity.
 */
public class ComplexityRanker {

    /**
     * Name of the ranking method.
     */
    final static String rankingMethod = "CC";

    /**
     * Rank mutants based on their complexity.
     *
     * @param mutants    List of mutants that needs to be ranked.
     * @param classesDir Directory that contains all the class files.
     */
    public static void rank(List<Mutant> mutants, final String classesDir) throws IOException {

        final List<File> files = (List<File>) FileUtils.listFiles(new File(classesDir), new String[]{"class"}, true);

        final CoverageRunner coverageRunner = new CoverageRunner();
        coverageRunner.runCoverage(files);

        // First iteration to get first the highest CC
        int highestCC = 0;
        List<Integer> ccs = new ArrayList<>();
        for (Mutant mutant : ProgressBar.wrap(mutants, "Calculating complexity")) {
            final String methodName = mutant.getMutatedClass() + "." + mutant.getMutatedMethod();
            final int cc = coverageRunner.getComplexity(methodName);
            if (cc > highestCC) {
                highestCC = cc;
            }
            ccs.add(cc);
        }

        for (int i = 0; i < mutants.size(); i++) {
            final Mutant mutant = mutants.get(i);
            final String methodName = mutant.getMutatedClass() + "." + mutant.getMutatedMethod();
            final double coeff = (double) ccs.get(i) / (double) highestCC;
            final String explanation = mutant.getMutatedMethod() + " has a Cyclomatic Complexity of " + coverageRunner.getComplexity(methodName);
            ((RankedMutant) mutant).addRankCoefficient(
                    new Coefficient(rankingMethod, coeff, explanation)
            );
        }
    }

}
