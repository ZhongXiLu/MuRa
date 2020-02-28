package core.rankers.history;

import lumutator.Configuration;
import lumutator.Mutant;

import java.io.File;
import java.io.IOException;
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

        HistoryCalculator historyCalculator = new HistoryCalculator(new File(".git/"));
        ChangesHistory history = historyCalculator.calculateChangedHistory();
    }

}