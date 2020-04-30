package core;

import core.rankers.complexity.ComplexityRanker;
import core.rankers.history.HistoryRanker;
import core.rankers.impact.ImpactRanker;
import core.rankers.usage.UsageRanker;
import lumutator.Configuration;
import lumutator.Mutant;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Main class of MuRa.
 */
public class MuRa {

    /**
     * Rank all the mutants.
     * Important: the config file already needs to be initialized before calling MuRa.
     *
     * @param survivedMutants List of survived mutants that need to be ranked.
     */
    public static void rankMutants(List<Mutant> survivedMutants) throws IOException {
        Configuration config = Configuration.getInstance();

        ComplexityRanker.rank(survivedMutants, config.get("classFiles"));
        UsageRanker.rank(survivedMutants, config.get("classFiles"));
        ImpactRanker.rank(survivedMutants, config.get("classFiles"));
        if (new File(config.get("projectDir") + File.pathSeparator + ".git").exists()) {
            HistoryRanker.rank(survivedMutants);
        }
    }

}