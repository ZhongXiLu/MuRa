package core.rankers.history;

import core.Coefficient;
import core.RankedMutant;
import lumutator.Configuration;
import lumutator.Mutant;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Ranks mutants based on their change history.
 */
public class HistoryRanker {

    /**
     * Name of the ranking method.
     */
    final static String rankingMethod = "History";

    /**
     * Rank mutants based on their change history.
     *
     * @param mutants List of mutants that needs to be ranked.
     */
    public static void rank(List<Mutant> mutants) throws IOException {
        Configuration config = Configuration.getInstance();

        // First iteration to get first the most changes count
        int mostChanges = 0;
        for (Mutant mutant : mutants) {
            Path sourcePath = Paths.get(config.get("projectDir")).relativize(mutant.getOriginalFile().toPath());
            final int changesCount = GitLogger.getChangesCount(sourcePath.toString(), mutant.getLineNr());
            if (changesCount > mostChanges) {
                mostChanges = changesCount;
            }
        }

        for (Mutant mutant : mutants) {
            Path sourcePath = Paths.get(config.get("projectDir")).relativize(mutant.getOriginalFile().toPath());
            final int changesCount = GitLogger.getChangesCount(sourcePath.toString(), mutant.getLineNr());
            final double coeff = (double) changesCount / (double) mostChanges;
            final String explanation = "the mutated line has been modified " + changesCount + " time(s) in the past";
            ((RankedMutant) mutant).addRankCoefficient(
                    new Coefficient(rankingMethod, coeff, explanation)
            );
        }
    }

}