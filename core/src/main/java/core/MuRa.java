package core;

import lumutator.Configuration;
import lumutator.Mutant;

import java.io.IOException;
import java.util.List;

import static pitest.Parser.getMutants;

/**
 * Main class of MuRa.
 */
public class MuRa {

    /**
     * Rank all the mutants.
     *
     * @param configFile Path to the configuration file.
     */
    public static List<Mutant> rankMutants(String configFile) throws IOException {
        Configuration.getInstance().initialize(configFile);
        Configuration config = Configuration.getInstance();

        List<Mutant> mutants = getMutants("target/pit-reports", false);

        // TODO: ...

        return mutants;
    }

}