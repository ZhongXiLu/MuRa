package core;

import core.rankers.complexity.ComplexityRanker;
import core.rankers.history.HistoryRanker;
import core.rankers.impact.ImpactRanker;
import core.rankers.usage.UsageRanker;
import lumutator.Configuration;
import lumutator.Mutant;

import java.io.IOException;
import java.util.List;

/**
 * Main class of MuRa.
 */
public class MuRa {

    /**
     * Rank all the mutants.
     *
     * @param survivedMutants List of survived mutants that need to be ranked.
     * @param configFile      Path to the configuration file.
     * @return List of mutants that are ranked.
     */
    public static List<Mutant> rankMutants(List<Mutant> survivedMutants, String configFile) throws IOException {
        Configuration.getInstance().initialize(configFile);
        Configuration config = Configuration.getInstance();

        ComplexityRanker.rank(survivedMutants, config.get("classFiles"));
        UsageRanker.rank(survivedMutants, config.get("classFiles"));
        //ImpactRanker.rank(survivedMutants, config.get("classFiles"));
        HistoryRanker.rank(survivedMutants);

        return survivedMutants;
    }

}