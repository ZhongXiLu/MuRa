package core.rankers.complexity;

import core.RankedMutant;
import lumutator.Mutant;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Ranks mutants based on their complexity.
 */
public class ComplexityRanker {

    /**
     * Rank mutants based on their complexity.
     *
     * @param mutants   List of mutants that needs to be ranked.
     * @param sourceDir Directory that contains all the source files.
     */
    public static void rank(List<Mutant> mutants, String sourceDir) throws IOException {

        System.out.println(new File(sourceDir).getCanonicalPath());
        List<File> files = (List<File>) FileUtils.listFiles(new File(sourceDir), new String[]{"class"}, true);

        CoverageRunner coverageRunner = new CoverageRunner();
        coverageRunner.runCoverage(files);

        // First iteration to get first the highest CC
        int highestCC = 0;
        for (Mutant mutant : mutants) {
            String methodName = mutant.getMutatedClass() + "." + mutant.getMutatedMethod();
            int cc = coverageRunner.getComplexity(methodName);
            if (cc > highestCC) {
                highestCC = cc;
            }
        }

        for (Mutant mutant : mutants) {
            String methodName = mutant.getMutatedClass() + "." + mutant.getMutatedMethod();
            double coeff = (double) coverageRunner.getComplexity(methodName) / (double) highestCC;
            ((RankedMutant) mutant).addRankCoefficient(coeff, "methodName");
        }
    }

}
