package core.rankers.usage;

import core.RankedMutant;
import lumutator.Mutant;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;

/**
 * Ranks mutants based on their usage.
 */
public class UsageRanker {

    /**
     * Rank mutants based on their complexity.
     *
     * @param mutants    List of mutants that needs to be ranked.
     * @param classesDir Directory that contains all the class files.
     */
    public static void rank(List<Mutant> mutants, String classesDir) {

        List<File> files = (List<File>) FileUtils.listFiles(new File(classesDir), new String[]{"class"}, true);

        UsageCalculator usageCalculator = new UsageCalculator(files);

        // First iteration to get first the highest usage count
        int highestUsage = 0;
        for (Mutant mutant : mutants) {
            String methodName = mutant.getMutatedClass() + "." + mutant.getMutatedMethod() + mutant.getMutatedMethodDescr();
            int usageCount = usageCalculator.getUsageCount(methodName);
            if (usageCount > highestUsage) {
                highestUsage = usageCount;
            }
        }

        for (Mutant mutant : mutants) {
            String methodName = mutant.getMutatedClass() + "." + mutant.getMutatedMethod() + mutant.getMutatedMethodDescr();
            double coeff = (double) usageCalculator.getUsageCount(methodName) / (double) highestUsage;
            ((RankedMutant) mutant).addRankCoefficient(coeff, "TODO");  // TODO: add meaningful explanation
        }
    }

}
