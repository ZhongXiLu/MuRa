package core.rankers.history;

import core.Coefficient;
import core.RankedMutant;
import lumutator.Configuration;
import lumutator.Mutant;
import me.tongfei.progressbar.ProgressBar;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
        GitLogger gitLogger = new GitLogger();

        // First iteration to get first the highest change count
        double highestCost = 0.0;
        List<ChangesCountCost> changesCountCosts = new ArrayList<>();
        for (Mutant mutant : ProgressBar.wrap(mutants, "Calculating history")) {
            Path sourcePath = Paths.get(config.get("projectDir")).relativize(mutant.getOriginalFile().toPath());
            final ChangesCountCost changesCountCost = gitLogger.getChangesCountCost(sourcePath.toString(), mutant.getLineNr());
            if (changesCountCost.cost > highestCost) {
                highestCost = changesCountCost.cost;
            }
            changesCountCosts.add(changesCountCost);
        }

        for (int i = 0; i < mutants.size(); i++) {
            Mutant mutant = mutants.get(i);
            final ChangesCountCost changesCountCost = changesCountCosts.get(i);
            final double coeff = changesCountCost.cost / highestCost;
            final String explanation =
                    "the mutated line has been modified " + changesCountCost.changes + " time(s) in the past and"
                            + " the most recent commit that modified this line was " + changesCountCost.recent + " commit(s) ago";
            ((RankedMutant) mutant).addRankCoefficient(
                    new Coefficient(rankingMethod, coeff, explanation)
            );
        }
    }

}