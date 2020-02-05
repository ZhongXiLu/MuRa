package core.rankers.impact;

import lumutator.Mutant;
import org.apache.commons.io.FileUtils;

import java.io.File;
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

        final List<File> files = (List<File>) FileUtils.listFiles(new File(classesDir), new String[]{"class"}, true);
        final CoverageRunner coverageRunner = new CoverageRunner();
        // TODO: get class files of the tests (also put it in the config)
        coverageRunner.runCoverage(files, "target/test-classes");
    }

}
