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
     * Rank mutants based on their change history.
     *
     * @param mutants List of mutants that needs to be ranked.
     */
    public static void rank(List<Mutant> mutants) throws IOException {
        Configuration config = Configuration.getInstance();
        GitLogger gitLogger = new GitLogger();

        // First iteration to get first the highest change count
        double highestCost = 0.0;
        int highestChanges = 0;
        int highestRecent = 0;
        int lowestRecent = Integer.MAX_VALUE;     // the lower, the better
        List<ChangesCountCost> changesCountCosts = new ArrayList<>();
        List<Integer> changes = new ArrayList<>();
        List<Integer> recents = new ArrayList<>();
        for (Mutant mutant : ProgressBar.wrap(mutants, "Calculating history")) {
            Path sourcePath = Paths.get(config.get("projectDir")).relativize(mutant.getOriginalFile().toPath());
            final ChangesCountCost changesCountCost = gitLogger.getChangesCountCost(sourcePath.toString(), mutant.getLineNr());
            final int change = changesCountCost.changes;
            int recent = -changesCountCost.recent;    // notice the `-` making it reverse
            if (changesCountCost.recent == -1) {
                recent = -1;
            }
            if (changesCountCost.cost > highestCost) {
                highestCost = changesCountCost.cost;
            }
            if (change > highestChanges) {
                highestChanges = change;
            }
            if (recent < lowestRecent) {
                lowestRecent = recent;
            }
            if (recent > highestRecent) {
                highestRecent = recent;
            }
            changesCountCosts.add(changesCountCost);
            changes.add(change);
            recents.add(recent);
        }

        for (int i = 0; i < mutants.size(); i++) {
            Mutant mutant = mutants.get(i);

            final ChangesCountCost changesCountCost = changesCountCosts.get(i);
            String explanation =
                    "the mutated line has been modified " + changesCountCost.changes + " time(s) in the past and"
                            + " the most recent commit that modified this line was " + changesCountCost.recent + " commit(s) ago";
            ((RankedMutant) mutant).addRankCoefficient(
                    new Coefficient("H", changesCountCost.cost / highestCost, explanation)
            );

            explanation = "the mutated line has been modified " + changes.get(i) + " time(s) in the past";
            ((RankedMutant) mutant).addRankCoefficient(
                    new Coefficient("HC", ((double) changes.get(i)) / highestChanges, explanation)
            );

            int recent2 = recents.get(i);
            if (recent2 == -1) {
                recent2 = lowestRecent;
            }
            explanation = "the most recent commit that modified the mutated line was " + recent2 + " commit(s) ago";
            ((RankedMutant) mutant).addRankCoefficient(
                    new Coefficient("HR", ((double) recent2 - lowestRecent) / (double) (highestRecent - lowestRecent), explanation)
            );
        }
    }

}