package core;

import core.rankers.ck.CKCalculator;
import core.rankers.ck.CKRanker;
import core.rankers.complexity.ComplexityRanker;
import core.rankers.history.HistoryRanker;
import core.rankers.impact.CoverageRanker;
import core.rankers.impact.ImpactRanker;
import core.rankers.usage.UsageRanker;
import lumutator.Configuration;
import lumutator.Mutant;
import org.apache.log4j.BasicConfigurator;

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
        BasicConfigurator.configure();  // fix log4j warning
        Configuration config = Configuration.getInstance();

        CKRanker.rankCK(survivedMutants, config.get("sourcePath"));
        ComplexityRanker.rank(survivedMutants, config.get("classFiles"));
        UsageRanker.rank(survivedMutants, config.get("classFiles"));
        if (new File(config.get("projectDir") + File.separator + ".git").exists()) {
            HistoryRanker.rank(survivedMutants);
        }
        CoverageRanker.rank(survivedMutants, config.get("classFiles"));
        ImpactRanker.rank(survivedMutants, config.get("classFiles"));
    }

}