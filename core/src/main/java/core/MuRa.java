package core;

import core.rankers.complexity.ComplexityRanker;
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
     * @param configFile Path to the configuration file.
     * @param mutants    List of mutants that need to be ranked.
     * @return List of mutants that are ranked.
     */
    public static List<Mutant> rankMutants(List<Mutant> mutants, String configFile) throws IOException {
        Configuration.getInstance().initialize(configFile);
        Configuration config = Configuration.getInstance();

        ComplexityRanker.rank(mutants, config.get("classFiles"));

        return mutants;
    }

}