package core.rankers.usage;

import core.Coefficient;
import core.RankedMutant;
import lumutator.Mutant;
import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;

/**
 * Ranks mutants based on their usage.
 */
public class UsageRanker {

    /**
     * Name of the ranking method.
     */
    final static String rankingMethod = "USG";

    /**
     * Rank mutants based on their complexity.
     *
     * @param mutants    List of mutants that needs to be ranked.
     * @param classesDir Directory that contains all the class files.
     */
    public static void rank(List<Mutant> mutants, final String classesDir) {

        final List<File> files = (List<File>) FileUtils.listFiles(new File(classesDir), new String[]{"class"}, true);

        final UsageCalculator usageCalculator = new UsageCalculator(files);

        // First iteration to get first the highest usage count
        int highestUsage = 0;
        for (Mutant mutant : ProgressBar.wrap(mutants, "Calculating usage")) {
            final String methodName = mutant.getMutatedClass() + "." + mutant.getMutatedMethod() + mutant.getMutatedMethodDescr();
            final int usageCount = usageCalculator.getUsageCount(methodName);
            if (usageCount > highestUsage) {
                highestUsage = usageCount;
            }
        }

        for (Mutant mutant : mutants) {
            final String methodName = mutant.getMutatedClass() + "." + mutant.getMutatedMethod() + mutant.getMutatedMethodDescr();
            final double coeff = (double) usageCalculator.getUsageCount(methodName) / (double) highestUsage;
            final String explanation = mutant.getMutatedMethod() + " is used " + usageCalculator.getUsageCount(methodName) + " time(s)";
            ((RankedMutant) mutant).addRankCoefficient(
                    new Coefficient(rankingMethod, coeff, explanation)
            );
        }
    }

}
